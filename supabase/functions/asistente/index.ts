// Edge Function "asistente" · Finanzia IA (spec 11)
//
// La app la llama con { "pregunta": "..." } y la sesión del usuario.
// 1. Lee SOLO los datos del usuario que inició sesión (RLS hace el filtro).
// 2. Arma un resumen y se lo envía a Claude (Anthropic) con instrucciones fijas.
// 3. Guarda la pregunta y la respuesta en `mensajes` (crea la conversación si no hay).
// 4. Devuelve { "respuesta": "..." }.
//
// La clave de la IA vive SOLO como secret de Supabase (ANTHROPIC_API_KEY). Nunca en la app.

import { createClient } from "npm:@supabase/supabase-js@2";

const MODELO = Deno.env.get("ASISTENTE_MODELO") ?? "claude-haiku-5-5";
const MAX_HISTORIAL = 10; // últimos mensajes que se le pasan a la IA como contexto

// DIAGNÓSTICO TEMPORAL: si es true, cuando algo falla el motivo se muestra como respuesta en el chat
// (y no se guarda en el historial). Cuando todo funcione, cámbialo a false y vuelve a desplegar.
const DIAGNOSTICO = true;
const diagnostico = (motivo: string) =>
  respuestaJson({ respuesta: `[Diagnóstico] ${motivo}`.slice(0, 600) });

const INSTRUCCIONES = `Eres Finanzia IA, el asistente de la app Finanzia.
Reglas obligatorias:
- Responde en español de Colombia, en máximo 4 frases, con un tono cercano y claro.
- Usa SOLO los datos del usuario que vienen en el resumen. Nunca inventes cifras.
- Si falta información para responder, dilo. Si el usuario no tiene movimientos ni metas,
  dile que aún no tiene movimientos registrados y sugiérele registrar sus ingresos y gastos.
- No recomiendes productos financieros específicos (bancos, fondos, acciones, créditos) ni prometas resultados.
- Los montos están en pesos colombianos; escríbelos con separador de miles, por ejemplo $1.250.000.
- Recuerda que orientas, pero no eres asesoría financiera profesional.
- Escribe en texto plano: la app NO muestra formato. No uses asteriscos, negrillas, numerales (#),
  viñetas ni listas; si das varias ideas, únelas en frases normales.
- Para preguntas generales (por ejemplo, cómo organizar mejor el dinero), da consejos sencillos
  y aterrízalos con las cifras reales del usuario cuando las haya.`;

/** Quita el formato Markdown por si la IA lo usa de todas formas. */
function textoPlano(texto: string): string {
  return texto
    .replace(/\*\*(.+?)\*\*/g, "$1")
    .replace(/__(.+?)__/g, "$1")
    .replace(/(^|\s)\*(\S.*?)\*(?=\s|$|[.,;:!?])/g, "$1$2")
    .replace(/^#{1,6}\s*/gm, "")
    .replace(/^\s*[-*•]\s+/gm, "")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

type MensajeIA = { role: "user" | "assistant"; content: string };

/**
 * Llama a Claude. Reintenta una vez si la IA está saturada o falla temporalmente (429, 5xx, 529).
 * Devuelve el texto, o un objeto con el error para registrarlo en los Logs.
 */
async function llamarIA(
  claveIA: string,
  sistema: string,
  mensajes: MensajeIA[],
): Promise<{ texto: string } | { estado: number; detalle: string }> {
  for (let intento = 1; intento <= 2; intento++) {
    const llamada = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "x-api-key": claveIA,
        "anthropic-version": "2023-06-01",
        "content-type": "application/json",
      },
      body: JSON.stringify({ model: MODELO, max_tokens: 500, system: sistema, messages: mensajes }),
    });
    if (llamada.ok) {
      const datos = await llamada.json();
      const texto = (datos.content ?? [])
        .filter((b: { type: string }) => b.type === "text")
        .map((b: { text: string }) => b.text).join("").trim();
      if (texto) return { texto: textoPlano(texto) };
      return { estado: 200, detalle: `Respuesta vacía (stop_reason: ${datos.stop_reason})` };
    }
    const detalle = await llamada.text();
    console.error(`Error de la IA (intento ${intento}):`, llamada.status, detalle);
    const temporal = llamada.status === 429 || llamada.status >= 500;
    if (!temporal || intento === 2) return { estado: llamada.status, detalle };
    await new Promise((r) => setTimeout(r, 1500));
  }
  return { estado: 500, detalle: "Sin respuesta" };
}

function respuestaJson(cuerpo: unknown, estado = 200): Response {
  return new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { "Content-Type": "application/json" },
  });
}

const num = (v: unknown) => Number(v ?? 0) || 0;

/** "AAAA-MM" del mes actual en Colombia. */
function mesActualColombia(): string {
  const ahora = new Date(Date.now() - 5 * 60 * 60 * 1000); // UTC-5, sin horario de verano
  return ahora.toISOString().slice(0, 7);
}

Deno.serve(async (req) => {
  try {
    return await atender(req);
  } catch (e) {
    console.error("Error inesperado en la función:", e);
    if (DIAGNOSTICO) return diagnostico(`Error inesperado: ${e instanceof Error ? e.message : String(e)}`);
    return respuestaJson({ error: "Error inesperado" }, 500);
  }
});

async function atender(req: Request): Promise<Response> {
  if (req.method !== "POST") return respuestaJson({ error: "Usa POST" }, 405);

  const claveIA = Deno.env.get("ANTHROPIC_API_KEY");
  if (!claveIA) {
    // 5xx → la app muestra "Finanzia IA todavía no está disponible".
    return respuestaJson({ error: "Falta el secret ANTHROPIC_API_KEY" }, 503);
  }

  const autorizacion = req.headers.get("Authorization");
  if (!autorizacion) return respuestaJson({ error: "Sin sesión" }, 401);

  let pregunta = "";
  try {
    pregunta = String((await req.json())?.pregunta ?? "").trim().slice(0, 500);
  } catch {
    // cuerpo inválido
  }
  if (!pregunta) return respuestaJson({ error: "La pregunta está vacía" }, 400);

  // Cliente con la sesión del usuario: RLS solo deja ver y escribir lo suyo.
  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_ANON_KEY")!,
    { global: { headers: { Authorization: autorizacion } } },
  );

  const { data: auth, error: errorAuth } = await supabase.auth.getUser(
    autorizacion.replace(/^Bearer\s+/i, ""),
  );
  if (errorAuth || !auth?.user) return respuestaJson({ error: "Sesión inválida" }, 401);

  const { data: usuario, error: errorUsuario } = await supabase
    .from("usuarios").select("id_usuario, nombre").eq("id_auth", auth.user.id).single();
  if (errorUsuario || !usuario) {
    return respuestaJson({ error: "No se encontró el usuario" }, 400);
  }
  const idUsuario = usuario.id_usuario;

  // ---------- 1. Datos del usuario ----------
  const [cuentasR, categoriasR, movimientosR, metasR, aportesR] = await Promise.all([
    supabase.from("cuentas").select("id_cuenta, nombre, saldo_inicial")
      .eq("id_usuario", idUsuario).eq("activo", true),
    supabase.from("categorias").select("id_categoria, nombre").eq("id_usuario", idUsuario),
    supabase.from("movimientos").select("id_cuenta, id_categoria, tipo, monto, fecha")
      .eq("id_usuario", idUsuario).eq("activo", true),
    supabase.from("metas").select("id_meta, nombre, monto_objetivo, fecha_limite")
      .eq("id_usuario", idUsuario).eq("activo", true),
    supabase.from("aportes").select("id_meta, id_cuenta, monto"),
  ]);
  const errorLectura = [cuentasR, categoriasR, movimientosR, metasR, aportesR].find((r) => r.error)?.error;
  if (errorLectura) {
    console.error("Error leyendo datos:", errorLectura);
    if (DIAGNOSTICO) return diagnostico(`Error leyendo datos: ${errorLectura.message}`);
    return respuestaJson({ error: "No se pudieron leer los datos" }, 500);
  }

  const cuentas = cuentasR.data ?? [];
  const idsCuentas = new Set(cuentas.map((c) => c.id_cuenta));
  const nombreCategoria = new Map((categoriasR.data ?? []).map((c) => [c.id_categoria, c.nombre]));
  const movimientos = movimientosR.data ?? [];
  const metas = metasR.data ?? [];
  const aportes = aportesR.data ?? [];

  // Saldo disponible = saldo inicial + ingresos − gastos − aportes (cuentas activas), igual que la app.
  let saldo = cuentas.reduce((s, c) => s + num(c.saldo_inicial), 0);
  for (const m of movimientos) {
    if (!idsCuentas.has(m.id_cuenta)) continue;
    saldo += String(m.tipo).toLowerCase() === "ingreso" ? num(m.monto) : -num(m.monto);
  }
  for (const a of aportes) if (idsCuentas.has(a.id_cuenta)) saldo -= num(a.monto);

  // Ingresos y gastos del mes por categoría.
  const mes = mesActualColombia();
  const ingresosMes = new Map<string, number>();
  const gastosMes = new Map<string, number>();
  for (const m of movimientos) {
    if (!String(m.fecha ?? "").startsWith(mes)) continue;
    const destino = String(m.tipo).toLowerCase() === "ingreso" ? ingresosMes : gastosMes;
    const cat = nombreCategoria.get(m.id_categoria) ?? "Sin categoría";
    destino.set(cat, (destino.get(cat) ?? 0) + num(m.monto));
  }
  const lista = (mapa: Map<string, number>) =>
    [...mapa.entries()].sort((a, b) => b[1] - a[1]).map(([cat, total]) => `  - ${cat}: ${total}`).join("\n") || "  (ninguno)";
  const total = (mapa: Map<string, number>) => [...mapa.values()].reduce((s, v) => s + v, 0);

  const resumenMetas = metas.map((m) => {
    const ahorrado = aportes.filter((a) => a.id_meta === m.id_meta).reduce((s, a) => s + num(a.monto), 0);
    return `  - ${m.nombre}: ahorrado ${ahorrado} de ${num(m.monto_objetivo)}` +
      (m.fecha_limite ? `, fecha límite ${m.fecha_limite}` : "");
  }).join("\n") || "  (no tiene metas)";

  const resumen = `Datos del usuario (fecha de hoy en Colombia: ${new Date(Date.now() - 5 * 3600 * 1000).toISOString().slice(0, 10)}):
- Nombre: ${usuario.nombre}
- Saldo disponible: ${saldo}
- Movimientos registrados en total: ${movimientos.length}
- Ingresos de este mes (${mes}), total ${total(ingresosMes)}:
${lista(ingresosMes)}
- Gastos de este mes (${mes}), total ${total(gastosMes)}:
${lista(gastosMes)}
- Metas:
${resumenMetas}`;

  // ---------- 2. Conversación activa (o una nueva) ----------
  const { data: convExistente, error: errorConv } = await supabase.from("conversaciones")
    .select("id_conversacion")
    .eq("id_usuario", idUsuario).eq("activo", true)
    .order("fecha_creacion", { ascending: false }).limit(1).maybeSingle();
  if (errorConv) console.error("No se pudo leer la conversación:", errorConv);
  let conversacion = convExistente;

  // Si el historial falla, se responde igual (sin contexto) en vez de romper la pregunta.
  let historial: { rol: string; contenido: string }[] = [];
  if (conversacion) {
    const { data, error } = await supabase.from("mensajes").select("rol, contenido")
      .eq("id_conversacion", conversacion.id_conversacion).eq("activo", true)
      .order("fecha", { ascending: false }).order("id_mensaje", { ascending: false })
      .limit(MAX_HISTORIAL);
    if (error) console.error("No se pudo leer el historial:", error);
    historial = (data ?? []).reverse();
  }

  // ---------- 3. Llamar a Claude ----------
  // La API exige que los mensajes alternen usuario/asistente y empiecen por usuario.
  const mensajesIA: MensajeIA[] = [];
  for (const m of historial) {
    const contenido = String(m.contenido ?? "").trim();
    if (!contenido) continue;
    const role = m.rol === "asistente" ? "assistant" : "user";
    if (mensajesIA.length === 0 && role === "assistant") continue;
    const ultimo = mensajesIA[mensajesIA.length - 1];
    if (ultimo && ultimo.role === role) ultimo.content += "\n" + contenido;
    else mensajesIA.push({ role, content: contenido });
  }
  if (mensajesIA.at(-1)?.role === "user") mensajesIA.pop(); // pregunta anterior sin respuesta
  mensajesIA.push({ role: "user", content: pregunta });

  const sistema = `${INSTRUCCIONES}\n\n${resumen}`;
  let resultado = await llamarIA(claveIA, sistema, mensajesIA);
  if ("estado" in resultado && mensajesIA.length > 1) {
    // Si falló con el historial, se intenta solo con la pregunta.
    console.error("Reintentando sin historial. Motivo:", resultado.estado, resultado.detalle);
    resultado = await llamarIA(claveIA, sistema, [{ role: "user", content: pregunta }]);
  }
  if ("estado" in resultado) {
    console.error("La IA no respondió:", resultado.estado, resultado.detalle);
    if (DIAGNOSTICO) return diagnostico(`La IA respondió ${resultado.estado}: ${resultado.detalle}`);
    return respuestaJson({ error: "La IA no respondió", detalle: resultado.detalle }, 502);
  }
  const respuesta = resultado.texto;

  // ---------- 4. Guardar pregunta y respuesta ----------
  const ahora = new Date().toISOString();
  if (!conversacion) {
    const titulo = pregunta.split(/\s+/).slice(0, 6).join(" ");
    const { data, error } = await supabase.from("conversaciones")
      .insert({ id_usuario: idUsuario, titulo, fecha_creacion: ahora, activo: true })
      .select("id_conversacion").single();
    if (error) console.error("No se pudo crear la conversación:", error);
    conversacion = data;
  }
  if (conversacion) {
    const { error } = await supabase.from("mensajes").insert([
      { id_conversacion: conversacion.id_conversacion, rol: "usuario", contenido: pregunta, fecha: ahora, activo: true },
      { id_conversacion: conversacion.id_conversacion, rol: "asistente", contenido: respuesta, fecha: new Date(Date.now() + 1).toISOString(), activo: true },
    ]);
    if (error) console.error("No se pudieron guardar los mensajes:", error);
  }

  return respuestaJson({ respuesta });
}

# Spec 11 · Asistente Finanzia IA

Lee antes `README.md`, `CLAUDE.md` y las specs 04 (Dashboard) y 07 (Metas).

## Reglas de diseño (obligatorias)

- No hay imagen de diseño para esta pantalla: usa **los mismos colores, tipografía, tarjetas, bordes, sombras e íconos que ya tiene la app**. En particular, la tarjeta "Finanzia IA" del Dashboard (fondo lila claro, robot morado con insignia verde de destellos) y el botón "Conversar con Finanzia IA" (degradado verde → azul). No inventes colores ni estilos nuevos.
- **Nada de lo que se muestra puede ser inventado ni de muestra:** las respuestas salen de la IA y se basan solo en los datos que el usuario registró. La pantalla nunca muestra conversaciones de ejemplo.
- Los errores van **dentro de la pantalla**, en rojo, con el estilo de `olvidar_contraseña_error.jpeg`. Sin Snackbar ni Toast.
- No se usa la palabra "copiloto" en esta pantalla.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Que el usuario pueda **conversar** con Finanzia IA sobre su propio dinero: en qué gasta más, cómo va con sus metas, cuánto le conviene aportar o cómo cambió su saldo. La IA responde en español, en pocas frases, usando **solo los datos reales del usuario**, y aclara que no es asesoría financiera profesional.

## Diseño de referencia

- Sin imagen propia. Referencias de estilo:
  - `docs/design/usuario_creado_onboarding.jpeg`: tarjeta "Finanzia IA", botón "Conversar con Finanzia IA" y barra inferior.
  - `docs/design/metas_1.png`: encabezado con flecha atrás circular y tarjetas blancas.

## Cómo se llega a esta pantalla

- Pestaña **Asistente** de la barra inferior.
- Botón **"Conversar con Finanzia IA"** del Dashboard.
- Chevron **"<"** de la tarjeta "Finanzia IA" del Dashboard.

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:**
   - Botón circular blanco con flecha hacia atrás.
   - Robot morado en un cuadro lila con la insignia verde de destellos, el mismo de la tarjeta del Dashboard.
   - Título "Finanzia IA" en negrita y debajo, en verde, "Responde con tus datos reales".
2. **Aviso:** texto pequeño y gris en una pastilla gris clara: "Finanzia IA te orienta con tus datos. No es asesoría financiera profesional."
3. **Conversación** (se desplaza verticalmente):
   - **Sin mensajes todavía:** una tarjeta lila con el robot y el texto "¡Hola, [primer nombre]! Pregúntame sobre tus gastos, ingresos o metas." Debajo, en gris, "Puedes empezar con:" y tres **sugerencias** en pastillas blancas con borde:
     - "¿En qué gasté más este mes?"
     - "¿Cómo voy con mis metas?"
     - "¿Cuánto puedo ahorrar este mes?"
   - **Mensaje del usuario:** burbuja a la derecha, con el degradado del botón "Conversar con Finanzia IA" y texto blanco.
   - **Respuesta de Finanzia IA:** burbuja blanca a la izquierda con borde gris claro y texto azul marino. A su izquierda, el robot pequeño en un círculo lila.
   - **Mientras responde:** una burbuja de Finanzia IA con el texto "Finanzia IA está escribiendo…" en gris.
   - Al llegar un mensaje nuevo, la conversación baja sola hasta el final.
4. **Barra para escribir** (fija abajo, encima de la barra de navegación):
   - Campo blanco redondeado con borde gris y el texto de ayuda "Escribe tu pregunta…". Hasta 500 caracteres; crece hasta 4 líneas.
   - A la derecha, un botón circular con el degradado verde → azul y una flecha de enviar blanca. Se ve más claro y no responde si el campo está vacío o si Finanzia IA está respondiendo.
5. **Barra de navegación inferior** fija, con la pestaña **Asistente** seleccionada.

## Datos reales (Supabase)

| Elemento | Fuente |
|---|---|
| Primer nombre del saludo | Campo `Nombre` de `Usuarios` (primera palabra) |
| Historial | La `Conversaciones` activa más reciente del usuario y sus `Mensajes` activos, del más antiguo al más reciente |
| Quién escribió cada mensaje | Campo `rol` de `Mensajes`: `usuario` o `asistente` |
| Respuesta | La genera la función `asistente` de Supabase (ver abajo) |

### Cómo se genera una respuesta

1. La app llama a la **Edge Function `asistente`** de Supabase con la pregunta y la sesión del usuario. La app **nunca** guarda ni conoce la clave de la IA.
2. La función, con la sesión del usuario, lee solo sus datos (RLS): nombre, saldo disponible, ingresos y gastos del mes por categoría, metas con lo ahorrado y su fecha límite, y los últimos mensajes de la conversación.
3. Le envía a un modelo de IA (por ejemplo, Claude de Anthropic) un **resumen** de esos datos con instrucciones fijas:
   - Responder en español de Colombia, en máximo 4 frases.
   - Usar solo los datos recibidos; si falta información, decirlo.
   - No recomendar productos financieros específicos ni prometer resultados.
4. Guarda la pregunta y la respuesta en `Mensajes`. Si no hay conversación activa, la crea en `Conversaciones`, con un título hecho de las primeras palabras de la pregunta.
5. Devuelve la respuesta a la app.

- La clave de la IA vive solo como *secret* de Supabase.
- No cambies las tablas de Supabase sin confirmación del equipo. `Conversaciones` y `Mensajes` ya existen con sus políticas RLS.

## Elementos que dependen de los datos reales y su estado vacío

- **Sin mensajes:** la tarjeta de bienvenida con las tres sugerencias. Al tocar una sugerencia, se envía como pregunta.
- **Usuario sin movimientos ni metas:** la IA lo dice ("Aún no tienes movimientos registrados…") y sugiere registrar ingresos y gastos. Nunca inventa cifras.
- **Asistente no disponible** (la función todavía no está desplegada o no tiene clave): al enviar, el error en rojo dice "Finanzia IA todavía no está disponible. Inténtalo más tarde." y la pregunta queda escrita en el campo.

## Comportamiento

- **Enviar:** quita los espacios del inicio y del final. Si queda vacío, no hace nada. Muestra de inmediato el mensaje del usuario y la burbuja "está escribiendo…", limpia el campo y llama a la función.
- **Respuesta recibida:** reemplaza la burbuja "está escribiendo…".
- **Error al responder** (sin conexión u otro): se quita el mensaje que no se pudo enviar, el texto vuelve al campo para no perderlo, y se muestra el error en rojo encima de la barra para escribir, con la opción "Reintentar".
- **Al abrir la pantalla:** carga el historial. Mientras carga, se ve un círculo de carga; si falla, se ve el error en rojo con "Reintentar".
- **Flecha atrás:** vuelve a la pantalla anterior (normalmente el Dashboard).
- **Pestañas de la barra:** Inicio abre el Dashboard y Metas abre Metas.

## Adaptable a cualquier dispositivo

- La conversación se desplaza verticalmente. La barra para escribir y la barra de navegación quedan fijas y nunca tapan el último mensaje.
- Con el teclado abierto, la barra para escribir sube con el teclado.
- Las burbujas ocupan como máximo el 80 % del ancho. Sin alturas fijas; funciona con letra grande, en pantallas pequeñas y en tabletas (ancho máximo centrado).

## Criterios de aceptación

- [ ] La pestaña Asistente, el botón "Conversar con Finanzia IA" y el chevron de la tarjeta Finanzia IA abren esta pantalla.
- [ ] Sin mensajes, se ve el saludo con el primer nombre real y las tres sugerencias.
- [ ] Al tocar una sugerencia o enviar una pregunta, aparece el mensaje del usuario y luego la respuesta.
- [ ] El botón de enviar no responde con el campo vacío ni mientras Finanzia IA responde.
- [ ] Si falla, el error se ve en rojo dentro de la pantalla y la pregunta no se pierde.
- [ ] Al volver a la pantalla, se ve el historial guardado.
- [ ] La app no contiene ninguna clave de IA.
- [ ] Las respuestas usan solo los datos del usuario que inició sesión.
- [ ] Se puede leer toda la conversación en pantalla pequeña y la barra para escribir no tapa nada.

## Decisiones tomadas por defecto (confirmar con el equipo)

- Una sola conversación activa por usuario: el historial continúa donde quedó.
- El modelo recomendado es uno rápido y económico de Claude (Anthropic), llamado desde la Edge Function.
- La respuesta tiene máximo 4 frases.
- Sin diseño propio: se reutiliza el estilo de la tarjeta "Finanzia IA" y del botón "Conversar con Finanzia IA" del Dashboard.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. ¿Qué servicio de IA se usa y quién paga su uso?
2. ¿Quién despliega la Edge Function `asistente` y guarda la clave como *secret* en Supabase?
3. ¿Se necesita un botón para empezar una conversación nueva o para borrar el historial?
4. ¿Hay un diseño propio para esta pantalla?
5. ¿Se limita cuántas preguntas puede hacer un usuario por día, para controlar el costo?

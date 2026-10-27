# Spec 04 · Dashboard (Inicio)

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a las imágenes de referencia: mismos colores, degradados, tipografía, tamaños, espaciados, bordes redondeados, sombras, íconos, emojis, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones y tarjetas mantienen sus colores y degradados tal como se ven en las imágenes.
- No agregar, quitar ni renombrar elementos que no estén en el diseño, salvo las excepciones aprobadas de este documento.
- Los datos de las imágenes (Paula, `$12,450.00`, "Mercadona", etc.) son solo un ejemplo del diseño. **Nada de lo que se muestra puede ser inventado ni de muestra:** todo valor sale de los datos que el usuario realmente ingresó. Si no hay datos para un elemento, se muestra el estado vacío indicado en esta spec. Lo que cambia son los valores, nunca el diseño.
- Formato de montos como en el diseño: signo `$`, coma para miles y punto con dos decimales (`$12,450.00`). Ingresos con `+` y gastos con `-`.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Ser la pantalla principal del usuario después de iniciar sesión o de terminar el onboarding. Aquí se **ve la información que el usuario ingresó**: su nombre, su balance, sus ingresos y gastos, y sus movimientos recientes, leídos desde Supabase.

## Diseños de referencia

- `docs/design/usuario_creado_onboarding.jpeg`: pantalla completa (de arriba hasta la barra de navegación).
- `docs/design/usuario_nuevo__onboarding.jpeg`: la misma pantalla, vista de la parte superior.

Son la **misma pantalla**; la primera muestra todo el contenido y la segunda un detalle de la parte de arriba.

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:** ícono de la app (cuadro verde-azulado con destellos) con "Finanzia" y debajo "Copiloto IA" en verde; a la derecha, ícono de campana con un punto azul y un botón circular azul con ícono de persona.
2. **Saludo:** "¡Hola, [primer nombre]! 👋" y debajo "Aquí tienes el resumen fina…" (una sola línea, cortada con "…" como en el diseño). A la derecha, pastilla verde clara "Saludable +4.2%" con ícono de tendencia.
3. **Tarjeta Balance total** (degradado azul → verde oscuro):
   - Etiqueta "BALANCE TOTAL" con ícono de banco, y un botón circular con ícono de ojo a la derecha.
   - Monto grande (en el diseño `$12,450.00`) y a su derecha una pequeña línea verde de tendencia.
   - Debajo del monto: ícono de escudo y "Cuenta [nombre de la cuenta] • • • • [dígitos]".
   - Dos recuadros: **Ingresos** (flecha verde hacia abajo, monto con `+`) y **Gastos** (flecha roja hacia arriba, monto con `-`). Los montos largos se cortan con "…" como en el diseño.
4. **Tarjeta Finanzia IA** (fondo lila claro): ícono de robot, "Finanzia IA", etiqueta "Copiloto" y "Análisis predictivo en tiempo real" en verde; chevron "<" a la derecha. Dentro, una tarjeta blanca con "💡 **Consejo inteligente:** …", el texto con el porcentaje destacado en verde y negrita, y al pie tres círculos con letras ("S", "N", "A"), el texto "3 suscripciones analizadas" y el enlace azul "Ver plan →".
5. **Tarjeta de meta** (fondo azul claro): etiqueta verde "Meta: Vacaciones 🏖️", texto "Llevas $1,800 de $2,500", barra de progreso azul, "¡A solo $700 de lograr tu objetivo!", y a la derecha un ícono verde con signo `$` y estrella.
6. **Movimientos recientes:** título y a la derecha el enlace azul "Ver todos". Tarjeta blanca con una lista de movimientos; cada fila tiene:
   - Un ícono en un cuadro de color (distinto por categoría).
   - Nombre del movimiento (en una línea, cortado con "…").
   - Debajo: fecha y hora ("Hoy, 14:32", "Ayer, 09:00", "12 Mar") y un tipo, separados por "•".
   - A la derecha: monto (rojo con `-` para gastos, verde con `+` para ingresos) y debajo la categoría (en verde para ingresos).
7. **Botón "Conversar con Finanzia IA →"** de ancho completo, con ícono de burbuja de chat y degradado verde → azul.
8. **Botón flotante "+"** circular verde-azulado, en la esquina inferior derecha, sobre el contenido.
9. **Barra de navegación inferior** fija, con cinco pestañas: **Inicio** (seleccionada, con fondo azul claro y texto azul), **Gráficos**, **Billetera**, **Metas** y **Asistente**.

## Datos reales (Supabase)

| Elemento del diseño | Fuente |
|---|---|
| Primer nombre en el saludo | Campo `Nombre` de `Usuarios` (solo la primera palabra) |
| Nombre de la cuenta en "Cuenta …" | Campo `Nombre` de `Cuentas` (la cuenta "Principal" por defecto) |
| Balance total | Suma, por cada cuenta activa del usuario, de `Saldo inicial` + ingresos − gastos de sus `Movimientos` activos |
| Ingresos | Suma de los `Movimientos` activos de tipo ingreso |
| Gastos | Suma de los `Movimientos` activos de tipo gasto |
| Lista de movimientos | Últimos 4 `Movimientos` activos del usuario, del más reciente al más antiguo |
| Nombre del movimiento | Campo `Descripcion` |
| Fecha y hora | Campo `Fecha`: "Hoy, HH:mm" si es de hoy, "Ayer, HH:mm" si es de ayer, y "dd MMM" en otro caso |
| Tipo (después del "•") | Nombre de la cuenta del movimiento |
| Categoría (derecha) | Nombre de la `Categorias` del movimiento |
| Color e ícono de cada fila | Color de la categoría (`Colores`) y un ícono según su nombre (ver abajo) |

- Solo se leen datos del usuario que inició sesión.
- Los datos se cargan al entrar a la pantalla. Justo después del onboarding debe mostrar de inmediato lo que el usuario acaba de registrar.
- Íconos por categoría: Gastos Fijos → casa, Comida → carrito o comida, Diversión → música o entretenimiento, Transporte → carro, Ingresos → billetes. Si no hay uno definido, usa un ícono genérico del mismo estilo.

## Elementos que dependen de los datos reales y su estado vacío

Ningún elemento puede mostrar valores inventados. Cada uno se calcula con los datos del usuario o muestra su estado vacío con la misma tipografía y estilo de la app:

- **Pastilla "Saludable +X%":** X es el ahorro del mes actual, es decir (ingresos del mes − gastos del mes) ÷ ingresos del mes × 100, redondeado a un decimal. Se muestra solo si hay ingresos en el mes y el resultado es 0 o positivo. En cualquier otro caso, la pastilla no se muestra. No crees colores ni textos nuevos para otros casos.
- **Línea verde de tendencia del balance:** se dibuja con el balance acumulado del usuario según las fechas de sus movimientos. Si hay menos de dos puntos, no se dibuja la línea.
- **Dígitos "• • • • 8492" de la cuenta:** no existen en los datos, así que no se muestran. Debajo del balance se ve el ícono de escudo y solo "Cuenta [nombre de la cuenta]".
- **Punto azul de la campana:** no hay notificaciones reales, así que no se muestra el punto.
- **Tarjeta Finanzia IA:** se mantiene el diseño del encabezado (ícono, "Finanzia IA", "Copiloto", "Análisis predictivo en tiempo real", chevron). El texto del "Consejo inteligente" se genera solo con datos reales, sin IA por ahora:
  - Si hay gastos en el mes: "Tu categoría con más gasto este mes es [categoría]: $[monto], el [X]% de tus gastos." (el monto y el porcentaje van destacados en verde y negrita, como el "12% extra" del diseño).
  - Si no hay gastos en el mes: "Aún no hay suficiente información para darte un consejo. Registra tus ingresos y gastos para empezar."
  - Los círculos "S N A", el texto "3 suscripciones analizadas" y el enlace "Ver plan →" **no se muestran**, porque no hay datos reales detrás.
- **Tarjeta de meta:** si el usuario tiene al menos una meta en `Metas` (se usa la más reciente activa), se muestra con el diseño del ejemplo: nombre de la meta en la etiqueta verde, "Llevas $[suma de sus Aportes] de $[Monto objetivo]", barra de progreso real y "¡A solo $[lo que falta] de lograr tu objetivo!". Si el usuario no tiene metas, la tarjeta se muestra con el mismo fondo y bordes, y solo el texto "Aún no tienes metas".

## Comportamiento

- **Ojo del balance:** alterna entre mostrar el monto y ocultarlo (por ejemplo con `••••`). Por defecto está visible.
- **Botón de persona (arriba a la derecha):** al tocarlo se abre un menú pequeño con una sola opción, "Cerrar sesión". Al elegirla se cierra la sesión de Supabase y se vuelve a la pantalla de inicio de sesión. *Es una excepción aprobada al diseño: el menú solo aparece al tocar y no cambia la pantalla en reposo.*
- **Movimientos (spec 10):** cada fila de "Movimientos recientes" se puede tocar para editar ese movimiento, sin cambiar el diseño de la fila, y "Ver todos" abre la lista completa de movimientos (`10-editar-movimientos.md`). Después de editar, el Dashboard muestra de inmediato los datos nuevos.
- **Campana, "Ver plan", chevron de Finanzia IA, botón "Conversar con Finanzia IA", botón "+" y pestañas Gráficos, Billetera, Metas y Asistente:** se muestran como en el diseño pero **no tienen acción por ahora** (no hay pantallas definidas). No inventes pantallas para ellos.
- **Pestaña Inicio:** queda seleccionada.
- Si faltan los datos de alguna sección (sin cuentas, sin gastos, sin metas, sin movimientos), esa sección muestra su estado vacío descrito en esta spec; nunca valores de ejemplo.
- Mientras carga, muestra un estado de carga simple que no cambie el diseño de la pantalla; si falla la lectura, muestra un texto de error en rojo dentro de la pantalla (sin Snackbar) con la opción de reintentar.
- **Usuario sin movimientos** (por ejemplo, omitió el onboarding): balance `$0.00`, ingresos `+$0.00`, gastos `-$0.00` y, en la tarjeta de movimientos, un texto breve y gris, "Aún no tienes movimientos", con la tipografía de la app.

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente. El botón "+" flota y la barra de navegación queda fija abajo, sin tapar el contenido.
- Sin alturas ni anchos fijos. Respeta las barras del sistema y funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] La pantalla es visualmente idéntica a las imágenes (colores, degradados, tipografía, íconos, textos y orden).
- [x] Después del onboarding muestra el nombre real del usuario, el balance, los ingresos, los gastos y los movimientos que acaba de registrar.
- [x] Tras cerrar sesión e iniciar sesión de nuevo, muestra los mismos datos.
- [x] El balance es igual al saldo inicial + ingresos − gastos.
- [x] Los movimientos aparecen del más reciente al más antiguo, con monto en verde y `+` para ingresos, y rojo y `-` para gastos, y la categoría correcta.
- [x] El ojo muestra y oculta el balance.
- [x] "Cerrar sesión" cierra la sesión y lleva al inicio de sesión.
- [x] Un usuario sin movimientos ve `$0.00` y el texto "Aún no tienes movimientos".
- [ ] Un usuario solo ve sus propios datos.
- [x] Se puede hacer scroll hasta el final en pantalla pequeña y los botones no quedan tapados.
- [x] No hay ningún dato inventado: no aparecen "Paula", "$12,450.00", "8492", "Mercadona", "Vacaciones", "12% extra" ni "3 suscripciones" si el usuario no los ingresó.
- [ ] Un usuario sin metas ve "Aún no tienes metas" y uno con metas ve su meta real con su progreso.
- [x] El texto del consejo y la pastilla "Saludable" salen de los datos reales del usuario y cambian cuando cambian sus movimientos.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño (salvo el menú de cerrar sesión y los estados vacíos).
- [ ] Tocar una fila de "Movimientos recientes" abre su edición y "Ver todos" abre la lista completa (spec 10).
- [ ] Después de editar un movimiento, el balance, los ingresos, los gastos, la lista y el consejo se actualizan de inmediato (spec 10).
- [ ] El balance nunca queda negativo por un movimiento nuevo o editado (spec 10).

> Verificación: los criterios marcados se comprobaron con `CalculosDashboardTest`, `FormatosDashboardTest`,
> `DashboardTest`, `DashboardNavegacionTest`, `DashboardPantallaPequenaTest`, la prueba manual contra Supabase real
> `integracion/manual/DemoMateoMarinTest` (Mateo Marin) y en el emulador (iniciar sesión, Dashboard y cerrar sesión).
> Sin marcar: la igualdad visual exacta y "ningún elemento ni color nuevo" (revisión a ojo del equipo); "un usuario solo
> ve sus propios datos" (las políticas RLS están activas, pero falta una prueba con dos usuarios reales); y la meta real
> con progreso (se probó la pantalla con datos simulados, pero no la lectura de `metas` y `aportes` desde Supabase,
> porque ningún usuario tiene metas).

## Decisiones tomadas por defecto (confirmar con el equipo)

- Pendiente 1: pastilla "Saludable" = ahorro del mes; consejo = categoría con más gasto del mes (porcentaje redondeado a entero).
- Pendiente 2: campana, "+", "Conversar con Finanzia IA", chevron y pestañas se ven pero no tienen acción. (Desde entonces "+" y la campana ya tienen pantalla; "Ver todos" abre la lista de la spec 10.)
- Pendiente 3: los recuadros Ingresos y Gastos muestran el **mes actual** (zona horaria del teléfono).
- Pendiente 4: "Cuenta …" muestra la primera cuenta activa (la "Principal").
- Pendiente 5: la frecuencia se guarda en `usuarios.frecuencia_ingreso` (columna de `docs/supabase/01-usuarios-y-auth.sql`).
- Subtítulo del saludo: "Aquí tienes el resumen financiero de hoy", en una línea y cortado con "…" como en el diseño.
- El balance grande reduce su letra solo si un monto largo no cabe (con montos cortos queda igual al diseño).
- Línea de tendencia: un punto por movimiento con el balance acumulado.
- Color de cada fila: el color de la categoría en `colores`, suavizado como fondo del ícono.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. Reglas aprobadas por defecto: pastilla "Saludable" = ahorro del mes; consejo = categoría con más gasto del mes. ¿El equipo las aprueba o prefiere otras? La IA real de Finanzia queda para una fase posterior.
2. Destino de "Conversar con Finanzia IA" y de las pestañas inferiores sin pantalla todavía. ("Ver todos" ya está definido en la spec 10.)
3. "Ingresos" y "Gastos" del recuadro: ¿es el total histórico o solo el del mes actual? (Por defecto, el mes actual, como sugiere el diseño. Confirmar.)
4. Si el usuario tiene varias cuentas, ¿qué nombre se muestra en "Cuenta …"? (Por defecto, la primera cuenta activa.)
5. La frecuencia del ingreso elegida en el onboarding no tiene dónde guardarse en el esquema actual; ver `docs/supabase/` y la spec 03.

No cambies las tablas de Supabase sin confirmación del equipo.

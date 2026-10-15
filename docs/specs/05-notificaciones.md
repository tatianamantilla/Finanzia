# Spec 05 · Notificaciones

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a la imagen de referencia: mismos colores, tipografía, tamaños, espaciados, bordes redondeados, sombras, íconos, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones, pastillas y tarjetas mantienen sus colores tal como se ven en la imagen.
- No agregar, quitar ni renombrar elementos que no estén en el diseño, salvo las excepciones aprobadas de este documento.
- Los datos de la imagen ("Límite de Restaurantes al 85%", "Tarjeta Bancolombia Visa", "$450.000 COP", etc.) son solo un ejemplo del diseño. **Nada de lo que se muestra puede ser inventado ni de muestra:** toda notificación sale de los datos reales del usuario. Si no hay notificaciones, se muestra el estado vacío indicado en esta spec. Lo que cambia son los valores, nunca el diseño.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Mostrar al usuario, en una sola lista, los avisos importantes sobre su dinero: cuándo se acerca al límite de una categoría, cuándo vence un pago y las oportunidades de ahorro que detecte Finanzia IA. Se llega a esta pantalla desde la campana del Dashboard (spec 04).

## Diseño de referencia

- `docs/design/notificaciones_1.png`: parte superior de la pantalla, con las dos primeras notificaciones completas y el comienzo de la tercera.

> La imagen tiene baja resolución (224 × 462 px) y algunos textos se ven cortados (el tercer filtro "Re…" y la tercera tarjeta "Oportunidad de…"). Ver "Decisiones pendientes".

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:**
   - A la izquierda, botón circular con flecha hacia atrás.
   - Título "Notificaciones" en negrita y debajo, en gris, "[N] alertas activas sin revisar".
   - A la derecha, dos botones cuadrados redondeados: uno con ícono de doble check (marcar todas como leídas) y otro con ícono de ajustes (líneas con controles deslizantes).
2. **Filtros** (fila que se desplaza horizontalmente):
   - "Todas ([N])": pastilla verde clara, seleccionada por defecto.
   - "Alertas de presupuesto": pastilla blanca con borde gris e ícono.
   - Un tercer filtro cortado en la imagen ("Re…").
3. **Sección "HOY":** punto azul y "HOY" en mayúsculas a la izquierda; "Tiempo real" en gris a la derecha.
4. **Tarjeta de alerta de presupuesto** (fondo blanco, bordes redondeados):
   - Ícono de cubiertos en un cuadro azul claro.
   - Título en negrita: "Límite de [categoría] al [X]%".
   - Menú de tres puntos a la derecha.
   - Debajo del título: punto azul, "Hace [tiempo] • Presupuestos inteligentes".
   - Texto: "Has consumido $[gastado] de tu cuota de $[límite] proyectada para este mes. Te restan [N] días del ciclo." (los montos en negrita).
   - Barra de progreso morada con el porcentaje consumido.
   - Al pie: "Margen libre: $[límite − gastado]" en gris, y a la derecha el botón con borde "Ajustar límite" con ícono.
5. **Tarjeta de próximo vencimiento** (fondo blanco, bordes redondeados):
   - Ícono de tarjeta en un cuadro azul.
   - Título en negrita: "[Nombre de la cuenta o tarjeta]".
   - Menú de tres puntos a la derecha.
   - Debajo del título: punto azul, "Hace [tiempo] • Próximo vencimiento".
   - Recuadro gris claro con "Pago total sugerido" y debajo el monto en negrita; a la derecha, pastilla roja clara "Vence mañana" con ícono.
   - Al pie: texto "Posponer 6h" y botón verde oscuro "Ver detalle →".
6. **Tarjeta de oportunidad de Finanzia IA:** ícono de destellos en un cuadro verde, título "Oportunidad de…" y pastilla verde "IA Alert". El resto de la tarjeta no se ve en la imagen.
7. **Barra de navegación inferior** fija, igual a la del Dashboard (ver "Barra de navegación" más abajo).

## Barra de navegación (decisión del equipo, aplica a todas las pantallas)

- Se **quita la pestaña Gráficos**. La barra queda con cuatro pestañas: **Inicio**, **Billetera**, **Metas** y **Asistente**.
- **Inicio** lleva al Dashboard (spec 04).
- **Billetera** mostrará los saldos de las cuentas y de los ingresos registrados (spec propia, pendiente de recibir su diseño).
- **Metas** mostrará las metas por categorías (spec propia, pendiente de recibir su diseño).
- **Asistente** lleva al Asistente de IA (spec propia, pendiente de recibir su diseño).
- Mientras una pestaña no tenga su pantalla construida, se muestra pero no tiene acción.

## Datos reales (Supabase)

El esquema actual (ver `README.md` y `docs/supabase/`) **no tiene** dónde guardar notificaciones, límites de presupuesto por categoría ni tarjetas con fecha de vencimiento. Por eso:

| Elemento del diseño | Fuente |
|---|---|
| Contador "[N] alertas activas sin revisar" | Número de notificaciones no leídas del usuario (requiere tabla nueva, ver "Decisiones pendientes") |
| Alerta de presupuesto: categoría | Nombre de la `Categorias` |
| Alerta de presupuesto: gastado | Suma de los `Movimientos` activos de tipo gasto de esa categoría en el mes actual |
| Alerta de presupuesto: límite | **No existe en el esquema** (requiere un límite mensual por categoría) |
| Próximo vencimiento: nombre y monto | **No existe en el esquema** (las `Cuentas` no tienen fecha de corte ni de pago) |
| Oportunidad de Finanzia IA | **No existe todavía** (la IA real queda para una fase posterior) |

- Solo se leen datos del usuario que inició sesión.
- No cambies las tablas de Supabase sin confirmación del equipo.

## Estado vacío

- Si el usuario no tiene notificaciones, debajo de los filtros se muestra un texto breve y gris, "No tienes notificaciones por ahora", con la tipografía de la app. El encabezado dice "0 alertas activas sin revisar" y el filtro "Todas (0)".
- Mientras no se resuelvan las decisiones pendientes sobre los datos, **la pantalla siempre muestra el estado vacío**. Nunca se muestran las tarjetas del ejemplo.

## Comportamiento

- **Campana del Dashboard:** abre esta pantalla.
- **Flecha atrás:** vuelve al Dashboard.
- **Filtros:** al tocar uno, queda seleccionado (pastilla verde clara) y la lista muestra solo las notificaciones de ese tipo. "Todas" muestra todas.
- **Doble check, ajustes, menú de tres puntos, "Ajustar límite", "Posponer 6h" y "Ver detalle →":** se muestran como en el diseño pero **no tienen acción por ahora**, hasta que el equipo defina qué hacen.
- **Pestaña Inicio:** queda seleccionada, porque a esta pantalla se llega desde Inicio.
- Mientras carga, muestra un estado de carga simple que no cambie el diseño; si falla la lectura, muestra un texto de error en rojo dentro de la pantalla (sin Snackbar) con la opción de reintentar.

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente. La barra de navegación queda fija abajo, sin tapar el contenido.
- La fila de filtros se desplaza horizontalmente si no cabe.
- Sin alturas ni anchos fijos. Respeta las barras del sistema y funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] La pantalla es visualmente idéntica a la imagen (colores, tipografía, íconos, textos y orden).
- [ ] La campana del Dashboard abre esta pantalla y la flecha atrás vuelve al Dashboard.
- [ ] La barra de navegación tiene cuatro pestañas (sin Gráficos) e Inicio lleva al Dashboard.
- [ ] Un usuario sin notificaciones ve "No tienes notificaciones por ahora", "0 alertas activas sin revisar" y "Todas (0)".
- [ ] Los filtros cambian de selección al tocarlos.
- [ ] No hay ningún dato inventado: no aparecen "Restaurantes", "Bancolombia", "$180.000", "$450.000" ni "Vence mañana" si el usuario no tiene esos datos.
- [ ] Un usuario solo ve sus propias notificaciones.
- [ ] Se puede hacer scroll hasta el final en pantalla pequeña y la barra de navegación no tapa el contenido.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño (salvo el estado vacío).

## Decisiones tomadas por defecto (confirmar con el equipo)

- La pantalla se abre desde la campana del Dashboard.
- Mientras no existan los datos, se muestra siempre el estado vacío.
- Los montos usan el mismo formato del Dashboard (`$180,000.00`), no el de la imagen (`$180.000 COP`), para que toda la app se vea igual.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. **Imagen en alta resolución:** se necesita la imagen original completa (con la tercera tarjeta y el resto de la lista) para copiar el diseño con exactitud.
2. **Tercer filtro:** ¿qué dice el filtro cortado "Re…"? (¿"Recordatorios"?) ¿Hay más filtros a la derecha?
3. **Tabla de notificaciones:** ¿se crea una tabla `Notificaciones` (id, id usuario, tipo, título, mensaje, leída, fecha, activo) o las notificaciones se calculan al abrir la pantalla?
4. **Límite de presupuesto:** ¿dónde se guarda el límite mensual de cada categoría? (Por ejemplo, un campo `limite mensual` en `Categorias` o una tabla `Presupuestos`.) ¿A qué porcentaje se dispara la alerta?
5. **Próximo vencimiento:** ¿qué cuentas tienen fecha de pago (tarjetas de crédito) y dónde se guardan esa fecha y el monto a pagar?
6. **Oportunidad de Finanzia IA:** ¿qué texto muestra y de dónde sale mientras no exista la IA real?
7. **Acciones:** qué hacen el doble check, ajustes, el menú de tres puntos, "Ajustar límite", "Posponer 6h" y "Ver detalle →".
8. **Formato de montos:** ¿se usa el formato del Dashboard (`$180,000.00`) o el de la imagen (`$180.000 COP`)? Debe ser el mismo en toda la app.
9. **Otras secciones:** además de "HOY", ¿hay secciones como "AYER" o "ESTA SEMANA"?

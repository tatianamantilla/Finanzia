# Spec 09 · Detalle de meta

Lee antes `README.md`, `CLAUDE.md` y las specs 07 (Metas) y 08 (Nueva meta).

## Reglas de diseño (obligatorias)

- No hay imagen de diseño para esta pantalla: usa **los mismos colores, tipografía, tarjetas, bordes y sombras de la pantalla Metas** (`docs/design/metas_1.png`). No inventes colores ni estilos nuevos.
- **Nada de lo que se muestra puede ser inventado ni de muestra:** todo valor sale de la meta y de los aportes que el usuario realmente registró. Si no hay datos para un elemento, su espacio muestra un mensaje breve y gris ("Aún no hay datos").
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Ver una meta al detalle: cuánto se lleva, cuál es el objetivo, cuánto falta, la fecha límite y la lista de aportes hechos, con su fecha y su monto. Desde aquí también se editan los aportes (ver "Editar aportes").

Se llega con el botón **"Detalle de meta"** de cada tarjeta en la pantalla Metas (spec 07).

## Cambio en la pantalla Metas (spec 07)

- Cada tarjeta de meta tiene dos botones del mismo tamaño, uno al lado del otro:
  - **"+ Aportar"**, igual que antes (azul oscuro, texto blanco).
  - **"Detalle de meta"**, con borde azul y texto azul, como el botón "+ Nueva Meta".

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:** botón circular con flecha hacia atrás y el título "Detalle de meta".
2. **Tarjeta de resumen** (blanca, bordes redondeados):
   - Ícono y color de la categoría de la meta (los mismos de la pantalla Metas), el nombre de la meta en negrita y la pastilla gris con la categoría ("Sin categoría" en las metas creadas antes de existir la columna).
   - "Ahorrado" con el monto en azul y "Objetivo" con el monto y "COP".
   - "Faltan $[falta] COP" y el porcentaje en verde.
   - Barra de progreso con el mismo degradado de la pantalla Metas.
   - Tres líneas con ícono: **Fecha límite** ("Junio 2027", o "Aún no hay datos"), **Creada el** ("14 Oct 2026") y **Aportes hechos** (cantidad).
3. **Aportes ([N]):** título en negrita y una tarjeta blanca con la lista de aportes, del más reciente al más antiguo. Cada fila tiene un ícono de alcancía, la fecha ("14 Oct 2026") y el monto en verde con "+" ("+$500,000.00").
   - Sin aportes: "Aún no tienes aportes en esta meta." en gris.

## Datos reales (Supabase)

| Elemento | Fuente |
|---|---|
| Nombre, objetivo, fecha límite y fecha de creación | Tabla `metas` |
| Ahorrado | Suma de los `aportes` de la meta |
| Faltan y porcentaje | Mismo cálculo de la spec 07 |
| Lista de aportes | Tabla `aportes` de la meta (`id_aporte`, `monto`, `fecha`) |

- Solo se leen datos del usuario que inició sesión (RLS de `metas` y `aportes`).
- No cambies las tablas de Supabase sin confirmación del equipo.

## Comportamiento

- **Flecha atrás:** vuelve a Metas, que se recarga para mostrar los totales actualizados.
- Mientras carga, se ve un círculo de carga. Si falla, un texto de error en rojo dentro de la pantalla (sin Snackbar) con "Reintentar".

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente. Sin alturas ni anchos fijos. Funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] Cada meta de la pantalla Metas tiene el botón "Detalle de meta" y abre esta pantalla.
- [ ] Se ven el nombre, el ahorrado, el objetivo, lo que falta, el porcentaje, la fecha límite y la fecha de creación reales.
- [ ] La lista muestra todos los aportes de la meta, del más reciente al más antiguo, con su fecha y monto.
- [ ] Una meta sin aportes muestra "Aún no tienes aportes en esta meta.".
- [ ] No hay datos inventados.

## Decisiones tomadas por defecto (confirmar con el equipo)

- Sin diseño propio: se reutiliza el estilo de la pantalla Metas.
- Fechas en formato corto: "14 Oct 2026". Montos con el mismo formato de la pantalla Metas ("$1,500,000.00").

## Decisiones pendientes (preguntar al equipo, no inventar)

1. ¿Se necesita un diseño propio para esta pantalla?
2. ¿Se pueden borrar aportes, además de editarlos?

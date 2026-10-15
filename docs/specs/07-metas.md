# Spec 07 · Metas

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a la imagen de referencia: mismos colores, degradados, tipografía, tamaños, espaciados, bordes redondeados, sombras, íconos, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones, pastillas, barras de progreso y tarjetas mantienen sus colores y degradados tal como se ven en la imagen.
- No agregar, quitar ni renombrar elementos que no estén en el diseño, salvo las excepciones aprobadas de este documento (ver "Simplificación aprobada").
- **Si un elemento que se mantiene no tiene datos todavía, su espacio se conserva y muestra un mensaje breve y gris (por ejemplo "Aún no hay datos"). Nunca se ponen datos ficticios ni datos que aún no existen.**
- Los datos de la imagen ("Vacaciones San Andrés 2025", "$14.850.000", "3 quincenas", etc.) son solo un ejemplo del diseño. **Nada de lo que se muestra puede ser inventado ni de muestra:** todo valor sale de las metas y aportes que el usuario realmente registró. Si no hay datos para un elemento, se muestra el estado vacío indicado en esta spec. Lo que cambia son los valores, nunca el diseño.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Mostrar las metas de ahorro del usuario, cuánto lleva ahorrado en cada una y cuánto le falta. Desde aquí el usuario crea una meta nueva (spec 08). Se llega desde la pestaña **Metas** de la barra de navegación.

## Simplificación aprobada (Valentina, 2026-10-14)

Para que la pantalla no se vea recargada y muestre solo lo importante, se **quitan** estos elementos del diseño:

- La tarjeta "¡Vas con paso firme!".
- El enlace "Ordenar ⌄".
- El botón "Ajustar regla" de cada meta (queda solo "+ Aportar").

Se **mantienen**, con sus datos reales o con el mensaje de "sin datos": el consejo de Finanzia IA y el recuadro "Próximo aporte".

## Diseño de referencia

- `docs/design/metas_1.png`: parte superior de la pantalla, con la primera meta completa y el comienzo de la segunda.

> La imagen tiene baja resolución (252 × 570 px), el título se ve cortado ("Metas Fin…") y no se ve la barra de navegación. Ver "Decisiones pendientes".

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:**
   - A la izquierda, botón circular con flecha hacia atrás.
   - Título "Metas Financieras" en negrita con una insignia verde de check, y debajo, en gris, "Finanzia proyecta tu ahorro".
   - A la derecha, botón con borde azul "⊕ + Nueva Meta".
2. **Tarjeta Ahorro acumulado** (fondo blanco, bordes redondeados):
   - Ícono de alcancía en un círculo azul, "Ahorro Acumulado en Metas" y, a la derecha, pastilla verde clara "[X]% completado".
   - Monto grande en negrita "$[ahorrado]" y, en gris, "/ $[objetivo] COP".
   - Barra de progreso con degradado azul → verde.
   - Recuadro azul claro con ícono de destellos, "Consejo de Finanzia IA ✦" en azul y un texto con el nombre de la meta en negrita.
3. **Metas Activas ([N]):** ícono de bandera y título en negrita.
4. **Tarjeta de meta** (fondo blanco, bordes redondeados), una por cada meta activa:
   - Ícono de la categoría en un cuadro azul claro, nombre de la meta en negrita (puede ocupar dos líneas) y menú de tres puntos.
   - Pastilla gris con la categoría ("Viajes & Ocio") y, en verde, ícono de reloj con el mes y año límite ("Junio 2025").
   - "Ahorrado" en gris y debajo el monto en azul y negrita; a la derecha, "Meta" en gris y el monto objetivo con "COP".
   - "Faltan $[falta] COP" en gris y el porcentaje en verde a la derecha.
   - Barra de progreso con degradado azul oscuro → verde.
   - Recuadro gris claro con ícono de calendario: "Próximo aporte: **$[monto] COP** [frecuencia] ([fechas]…".
   - Botón "+ Aportar" (azul oscuro, texto blanco).
5. **Barra de navegación inferior** fija, con la pestaña **Metas** seleccionada (ver spec 05, "Barra de navegación": sin Gráficos).

## Datos reales (Supabase)

| Elemento del diseño | Fuente |
|---|---|
| Lista de metas | `Metas` activas del usuario, de la más reciente a la más antigua |
| Nombre de la meta | Campo `Nombre` de `Metas` |
| Mes y año límite | Campo `Fecha limite` de `Metas`, como "[Mes] [año]" |
| Meta (objetivo) | Campo `Monto objetivo` de `Metas` |
| Ahorrado | Suma de los `Aportes` de la meta |
| Faltan | Monto objetivo − ahorrado (nunca menor que 0) |
| Porcentaje de la meta | Ahorrado ÷ monto objetivo × 100, redondeado a entero (máximo 100) |
| Ahorro acumulado y objetivo total | Suma de lo ahorrado y suma de los montos objetivo de todas las metas activas |
| "[X]% completado" | Ahorro acumulado ÷ objetivo total × 100, redondeado a entero |
| "Metas Activas ([N])" | Número de `Metas` activas |
| Categoría de la meta | **No existe en el esquema** (ver spec 08) |
| Próximo aporte y frecuencia | **No existe en el esquema** (ver spec 08) |

- Solo se leen datos del usuario que inició sesión.
- No cambies las tablas de Supabase sin confirmación del equipo.

## Elementos que dependen de los datos reales y su estado vacío

- **Consejo de Finanzia IA:** sin IA por ahora. Si una meta tiene al menos dos aportes, se calcula su ritmo (ahorrado ÷ días desde el primer aporte) y se muestra: "A tu ritmo actual alcanzarás la meta **"[nombre]"** [N] días antes de lo previsto." o "… [N] días después de lo previsto.", comparando la fecha proyectada con la fecha límite. Se usa la meta activa con fecha límite más cercana. Si no hay datos suficientes: "Aún no hay suficiente información para darte un consejo. Registra tus aportes para empezar."
- **Pastilla de categoría e ícono de la meta:** mientras no exista la categoría, la pastilla dice "Sin categoría" y se usa un ícono genérico de meta del mismo estilo.
- **Recuadro "Próximo aporte":** se mantiene. Mientras no exista la frecuencia del aporte, dice "Próximo aporte: aún no hay datos" en gris, con el mismo ícono de calendario.
- **Usuario sin metas:** la tarjeta de ahorro acumulado muestra `$0` de `$0` sin pastilla de porcentaje ni consejo, "Metas Activas (0)" y, en lugar de las tarjetas, un texto breve y gris, "Aún no tienes metas. Crea la primera con + Nueva Meta.", con la tipografía de la app.

## Comportamiento

- **Pestaña Metas:** abre esta pantalla y queda seleccionada. **Inicio** vuelve al Dashboard.
- **Flecha atrás:** vuelve al Dashboard.
- **"+ Nueva Meta":** abre la pantalla Nueva Meta (spec 08). Al guardar una meta, vuelve aquí y la meta nueva aparece de primera.
- **Menú de tres puntos y "+ Aportar":** se muestran como en el diseño pero **no tienen acción por ahora**, hasta que el equipo defina qué hacen.
- Los datos se cargan al entrar a la pantalla. Mientras carga, muestra un estado de carga simple que no cambie el diseño; si falla la lectura, muestra un texto de error en rojo dentro de la pantalla (sin Snackbar) con la opción de reintentar.

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente. La barra de navegación queda fija abajo, sin tapar el contenido.
- Los montos largos reducen su letra o se cortan con "…" sin romper el diseño.
- Sin alturas ni anchos fijos. Respeta las barras del sistema y funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] La pantalla es visualmente idéntica a la imagen (colores, degradados, tipografía, íconos, textos y orden).
- [ ] La pestaña Metas abre esta pantalla y "+ Nueva Meta" abre la spec 08.
- [ ] Cada meta muestra su nombre, mes límite, ahorrado, objetivo, lo que falta y el porcentaje correctos.
- [ ] El ahorro acumulado y "[X]% completado" coinciden con la suma de todas las metas activas.
- [ ] Una meta recién creada aparece de primera al volver.
- [ ] Un usuario sin metas ve el estado vacío.
- [ ] No hay ningún dato inventado: no aparecen "Vacaciones San Andrés", "$14.850.000" ni "$200.000 COP quincenal" si el usuario no los registró.
- [ ] No aparecen la tarjeta "¡Vas con paso firme!", "Ordenar" ni "Ajustar regla".
- [ ] Lo que no tiene datos muestra su mensaje de "sin datos" en su mismo espacio.
- [ ] Un usuario solo ve sus propias metas.
- [ ] Se puede hacer scroll hasta el final en pantalla pequeña y la barra de navegación no tapa el contenido.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño (salvo los estados vacíos y los mensajes de "sin datos").

## Decisiones tomadas por defecto (confirmar con el equipo)

- Las metas se ordenan de la más reciente a la más antigua.
- El consejo se calcula con el ritmo real de aportes, sin IA.
- Mientras no existan sus datos, la pastilla de categoría dice "Sin categoría" y el recuadro "Próximo aporte" dice "aún no hay datos".

## Decisiones pendientes (preguntar al equipo, no inventar)

1. **Imagen en alta resolución:** se necesita la imagen original completa (título completo, segunda meta y barra de navegación).
2. **Título:** ¿el título completo es "Metas Financieras"? ¿Qué dice completo el subtítulo "Tu copiloto IA proyect…"?
3. **Categoría y frecuencia de la meta:** ver spec 08, decisiones pendientes 2 y 3.
4. **Acciones:** qué hacen el menú de tres puntos y "+ Aportar" (¿registra un aporte desde una cuenta?).
5. **Metas cumplidas:** cuando una meta llega al 100 %, ¿sigue en "Metas Activas" o pasa a otra sección?
6. **Formato de montos:** las pantallas nuevas usan `$14.850.000 COP` y el Dashboard usa `$12,450.00`. Debe ser el mismo en toda la app.

# Spec 12 · Estadísticas (pestaña Gráficos)

Lee antes `README.md`, `CLAUDE.md` y `docs/specs/04-dashboard-inicio.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a la imagen de referencia: mismos colores, degradados, tipografía, tamaños, espaciados, bordes redondeados, sombras, íconos, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Las barras, tarjetas y botones mantienen sus colores y degradados tal como se ven en la imagen.
- No agregar, quitar ni renombrar elementos que no estén en el diseño, salvo las excepciones aprobadas de este documento.
- **Nada de lo que se muestra puede ser inventado ni de muestra.** Todo valor sale de los movimientos reales del usuario en Supabase. Los datos de la imagen (Mar 2026, `+$2.350.000`, `+14.2%`, `+6.5% vs Feb`, `44%`, `$61.600/día`, `IA 98%`, "Vacaciones San Andrés", `$120.000`, "6 días antes", etc.) son solo un ejemplo del diseño y no deben aparecer en el código ni en la pantalla. Si no hay datos para un elemento, se muestra el estado vacío o se oculta, según esta spec.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Mostrar al usuario una visión global de sus ingresos y gastos en un periodo (semana, mes, año o rango), con el flujo neto, la comparación con el periodo anterior, una gráfica de evolución y un diagnóstico, todo calculado con los movimientos que él registró.

## Diseño de referencia

- `docs/design/estadisticas_1.jpeg`: pantalla completa de Estadísticas.

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:** ícono de la app con "Finanzia" y "Copiloto IA" en verde; a la derecha, campana con punto azul y botón circular azul con ícono de persona (igual que en el Dashboard).
2. **Título:** "Estadísticas" con un ícono de destellos morado, y debajo "Visión global de tus flujos de dinero". A la derecha, un selector con punto verde: "COP ($)" y chevron hacia abajo.
3. **Selector de periodo:** cuatro opciones en una barra: **Semana**, **Mes (Mmm AAAA)**, **Año** y **Rango**. La opción activa va en una píldora blanca con texto azul en negrita; las demás en texto gris. Por defecto: Mes.
4. **Tarjeta "Flujo Neto Acumulado":**
   - Título "Flujo Neto Acumulado" y, a la derecha, una pastilla verde con flecha de tendencia y un porcentaje.
   - Monto grande con signo (`+` o `−`), seguido de "COP" en pequeño.
   - Línea con ícono de verificación verde y un mensaje de salud financiera.
   - Dos recuadros: **Ingresos** (flecha verde hacia abajo, monto, y debajo un texto verde de comparación) y **Gastos** (flecha roja hacia arriba, monto, y debajo un texto gris con el porcentaje del ingreso).
5. **Tarjeta "Evolución Semanal":** título, subtítulo "Ingresos vs. Gastos (…)", pastilla gris azulada a la derecha con el promedio diario; leyenda con "Ingresos" (punto verde oscuro) y "Gastos" (punto azul); gráfica de barras dobles (una verde y una azul por grupo) con líneas guía punteadas; etiquetas de cada grupo debajo, donde el grupo seleccionado va en negrita azul con un punto azul sobre sus barras; y una nota al pie.
6. **Tarjeta "Diagnóstico de tu Copiloto IA"** (fondo azul claro): ícono de robot con punto verde, título, subtítulo "Análisis conductual activo" en azul y una pastilla blanca "IA 98%"; un párrafo con texto y partes destacadas en negrita; y una tarjeta blanca "✨ Recomendación sugerida:" con su texto y un botón de degradado verde oscuro → azul "Aplicar sugerencia IA →".
7. **Barra de navegación inferior** fija: Inicio, Gráficos, Billetera, Metas, Asistente.

## Formato de números en esta pantalla

Esta pantalla usa el formato del diseño, que es distinto al del Dashboard:
- Montos en pesos colombianos, **con punto para miles y sin decimales**: `$4.200.000`, redondeando al peso más cercano.
- Porcentajes con **un decimal y punto decimal** y signo explícito en las variaciones: `+14.2%`, `+6.5%`. El porcentaje "de ingreso" va sin signo y sin decimales (`44%`).
- Meses abreviados en español con la primera letra mayúscula: Ene, Feb, Mar, Abr, May, Jun, Jul, Ago, Sep, Oct, Nov, Dic. Meses completos donde el diseño los usa ("Marzo", "Febrero").

## Definiciones de periodo

Usa la zona horaria del dispositivo. Las semanas van de lunes a domingo.

| Opción | Periodo actual | Periodo anterior (para comparar) |
|---|---|---|
| Semana | La semana de hoy | La semana anterior |
| Mes | El mes calendario de hoy (el texto de la opción es "Mes (Nov 2026)", con el mes y año reales) | El mes anterior |
| Año | El año de hoy | El año anterior |
| Rango | Las fechas que elija el usuario | Un periodo inmediatamente anterior y de la misma duración |

Los cálculos usan solo los movimientos **activos** del usuario que inició sesión, por su campo `Fecha`.

## Datos reales (Supabase)

| Elemento del diseño | Cómo se calcula |
|---|---|
| Monto del "Flujo Neto Acumulado" | Ingresos del periodo − gastos del periodo, con signo `+` o `−` |
| Pastilla de porcentaje del flujo neto | Variación del flujo neto respecto del periodo anterior: (neto actual − neto anterior) ÷ \|neto anterior\| × 100, con un decimal. Solo se muestra si el periodo anterior tiene movimientos y su neto no es cero. Si es positiva: pastilla verde con flecha hacia arriba, como en el diseño. Si es negativa: la misma pastilla con los tonos rojos que ya usa la app (los del recuadro de Gastos) y flecha hacia abajo |
| Mensaje de salud financiera | Con el ahorro del periodo = neto ÷ ingresos × 100: si es 20% o más, "Salud financiera excelente este periodo"; si está entre 0% y menos de 20%, "Salud financiera estable este periodo"; si es negativo, "Tus gastos superan tus ingresos este periodo" (con ícono de alerta y el rojo ya usado en la app). Si no hay ingresos en el periodo, no se muestra |
| Recuadro Ingresos | Suma de movimientos de tipo ingreso del periodo |
| Comparación bajo Ingresos | Variación de los ingresos respecto del periodo anterior, con un decimal y el texto "vs [periodo anterior]" ("vs Feb" para mes, "vs sem. anterior" para semana, "vs 2025" para año, "vs periodo anterior" para rango). Verde si sube, rojo si baja. Se oculta si el periodo anterior no tuvo ingresos |
| Recuadro Gastos | Suma de movimientos de tipo gasto del periodo |
| Texto bajo Gastos | "[X]% de ingreso", donde X = gastos ÷ ingresos × 100. Se oculta si no hay ingresos |
| Pastilla "Prom. $X/día" | Gastos del periodo ÷ número de días del periodo. Si el periodo está en curso, se cuentan los días transcurridos hasta hoy inclusive |
| Gráfica | Ver "Gráfica de evolución" |
| Texto del diagnóstico | Ver "Diagnóstico" |
| Recomendación | Ver "Diagnóstico" |

## Gráfica de evolución

Cada grupo muestra dos barras: ingresos (verde) y gastos (azul), con las sumas de los movimientos de ese tramo.

| Periodo | Grupos | Título y subtítulo |
|---|---|---|
| Semana | Un grupo por día (Lun, Mar, Mié, Jue, Vie, Sáb, Dom) | "Evolución Diaria" / "Ingresos vs. Gastos (Esta semana)" |
| Mes | 4 grupos: Sem 1 (días 1 a 7), Sem 2 (8 a 14), Sem 3 (15 a 21), Sem 4 (22 hasta fin de mes) | "Evolución Semanal" / "Ingresos vs. Gastos ([Mes completo])" |
| Año | Un grupo por mes (Ene a Dic) | "Evolución Mensual" / "Ingresos vs. Gastos ([Año])" |
| Rango | Por días si dura 14 días o menos; por semanas ("Sem 1", "Sem 2"…, de 7 días desde el inicio) si dura hasta 120 días; por meses si dura más | Según el agrupamiento, con el subtítulo "Ingresos vs. Gastos (dd MMM – dd MMM)" |

- La altura de las barras es proporcional al valor más alto de toda la gráfica. Un valor mayor que cero siempre tiene una barra visible; un valor de cero no tiene barra. Mantén las líneas guía punteadas del diseño.
- **Grupo seleccionado:** por defecto, el grupo que contiene el día de hoy (o el último grupo si el periodo ya terminó). Al tocar otro grupo, queda seleccionado: etiqueta en negrita azul y un punto azul sobre sus barras. La selección no cambia ninguna otra cifra de la pantalla.
- **Grupos futuros** (que aún no han llegado): se muestran con la etiqueta atenuada y sin barras.
- **No hay proyecciones.** El asterisco "*" de la etiqueta y la nota "*Semana 4 incluye proyección estimada" del diseño **no se muestran**, porque no hay datos reales detrás.
- **Sin movimientos en el periodo:** en lugar de las barras, un texto gris centrado "Aún no tienes movimientos en este periodo". La pastilla de promedio se oculta.

## Diagnóstico

El diagnóstico se genera solo con datos reales, sin inteligencia artificial por ahora.

1. **Encabezado:** se mantienen el ícono, el título "Diagnóstico de tu Copiloto IA" y el subtítulo "Análisis conductual activo". La pastilla "IA 98%" **no se muestra**, porque no hay un valor real detrás.
2. **Primera frase:** si hay gastos en el periodo: "Tu mayor concentración de gasto está en **[Categoría] ([X]%)**." (categoría con más gasto y su porcentaje del total de gastos, con el nombre y el porcentaje en negrita). Si no hay gastos: "Aún no hay suficiente información para generar tu diagnóstico. Registra tus ingresos y gastos para empezar." y no se muestra la recomendación.
3. **Segunda frase (opcional):** si alguna categoría con gastos en ambos periodos bajó al menos 1%, se usa la de mayor reducción: "Redujiste un **[X]%** tu gasto en [Categoría] comparado con [periodo anterior]." ("comparado con Febrero", "la semana anterior", "2025" o "el periodo anterior"). Si ninguna bajó, o no hay periodo anterior con gastos, esta frase no se muestra.
4. **Recomendación sugerida:** el bloque blanco se muestra **solo si** el usuario tiene al menos una meta activa en `Metas` (se usa la más reciente) y su flujo neto del periodo es mayor que cero. Texto: "Tu flujo neto de este periodo es **$[neto] COP**. Podrías destinar parte a tu meta **"[Nombre de la meta]"**: te faltan **$[faltante] COP** para completarla." El faltante es el `Monto objetivo` menos la suma de sus `Aportes`. En caso contrario, el bloque no aparece.
5. El botón "Aplicar sugerencia IA →" se muestra igual que en el diseño dentro de la recomendación, pero **no tiene acción por ahora**.

## Comportamiento

- **Acceso:** esta pantalla es la pestaña **Gráficos** de la barra inferior. Desde el Dashboard, tocar "Gráficos" abre Estadísticas, y tocar "Inicio" vuelve al Dashboard. Las pestañas Billetera, Metas y Asistente siguen sin acción.
- **Pestaña seleccionada:** en esta pantalla la pestaña resaltada es **Gráficos**. (La imagen de referencia tiene resaltada "Inicio", pero esta pantalla pertenece a Gráficos.) Es una excepción aprobada y solo cambia cuál pestaña está resaltada.
- **Selector de periodo:** al tocar una opción, la pantalla se recalcula con ese periodo. **Rango** abre un selector de fechas de Material 3 (rango de fechas) en un cuadro de diálogo, con los colores de la app y sin estilos nuevos; al confirmar, se usa ese rango. Es una excepción aprobada porque el diseño no incluye este selector.
- **Selector "COP ($)":** la app trabaja solo en pesos colombianos. Se muestra igual que en el diseño y no tiene acción.
- **Campana, botón de persona:** igual que en el Dashboard (el punto azul de la campana no se muestra porque no hay notificaciones reales; el botón de persona abre el menú "Cerrar sesión").
- **Carga y error:** estado de carga simple sin cambiar el diseño; si falla la lectura, un texto de error en rojo dentro de la pantalla (sin Snackbar) con la opción de reintentar.
- **Actualización:** al entrar a la pantalla, y al volver de registrar o editar un movimiento, se leen los datos de nuevo.

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente y la barra de navegación inferior queda fija sin tapar el contenido.
- Sin alturas ni anchos fijos: las barras de la gráfica se ajustan al ancho disponible. Respeta las barras del sistema y funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] La pantalla es visualmente idéntica a `estadisticas_1.jpeg` (colores, degradados, tipografía, íconos, textos y orden).
- [x] No aparece ningún dato de ejemplo ("2.350.000", "Mar 2026", "14.2%", "6.5%", "61.600", "98%", "Vacaciones San Andrés", "120.000", "6 días antes") si el usuario no lo generó con sus datos.
- [x] El flujo neto, los ingresos y los gastos coinciden con la suma de los movimientos del usuario en el periodo elegido.
- [x] Cambiar entre Semana, Mes, Año y Rango recalcula toda la pantalla; el texto de la opción "Mes" muestra el mes y año reales.
- [x] Las comparaciones "vs [periodo anterior]" se calculan con el periodo anterior y se ocultan cuando no hay datos para compararlas.
- [x] La gráfica muestra las barras con los valores reales, el grupo seleccionado se resalta y no hay proyecciones ni asterisco.
- [x] Un usuario sin movimientos ve montos en `$0`, el texto "Aún no tienes movimientos en este periodo" y el diagnóstico de falta de información.
- [ ] Un usuario solo ve sus propios datos.
- [x] La pestaña Gráficos abre esta pantalla y la pestaña Inicio vuelve al Dashboard.
- [x] Se puede hacer scroll hasta el final en pantalla pequeña y con letra grande.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño (salvo el selector de fechas del Rango, los estados vacíos, y los textos y pastillas en rojo descritos).

> Verificación: los criterios marcados se comprobaron con `CalculosEstadisticasTest`, `FormatosEstadisticasTest`,
> `EstadisticasTest`, `NavegacionEstadisticasTest`, `EstadisticasPantallaPequenaTest`, `PantallasAdaptablesTest`
> (pantalla pequeña con letra normal y al 200 %) y la prueba manual contra Supabase real
> `integracion/manual/DemoMateoMarinTest` (Mateo Marin: ingresos, gastos, neto y cada semana de octubre de 2026
> coinciden con la tabla `movimientos`). El Rango se probó con las fechas que entrega el selector; el cuadro de
> diálogo de fechas en sí no se probó con un test automático.
> Sin marcar: la igualdad visual exacta y "ningún elemento ni color nuevo" (revisión a ojo del equipo en el emulador);
> "un usuario solo ve sus propios datos" (se filtra por el usuario y las políticas RLS están activas, pero falta una
> prueba con dos usuarios reales).

## Decisiones tomadas por defecto (confirmar con el equipo)

- Pendientes 1 a 6: se aplicaron tal como las propone esta spec (umbrales 20 % y 0 %; "Aplicar sugerencia IA" sin acción;
  sin proyecciones; sin IA real; agrupación del Rango por días, semanas o meses; formato `$4.200.000` solo en esta pantalla).
- Sin conexión con IA real por ahora: el diagnóstico se calcula con los datos.
- La recomendación no se muestra si a la meta no le falta dinero.
- Si se cancela el selector de fechas, queda el periodo que estaba.
- Rango por días: cada grupo se nombra con el número del día. Rango de más de 120 días que cruza de año: el mes lleva el año corto ("Ene 27").
  Si hay muchos grupos, se escribe una etiqueta de cada tantas (siempre la seleccionada).
- Porcentajes de categoría y de reducción, sin decimales, como en el diseño ("44%", "18%"). Empate entre categorías: la primera en orden alfabético.
- Signo negativo con guion normal ("-$510.000", "-3.0%"), igual que el Dashboard. Flujo neto en cero: "$0" sin signo.
- Desde Estadísticas, la pestaña Metas abre Metas; Gráficos también funciona desde Metas y Notificaciones. "Atrás" siempre vuelve al Dashboard.
- Pantallas angostas o letra grande: el selector de moneda, las pastillas y los recuadros pasan debajo y el selector de periodo usa dos filas; en un teléfono normal queda igual al diseño.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. Los umbrales del mensaje de salud financiera (20% = "excelente", entre 0% y 20% = "estable", negativo = "Tus gastos superan tus ingresos"): ¿se aprueban o el equipo prefiere otros?
2. Qué hace "Aplicar sugerencia IA": ¿registra un aporte a la meta (tabla `Aportes`) y de qué cuenta sale? Por ahora no hace nada.
3. Las proyecciones de la semana en curso (que el diseño sugiere con "Sem 4*") quedan fuera por ahora.
4. La IA real de Finanzia (pastilla "IA 98%", recomendaciones personalizadas) queda para una fase posterior.
5. El agrupamiento del Rango (por días, semanas o meses según la duración) es una propuesta; confirmar.
6. Unificar el formato de montos: el Dashboard usa `$12,450.00` y esta pantalla usa `$4.200.000`, como en sus diseños. ¿Se deja así?

No cambies las tablas de Supabase sin confirmación del equipo.

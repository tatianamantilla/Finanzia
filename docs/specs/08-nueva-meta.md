# Spec 08 · Nueva meta

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a la imagen de referencia: mismos colores, tipografía, tamaños, espaciados, bordes redondeados, sombras, íconos, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones, pastillas y tarjetas mantienen sus colores tal como se ven en la imagen.
- No agregar, quitar ni renombrar elementos que no estén en el diseño, salvo las excepciones aprobadas de este documento.
- Los valores de la imagen ("Vacaciones en San Andrés", "3.500.000", "Noviembre 2026") son solo un ejemplo del diseño. Al abrir la pantalla, los campos están vacíos; lo que se guarda es lo que el usuario escribe y elige.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Permitir que el usuario cree una meta de ahorro: su nombre, su categoría, el monto que quiere juntar, la fecha en que quiere lograrlo y cada cuánto va a aportar. Se llega desde el botón "+ Nueva Meta" de la pantalla Metas (spec 07).

## Diseño de referencia

- `docs/design/nueva_meta_1.png`: paso 1 de 2 (Configuración), de arriba hasta la frecuencia del aporte.

> La imagen tiene baja resolución (238 × 577 px), la frecuencia del aporte y el cuarto monto rápido se ven cortados, y no se ve el botón para pasar al paso 2 ni el paso 2. Ver "Decisiones pendientes".

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:** botón circular con flecha hacia atrás a la izquierda; en el centro, "Nueva Meta" en negrita y debajo, en gris, "Paso 1 de 2 • Configuración"; a la derecha, botón circular con "?".
2. **Tarjeta de introducción** (fondo gris azulado claro):
   - Pastillas "✦ Asistido por Finanzia IA" (verde clara) y "Optimizado COP" (gris).
   - Título "Define tu objetivo financiero" en negrita.
   - Texto: "Establece tu meta y nuestro copiloto calculará el ritmo de ahorro perfecto según tus ingresos reales en Colombia."
3. **Nombre de la meta:** etiqueta en negrita y, a la derecha, "Obligatorio" en gris. Campo blanco con ícono de bandera, el texto escrito y un botón "⊗" para borrarlo.
4. **Categoría de la meta:** etiqueta en negrita y, a la derecha, "6 disponibles" en verde. Cuadrícula de 3 × 2 botones, cada uno con ícono y nombre:
   - Viajes & Ocio (avión), Emergencia (escudo), Tecnología (computador), Vehículo (moto), Vivienda (casa), Educación (birrete).
   - El seleccionado tiene fondo verde y texto oscuro; los demás, fondo blanco.
5. **Tarjeta Monto objetivo total:** etiqueta en negrita y, a la derecha, pastilla gris "Moneda: COP ($)". Debajo, "$" grande en azul a la izquierda y el monto grande en negrita a la derecha con "COP" en gris. Fila de montos rápidos: "+$500.000", "+$1.000.000", "+$2.000.000" y un cuarto cortado en la imagen; el elegido se ve en verde.
6. **Tarjeta Fecha estimada de logro:** etiqueta en negrita. Campo con ícono de calendario, el mes y año en negrita y debajo, en gris, "[N] meses restantes • [N] quincenas"; a la derecha, ícono de calendario para cambiar la fecha.
7. **Frecuencia del aporte:** etiqueta en negrita y un selector de dos opciones: "Quincenal (15 y 30)" y "Mensual (Día 30)".
8. El resto de la pantalla (botón para continuar) no se ve en la imagen.

## Validación

Cada error se muestra en rojo dentro de su campo (borde rojo y texto debajo), sin Snackbar, como en las demás pantallas. El error se borra al corregir el campo.

- **Nombre:** obligatorio, de 1 a 60 caracteres sin contar espacios al inicio y al final. Error: "Escribe el nombre de tu meta".
- **Categoría:** obligatoria, solo una a la vez. Error: "Elige una categoría".
- **Monto objetivo:** obligatorio, solo números y mayor que 0. Error: "Escribe un monto mayor que 0".
- **Fecha estimada:** obligatoria y posterior al mes actual. Error: "Elige una fecha futura".
- **Frecuencia:** obligatoria. Por defecto, "Quincenal (15 y 30)".

## Datos reales (Supabase)

Al terminar la creación se guarda una fila en `Metas`:

| Campo de `Metas` | Valor |
|---|---|
| `Id usuario` | El usuario que inició sesión |
| `Nombre` | El nombre escrito, sin espacios al inicio y al final |
| `Monto objetivo` | El monto escrito |
| `Fecha limite` | Último día del mes elegido |
| `Fecha creacion` | Fecha y hora actuales |
| `Activo` | Verdadero |

- La **categoría** y la **frecuencia del aporte** no tienen dónde guardarse en el esquema actual. Ver "Decisiones pendientes" 2 y 3.
- No cambies las tablas de Supabase sin confirmación del equipo.

## Comportamiento

- Al abrir la pantalla, el nombre y el monto están vacíos, ninguna categoría está seleccionada y la fecha está vacía.
- **⊗ del nombre:** borra el texto.
- **Categoría:** al tocar una queda seleccionada y la anterior deja de estarlo.
- **Montos rápidos:** al tocar uno se **suma** ese valor al monto escrito, y ese botón se ve en verde.
- **Fecha:** al tocar el campo o el ícono de calendario se abre un selector de mes y año. "[N] meses restantes" y "[N] quincenas" se calculan desde hoy hasta la fecha elegida (dos quincenas por mes).
- **Flecha atrás:** vuelve a Metas sin guardar.
- **"?":** se muestra como en el diseño pero **no tiene acción por ahora**.
- Mientras se guarda, el botón para continuar o guardar se desactiva; si falla, muestra un texto de error en rojo dentro de la pantalla (sin Snackbar).

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente y el teclado no tapa el campo que se está escribiendo.
- La cuadrícula de categorías mantiene tres columnas en un teléfono normal; la fila de montos rápidos se desplaza horizontalmente si no cabe.
- Sin alturas ni anchos fijos. Respeta las barras del sistema y funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] La pantalla es visualmente idéntica a la imagen (colores, tipografía, íconos, textos y orden).
- [ ] "+ Nueva Meta" de la pantalla Metas abre esta pantalla y la flecha atrás vuelve sin guardar.
- [ ] Solo se puede elegir una categoría a la vez.
- [ ] Los montos rápidos suman al monto escrito.
- [ ] "[N] meses restantes • [N] quincenas" cambia al elegir otra fecha.
- [ ] Con campos vacíos o inválidos no se guarda y cada error aparece en rojo dentro de su campo.
- [ ] Al terminar, la meta aparece en Supabase (`Metas`) y de primera en la pantalla Metas.
- [ ] Un usuario solo puede crear metas a su nombre.
- [ ] Se puede hacer scroll hasta el final en pantalla pequeña y el teclado no tapa los campos.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño (salvo los mensajes de error).

## Decisiones tomadas por defecto (confirmar con el equipo)

- Los campos empiezan vacíos; los valores de la imagen son solo ejemplo.
- Frecuencia por defecto: "Quincenal (15 y 30)".
- Los montos rápidos suman al monto escrito.
- La fecha límite que se guarda es el último día del mes elegido.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. **Paso 2:** se necesita el diseño del paso 2 y del botón para pasar a él. Mientras no exista, ¿la meta se guarda al terminar el paso 1?
2. **Categoría de la meta:** ¿se agrega un campo `id categoria` en `Metas` (usando `Categorias` con un tipo "meta") o una tabla nueva? ¿Las seis categorías son fijas para todos los usuarios?
3. **Frecuencia del aporte:** ¿se agrega un campo `frecuencia` en `Metas`?
4. **Montos rápidos:** ¿suman al monto escrito o lo reemplazan? ¿Cuál es el cuarto monto?
5. **Imagen en alta resolución:** se necesita la imagen original completa.
6. **Botón "?":** ¿qué muestra?
7. **Formato de montos:** las pantallas nuevas usan `3.500.000 COP` y el Dashboard usa `$12,450.00`. Debe ser el mismo en toda la app.

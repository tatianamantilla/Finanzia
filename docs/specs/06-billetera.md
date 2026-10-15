# Spec 06 · Billetera

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a la imagen de referencia: mismos colores, degradados, tipografía, tamaños, espaciados, bordes redondeados, sombras, íconos, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones, pastillas y tarjetas mantienen sus colores y degradados tal como se ven en la imagen.
- No agregar, quitar ni renombrar elementos que no estén en el diseño, salvo las excepciones aprobadas de este documento.
- Los datos de la imagen ("Bancolombia Nómina", "$18.450.000", "8492", "Visa Gold Clásica", etc.) son solo un ejemplo del diseño. **Nada de lo que se muestra puede ser inventado ni de muestra:** todo valor sale de las cuentas y movimientos que el usuario realmente registró. Si no hay datos para un elemento, se muestra el estado vacío indicado en esta spec. Lo que cambia son los valores, nunca el diseño.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Mostrar en un solo lugar el dinero del usuario: el saldo total de todas sus cuentas y el saldo de cada cuenta, calculado con su saldo inicial y los ingresos y gastos que registró. Se llega desde la pestaña **Billetera** de la barra de navegación.

## Diseño de referencia

- `docs/design/billetera_1.png`: parte superior de la pantalla, con la primera cuenta completa y el comienzo de la segunda.

> La imagen tiene baja resolución (249 × 569 px) y algunos elementos se ven cortados (el cuarto filtro "B…" y la segunda cuenta "Visa Gold Clásica"). Ver "Decisiones pendientes".

## Estructura de la pantalla (de arriba hacia abajo)

1. **Encabezado:** igual al del Dashboard (spec 04): ícono de la app con "Finanzia" y "Copiloto IA" en verde; a la derecha, la campana y el botón circular azul con ícono de persona.
2. **Título:** "Mi Billetera" en negrita y debajo, en gris, "Cuentas y productos sincronizados". A la derecha, botón con borde azul "+ Conectar".
3. **Pastilla verde clara** con punto verde, "Sincronización bancaria segura (AES-256)" e ícono de escudo. *Ver "Decisiones pendientes" 1.*
4. **Tarjeta Saldo total consolidado** (degradado azul → verde oscuro, como la tarjeta Balance total del Dashboard):
   - Ícono de tarjeta y "SALDO TOTAL CONSOLIDADO"; botón circular con ícono de ojo a la derecha.
   - Monto grande en negrita.
   - Debajo, en texto claro, "Actualizado hace [tiempo] vía Open Banking".
   - Dos recuadros: **Disponible Débito** y **Cupo de Crédito**, cada uno con su monto.
5. **Tarjeta Consejo inteligente** (fondo lila claro): ícono morado en círculo, "Consejo inteligente" con destellos y un texto de una línea cortado con "…".
6. **Filtros** (fila que se desplaza horizontalmente): "Todas ([N])" (pastilla verde clara, seleccionada por defecto), "Débito ([N])" con ícono de banco, "Crédito ([N])" con ícono de tarjeta y un cuarto filtro cortado ("B…").
7. **Cuentas vinculadas:** título en negrita y a la derecha "[N] ACTIVAS" en verde.
8. **Tarjeta de cuenta** (fondo blanco, bordes redondeados), una por cada cuenta:
   - Ícono de banco en un cuadro azul claro.
   - Nombre de la cuenta en negrita (puede ocupar dos líneas).
   - Pastilla verde clara "Principal" (solo en la cuenta principal) y menú de tres puntos.
   - Debajo del nombre, en gris: "[tipo] • • • • [dígitos]".
   - "Saldo disponible" en gris y el monto en negrita.
   - A la derecha, botón con borde "Transferir" con ícono.
9. **Tarjeta de cuenta de crédito** (fondo verde oscuro, como "Visa Gold Clásica"), con pastilla "Activa". El resto no se ve en la imagen.
10. **Barra de navegación inferior** fija, con la pestaña **Billetera** seleccionada (ver spec 05, "Barra de navegación": sin Gráficos).

## Datos reales (Supabase)

| Elemento del diseño | Fuente |
|---|---|
| Saldo de cada cuenta | `Saldo inicial` de la `Cuentas` + ingresos − gastos de sus `Movimientos` activos |
| Saldo total consolidado | Suma de los saldos de todas las `Cuentas` activas del usuario (es el mismo Balance total del Dashboard) |
| Nombre de la cuenta | Campo `Nombre` de `Cuentas` |
| Tipo de la cuenta | Campo `Tipo` de `Cuentas` |
| Pastilla "Principal" | La cuenta con tipo `principal` |
| "[N] ACTIVAS" y "Todas ([N])" | Número de `Cuentas` activas del usuario |
| Dígitos "• • • • 8492" | **No existen en el esquema** |
| "Disponible Débito" / "Cupo de Crédito" | **No existe en el esquema** la diferencia entre débito y crédito ni el cupo |
| "Actualizado hace … vía Open Banking" | **No existe**: la app no se conecta a bancos |
| Consejo inteligente | Mismo cálculo del consejo del Dashboard (spec 04), sin IA por ahora |

- Solo se leen datos del usuario que inició sesión.
- Hoy cada usuario tiene una sola cuenta, "Principal", creada al registrarse (`docs/supabase/01-usuarios-y-auth.sql`). La app todavía no permite crear más cuentas.
- No cambies las tablas de Supabase sin confirmación del equipo.

## Elementos que dependen de los datos reales y su estado vacío

- **Dígitos de la cuenta:** como no existen, no se muestran. Debajo del nombre se ve solo el tipo de la cuenta.
- **"Actualizado hace … vía Open Banking":** no se muestra, porque no hay conexión bancaria.
- **Recuadros Disponible Débito y Cupo de Crédito:** mientras no exista el tipo débito/crédito, "Disponible Débito" muestra el saldo total y "Cupo de Crédito" muestra `$0.00`.
- **Filtros Débito y Crédito:** mientras no exista ese tipo, muestran "(0)" y al elegirlos se ve el estado vacío.
- **Tarjeta de cuenta de crédito:** solo se muestra si el usuario tiene una cuenta de crédito.
- **Consejo inteligente:** si no hay gastos en el mes, muestra "Aún no hay suficiente información para darte un consejo. Registra tus ingresos y gastos para empezar.", igual que en el Dashboard.
- **Usuario sin movimientos:** la cuenta Principal muestra su saldo inicial (o `$0.00`).
- **Sin cuentas** (o un filtro sin resultados): en la sección de cuentas, un texto breve y gris, "Aún no tienes cuentas", con la tipografía de la app.

## Comportamiento

- **Pestaña Billetera:** abre esta pantalla y queda seleccionada. **Inicio** vuelve al Dashboard.
- **Campana:** abre Notificaciones (spec 05).
- **Botón de persona:** el mismo menú "Cerrar sesión" del Dashboard.
- **Ojo:** alterna entre mostrar y ocultar los montos de la tarjeta de saldo (por ejemplo con `••••`). Por defecto está visible.
- **Filtros:** al tocar uno queda seleccionado y la lista muestra solo esas cuentas.
- **"+ Conectar", menú de tres puntos y "Transferir":** se muestran como en el diseño pero **no tienen acción por ahora**, hasta que el equipo defina qué hacen.
- Los datos se cargan al entrar a la pantalla. Mientras carga, muestra un estado de carga simple que no cambie el diseño; si falla la lectura, muestra un texto de error en rojo dentro de la pantalla (sin Snackbar) con la opción de reintentar.

## Adaptable a cualquier dispositivo

- Toda la pantalla se desplaza verticalmente. La barra de navegación queda fija abajo, sin tapar el contenido.
- La fila de filtros se desplaza horizontalmente si no cabe. Los montos largos reducen su letra o se cortan con "…" sin romper el diseño.
- Sin alturas ni anchos fijos. Respeta las barras del sistema y funciona con letra grande y en pantallas pequeñas.
- En pantallas grandes, centra el contenido con un ancho máximo razonable.

## Criterios de aceptación

- [ ] La pantalla es visualmente idéntica a la imagen (colores, degradados, tipografía, íconos, textos y orden).
- [ ] La pestaña Billetera abre esta pantalla e Inicio vuelve al Dashboard.
- [ ] El saldo de cada cuenta es igual a su saldo inicial + ingresos − gastos.
- [ ] El saldo total consolidado es igual al Balance total del Dashboard.
- [ ] La cuenta Principal muestra la pastilla "Principal" y "[N] ACTIVAS" cuenta las cuentas reales.
- [ ] El ojo muestra y oculta los montos.
- [ ] No hay ningún dato inventado: no aparecen "Bancolombia", "8492", "$18.450.000", "Visa Gold" ni "Open Banking" si el usuario no los tiene.
- [ ] Un usuario solo ve sus propias cuentas.
- [ ] Se puede hacer scroll hasta el final en pantalla pequeña y la barra de navegación no tapa el contenido.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño (salvo los estados vacíos).

## Decisiones tomadas por defecto (confirmar con el equipo)

- El saldo total consolidado se calcula igual que el Balance total del Dashboard.
- Sin conexión bancaria: no se muestran "Actualizado hace … vía Open Banking" ni los dígitos de la cuenta.
- Los montos usan el mismo formato del Dashboard (`$18,450,000.00`), no el de la imagen (`$18.450.000 COP`).

## Decisiones pendientes (preguntar al equipo, no inventar)

1. **Texto "Sincronización bancaria segura (AES-256)":** el equipo pidió quitar los textos de AES-256 en otras pantallas y la app no sincroniza con bancos. ¿Se quita la pastilla o se cambia el texto?
2. **Imagen en alta resolución:** se necesita la imagen original completa (con la tarjeta de crédito y el resto de la lista).
3. **Cuarto filtro:** ¿qué dice el filtro cortado "B…"? (¿"Billeteras digitales"?)
4. **Débito y crédito:** ¿se agrega al esquema la diferencia entre cuentas de débito y de crédito, y el cupo de las tarjetas de crédito?
5. **Crear cuentas:** ¿cómo agrega el usuario una cuenta nueva? ¿Es lo que hace "+ Conectar"?
6. **Dígitos de la cuenta:** ¿se guardan los últimos cuatro dígitos en `Cuentas`?
7. **Acciones:** qué hacen "+ Conectar", el menú de tres puntos y "Transferir".
8. **Formato de montos:** ¿formato del Dashboard (`$18,450,000.00`) o el de la imagen (`$18.450.000 COP`)? Debe ser el mismo en toda la app.

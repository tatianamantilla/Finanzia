# Spec 10 · Saldo nunca negativo y editar movimientos

Lee antes `README.md`, `CLAUDE.md`, `03-crear-cuenta.md` y `04-dashboard-inicio.md`.

> Se llama 10 porque los números 05 a 09 ya los usan Notificaciones, Billetera, Metas, Nueva meta y Detalle de meta.

## Reglas de diseño (obligatorias)

- Todo lo que ya existe queda **idéntico** a su diseño en `docs/design/`.
- La pantalla de edición **reutiliza** el diseño de `crear_cuenta_4.jpeg` (ingreso) y `crear_cuenta_5.jpeg` (gasto): mismos colores, tipografía, tarjetas, opciones y botones. Solo cambian el título, los valores cargados y el texto del botón (ver abajo).
- La lista "Ver todos" **no tiene diseño de referencia**: usa exactamente las filas de "Movimientos recientes" del Dashboard, el encabezado con flecha atrás de `crear_cuenta_4.jpeg` y los colores, tipografía y componentes que ya tiene la app. No se inventan estilos nuevos.
- Los errores van **dentro de la pantalla**, en rojo, con el estilo de `olvidar_contraseña_error.jpeg` (borde rojo, fondo rosado claro, etiqueta roja, ícono de alerta y texto rojo debajo). Sin Snackbar, Toast ni cajas grises.

## Objetivo

1. Que el saldo de una cuenta **nunca quede negativo** por un movimiento nuevo o editado.
2. Que el usuario pueda **corregir** un ingreso o un gasto que registró mal.

## Regla del saldo

- **Saldo disponible de una cuenta** = `Saldo inicial` + ingresos − gastos de sus `Movimientos` activos.
- Ningún movimiento **nuevo** ni **editado** puede dejar el saldo disponible de su cuenta por debajo de **$0.00**.
- Un gasto **exactamente igual** al saldo disponible sí se permite (el saldo queda en `$0.00`).
- El monto debe ser un número válido **mayor que cero**.
- Los cálculos de dinero usan un tipo decimal exacto (`BigDecimal`), nunca `Float` ni `Double`.
- Si la regla se rompe, **no se guarda nada** y el campo del monto muestra en rojo:
  "No tienes saldo suficiente. Tu saldo disponible es $10,000.00." (con el saldo real).
  El error desaparece cuando el usuario cambia el monto.
- **Onboarding (`crear_cuenta_5`):** si el usuario no tiene saldo suficiente para su primer gasto (por ejemplo, omitió el ingreso), se bloquea con el mismo error y un texto que le indica que primero registre un ingreso. El diseño de la pantalla no cambia.
- **Cuentas que ya están en negativo** (datos de pruebas anteriores): no se corrigen solas. Se muestran tal cual y solo se permiten ediciones que **no empeoren** el saldo (lo dejan en cero o más, o lo suben).
- **Dónde se aplica:** el primer gasto del onboarding, el gasto nuevo del botón "+" del Dashboard, y la edición de ingresos y gastos.
- **Al editar:** el saldo disponible se calcula **sin** el movimiento que se edita y luego se aplica su nuevo valor. Así, al editar un gasto se cuenta lo que ese gasto ya había descontado, y al reducir un ingreso el resultado no puede quedar negativo.
- Además de la validación en la app, se propone un trigger en Supabase que rechace lo mismo: `docs/supabase/08-saldo-no-negativo.sql` (lo revisa y lo ejecuta el equipo).

## Pantalla 1 · Todos los movimientos ("Ver todos")

1. Encabezado como el de `crear_cuenta_4.jpeg`: botón blanco con flecha atrás y, al lado, el título "Movimientos".
2. Tarjeta blanca con **todos** los movimientos activos del usuario, del más reciente al más antiguo, con **exactamente** las mismas filas del Dashboard (ícono y color de la categoría, descripción, fecha y cuenta, monto con signo y categoría).
3. Sin movimientos: el texto gris "Aún no tienes movimientos", como en el Dashboard.
4. Tocar una fila abre su edición. La flecha atrás vuelve al Dashboard.

## Pantalla 2 · Editar ingreso / Editar gasto

Es la pantalla `crear_cuenta_4` (ingreso) o `crear_cuenta_5` (gasto) con estos cambios mínimos:

1. Título: **"Editar ingreso"** o **"Editar gasto"**.
2. El monto y la opción elegida (frecuencia del ingreso o categoría del gasto) vienen **cargados** con los valores actuales del movimiento.
3. Botón principal: **"Guardar cambios →"**, con el mismo estilo del botón de esa pantalla.
4. **No** hay enlace "Omitir este paso por ahora". La flecha atrás vuelve **sin guardar**.
5. Se puede cambiar: el **monto** y la **frecuencia** (ingreso) o la **categoría** (gasto). El **tipo** (ingreso o gasto) no se puede cambiar.

## Comportamiento

- En el Dashboard, cada fila de "Movimientos recientes" se puede **tocar** para editarla, sin cambiar el diseño de la fila.
- "Ver todos" abre la Pantalla 1.
- **Guardar cambios:** valida el monto y la regla del saldo. Si todo está bien, **actualiza el mismo registro** en `Movimientos` (un `UPDATE`, nunca un registro nuevo) y vuelve a la pantalla anterior con los datos al día: el Dashboard muestra de inmediato el nuevo balance, ingresos, gastos, lista de movimientos y consejo.
- Mientras guarda, el botón muestra el estado de carga sin cambiar su diseño. Los errores de conexión van en rojo dentro de la pantalla.
- Solo se pueden editar los movimientos del usuario que inició sesión (la política RLS `movimientos: solo los propios` de `02-permisos-y-rls.sql` ya lo exige para `UPDATE`).
- **No** se pueden eliminar movimientos (ver decisiones pendientes).

## Datos (Supabase)

| Acción | Tabla y campos |
|---|---|
| Leer todos los movimientos | `movimientos` activos del usuario, con su cuenta y categoría |
| Saldo disponible | `cuentas.saldo_inicial` + ingresos − gastos de los `movimientos` activos de esa cuenta |
| Guardar cambios | `UPDATE movimientos SET monto, id_categoria WHERE id_movimiento = …` (del usuario con sesión) |
| Frecuencia de un ingreso | Es su **categoría** (Mensual, Quincenal, Semanal o Diario), como se guarda desde el cambio de Valentina del 2026-10-26 |

## Criterios de aceptación

- [ ] Un gasto que deja el saldo por debajo de cero no se guarda y muestra el error en rojo en el campo del monto.
- [ ] Un gasto igual al saldo disponible se guarda y el saldo queda en `$0.00`.
- [ ] Un monto vacío, en cero o inválido no se guarda y muestra el error en rojo.
- [ ] El error desaparece al corregir el monto.
- [ ] En el onboarding, el primer gasto sin saldo suficiente se bloquea y pide registrar primero un ingreso.
- [ ] Tocar un movimiento del Dashboard abre su edición con el monto y la opción cargados.
- [ ] "Ver todos" abre la lista completa, del más reciente al más antiguo, con las mismas filas del Dashboard, y su estado vacío.
- [ ] Editar un gasto o un ingreso respeta la regla del saldo (incluido lo que el gasto ya había descontado).
- [ ] Al guardar se actualiza el mismo movimiento en Supabase, sin duplicarlo.
- [ ] El Dashboard muestra de inmediato el balance, ingresos, gastos, movimientos y consejo actualizados.
- [ ] En una cuenta ya negativa solo se permiten ediciones que no empeoran el saldo.
- [ ] Solo se pueden editar los movimientos propios.
- [ ] No se agregó ningún elemento ni color fuera de lo descrito en esta spec.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. **Eliminar movimientos:** no se incluye. Podría hacerse marcando `activo = false` (el movimiento deja de contar en el saldo). ¿Se agrega más adelante?
2. **Aportes a metas:** hoy el saldo del Dashboard **no** resta los aportes a metas. ¿Un aporte debe contar como salida de dinero de la cuenta (y entonces también aplicarle la regla del saldo)?
3. **Descripción y fecha:** ¿se pueden editar? Por ahora solo el monto y la frecuencia o categoría, como en el diseño.
4. **Varias cuentas:** hoy cada usuario tiene una sola cuenta ("Principal"). Si en el futuro hay varias, la regla se aplica por cuenta.

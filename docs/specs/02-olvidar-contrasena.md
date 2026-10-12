# Spec 02 · Olvidar contraseña

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- Las pantallas deben quedar **exactamente iguales** a las imágenes de referencia: mismos colores, misma tipografía, mismos tamaños, espaciados, bordes redondeados, íconos, emojis, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones mantienen sus colores y degradados tal como se ven en las imágenes (degradado verde → azul).
- Los estados de color de la validación (verde cuando se cumple, rojo en el error) se mantienen como en el diseño.
- No agregar, quitar ni renombrar elementos que no estén en el diseño. No "mejorar" el diseño.
- Los textos grises dentro de los campos son *placeholders* y se conservan iguales.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Permitir que un usuario que olvidó su contraseña la restablezca en dos pasos: verificar su identidad y crear una contraseña nueva.

## Diseños de referencia

- `docs/design/olvidar_contraseña_1.jpeg`: Paso 1 de 2, verificación.
- `docs/design/olvidar_contraseña_2.jpeg`: Paso 2 de 2, nueva contraseña.
- `docs/design/olvidar_contraseña_error.jpeg`: Paso 2 de 2 con error "Las contraseñas no coinciden".
- `docs/design/olvidar_contraseña_3.jpeg`: pantalla "Acceso autorizado" (descrita en `01-inicio-sesion.md`).

## Pantalla 1 · Paso 1 de 2 (`olvidar_contraseña_1.jpeg`)

1. Arriba: botón circular blanco con flecha atrás (izquierda) y pastilla "PASO 1 DE 2" con ícono de candado (derecha).
2. Centro: "FINANZIA" con punto verde y debajo "TU ASISTENTE FINANCIERO CON IA".
3. Tarjeta blanca:
   - Título: "¿Olvidaste tu contraseña? 🔐"
   - Texto: "Ingresa tu nombre de usuario y correo electrónico registrados para verificar tu identidad y restablecer tu contraseña."
   - Campo **NOMBRE DE USUARIO**: ícono de persona, placeholder `Ej. carlos_mendoza`.
   - Campo **CORREO ELECTRÓNICO** con la etiqueta "Seguro SSL" (escudo azul) a la derecha: ícono de sobre, placeholder `hola@ejemplo.com`.
   - Recuadro azul claro: "¿Cambiaste de correo?" y enlace azul "Contactar a soporte de Finanzia".
4. Botón principal con degradado: "Verificar y continuar →"
5. Debajo, tarjeta blanca con "← Volver a Iniciar sesión".

## Pantalla 2 · Paso 2 de 2 (`olvidar_contraseña_2.jpeg`)

1. Arriba: flecha atrás (izquierda) y botón circular azul con ícono de persona (derecha).
2. Fila: punto verde con "RESTABLECIMIENTO DE ACCESO" y pastilla "PASO 2 DE 2".
3. Recuadro azul claro con un chip: ícono de escudo, "Correo verificado: p***a@ejem…" (correo enmascarado) y etiqueta verde "Activo".
4. "Nueva contraseña" con, a la derecha, el texto verde de seguridad (en el diseño: "Seguridad media").
5. Campo de contraseña: ícono de candado, puntos, ícono de ojo.
6. Barra de fuerza de 4 segmentos (en el diseño, 3 verdes y 1 gris).
7. Recuadro "Requisitos de robustez:" con cuatro filas (verde con ✓ si se cumple, círculo gris si no):
   - Mínimo 8 caracteres
   - Al menos 1 número
   - Al menos 1 letra mayúscula
   - Al menos 1 símbolo especial (!@#$)
8. "Confirmar nueva contraseña": ícono de candado con flecha circular, placeholder "Repite tu nueva contraseña", ícono de ojo.
9. Tarjeta con casilla azul: "Cerrar sesión en todos los demás dispositivos" y debajo "Recomendado para garantizar que solo tú tengas acceso inmediatamente."
10. Botón principal con degradado: "Actualizar contraseña →"

## Pantalla 2 con error (`olvidar_contraseña_error.jpeg`)

Se muestra cuando la confirmación no coincide con la nueva contraseña:

1. Arriba, tarjeta roja de error: ícono de alerta, título "Las contraseñas no coinciden" y texto "La confirmación debe ser exactamente igual a la nueva contraseña ingresada. Por favor verifica ambos campos."
2. Debajo: fila "Seguridad de la cuenta" con pastilla "PASO 2 DE 2".
3. Tarjeta azul clara: etiqueta verde "PASO FINAL", título "Crea una nueva contraseña 🔐" y texto "Tu compañero financiero te ayuda a proteger tus fondos."
4. "Nueva contraseña" con el texto verde "Cumple requisitos" y su campo; debajo cuatro chips verdes con ✓: "8+ caracteres", "Al menos un número", "Una mayúscula", "Un símbolo especial".
5. "Confirmar nueva contraseña" en rojo con la etiqueta roja "No coincide"; campo con fondo rosado, borde rojo e ícono de alerta; debajo, texto rojo "Ambas contraseñas deben ser idénticas."
6. Botón azul claro "Mostrar ambas contraseñas" (con ícono de ojo).
7. Botón principal con degradado: "Actualizar contraseña →"

## Pantalla final

Tras actualizar la contraseña con éxito se muestra "Acceso autorizado" (`olvidar_contraseña_3.jpeg`, descrita en `01-inicio-sesion.md`) y desde ahí "Continuar al Dashboard".

## Comportamiento

**Paso 1**
- Ambos campos son obligatorios; el correo debe tener formato válido.
- "Verificar y continuar" valida los datos y pasa al Paso 2 (ver decisión de seguridad abajo).
- "Contactar a soporte de Finanzia": el diseño no define destino (ver "Decisiones pendientes").
- "Volver a Iniciar sesión" y la flecha atrás regresan a la pantalla de inicio de sesión.

**Paso 2**
- Los requisitos se marcan en vivo al escribir. Los segmentos verdes de la barra corresponden a los requisitos cumplidos (3 de 4 en `olvidar_contraseña_2.jpeg`; 4 de 4 en el estado de error, con el texto "Cumple requisitos").
- Si los dos campos no son idénticos al pulsar "Actualizar contraseña", se muestra la pantalla de error descrita arriba.
- La contraseña nueva debe cumplir los cuatro requisitos para poder actualizarse.
- Casilla "Cerrar sesión en todos los demás dispositivos": si está marcada, al actualizar se cierran las demás sesiones del usuario en Supabase.
- Los ojos alternan mostrar y ocultar cada campo; "Mostrar ambas contraseñas" muestra las dos a la vez.
- Éxito: ir a "Acceso autorizado".

## Criterios de aceptación

- [ ] Las cuatro pantallas son visualmente idénticas a sus imágenes.
- [ ] Los requisitos y la barra de fuerza reaccionan en vivo.
- [ ] Con confirmación distinta se muestra el estado de error del diseño.
- [ ] Con contraseña válida e idéntica se actualiza y se llega a "Acceso autorizado".
- [ ] Una contraseña que no cumple los cuatro requisitos no se puede guardar.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño.

## Decisión de seguridad (leer antes de implementar)

El diseño verifica la identidad solo con **nombre de usuario y correo**. Eso no es seguro: cualquiera que conozca esos dos datos podría cambiar la contraseña de otra persona. Hasta que el equipo decida, **no implementes un cambio de contraseña basado solo en esos dos datos.**

Opción recomendada: usar la recuperación de Supabase Auth, que envía un enlace o código al correo del usuario, y que el cambio de contraseña solo se permita después de abrir ese enlace. El diseño de las pantallas no cambia; solo cambia cómo se valida el Paso 1.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. Cómo se verifica la identidad en el Paso 1 (ver la sección anterior).
2. La tabla `Usuarios` no tiene campo "nombre de usuario": ¿se usa `Nombre` o se agrega un campo?
3. Destino de "Contactar a soporte de Finanzia" (correo, enlace u otra pantalla).
4. Textos de la barra de fuerza para 1 o 2 requisitos cumplidos: el diseño solo muestra "Seguridad media" y "Cumple requisitos".

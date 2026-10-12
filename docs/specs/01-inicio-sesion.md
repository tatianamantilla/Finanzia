# Spec 01 · Inicio de sesión

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- La pantalla debe quedar **exactamente igual** a la imagen de referencia: mismos colores, misma tipografía, mismos tamaños, espaciados, bordes redondeados, íconos, emojis, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones mantienen sus colores y degradados tal como se ven en la imagen (el botón principal usa el degradado verde → azul).
- No agregar, quitar ni renombrar elementos que no estén en el diseño. No "mejorar" el diseño.
- Los textos grises dentro de los campos (`tu@correo.com`, puntos de la contraseña) son *placeholders* y se conservan iguales.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Permitir que un usuario ya registrado inicie sesión con su correo y contraseña.

## Diseños de referencia

- `docs/design/inicio_sesion_1.jpeg`: pantalla de inicio de sesión.
- `docs/design/olvidar_contraseña_3.jpeg`: pantalla "Acceso autorizado" (bienvenida tras iniciar sesión).

## Pantalla 1 · Inicio de sesión (`inicio_sesion_1.jpeg`)

De arriba hacia abajo:

1. Logo **FINANZIA** con la estrella verde, y debajo el texto verde "Tu asistente financiero con IA".
2. Título: "¡Qué bueno verte! 👋"
3. Subtítulo (dos líneas): "Inicia sesión para continuar y seguir creciendo."
4. Campo **CORREO ELECTRÓNICO**: ícono de sobre, placeholder `tu@correo.com`.
5. Campo **CONTRASEÑA**: ícono de candado, placeholder de puntos, ícono de ojo a la derecha para mostrar u ocultar.
6. Enlace azul centrado: "¿Olvidaste tu contraseña?"
7. Botón principal con degradado: "Entrar →"

Nota: el diseño **no** tiene enlace a "Crear cuenta" en esta pantalla. No lo agregues.

## Pantalla 2 · Acceso autorizado (`olvidar_contraseña_3.jpeg`)

1. Pastilla verde clara arriba: "ACCESO AUTORIZADO • SESIÓN LISTA" con ícono de verificación.
2. Título: "¡Qué alegría tenerte de vuelta, [primer nombre]!" (en el diseño: "Paula").
3. Subtítulo: "Tus finanzas y metas con IA están al día y sincronizadas en tiempo real."
4. Tarjeta blanca:
   - "SALDO TOTAL DISPONIBLE" con ícono de billetera, monto grande (en el diseño: `$14,250.00`), ícono de ojo y pastilla verde de variación (`+4.8%`).
   - Recuadro azul claro "Insight Inteligente de Hoy" con el texto (en el diseño: "Mantuviste tus gastos un 12% por debajo del presupuesto semanal proyectado.").
   - Pie de la tarjeta: "Cifrado AES-256 Activo" y "Conexión Segura" (punto verde).
5. Botón principal con degradado: "Continuar al Dashboard →"
6. Pie: "PROTECCIÓN DE GRADO BANCARIO INTERNACIONAL" y debajo "Finanzia Mobile App v3.4.1 (Build 2049)".

## Comportamiento

- El ojo de la contraseña alterna entre mostrar y ocultar el texto.
- **Entrar:** inicia sesión con Supabase Auth (correo y contraseña).
  - Éxito: ir a la Pantalla 2. "Continuar al Dashboard" lleva al Dashboard.
  - Error (credenciales incorrectas o campos vacíos): el diseño no define el mensaje. Usa un mensaje temporal estándar (Snackbar) con texto simple, sin cambiar el diseño de la pantalla.
- **¿Olvidaste tu contraseña?:** abre el flujo de la spec `02-olvidar-contrasena.md`.
- El nombre del usuario sale de la tabla `Usuarios` (campo `Nombre`); usa solo el primer nombre.
- El saldo y el insight del diseño son **datos de muestra**: ver "Decisiones pendientes".

## Criterios de aceptación

- [ ] La pantalla 1 es visualmente idéntica a `inicio_sesion_1.jpeg` (colores, tipografía, textos, íconos).
- [ ] La pantalla 2 es visualmente idéntica a `olvidar_contraseña_3.jpeg`.
- [ ] Un usuario existente en Supabase puede entrar con su correo y contraseña.
- [ ] Con credenciales incorrectas no entra y ve un mensaje de error.
- [ ] El ojo muestra y oculta la contraseña.
- [ ] "¿Olvidaste tu contraseña?" abre el flujo de recuperación.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. Pantalla inicial de la app: ¿abre en Inicio de sesión o en Crear cuenta? El diseño no muestra enlace de Inicio de sesión → Crear cuenta.
2. ¿La pantalla "Acceso autorizado" se muestra siempre tras iniciar sesión, o solo tras restablecer contraseña?
3. Saldo, porcentaje e insight de la pantalla 2: ¿se calculan con datos reales (`Cuentas` y `Movimientos`) o quedan como datos de muestra por ahora?

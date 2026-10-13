# Spec 03 · Crear cuenta y onboarding

Lee antes `README.md` y `CLAUDE.md`.

## Reglas de diseño (obligatorias)

- Las pantallas deben quedar **exactamente iguales** a las imágenes de referencia: mismos colores, misma tipografía, mismos tamaños, espaciados, bordes redondeados, íconos, emojis, textos y orden de los elementos.
- **No modificar** colores, tipografía ni estilos. Los botones mantienen sus colores y degradados tal como se ven en las imágenes: degradado verde → azul en los botones principales y azul sólido en "Guardar gasto y finalizar".
- Las opciones seleccionadas conservan su estilo: borde y fondo verdes en ingresos, borde y fondo rojizos en gastos.
- No agregar, quitar ni renombrar elementos que no estén en el diseño. No "mejorar" el diseño.
- Los textos grises dentro de los campos son *placeholders* y se conservan iguales. Los datos de ejemplo (Paula Gómez, `paula.gomez@e…`, `18,500.00`, `3,200.00`) son de muestra y no deben quedar fijos en el código.
- Si algo no se puede reproducir igual, o hay duda, **pregunta antes de decidir**.

## Objetivo

Permitir que una persona nueva cree su cuenta y pase por un onboarding corto donde puede registrar su primer ingreso o su primer gasto.

## Diseños de referencia

- `docs/design/crear_cuenta_1.jpeg`: formulario de registro.
- `docs/design/crear_cuenta_2.jpeg`: cuenta creada con éxito.
- `docs/design/crear_cuenta_3.jpeg`: onboarding, Paso 1 de 2 (primeros pasos).
- `docs/design/crear_cuenta_4.jpeg`: onboarding, Paso 2 • Ingresos.
- `docs/design/crear_cuenta_5.jpeg`: onboarding, Paso 2 • Gastos.
- Destino final, **fuera de esta entrega**: `docs/design/usuario_creado_onboarding.jpeg` y `docs/design/usuario_nuevo__onboarding.jpeg` (Dashboard).

## Pantalla 1 · Crear cuenta (`crear_cuenta_1.jpeg`)

1. Logo **FINANZIA** con la estrella verde y debajo "TU ASISTENTE FINANCIERO CON IA" en verde.
2. Título: "¡Empieza tu viaje! 🚀"
3. Subtítulo: "Toma el control inteligente de tu dinero en minutos."
4. Campo **NOMBRE COMPLETO**: ícono de persona, placeholder `Paula Gómez`.
5. Campo **CORREO ELECTRÓNICO**: ícono de sobre, placeholder `hola@ejemplo.com`.
6. Campo **CONTRASEÑA**: ícono de candado, placeholder "Mínimo 8 caracteres", ícono de ojo.
7. Tres indicadores en fila, cada uno con un punto (verde si se cumple, gris si no): "8+ carac.", "1 número", "1 mayúscula".
8. Casilla: "Acepto los **Términos de Servicio** y la **Política de Privacidad**" (los dos enlaces en azul).
9. Botón principal con degradado: "Crear cuenta →"
10. Pie: "¿Ya tienes una cuenta? **Iniciar sesión**" (enlace azul en negrita).

## Pantalla 2 · Cuenta creada (`crear_cuenta_2.jpeg`)

1. Pastilla azul clara arriba: "🚀 ¡CUENTA CREADA CON ÉXITO!" y un chevron "<" a la derecha.
2. Título grande: "¡Te damos la bienvenida a bordo!"
3. Texto: "Tu asistente financiero impulsado por IA está listo para ayudarte a ahorrar, optimizar tus gastos y alcanzar cada meta."
4. Tarjeta blanca "Todo listo para comenzar" con ícono de escudo azul y pastilla verde "3 de 3". Contiene tres filas con ✓ verde:
   - "Bóveda y cifrado bancario configurados" / "Protocolo AES-256 de nivel bancario activado."
   - "Asistente IA Copilot activado" / "Detección automática de gastos hormiga y suscripciones."
   - "Bono de bienvenida desbloqueado" con etiqueta morada "GRATIS" / "Diagnóstico de hábitos financieros y proyección a 12 meses."
5. Recuadro azul claro: ícono de sobre, "Correo de confirmación enviado", "Revisa tu bandeja en" + correo del usuario (en azul, truncado con "…"), y un ícono de recarga a la derecha.
6. Botón principal con degradado: "Comenzar ahora →"

## Pantalla 3 · Primeros pasos (`crear_cuenta_3.jpeg`)

1. Pastilla verde clara: punto verde y "Paso 1 de 2 • Primeros pasos".
2. "FINANZIA" con la etiqueta azul clara "IA".
3. Título: "¡Hola! Soy Finanzia 💸" y debajo, en verde, "Tu asistente financiero inteligente".
4. Texto: "Estoy aquí para acompañarte paso a paso. Quiero conocer cómo se mueven tus finanzas para darte los mejores consejos y hacer crecer tu dinero."
5. Tarjeta blanca con ícono de rayo: "¿Por dónde te gustaría empezar?" y "Comienza registrando tu fuente de dinero o lo que gastas habitualmente para calibrar tu balance."
6. Botón con degradado e ícono "+": "Quiero registrar mi primer ingreso" con chevron ">".
7. Botón blanco con ícono rojo "−": "Quiero registrar mi primer gasto" con chevron ">".
8. Enlace gris: "Explorar más tarde e ir al Dashboard".
9. Pie con escudo verde: "Tus datos financieros viajan cifrados de punto a punto".

## Pantalla 4 · Primer ingreso (`crear_cuenta_4.jpeg`)

1. Arriba: botón blanco con flecha atrás y pastilla verde "Paso 2 • Ingresos".
2. Ícono verde con flecha hacia abajo y título "Registra tu primer ingreso".
3. Texto: "Indica la frecuencia con la que recibes dinero y el monto aproximado para proyectar tus metas."
4. Tarjeta blanca "MONTO DEL INGRESO (COP)": signo `$` verde y campo numérico con borde verde; formato con coma de miles y dos decimales (en el diseño `18,500.00`). Debajo, con ícono verde: "Podrás modificar o agregar más fuentes de ingreso después."
5. Encabezado "TIPO / FRECUENCIA DE INGRESO" con "Selecciona uno" en verde. Cuatro tarjetas con selección única:
   - **Mensual**: "Sueldo o nómina cada 30 días" (seleccionada por defecto, con borde y fondo verdes y ✓)
   - **Quincenal**: "Pago los días 15 y 30"
   - **Semanal**: "Cobro cada 7 días o fin de semana"
   - **Diario**: "Ventas o ingresos constantes por día"
6. Recuadro azul claro: "**Consejo de Finanzia:** Configurar tu frecuencia nos permite predecir cuándo tendrás dinero disponible para tus ahorros."
7. Botón principal con degradado: "Guardar y continuar →"
8. Enlace gris: "Omitir este paso por ahora".

## Pantalla 5 · Primer gasto (`crear_cuenta_5.jpeg`)

1. Arriba: botón blanco con flecha atrás y pastilla rosada "Paso 2 • Gastos".
2. Ícono rojo con flecha hacia arriba y título "Registra tu primer gasto".
3. Texto: "Selecciona la categoría principal y anota cuánto sueles destinar para comenzar a optimizar."
4. Tarjeta blanca "MONTO ESTIMADO DEL GASTO (COP)": signo `$` rojo y campo con subrayado rojo; mismo formato de miles y decimales (en el diseño `3,200.00`). Debajo, con ícono rojo: "Finanzia detectará oportunidades de ahorro en esta categoría."
5. Encabezado "CATEGORÍA DE GASTO" y a la derecha "Administrar categorías" con ícono de engranaje. Cuatro tarjetas con selección única:
   - 🏠 **Gastos Fijos**: "Renta, servicios, internet, luz, agua" (seleccionada por defecto, con borde y fondo rojizos y ✓ rojo)
   - 🍔 **Comida**: "Supermercado, despensa y restaurantes"
   - 🎉 **Diversión**: "Streaming, salidas, ocio y cine"
   - 🚗 **Transporte**: "Gasolina, transporte público, Uber/DiDi"
6. Recuadro rosado: "**Dato de Finanzia:** Identificar tus gastos fijos es la clave para saber exactamente cuánto dinero libre tienes para ahorrar e invertir."
7. Botón azul sólido: "Guardar gasto y finalizar →"
8. Enlace gris: "Omitir este paso por ahora".

## Comportamiento y navegación

**Crear cuenta**
- Nombre, correo y contraseña son obligatorios; el correo debe tener formato válido.
- La contraseña debe tener mínimo 8 caracteres, un número y una mayúscula; los tres indicadores se actualizan en vivo.
- La casilla de Términos y Política debe estar marcada para poder crear la cuenta.
- "Crear cuenta" registra al usuario con Supabase Auth y guarda su nombre y correo en la tabla `Usuarios`.
- Éxito: ir a la Pantalla 2. Error (por ejemplo, correo ya registrado): el diseño no define el mensaje; usa un mensaje temporal estándar (Snackbar) sin cambiar el diseño.
- "Iniciar sesión" abre el módulo de `01-inicio-sesion.md`.

**Cuenta creada y onboarding**
- Pantalla 2: "Comenzar ahora" lleva a la Pantalla 3. En "Revisa tu bandeja en" se muestra el correo real del usuario.
- Pantalla 3: "Quiero registrar mi primer ingreso" lleva a la Pantalla 4; "Quiero registrar mi primer gasto" lleva a la Pantalla 5; "Explorar más tarde e ir al Dashboard" lleva al Dashboard.
- Pantallas 4 y 5: la flecha atrás regresa a la Pantalla 3. "Omitir este paso por ahora" lleva al Dashboard sin guardar. "Guardar y continuar" y "Guardar gasto y finalizar" guardan el registro (ver "Decisiones pendientes") y llevan al Dashboard.
- Los montos solo aceptan números; el campo no puede guardarse vacío ni en cero.
- El Dashboard no es parte de esta entrega: crea una pantalla provisional con el texto "Dashboard" para que la navegación termine ahí.

## Criterios de aceptación

- [ ] Las cinco pantallas son visualmente idénticas a sus imágenes.
- [x] Se puede crear una cuenta y el usuario aparece en Supabase (Auth y tabla `Usuarios`).
- [x] Los tres indicadores de contraseña reaccionan en vivo y la casilla es obligatoria.
- [x] La navegación entre las pantallas 1 a 5 y hacia el Dashboard funciona como se describe.
- [x] En las pantallas 4 y 5 solo se puede seleccionar una opción a la vez.
- [x] "Omitir este paso por ahora" no guarda nada.
- [ ] No se agregó ningún elemento ni color que no esté en el diseño.

> Verificación (fase 2, Supabase real): la creación de cuenta se comprobó con la prueba manual
> `integracion/manual/DemoMateoMarinTest` (registro en Auth, fila en `usuarios`, cuenta "Principal",
> 5 categorías con color, primer ingreso y primer gasto) y en el emulador.
>
> Verificación (fase 1, datos simulados): los criterios marcados se comprobaron con los tests de
> `app/src/test` (`NavegacionCrearCuentaTest`, `ValidacionRegistroTest`, `MontosTest`). Los que dependen de
> Supabase o de una revisión visual en el emulador siguen sin marcar.

## Decisiones tomadas con el equipo (fase 1)

- **Montos sin decimales**: la app pone sola las comas de miles (`18,500`), sin `.00`. Esto reemplaza el formato con dos decimales de la Pantalla 4 y la Pantalla 5.
- Con el campo de monto vacío se muestra un `0` gris como placeholder.
- Errores de validación (fase 2): sin Snackbar. Cada error va en rojo dentro de su campo (borde rojo, fondo rosado,
  etiqueta roja, ícono de alerta y texto debajo) con el estilo de `olvidar_contraseña_error.jpeg`, y se borra al corregir.
  Los errores que no son de un campo (sin conexión, confirmar correo) van en rojo sobre el botón.
- "Iniciar sesión" lleva a una pantalla provisional hasta que se construya el módulo 01.
- Los enlaces de Términos y Política se ven, pero no tienen acción (punto 6 de las decisiones pendientes).
- Tipografía: Manrope (Google Fonts), incluida en la app.

## Decisiones pendientes (preguntar al equipo, no inventar)

1. `Usuarios` tiene un campo `Contraseña`. Con Supabase Auth la contraseña **no** debe guardarse ahí. Confirmar si se elimina o se deja sin usar, y que el id de `Usuarios` sea el mismo que el del usuario en Auth.
2. Dónde se guarda la frecuencia del ingreso (Mensual, Quincenal, Semanal, Diario): ninguna tabla tiene ese campo.
3. Cómo se guardan el primer ingreso y el primer gasto: `Movimientos` exige una cuenta y una categoría. ¿Se crea una cuenta por defecto? ¿Las categorías Gastos Fijos, Comida, Diversión y Transporte ya existen en `Categorias`?
4. ¿Supabase tiene activada la confirmación por correo? Si lo está, el usuario no podrá iniciar sesión hasta confirmarlo.
5. Funciones que el diseño muestra pero no define: "Administrar categorías", el ícono de recarga de la Pantalla 2 y el chevron "<" de su pastilla. Hasta definirlas, déjalos visibles pero sin acción.
6. Pantalla 1 → Términos de Servicio y Política de Privacidad: ¿a dónde llevan los enlaces?

No cambies las tablas de Supabase sin confirmación del equipo.

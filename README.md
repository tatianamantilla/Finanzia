# Finanzia

Aplicación móvil para Android que facilita el registro y análisis de la información financiera personal, con apoyo de inteligencia artificial que explica los datos y da recomendaciones comprensibles.

## Público objetivo

- Personas en general, sin límite de edad, que reciban un ingreso (sueldo).
- Esto incluye a los vendedores. No tienen un módulo aparte: usan los mismos módulos que cualquier persona, personalizados de forma estándar, y pueden indicar si reciben sus ingresos de forma **diaria, semanal o mensual**.

## Objetivo general

Diseñar y desarrollar una aplicación móvil para Android que facilite el registro y análisis de la información financiera de los usuarios, mediante herramientas que simplifiquen el registro de ingresos y gastos e incorporen inteligencia artificial para interpretar los datos financieros y generar recomendaciones comprensibles que apoyen la toma de decisiones.

## Objetivos específicos

- Diseñar una interfaz intuitiva que permita registrar ingresos y gastos de forma rápida, y reducir la dependencia del ingreso manual de información.
- Implementar un sistema basado en inteligencia artificial capaz de interpretar las gráficas e indicadores financieros, ofreciendo explicaciones claras y recomendaciones contextualizadas.
- Diseñar un sistema de visualización de reportes financieros que presente la información de manera clara y sencilla, sin requerir conocimientos técnicos o financieros avanzados.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin (100 %) |
| Interfaz | Jetpack Compose (100 %) |
| IDE | Android Studio |
| Base de datos y autenticación | Supabase (en la nube, no local) |
| Tipografía | Manrope (Google Fonts, incluida en `app/src/main/res/font`) |
| Paquete base | `com.finanzia.finanzia` |

> Requisito académico: el proyecto debe desarrollarse completamente en Kotlin, Compose y Android Studio.

## Módulos de la aplicación

1. **Registro / crear cuenta**: el usuario se registra en la app.
2. **Onboarding**: al crear la cuenta recibe una bienvenida y se le pregunta si quiere registrar su primer gasto o su primer ingreso.
3. **Inicio de sesión**: ingreso de usuarios existentes, con mensaje de bienvenida.
4. **Resumen (inicio)**: resumen de los movimientos y saldo actual.
5. **Notificaciones**.
6. **Asistente de IA**: conversaciones que explican gráficas e indicadores y dan recomendaciones.

### Alcance de la entrega actual

> Pendiente por definir: cuáles son los dos módulos que se entregan primero y su especificación en `docs/specs/`.

## Modelo de datos (Supabase)

| Tabla | Campos |
|---|---|
| Usuarios | id usuario (PK), nombre, correo, contraseña |
| Categorias | id categoria (PK), id usuario (FK), id color (FK), nombre, tipo, activo |
| Colores | id color (PK), color |
| Cuentas | id cuenta (PK), id usuario (FK), id color (FK), nombre, tipo, saldo inicial, fecha creacion, activo |
| Movimientos | id movimiento (PK), id usuario (FK), id cuenta (FK), id categoria (FK), tipo, monto, descripcion, fecha, activo |
| Metas | id meta (PK), id usuario (FK), nombre, monto objetivo, fecha limite, fecha creacion, activo |
| Aportes | id aporte (PK), id meta (FK), id cuenta (FK), monto, fecha |
| Conversaciones | id conversacion (PK), id usuario (FK), fecha creacion, titulo, activo |
| Mensajes | id mensaje (PK), id conversacion (FK), rol, contenido, fecha, activo |

## Configuración local

1. Clonar el repositorio y abrirlo en Android Studio.
2. Crear (o editar) el archivo `local.properties` en la raíz, **sin subirlo a git**:

```
SUPABASE_URL=https://TU-PROYECTO.supabase.co
SUPABASE_ANON_KEY=tu-clave-publica
```

3. Sincronizar Gradle y ejecutar la app en el emulador.

> La compilación se detiene con un mensaje en español si falta alguna de las dos claves, si la URL no tiene el formato `https://TU-PROYECTO.supabase.co` o si la clave no es la *publishable* (`sb_publishable_…`). Nunca uses la clave secreta (`sb_secret_…` / `service_role`) en la app.

> Fase 1 (actual): la app funciona con datos simulados en memoria, sin Supabase. Por ahora no hace falta llenar las claves.

## Tests

Los tests corren en la computadora, sin emulador (los de pantallas usan Robolectric):

```
./gradlew :app:testDebugUnitTest
```

En Android Studio: clic derecho sobre `app/src/test` → *Run 'Tests in…'*.

## Pantallas adaptables

- Todas las pantallas usan `ContenidoDesplazable` (`ui/componentes/Componentes.kt`): se desplazan cuando el contenido no cabe, respetan las barras del sistema y el teclado, y en tabletas centran el contenido con un ancho máximo de 480 dp.
- Con letra del sistema muy grande, algunas filas pasan a una distribución compacta (`esCompacto`) para no partir palabras. Con letra normal, cada pantalla se ve como su imagen de `docs/design`.
- Cada pantalla tiene Previews en cuatro variantes (`@PreviewsFinanzia`): teléfono pequeño, normal, tableta y letra grande.

## Metodología de trabajo

El proyecto sigue **Spec Driven Development**: primero se escribe qué debe hacer cada módulo y luego se implementa a partir de esa especificación.

```
README.md              contexto general (este archivo)
CLAUDE.md              reglas para el asistente de código
docs/specs/            una especificación por módulo
docs/design/           pantallazos de los diseños (Stitch)
```

## Equipo

- Valentina
- Juan
- Tatiana

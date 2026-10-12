# Finanzia: instrucciones para Claude Code

Lee primero `README.md` para el contexto del proyecto y la carpeta `docs/specs/` para lo que se debe construir.

## Sobre la persona que te usa

- No programa: explica lo que haces en lenguaje sencillo y en español.
- Al terminar cada tarea, indica qué archivos cambiaste y cómo probarlo en el emulador de Android Studio.
- Si algo es ambiguo, pregunta antes de decidir.

## Reglas técnicas

- Todo el código en **Kotlin** con **Jetpack Compose**. No uses XML para pantallas, Flutter, React Native ni otros frameworks.
- Paquete base: `com.finanzia.finanzia`.
- La base de datos es **Supabase en la nube**. No uses bases locales (Room, SQLite) como fuente de datos.
- Implementa solo lo que pida la especificación del módulo en `docs/specs/`. No agregues funciones extra.
- Las pantallas deben ser fieles a los diseños de `docs/design/`. No cambies colores, textos ni distribución sin que se pida.
- No modifiques la estructura de las tablas de Supabase sin avisar y pedir confirmación.

## Seguridad

- Las claves de Supabase van en `local.properties` y se leen desde `BuildConfig`. **Nunca** las escribas en el código ni las subas a git.
- Verifica que `local.properties` esté en `.gitignore`.
- No guardes contraseñas en texto plano. La autenticación debe hacerse con Supabase Auth.

## Flujo de trabajo

1. Lee la especificación del módulo.
2. Implementa por partes pequeñas.
3. Compila el proyecto y corrige errores antes de decir que terminaste.
4. Haz commits pequeños con mensajes claros en español, en la rama del módulo.
5. Mantén actualizado el README si cambia algo importante.

package com.finanzia.finanzia.datos

import java.security.MessageDigest
import java.util.UUID

/** Registro e inicio de sesión de usuarios. Las pantallas solo conocen esta interfaz. */
interface AuthRepository {
    /** Usuario que acaba de crear su cuenta o de iniciar sesión (o `null` si nadie lo ha hecho). */
    val usuarioActual: Usuario?

    suspend fun crearCuenta(nombre: String, correo: String, contrasena: String): Result<Usuario>

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario>

    /** Olvidar contraseña, Paso 1: comprueba que el nombre de usuario y el correo sean de una cuenta. */
    suspend fun verificarIdentidad(nombreUsuario: String, correo: String): Result<Unit>

    /** Olvidar contraseña, Paso 2: guarda la contraseña nueva y deja la sesión iniciada. */
    suspend fun restablecerContrasena(correo: String, nuevaContrasena: String, cerrarOtrasSesiones: Boolean): Result<Usuario>

    /** Al abrir la app: recupera la sesión guardada en el teléfono, o `null` si no hay ninguna. */
    suspend fun restaurarSesion(): Usuario? = usuarioActual

    /** Cierra la sesión (menú del Dashboard). */
    suspend fun cerrarSesion(): Result<Unit>
}

class CorreoYaRegistradoException : Exception("Este correo ya está registrado")

class CorreoNoRegistradoException : Exception("Este correo no está registrado")

class ContrasenaIncorrectaException : Exception("La contraseña es incorrecta")

class NombreUsuarioNoCoincideException : Exception("El nombre de usuario no coincide con este correo")

/** Supabase no dice si falló el correo o la contraseña (por seguridad): un solo mensaje para ambos. */
class CredencialesIncorrectasException : Exception("El correo o la contraseña son incorrectos")

class SinConexionException : Exception("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo.")

/** Supabase pide confirmar el correo antes de entregar la sesión. */
class ConfirmacionCorreoPendienteException :
    Exception("Revisa tu correo y confirma tu cuenta antes de iniciar sesión.")

/** La sesión ya no existe (se cerró o venció). */
class SesionTerminadaException : Exception("Tu sesión terminó. Inicia sesión de nuevo.")

/**
 * Versión simulada: guarda los usuarios en memoria y se pierden al cerrar la app.
 * La contraseña nunca se guarda en texto plano: solo una huella (hash) para poder comparar al entrar.
 */
// TODO(Supabase): reemplazar por una implementación con Supabase Auth que además guarde
//  nombre y correo en la tabla Usuarios (ver "Decisiones pendientes" 1 y 4 de la spec 03).
class FakeAuthRepository : AuthRepository {

    private class Registro(val usuario: Usuario, val huellaContrasena: String)

    private val usuarios = mutableMapOf<String, Registro>()

    override var usuarioActual: Usuario? = null
        private set

    override suspend fun crearCuenta(nombre: String, correo: String, contrasena: String): Result<Usuario> {
        val clave = correo.trim().lowercase()
        if (clave in usuarios) return Result.failure(CorreoYaRegistradoException())
        val usuario = agregarUsuario(nombre, correo, contrasena)
        usuarioActual = usuario
        return Result.success(usuario)
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        val registro = usuarios[correo.trim().lowercase()]
            ?: return Result.failure(CorreoNoRegistradoException())
        if (registro.huellaContrasena != huella(contrasena)) {
            return Result.failure(ContrasenaIncorrectaException())
        }
        usuarioActual = registro.usuario
        return Result.success(registro.usuario)
    }

    /**
     * El nombre de usuario se compara con el nombre completo, sin importar mayúsculas
     * ("Decisiones pendientes" 2 de la spec 02: la tabla Usuarios no tiene nombre de usuario).
     */
    // TODO(Supabase): la spec 02 pide NO cambiar la contraseña solo con nombre y correo. En la fase 2,
    //  usar la recuperación de Supabase Auth (enlace o código al correo) antes de permitir el Paso 2.
    override suspend fun verificarIdentidad(nombreUsuario: String, correo: String): Result<Unit> {
        val registro = usuarios[correo.trim().lowercase()]
            ?: return Result.failure(CorreoNoRegistradoException())
        if (!registro.usuario.nombre.equals(nombreUsuario.trim(), ignoreCase = true)) {
            return Result.failure(NombreUsuarioNoCoincideException())
        }
        return Result.success(Unit)
    }

    /** Solo para los tests: si en el último restablecimiento se pidió cerrar las demás sesiones. */
    var cerroOtrasSesiones: Boolean? = null
        private set

    // TODO(Supabase): actualizar la contraseña con Supabase Auth y, si se pide, cerrar las demás sesiones.
    override suspend fun restablecerContrasena(
        correo: String,
        nuevaContrasena: String,
        cerrarOtrasSesiones: Boolean,
    ): Result<Usuario> {
        val clave = correo.trim().lowercase()
        val registro = usuarios[clave] ?: return Result.failure(CorreoNoRegistradoException())
        usuarios[clave] = Registro(registro.usuario, huella(nuevaContrasena))
        cerroOtrasSesiones = cerrarOtrasSesiones
        usuarioActual = registro.usuario
        return Result.success(registro.usuario)
    }

    override suspend fun cerrarSesion(): Result<Unit> {
        usuarioActual = null
        return Result.success(Unit)
    }

    /** Agrega un usuario sin iniciar sesión (para el usuario de prueba y los tests). */
    fun agregarUsuario(nombre: String, correo: String, contrasena: String): Usuario {
        val usuario = Usuario(id = UUID.randomUUID().toString(), nombre = nombre.trim(), correo = correo.trim())
        usuarios[correo.trim().lowercase()] = Registro(usuario, huella(contrasena))
        return usuario
    }

    private fun huella(contrasena: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(contrasena.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

package com.finanzia.finanzia.datos

import java.security.MessageDigest
import java.util.UUID

/** Registro e inicio de sesión de usuarios. Las pantallas solo conocen esta interfaz. */
interface AuthRepository {
    /** Usuario que acaba de crear su cuenta o de iniciar sesión (o `null` si nadie lo ha hecho). */
    val usuarioActual: Usuario?

    suspend fun crearCuenta(nombre: String, correo: String, contrasena: String): Result<Usuario>

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario>
}

class CorreoYaRegistradoException : Exception("Este correo ya está registrado")

class CorreoNoRegistradoException : Exception("Este correo no está registrado")

class ContrasenaIncorrectaException : Exception("La contraseña es incorrecta")

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

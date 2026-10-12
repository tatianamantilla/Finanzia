package com.finanzia.finanzia.datos

import java.util.UUID

/** Registro de usuarios. Las pantallas solo conocen esta interfaz. */
interface AuthRepository {
    /** Usuario que acaba de crear su cuenta (o `null` si nadie lo ha hecho). */
    val usuarioActual: Usuario?

    suspend fun crearCuenta(nombre: String, correo: String, contrasena: String): Result<Usuario>
}

class CorreoYaRegistradoException : Exception("Este correo ya está registrado")

/**
 * Versión simulada: guarda los usuarios en memoria y se pierden al cerrar la app.
 * La contraseña no se guarda en ningún lado.
 */
// TODO(Supabase): reemplazar por una implementación con Supabase Auth que además guarde
//  nombre y correo en la tabla Usuarios (ver "Decisiones pendientes" 1 y 4 de la spec 03).
class FakeAuthRepository : AuthRepository {

    private val usuarios = mutableMapOf<String, Usuario>()

    override var usuarioActual: Usuario? = null
        private set

    override suspend fun crearCuenta(nombre: String, correo: String, contrasena: String): Result<Usuario> {
        val clave = correo.trim().lowercase()
        if (clave in usuarios) return Result.failure(CorreoYaRegistradoException())
        val usuario = Usuario(id = UUID.randomUUID().toString(), nombre = nombre.trim(), correo = correo.trim())
        usuarios[clave] = usuario
        usuarioActual = usuario
        return Result.success(usuario)
    }
}

package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.CorreoYaRegistradoException
import com.finanzia.finanzia.datos.Usuario
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Supabase no dice si falló el correo o la contraseña (por seguridad): un solo mensaje para ambos. */
class CredencialesIncorrectasException : Exception("El correo o la contraseña son incorrectos")

class SinConexionException : Exception("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo.")

class RecuperacionNoDisponibleException :
    Exception("La recuperación de contraseña todavía no está disponible.")

/**
 * Registro e inicio de sesión con Supabase Auth.
 *
 * - La contraseña la guarda Supabase Auth (cifrada); nunca pasa por nuestras tablas.
 * - Al registrarse, el trigger de docs/supabase/01-usuarios-y-auth.sql crea solo la fila en
 *   `usuarios`, la cuenta "Principal" y las categorías. Por eso el nombre se envía en los
 *   datos del registro ("nombre").
 */
class SupabaseAuthRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : AuthRepository {

    override var usuarioActual: Usuario? = null
        private set

    override suspend fun crearCuenta(nombre: String, correo: String, contrasena: String): Result<Usuario> =
        intentar {
            try {
                cliente.auth.signUpWith(Email) {
                    email = correo.trim()
                    password = contrasena
                    data = buildJsonObject { put("nombre", nombre.trim()) }
                }
            } catch (e: Exception) {
                if (e.esError("already", "exists", "registered")) throw CorreoYaRegistradoException()
                throw e
            }
            // Con "Confirm email" desactivado, Supabase deja la sesión iniciada al registrarse.
            // Si no fuera así, se inicia sesión aquí mismo.
            val info = cliente.auth.currentUserOrNull() ?: run {
                cliente.auth.signInWith(Email) {
                    email = correo.trim()
                    password = contrasena
                }
                cliente.auth.currentUserOrNull()
            } ?: error("No se pudo crear la cuenta. Inténtalo de nuevo.")
            cargarUsuario(info, nombrePorDefecto = nombre.trim())
        }

    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> =
        intentar {
            try {
                cliente.auth.signInWith(Email) {
                    email = correo.trim()
                    password = contrasena
                }
            } catch (e: Exception) {
                if (e.esError("invalid", "credentials")) throw CredencialesIncorrectasException()
                throw e
            }
            val info = cliente.auth.currentUserOrNull() ?: throw CredencialesIncorrectasException()
            cargarUsuario(info, nombrePorDefecto = null)
        }

    // TODO(Supabase): decisión pendiente del equipo (spec 02). Comprobar solo nombre y correo no es
    //  seguro con usuarios reales: hay que usar la recuperación de Supabase Auth (código al correo).
    override suspend fun verificarIdentidad(nombreUsuario: String, correo: String): Result<Unit> =
        Result.failure(RecuperacionNoDisponibleException())

    override suspend fun restablecerContrasena(
        correo: String,
        nuevaContrasena: String,
        cerrarOtrasSesiones: Boolean,
    ): Result<Usuario> = Result.failure(RecuperacionNoDisponibleException())

    /** Lee nombre y correo de la tabla `usuarios` (la RLS solo deja ver la fila propia). */
    private suspend fun cargarUsuario(info: UserInfo, nombrePorDefecto: String?): Usuario {
        val fila = runCatching {
            cliente.from("usuarios").select {
                filter { eq("id_auth", info.id) }
            }.decodeSingleOrNull<FilaUsuario>()
        }.getOrNull()

        val correo = fila?.correo ?: info.email.orEmpty()
        val nombre = fila?.nombre
            ?: nombrePorDefecto
            ?: info.userMetadata?.get("nombre")?.jsonPrimitive?.content
            ?: correo.substringBefore("@")

        return Usuario(id = info.id, nombre = nombre, correo = correo).also { usuarioActual = it }
    }

    /** Convierte errores de red en un mensaje claro y deja pasar los nuestros. */
    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CorreoYaRegistradoException) {
            Result.failure(e)
        } catch (e: CredencialesIncorrectasException) {
            Result.failure(e)
        } catch (e: io.github.jan.supabase.exceptions.HttpRequestException) {
            Result.failure(SinConexionException())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun Exception.esError(vararg palabras: String): Boolean {
        val texto = (message ?: "").lowercase()
        return palabras.any { it in texto }
    }

    @Serializable
    private data class FilaUsuario(
        @SerialName("id_usuario") val idUsuario: Long,
        val nombre: String,
        val correo: String,
    )
}

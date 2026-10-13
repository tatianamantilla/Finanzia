package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.CorreoNoRegistradoException
import com.finanzia.finanzia.datos.CorreoYaRegistradoException
import com.finanzia.finanzia.datos.NombreUsuarioNoCoincideException
import com.finanzia.finanzia.datos.Usuario
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Supabase no dice si falló el correo o la contraseña (por seguridad): un solo mensaje para ambos. */
class CredencialesIncorrectasException : Exception("El correo o la contraseña son incorrectos")

class SinConexionException : Exception("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo.")

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

    /**
     * Olvidé mi contraseña · Paso 1: comprueba nombre y correo con la función
     * `verificar_identidad` (docs/supabase/04-recuperar-contrasena.sql).
     *
     * ADVERTENCIA (decisión del equipo, spec 02): solo nombre + correo no es seguro con usuarios
     * reales. Antes de publicar la app, cambiarlo por un código enviado al correo.
     */
    override suspend fun verificarIdentidad(nombreUsuario: String, correo: String): Result<Unit> =
        intentar {
            val resultado = llamarFuncion("verificar_identidad") {
                put("p_nombre", nombreUsuario.trim())
                put("p_correo", correo.trim())
            }
            lanzarSiNoEsOk(resultado)
            // El Paso 2 solo recibe el correo: se recuerda el nombre ya verificado.
            nombresVerificados[correo.trim().lowercase()] = nombreUsuario.trim()
            Unit
        }

    /** Olvidé mi contraseña · Paso 2: guarda la contraseña nueva y deja la sesión iniciada. */
    override suspend fun restablecerContrasena(
        correo: String,
        nuevaContrasena: String,
        cerrarOtrasSesiones: Boolean,
    ): Result<Usuario> = intentar {
        val clave = correo.trim().lowercase()
        val nombre = nombresVerificados[clave]
            ?: error("Vuelve al paso anterior y verifica tu identidad otra vez.")
        val resultado = llamarFuncion("restablecer_contrasena") {
            put("p_nombre", nombre)
            put("p_correo", correo.trim())
            put("p_nueva", nuevaContrasena)
            put("p_cerrar_sesiones", cerrarOtrasSesiones)
        }
        lanzarSiNoEsOk(resultado)
        nombresVerificados.remove(clave)

        // Con la contraseña nueva se inicia sesión para llegar a "Acceso autorizado".
        cliente.auth.signInWith(Email) {
            email = correo.trim()
            password = nuevaContrasena
        }
        val info = cliente.auth.currentUserOrNull() ?: error("No se pudo iniciar sesión. Inténtalo de nuevo.")
        cargarUsuario(info, nombrePorDefecto = nombre)
    }

    private val nombresVerificados = mutableMapOf<String, String>()

    private suspend fun llamarFuncion(nombre: String, parametros: JsonObjectBuilder.() -> Unit): String =
        cliente.postgrest.rpc(nombre, buildJsonObject(parametros)).decodeAs<String>()

    private fun lanzarSiNoEsOk(resultado: String) {
        when (resultado) {
            "ok" -> Unit
            "correo_no_registrado" -> throw CorreoNoRegistradoException()
            "nombre_no_coincide" -> throw NombreUsuarioNoCoincideException()
            "contrasena_invalida" -> error("La contraseña no cumple los requisitos.")
            else -> error("No se pudo verificar tu identidad. Inténtalo de nuevo.")
        }
    }

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
        } catch (e: CorreoNoRegistradoException) {
            Result.failure(e)
        } catch (e: NombreUsuarioNoCoincideException) {
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

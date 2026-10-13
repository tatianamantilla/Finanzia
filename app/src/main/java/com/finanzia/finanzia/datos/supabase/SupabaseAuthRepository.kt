package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.ConfirmacionCorreoPendienteException
import com.finanzia.finanzia.datos.CredencialesIncorrectasException
import com.finanzia.finanzia.datos.SinConexionException
import com.finanzia.finanzia.datos.CorreoNoRegistradoException
import com.finanzia.finanzia.datos.CorreoYaRegistradoException
import com.finanzia.finanzia.datos.NombreUsuarioNoCoincideException
import com.finanzia.finanzia.datos.Usuario
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
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
                    // El trigger del registro usa este nombre para crear la fila en `usuarios`.
                    data = buildJsonObject { put("nombre", nombre.trim()) }
                }
            } catch (e: AuthRestException) {
                if (e.errorCode == AuthErrorCode.UserAlreadyExists || e.errorCode == AuthErrorCode.EmailExists) {
                    throw CorreoYaRegistradoException()
                }
                throw e
            }
            // Con "Confirm email" desactivado, Supabase entrega la sesión al registrarse. Si no la
            // entrega, es porque pide confirmar el correo: se avisa en la pantalla, no se falla en silencio.
            val info = cliente.auth.currentUserOrNull() ?: throw ConfirmacionCorreoPendienteException()
            // Cuenta "Principal" + categorías (incluida "Ingresos") con su color, sin duplicar.
            DatosInicialesSupabase.asegurar(cliente)
            cargarUsuario(info, nombrePorDefecto = nombre.trim())
        }

    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> =
        intentar {
            try {
                cliente.auth.signInWith(Email) {
                    email = correo.trim()
                    password = contrasena
                }
            } catch (e: AuthRestException) {
                when (e.errorCode) {
                    AuthErrorCode.InvalidCredentials -> throw CredencialesIncorrectasException()
                    AuthErrorCode.EmailNotConfirmed -> throw ConfirmacionCorreoPendienteException()
                    else -> throw e
                }
            }
            val info = cliente.auth.currentUserOrNull() ?: throw CredencialesIncorrectasException()
            DatosInicialesSupabase.asegurar(cliente)
            cargarUsuario(info, nombrePorDefecto = null)
        }

    /** Al abrir la app: si el teléfono tiene una sesión guardada, la usa. */
    override suspend fun restaurarSesion(): Usuario? {
        usuarioActual?.let { return it }
        return intentar {
            cliente.auth.awaitInitialization()
            cliente.auth.currentUserOrNull()?.let { cargarUsuario(it, nombrePorDefecto = null) }
        }.getOrNull()
    }

    override suspend fun cerrarSesion(): Result<Unit> = intentar {
        cliente.auth.signOut()
        usuarioActual = null
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
        // Si falla (sin conexión, permisos), el error sube y la pantalla lo muestra.
        val fila = cliente.from("usuarios").select {
            filter { eq("id_auth", info.id) }
        }.decodeSingleOrNull<FilaUsuario>()

        val correo = fila?.correo ?: info.email.orEmpty()
        val nombre = fila?.nombre
            ?: nombrePorDefecto
            ?: info.userMetadata?.get("nombre")?.jsonPrimitive?.content
            ?: correo.substringBefore("@")

        return Usuario(id = info.id, nombre = nombre, correo = correo).also { usuarioActual = it }
    }

    /** Errores de red → mensaje claro; los demás se devuelven tal cual (ver ErroresSupabase.kt). */
    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> = conErroresClaros(bloque)

    @Serializable
    private data class FilaUsuario(
        @SerialName("id_usuario") val idUsuario: Long,
        val nombre: String,
        val correo: String,
    )
}

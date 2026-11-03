package com.finanzia.finanzia.datos

/** Quién escribió un mensaje (campo `rol` de la tabla mensajes). */
enum class RolMensaje(val valor: String) {
    USUARIO("usuario"),
    ASISTENTE("asistente"),
    ;

    companion object {
        fun desde(valor: String): RolMensaje = if (valor == ASISTENTE.valor) ASISTENTE else USUARIO
    }
}

/** Un mensaje de la conversación con Finanzia IA (spec 11). */
data class MensajeChat(val rol: RolMensaje, val texto: String)

/** La Edge Function `asistente` todavía no está desplegada o no tiene la clave de la IA. */
class AsistenteNoDisponibleException : Exception("Finanzia IA todavía no está disponible. Inténtalo más tarde.")

/** Conversación con Finanzia IA del usuario que inició sesión (spec 11). */
interface AsistenteRepository {
    /** Mensajes de la conversación activa, del más antiguo al más reciente (vacío si no hay). */
    suspend fun cargarHistorial(): Result<List<MensajeChat>>

    /** Envía la pregunta y devuelve la respuesta de Finanzia IA (ya guardada en Supabase). */
    suspend fun preguntar(pregunta: String): Result<MensajeChat>
}

/**
 * Versión simulada para tests y Previews: guarda en memoria y responde con [responder].
 * Nunca se usa en la app real.
 */
class AsistenteSimulado(
    historial: List<MensajeChat> = emptyList(),
    private val responder: (String) -> Result<String> = { Result.success("Respuesta de prueba.") },
) : AsistenteRepository {

    val mensajes = historial.toMutableList()

    /** Si no es null, [cargarHistorial] falla con este error. */
    var errorAlCargar: Throwable? = null

    override suspend fun cargarHistorial(): Result<List<MensajeChat>> {
        errorAlCargar?.let { return Result.failure(it) }
        return Result.success(mensajes.toList())
    }

    override suspend fun preguntar(pregunta: String): Result<MensajeChat> =
        responder(pregunta).map { texto ->
            mensajes += MensajeChat(RolMensaje.USUARIO, pregunta)
            MensajeChat(RolMensaje.ASISTENTE, texto).also { mensajes += it }
        }
}

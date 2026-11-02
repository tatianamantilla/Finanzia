package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.AsistenteNoDisponibleException
import com.finanzia.finanzia.datos.AsistenteRepository
import com.finanzia.finanzia.datos.MensajeChat
import com.finanzia.finanzia.datos.RolMensaje
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Asistente Finanzia IA (spec 11).
 *
 * - El historial se lee de `conversaciones` y `mensajes`; RLS solo deja ver lo del usuario.
 * - La respuesta la genera la Edge Function `asistente`, que lee los datos del usuario, llama a la
 *   IA con la clave guardada como secret de Supabase y guarda la pregunta y la respuesta.
 *   La app nunca conoce la clave de la IA.
 */
class SupabaseAsistenteRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : AsistenteRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun cargarHistorial(): Result<List<MensajeChat>> = conErroresClaros {
        val idUsuario = DatosInicialesSupabase.idUsuarioActual(cliente)
        val conversacion = cliente.from("conversaciones").select {
            filter {
                eq("id_usuario", idUsuario)
                eq("activo", true)
            }
            order("fecha_creacion", Order.DESCENDING)
            limit(1)
        }.decodeList<FilaConversacion>().firstOrNull() ?: return@conErroresClaros emptyList()

        cliente.from("mensajes").select {
            filter {
                eq("id_conversacion", conversacion.idConversacion)
                eq("activo", true)
            }
            order("fecha", Order.ASCENDING)
            order("id_mensaje", Order.ASCENDING)
        }.decodeList<FilaMensaje>().map { MensajeChat(RolMensaje.desde(it.rol), it.contenido) }
    }

    override suspend fun preguntar(pregunta: String): Result<MensajeChat> = conErroresClaros {
        val respuesta = try {
            cliente.functions.invoke(
                function = "asistente",
                body = buildJsonObject { put("pregunta", pregunta) },
            )
        } catch (e: RestException) {
            // 404: la función no está desplegada. 5xx: la función no tiene clave o la IA falló.
            if (e.statusCode == 404 || e.statusCode >= 500) throw AsistenteNoDisponibleException()
            throw e
        }
        val texto = json.decodeFromString<RespuestaAsistente>(respuesta.bodyAsText()).respuesta.trim()
        if (texto.isEmpty()) throw AsistenteNoDisponibleException()
        MensajeChat(RolMensaje.ASISTENTE, texto)
    }

    @Serializable
    private data class FilaConversacion(@SerialName("id_conversacion") val idConversacion: Long)

    @Serializable
    private data class FilaMensaje(val rol: String, val contenido: String)

    /** Lo que devuelve la Edge Function: `{ "respuesta": "…" }`. */
    @Serializable
    private data class RespuestaAsistente(val respuesta: String = "")
}

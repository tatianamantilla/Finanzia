package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.ClaseNotificacion
import com.finanzia.finanzia.datos.Notificacion
import com.finanzia.finanzia.datos.NotificacionesRepository
import com.finanzia.finanzia.datos.SesionTerminadaException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Notificaciones del usuario que inició sesión (spec 05, docs/supabase/05-notificaciones.sql).
 *
 * 1. Llama a generar_notificaciones: Supabase revisa los datos del usuario y crea los avisos
 *    nuevos (nunca repite uno).
 * 2. Lee las notificaciones activas, de la más nueva a la más vieja. Las políticas RLS
 *    impiden ver las de otras personas.
 */
class SupabaseNotificacionesRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
    private val zona: () -> ZoneId = ZoneId::systemDefault,
) : NotificacionesRepository {

    override suspend fun cargar(): Result<List<Notificacion>> = conErroresClaros {
        if (cliente.auth.currentUserOrNull() == null) throw SesionTerminadaException()

        // Si generar falla, igual se muestran las que ya existen (no se pierde la pantalla).
        try {
            cliente.postgrest.rpc("generar_notificaciones", buildJsonObject { put("p_zona", zona().id) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Se ignora a propósito: abajo se leen las notificaciones que ya existen.
        }

        cliente.from("notificaciones").select {
            filter { eq("activo", true) }
            order("fecha", Order.DESCENDING)
            limit(LIMITE)
        }.decodeList<FilaNotificacion>().mapNotNull { fila ->
            val clase = ClaseNotificacion.desde(fila.tipo) ?: return@mapNotNull null
            Notificacion(
                id = fila.idNotificacion,
                clase = clase,
                titulo = fila.titulo,
                mensaje = fila.mensaje,
                fecha = OffsetDateTime.parse(fila.fecha).toInstant(),
                leida = fila.leida,
                progreso = fila.progreso,
            )
        }
    }

    @Serializable
    private data class FilaNotificacion(
        @SerialName("id_notificacion") val idNotificacion: Long,
        val tipo: String,
        val titulo: String,
        val mensaje: String,
        val progreso: Double? = null,
        val leida: Boolean,
        val fecha: String,
    )

    private companion object {
        /** Las más recientes; suficiente para la pantalla. */
        const val LIMITE = 50L
    }
}

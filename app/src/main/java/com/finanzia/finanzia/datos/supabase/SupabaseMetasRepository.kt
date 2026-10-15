package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.Aporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.datos.NuevaMeta
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Metas (spec 07) y Nueva meta (spec 08) del usuario que inició sesión. Cada consulta filtra
 * por su id_usuario y, además, las políticas RLS impiden ver o crear metas de otras personas.
 */
class SupabaseMetasRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : MetasRepository {

    override suspend fun cargar(): Result<List<Meta>> = conErroresClaros {
        val idUsuario = DatosInicialesSupabase.idUsuarioActual(cliente)
        val filas = cliente.from("metas").select {
            filter {
                eq("id_usuario", idUsuario)
                eq("activo", true)
            }
            order("fecha_creacion", Order.DESCENDING)
        }.decodeList<FilaMeta>()

        val aportes = if (filas.isEmpty()) {
            emptyMap()
        } else {
            cliente.from("aportes").select {
                filter { isIn("id_meta", filas.map { it.idMeta }) }
            }.decodeList<FilaAporte>().groupBy { it.idMeta }
        }

        filas.map { fila ->
            Meta(
                id = fila.idMeta,
                nombre = fila.nombre,
                montoObjetivo = fila.montoObjetivo,
                fechaLimite = fila.fechaLimite?.let(::aFecha),
                fechaCreacion = fila.fechaCreacion?.let(::aInstante) ?: Instant.EPOCH,
                aportes = aportes[fila.idMeta].orEmpty().map { Aporte(it.monto, it.fecha?.let(::aInstante) ?: Instant.EPOCH) },
            )
        }
    }

    override suspend fun crear(meta: NuevaMeta): Result<Unit> = conErroresClaros {
        val idUsuario = DatosInicialesSupabase.idUsuarioActual(cliente)
        cliente.from("metas").insert(
            FilaNuevaMeta(
                idUsuario = idUsuario,
                nombre = meta.nombre,
                montoObjetivo = meta.montoObjetivo,
                fechaLimite = meta.fechaLimite.toString(),
                fechaCreacion = OffsetDateTime.now(ZoneOffset.UTC).toString(),
            ),
        )
        Unit
    }

    /** Acepta una fecha ("2026-11-30") o una fecha con hora. */
    private fun aFecha(texto: String): LocalDate = LocalDate.parse(texto.take(10))

    /** Acepta fechas con zona ("…+00:00"), sin zona (se toma como UTC) o solo la fecha. */
    private fun aInstante(texto: String): Instant =
        runCatching { OffsetDateTime.parse(texto).toInstant() }
            .recoverCatching { LocalDateTime.parse(texto).toInstant(ZoneOffset.UTC) }
            .getOrElse { LocalDate.parse(texto.take(10)).atStartOfDay().toInstant(ZoneOffset.UTC) }

    @Serializable
    private data class FilaMeta(
        @SerialName("id_meta") val idMeta: Long,
        val nombre: String,
        @SerialName("monto_objetivo") val montoObjetivo: Double,
        @SerialName("fecha_limite") val fechaLimite: String? = null,
        @SerialName("fecha_creacion") val fechaCreacion: String? = null,
    )

    @Serializable
    private data class FilaAporte(
        @SerialName("id_meta") val idMeta: Long,
        val monto: Double,
        val fecha: String? = null,
    )

    @Serializable
    private data class FilaNuevaMeta(
        @SerialName("id_usuario") val idUsuario: Long,
        val nombre: String,
        @SerialName("monto_objetivo") val montoObjetivo: Long,
        @SerialName("fecha_limite") val fechaLimite: String,
        @SerialName("fecha_creacion") val fechaCreacion: String,
        val activo: Boolean = true,
    )
}

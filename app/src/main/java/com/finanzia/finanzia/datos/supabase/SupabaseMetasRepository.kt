package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.AporteMeta
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.datos.NuevaMeta
import com.finanzia.finanzia.datos.exigirPermitido
import com.finanzia.finanzia.validacion.ReglaSaldo
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
 * Metas del usuario que inició sesión (specs 07 y 08). Cada consulta filtra por su id_usuario y,
 * además, las políticas RLS impiden ver o crear metas de otras personas.
 *
 * Usa las columnas categoria y frecuencia_aporte de docs/supabase/07-metas-categoria-frecuencia.sql.
 */
class SupabaseMetasRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : MetasRepository {

    override suspend fun cargar(): Result<List<Meta>> = conErroresClaros {
        val idUsuario = DatosInicialesSupabase.idUsuarioActual(cliente)
        val metas = cliente.from("metas").select {
            filter {
                eq("id_usuario", idUsuario)
                eq("activo", true)
            }
            order("fecha_creacion", Order.DESCENDING)
        }.decodeList<FilaMeta>()
        if (metas.isEmpty()) return@conErroresClaros emptyList()

        val aportes = cliente.from("aportes").select {
            filter { isIn("id_meta", metas.map { it.idMeta }) }
        }.decodeList<FilaAporte>().groupBy { it.idMeta }

        metas.map { fila ->
            Meta(
                id = fila.idMeta,
                nombre = fila.nombre,
                montoObjetivo = fila.montoObjetivo,
                fechaLimite = fila.fechaLimite?.let(::aFecha),
                fechaCreacion = aInstante(fila.fechaCreacion),
                categoria = CategoriaMeta.desde(fila.categoria),
                frecuencia = FrecuenciaAporte.desde(fila.frecuenciaAporte),
                aportes = aportes[fila.idMeta].orEmpty().map { AporteMeta(it.monto, aFecha(it.fecha), it.idAporte) },
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
                fechaLimite = meta.mesLimite.atEndOfMonth().toString(),
                fechaCreacion = OffsetDateTime.now().toString(),
                categoria = meta.categoria.valor,
                frecuenciaAporte = meta.frecuencia.valor,
            ),
        )
        Unit
    }

    override suspend fun aportar(idMeta: Long, monto: Long): Result<Unit> = conErroresClaros {
        // El aporte sale de la cuenta Principal: RLS exige que la meta y la cuenta sean del usuario.
        val ids = DatosInicialesSupabase.asegurar(cliente)
        // Un aporte es un ahorro: sale del saldo disponible y no puede dejarlo en negativo (spec 10).
        ReglaSaldo.validarSalida(SaldoSupabase.disponible(cliente, ids.idCuentaPrincipal), java.math.BigDecimal.valueOf(monto))
            .exigirPermitido()
        cliente.from("aportes").insert(
            FilaNuevoAporte(
                idMeta = idMeta,
                idCuenta = ids.idCuentaPrincipal,
                monto = monto,
                fecha = OffsetDateTime.now().toString(),
            ),
        )
        Unit
    }

    override suspend fun editarAporte(idAporte: Long, monto: Long): Result<Unit> = conErroresClaros {
        // RLS solo deja cambiar aportes de metas del usuario que inició sesión.
        // Regla del saldo (spec 10): se cuenta lo que este aporte ya había descontado.
        val actual = cliente.from("aportes").select { filter { eq("id_aporte", idAporte) } }
            .decodeSingle<AporteActual>()
        ReglaSaldo.validarEdicionSalida(
            SaldoSupabase.disponible(cliente, actual.idCuenta),
            actual.monto,
            java.math.BigDecimal.valueOf(monto),
        ).exigirPermitido()
        cliente.from("aportes").update(CambioMontoAporte(monto)) {
            filter { eq("id_aporte", idAporte) }
        }
        Unit
    }

    /** "2026-11-30" o "2026-11-30T05:00:00+00:00" → 2026-11-30 (sirve si la columna es date o timestamp). */
    private fun aFecha(texto: String): LocalDate = LocalDate.parse(texto.take(10))

    /** Acepta fecha y hora con zona, sin zona, o solo la fecha (según el tipo de la columna). */
    private fun aInstante(texto: String): Instant =
        runCatching { OffsetDateTime.parse(texto).toInstant() }
            .recoverCatching { LocalDateTime.parse(texto).toInstant(ZoneOffset.UTC) }
            .getOrElse { aFecha(texto).atStartOfDay(ZoneOffset.UTC).toInstant() }

    @Serializable
    private data class FilaMeta(
        @SerialName("id_meta") val idMeta: Long,
        val nombre: String,
        @SerialName("monto_objetivo") val montoObjetivo: Double,
        @SerialName("fecha_limite") val fechaLimite: String? = null,
        @SerialName("fecha_creacion") val fechaCreacion: String,
        val categoria: String? = null,
        @SerialName("frecuencia_aporte") val frecuenciaAporte: String? = null,
    )

    @Serializable
    private data class FilaAporte(
        @SerialName("id_aporte") val idAporte: Long,
        @SerialName("id_meta") val idMeta: Long,
        val monto: Double,
        val fecha: String,
    )

    @Serializable
    private data class CambioMontoAporte(val monto: Long)

    @Serializable
    private data class AporteActual(
        @SerialName("id_cuenta") val idCuenta: Long,
        val monto: Dinero,
    )

    @Serializable
    private data class FilaNuevoAporte(
        @SerialName("id_meta") val idMeta: Long,
        @SerialName("id_cuenta") val idCuenta: Long,
        val monto: Long,
        val fecha: String,
    )

    @Serializable
    private data class FilaNuevaMeta(
        @SerialName("id_usuario") val idUsuario: Long,
        val nombre: String,
        @SerialName("monto_objetivo") val montoObjetivo: Long,
        @SerialName("fecha_limite") val fechaLimite: String,
        @SerialName("fecha_creacion") val fechaCreacion: String,
        val categoria: String,
        @SerialName("frecuencia_aporte") val frecuenciaAporte: String,
        val activo: Boolean = true,
    )
}

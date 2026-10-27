package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.AporteDashboard
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardRepository
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MetaDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.SesionTerminadaException
import com.finanzia.finanzia.datos.TipoMovimiento
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * Lee el Dashboard (spec 04) del usuario que inició sesión. Cada consulta filtra por su
 * id_usuario y, además, las políticas RLS impiden ver datos de otras personas.
 */
class SupabaseDashboardRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : DashboardRepository {

    override suspend fun cargar(): Result<DatosDashboard> = conErroresClaros {
        val idAuth = cliente.auth.currentUserOrNull()?.id ?: throw SesionTerminadaException()
        val usuario = cliente.from("usuarios").select { filter { eq("id_auth", idAuth) } }
            .decodeSingleOrNull<FilaUsuario>() ?: throw SesionTerminadaException()
        val id = usuario.idUsuario

        val cuentas = cliente.from("cuentas").select {
            filter {
                eq("id_usuario", id)
                eq("activo", true)
            }
            order("id_cuenta", Order.ASCENDING)
        }.decodeList<FilaCuenta>()
        val nombreCuenta = cuentas.associate { it.idCuenta to it.nombre }

        val categorias = cliente.from("categorias").select { filter { eq("id_usuario", id) } }
            .decodeList<FilaCategoria>().associateBy { it.idCategoria }
        val colores = cliente.from("colores").select().decodeList<FilaColor>().associate { it.idColor to it.color }

        val movimientos = cliente.from("movimientos").select {
            filter {
                eq("id_usuario", id)
                eq("activo", true)
            }
            order("fecha", Order.DESCENDING)
        }.decodeList<FilaMovimiento>()
            // Solo los de cuentas activas (spec 04: balance por cada cuenta activa).
            .filter { it.idCuenta in nombreCuenta }
            .mapNotNull { fila ->
                val tipo = when (fila.tipo.lowercase()) {
                    "ingreso" -> TipoMovimiento.INGRESO
                    "gasto" -> TipoMovimiento.GASTO
                    else -> return@mapNotNull null
                }
                val categoria = fila.idCategoria?.let { categorias[it] }
                MovimientoDashboard(
                    id = fila.idMovimiento,
                    tipo = tipo,
                    monto = fila.monto,
                    descripcion = fila.descripcion.orEmpty(),
                    fecha = OffsetDateTime.parse(fila.fecha).toInstant(),
                    nombreCuenta = nombreCuenta.getValue(fila.idCuenta),
                    nombreCategoria = categoria?.nombre,
                    colorCategoria = categoria?.idColor?.let { colores[it] },
                )
            }

        val meta = cliente.from("metas").select {
            filter {
                eq("id_usuario", id)
                eq("activo", true)
            }
            order("fecha_creacion", Order.DESCENDING)
            limit(1)
        }.decodeList<FilaMeta>().firstOrNull()?.let { fila ->
            val aportado = cliente.from("aportes").select { filter { eq("id_meta", fila.idMeta) } }
                .decodeList<FilaAporte>().fold(java.math.BigDecimal.ZERO) { suma, it -> suma + it.monto }
            MetaDashboard(fila.nombre, fila.montoObjetivo, aportado)
        }

        // Aportes a metas (RLS: solo de metas propias) desde las cuentas activas: descuentan del saldo (spec 10).
        val aportes = cliente.from("aportes").select().decodeList<FilaAporte>()
            .filter { it.idCuenta in nombreCuenta }
            .map { AporteDashboard(it.monto, it.fecha?.let(::aInstante) ?: java.time.Instant.EPOCH) }

        DatosDashboard(
            aportes = aportes,
            nombreUsuario = usuario.nombre,
            cuentas = cuentas.map { CuentaDashboard(it.nombre, it.saldoInicial) },
            movimientos = movimientos,
            meta = meta,
        )
    }

    /** La fecha del aporte puede venir con o sin hora (según el tipo de la columna). */
    private fun aInstante(texto: String): java.time.Instant =
        runCatching { OffsetDateTime.parse(texto).toInstant() }
            .recoverCatching { java.time.LocalDateTime.parse(texto).toInstant(java.time.ZoneOffset.UTC) }
            .getOrElse { java.time.LocalDate.parse(texto.take(10)).atStartOfDay(java.time.ZoneOffset.UTC).toInstant() }

    @Serializable
    private data class FilaUsuario(@SerialName("id_usuario") val idUsuario: Long, val nombre: String)

    @Serializable
    private data class FilaCuenta(
        @SerialName("id_cuenta") val idCuenta: Long,
        val nombre: String,
        @SerialName("saldo_inicial") val saldoInicial: Dinero,
    )

    @Serializable
    private data class FilaCategoria(
        @SerialName("id_categoria") val idCategoria: Long,
        val nombre: String,
        @SerialName("id_color") val idColor: Long? = null,
    )

    @Serializable
    private data class FilaColor(@SerialName("id_color") val idColor: Long, val color: String)

    @Serializable
    private data class FilaMovimiento(
        @SerialName("id_movimiento") val idMovimiento: Long,
        @SerialName("id_cuenta") val idCuenta: Long,
        @SerialName("id_categoria") val idCategoria: Long? = null,
        val tipo: String,
        val monto: Dinero,
        val descripcion: String? = null,
        val fecha: String,
    )

    @Serializable
    private data class FilaMeta(
        @SerialName("id_meta") val idMeta: Long,
        val nombre: String,
        @SerialName("monto_objetivo") val montoObjetivo: Dinero,
    )

    @Serializable
    private data class FilaAporte(
        val monto: Dinero,
        @SerialName("id_cuenta") val idCuenta: Long? = null,
        val fecha: String? = null,
    )
}

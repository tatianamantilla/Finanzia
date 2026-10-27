package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.validacion.ReglaSaldo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Saldo disponible de una cuenta, leído de Supabase justo antes de guardar (spec 10):
 * saldo inicial + ingresos − gastos de sus movimientos activos − aportes a metas desde esa cuenta.
 * Las políticas RLS solo dejan leer lo del usuario que inició sesión.
 */
internal object SaldoSupabase {

    suspend fun disponible(
        cliente: SupabaseClient,
        idCuenta: Long,
        sinMovimiento: Long? = null,
        sinAporte: Long? = null,
    ): BigDecimal {
        val cuenta = cliente.from("cuentas").select { filter { eq("id_cuenta", idCuenta) } }
            .decodeSingle<FilaCuenta>()
        val movimientos = cliente.from("movimientos").select {
            filter {
                eq("id_cuenta", idCuenta)
                eq("activo", true)
            }
        }.decodeList<FilaMovimiento>().filter { it.idMovimiento != sinMovimiento }
        val aportes = cliente.from("aportes").select { filter { eq("id_cuenta", idCuenta) } }
            .decodeList<FilaAporte>().filter { it.idAporte != sinAporte }

        fun suma(montos: List<BigDecimal>) = montos.fold(BigDecimal.ZERO, BigDecimal::add)
        return ReglaSaldo.saldoDisponible(
            saldoInicial = cuenta.saldoInicial,
            ingresos = suma(movimientos.filter { it.tipo.equals("ingreso", ignoreCase = true) }.map { it.monto }),
            gastos = suma(movimientos.filter { it.tipo.equals("gasto", ignoreCase = true) }.map { it.monto }),
            aportes = suma(aportes.map { it.monto }),
        )
    }

    @Serializable
    private data class FilaCuenta(@SerialName("saldo_inicial") val saldoInicial: Dinero)

    @Serializable
    private data class FilaMovimiento(
        @SerialName("id_movimiento") val idMovimiento: Long,
        val tipo: String,
        val monto: Dinero,
    )

    @Serializable
    private data class FilaAporte(@SerialName("id_aporte") val idAporte: Long, val monto: Dinero)
}

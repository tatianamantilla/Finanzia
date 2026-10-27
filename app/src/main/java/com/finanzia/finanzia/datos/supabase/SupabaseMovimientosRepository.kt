package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.MovimientoNoEncontradoException
import com.finanzia.finanzia.datos.MovimientosRepository
import com.finanzia.finanzia.datos.exigirPermitido
import com.finanzia.finanzia.validacion.ReglaSaldo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Editar un movimiento (spec 10): un UPDATE sobre el mismo registro de `movimientos`, nunca uno
 * nuevo. Antes de guardar se aplica la regla del saldo con el saldo real de su cuenta, contando
 * que el movimiento ya estaba aplicado. La política RLS "movimientos: solo los propios"
 * (docs/supabase/02-permisos-y-rls.sql) impide editar movimientos de otra persona.
 */
class SupabaseMovimientosRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : MovimientosRepository {

    override suspend fun editar(idMovimiento: Long, monto: Long, categoria: String): Result<Unit> = conErroresClaros {
        val ids = DatosInicialesSupabase.asegurar(cliente)
        val actual = cliente.from("movimientos").select {
            filter {
                eq("id_movimiento", idMovimiento)
                eq("id_usuario", ids.idUsuario)
                eq("activo", true)
            }
        }.decodeSingleOrNull<FilaMovimiento>() ?: throw MovimientoNoEncontradoException()

        val nuevo = BigDecimal.valueOf(monto)
        val saldo = SaldoSupabase.disponible(cliente, actual.idCuenta)
        val esIngreso = actual.tipo.equals("ingreso", ignoreCase = true)
        val resultado = if (esIngreso) {
            ReglaSaldo.validarEdicionEntrada(saldo, actual.monto, nuevo)
        } else {
            ReglaSaldo.validarEdicionSalida(saldo, actual.monto, nuevo)
        }
        resultado.exigirPermitido()

        val idCategoria = ids.categorias[categoria] ?: error("No se encontró la categoría $categoria.")
        val actualizados = cliente.from("movimientos").update(Cambio(nuevo, idCategoria)) {
            filter {
                eq("id_movimiento", idMovimiento)
                eq("id_usuario", ids.idUsuario)
            }
            select()
        }.decodeList<FilaMovimiento>()
        if (actualizados.size != 1) throw MovimientoNoEncontradoException()
    }

    @Serializable
    private data class FilaMovimiento(
        @SerialName("id_movimiento") val idMovimiento: Long,
        @SerialName("id_cuenta") val idCuenta: Long,
        val tipo: String,
        val monto: Dinero,
    )

    @Serializable
    private data class Cambio(
        val monto: Dinero,
        @SerialName("id_categoria") val idCategoria: Long,
    )
}

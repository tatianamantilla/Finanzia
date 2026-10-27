package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.exigirPermitido
import com.finanzia.finanzia.validacion.ReglaSaldo
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.datos.nombreVisible
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Guarda el primer ingreso o el primer gasto del onboarding en `movimientos`, en la cuenta
 * "Principal" del usuario. El ingreso va en la categoría de su frecuencia (Mensual, Quincenal,
 * Semanal o Diario) y, en el onboarding, la frecuencia también se guarda en
 * `usuarios.frecuencia_ingreso` (columna del archivo docs/supabase/01-usuarios-y-auth.sql).
 *
 * La fecha la pone Supabase (fecha y hora actuales, `default now()`): así no se corre de día
 * por la zona horaria.
 */
class SupabaseOnboardingRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : OnboardingRepository {

    /** Ingreso desde el Dashboard: solo el movimiento, en la categoría de su frecuencia; la frecuencia del usuario no cambia. */
    override suspend fun guardarIngreso(monto: Long, descripcion: String, frecuencia: FrecuenciaIngreso): Result<Unit> = conErroresClaros {
        val ids = DatosInicialesSupabase.asegurar(cliente)
        cliente.from("movimientos").insert(
            NuevoMovimiento(
                idUsuario = ids.idUsuario,
                idCuenta = ids.idCuentaPrincipal,
                idCategoria = categoriaDeIngreso(ids, frecuencia),
                tipo = "ingreso",
                monto = monto,
                descripcion = descripcion,
            ),
        )
        Unit
    }

    override suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit> = conErroresClaros {
        val ids = DatosInicialesSupabase.asegurar(cliente)
        cliente.from("movimientos").insert(
            NuevoMovimiento(
                idUsuario = ids.idUsuario,
                idCuenta = ids.idCuentaPrincipal,
                idCategoria = categoriaDeIngreso(ids, ingreso.frecuencia),
                tipo = "ingreso",
                monto = ingreso.monto,
                descripcion = ingreso.descripcion,
            ),
        )
        // Primero el movimiento: si falla, no queda guardada la frecuencia sola.
        cliente.from("usuarios").update(CambioFrecuencia(ingreso.frecuencia.valorEnBaseDeDatos())) {
            filter { eq("id_usuario", ids.idUsuario) }
        }
        Unit
    }

    override suspend fun guardarPrimerGasto(gasto: PrimerGasto): Result<Unit> = conErroresClaros {
        val ids = DatosInicialesSupabase.asegurar(cliente)
        // Regla del saldo (spec 10): el gasto no puede dejar la cuenta por debajo de $0.00.
        ReglaSaldo.validarSalida(SaldoSupabase.disponible(cliente, ids.idCuentaPrincipal), java.math.BigDecimal.valueOf(gasto.monto))
            .exigirPermitido()
        val nombreCategoria = gasto.categoria.nombreVisible
        cliente.from("movimientos").insert(
            NuevoMovimiento(
                idUsuario = ids.idUsuario,
                idCuenta = ids.idCuentaPrincipal,
                // Sin categoría no se guarda: el gasto quedaría mal clasificado sin avisar.
                idCategoria = ids.categorias[nombreCategoria] ?: error("No se encontró la categoría $nombreCategoria."),
                tipo = "gasto",
                monto = gasto.monto,
                descripcion = gasto.descripcion,
            ),
        )
        Unit
    }

    /** Cada ingreso va en la categoría de su frecuencia (Mensual, Quincenal…), como los gastos en la suya. */
    private fun categoriaDeIngreso(ids: IdsUsuario, frecuencia: FrecuenciaIngreso): Long =
        ids.categorias[frecuencia.nombreVisible] ?: error("No se encontró la categoría ${frecuencia.nombreVisible}.")

    private fun FrecuenciaIngreso.valorEnBaseDeDatos() = when (this) {
        FrecuenciaIngreso.MENSUAL -> "mensual"
        FrecuenciaIngreso.QUINCENAL -> "quincenal"
        FrecuenciaIngreso.SEMANAL -> "semanal"
        FrecuenciaIngreso.DIARIO -> "diario"
    }

    @Serializable
    private data class CambioFrecuencia(@SerialName("frecuencia_ingreso") val frecuenciaIngreso: String)

    @Serializable
    private data class NuevoMovimiento(
        @SerialName("id_usuario") val idUsuario: Long,
        @SerialName("id_cuenta") val idCuenta: Long,
        @SerialName("id_categoria") val idCategoria: Long,
        val tipo: String,
        val monto: Long,
        val descripcion: String,
        val activo: Boolean = true,
    )
}

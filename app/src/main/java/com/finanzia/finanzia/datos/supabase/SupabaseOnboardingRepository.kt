package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.CATEGORIA_INGRESOS
import com.finanzia.finanzia.datos.FrecuenciaIngreso
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
 * "Principal" del usuario. El ingreso va en la categoría "Ingresos" y su frecuencia en
 * `usuarios.frecuencia_ingreso` (columna del archivo docs/supabase/01-usuarios-y-auth.sql).
 *
 * La fecha la pone Supabase (fecha y hora actuales, `default now()`): así no se corre de día
 * por la zona horaria.
 */
class SupabaseOnboardingRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : OnboardingRepository {

    override suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit> = conErroresClaros {
        val ids = DatosInicialesSupabase.asegurar(cliente)
        cliente.from("movimientos").insert(
            NuevoMovimiento(
                idUsuario = ids.idUsuario,
                idCuenta = ids.idCuentaPrincipal,
                idCategoria = ids.categorias[CATEGORIA_INGRESOS] ?: error("No se encontró la categoría $CATEGORIA_INGRESOS."),
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

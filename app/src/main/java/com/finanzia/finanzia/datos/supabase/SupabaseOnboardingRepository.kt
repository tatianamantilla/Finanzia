package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Guarda el primer ingreso o el primer gasto del onboarding en `movimientos`.
 *
 * Usa lo que crea el trigger del registro (docs/supabase/01-usuarios-y-auth.sql):
 * la cuenta "Principal" y las categorías Gastos Fijos, Comida, Diversión y Transporte.
 * La frecuencia del ingreso se guarda en `usuarios.frecuencia_ingreso`.
 */
class SupabaseOnboardingRepository(
    private val cliente: SupabaseClient = ClienteSupabase.cliente,
) : OnboardingRepository {

    override suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit> = runCatching {
        val idUsuario = idUsuarioActual()
        cliente.from("usuarios").update(CambioFrecuencia(ingreso.frecuencia.valorEnBaseDeDatos())) {
            filter { eq("id_usuario", idUsuario) }
        }
        cliente.from("movimientos").insert(
            NuevoMovimiento(
                idUsuario = idUsuario,
                idCuenta = idCuentaPrincipal(idUsuario),
                idCategoria = null,
                tipo = "ingreso",
                monto = ingreso.monto,
                descripcion = "Primer ingreso",
                fecha = LocalDate.now().toString(),
            ),
        )
        Unit
    }.mensajeClaro()

    override suspend fun guardarPrimerGasto(gasto: PrimerGasto): Result<Unit> = runCatching {
        val idUsuario = idUsuarioActual()
        cliente.from("movimientos").insert(
            NuevoMovimiento(
                idUsuario = idUsuario,
                idCuenta = idCuentaPrincipal(idUsuario),
                idCategoria = idCategoria(idUsuario, gasto.categoria.nombreEnBaseDeDatos()),
                tipo = "gasto",
                monto = gasto.monto,
                descripcion = "Primer gasto",
                fecha = LocalDate.now().toString(),
            ),
        )
        Unit
    }.mensajeClaro()

    private suspend fun idUsuarioActual(): Long {
        val idAuth = cliente.auth.currentUserOrNull()?.id ?: error("Tu sesión terminó. Inicia sesión de nuevo.")
        return cliente.from("usuarios").select { filter { eq("id_auth", idAuth) } }
            .decodeSingle<FilaId>().idUsuario
            ?: error("No se encontró tu usuario.")
    }

    private suspend fun idCuentaPrincipal(idUsuario: Long): Long =
        cliente.from("cuentas").select {
            filter {
                eq("id_usuario", idUsuario)
                eq("nombre", "Principal")
            }
        }.decodeList<FilaCuenta>().firstOrNull()?.idCuenta
            ?: error("No se encontró tu cuenta Principal.")

    private suspend fun idCategoria(idUsuario: Long, nombre: String): Long? =
        cliente.from("categorias").select {
            filter {
                eq("id_usuario", idUsuario)
                eq("nombre", nombre)
            }
        }.decodeList<FilaCategoria>().firstOrNull()?.idCategoria

    private fun FrecuenciaIngreso.valorEnBaseDeDatos() = when (this) {
        FrecuenciaIngreso.MENSUAL -> "mensual"
        FrecuenciaIngreso.QUINCENAL -> "quincenal"
        FrecuenciaIngreso.SEMANAL -> "semanal"
        FrecuenciaIngreso.DIARIO -> "diario"
    }

    /** Los mismos nombres que crea el trigger del registro. */
    private fun CategoriaGasto.nombreEnBaseDeDatos() = when (this) {
        CategoriaGasto.GASTOS_FIJOS -> "Gastos Fijos"
        CategoriaGasto.COMIDA -> "Comida"
        CategoriaGasto.DIVERSION -> "Diversión"
        CategoriaGasto.TRANSPORTE -> "Transporte"
    }

    private fun Result<Unit>.mensajeClaro(): Result<Unit> = recoverCatching { e ->
        if (e is kotlinx.coroutines.CancellationException) throw e
        if (e is io.github.jan.supabase.exceptions.HttpRequestException) throw SinConexionException()
        throw e
    }

    @Serializable
    private data class FilaId(@SerialName("id_usuario") val idUsuario: Long? = null)

    @Serializable
    private data class FilaCuenta(@SerialName("id_cuenta") val idCuenta: Long)

    @Serializable
    private data class FilaCategoria(@SerialName("id_categoria") val idCategoria: Long)

    @Serializable
    private data class CambioFrecuencia(@SerialName("frecuencia_ingreso") val frecuenciaIngreso: String)

    @Serializable
    private data class NuevoMovimiento(
        @SerialName("id_usuario") val idUsuario: Long,
        @SerialName("id_cuenta") val idCuenta: Long,
        @SerialName("id_categoria") val idCategoria: Long?,
        val tipo: String,
        val monto: Long,
        val descripcion: String,
        val fecha: String,
        val activo: Boolean = true,
    )
}

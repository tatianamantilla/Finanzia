package com.finanzia.finanzia.datos

import java.time.Instant

enum class TipoMovimiento { INGRESO, GASTO }

/** Un movimiento activo del usuario, con los nombres ya resueltos para mostrarlo. */
data class MovimientoDashboard(
    val id: Long,
    val tipo: TipoMovimiento,
    val monto: Double,
    val descripcion: String,
    val fecha: Instant,
    val nombreCuenta: String,
    val nombreCategoria: String?,
    /** Valor de la tabla colores (por ejemplo "#22C55E"), o null si la categoría no tiene color. */
    val colorCategoria: String?,
)

data class CuentaDashboard(val nombre: String, val saldoInicial: Double)

/** La meta activa más reciente con la suma de sus aportes. */
data class MetaDashboard(val nombre: String, val montoObjetivo: Double, val aportado: Double)

/** Todo lo que necesita el Dashboard, leído de los datos reales del usuario. */
data class DatosDashboard(
    val nombreUsuario: String,
    val cuentas: List<CuentaDashboard>,
    val movimientos: List<MovimientoDashboard>,
    val meta: MetaDashboard?,
)

/** Lee los datos del Dashboard del usuario que inició sesión. */
interface DashboardRepository {
    suspend fun cargar(): Result<DatosDashboard>
}

/**
 * Versión simulada para tests y Previews: arma el Dashboard con el usuario del [auth] simulado
 * y lo que se guardó en el [onboarding] simulado (así se ve lo que se acaba de registrar).
 */
class DashboardSimulado(
    private val auth: AuthRepository,
    private val onboarding: OnboardingRepository,
    private val ahora: () -> Instant = Instant::now,
) : DashboardRepository {

    /** Si no es null, [cargar] falla con este error (para probar el estado de error). */
    var errorAlCargar: Throwable? = null

    override suspend fun cargar(): Result<DatosDashboard> {
        errorAlCargar?.let { return Result.failure(it) }
        val usuario = auth.usuarioActual ?: return Result.failure(SesionTerminadaException())
        val simulado = onboarding as? FakeOnboardingRepository
        var id = 0L
        val movimientos = buildList {
            simulado?.ingresos?.forEach {
                add(MovimientoDashboard(++id, TipoMovimiento.INGRESO, it.monto.toDouble(), it.descripcion, ahora(), CUENTA_PRINCIPAL, CATEGORIA_INGRESOS, null))
            }
            simulado?.gastos?.forEach {
                add(MovimientoDashboard(++id, TipoMovimiento.GASTO, it.monto.toDouble(), it.descripcion, ahora(), CUENTA_PRINCIPAL, it.categoria.nombreVisible, null))
            }
        }
        return Result.success(
            DatosDashboard(
                nombreUsuario = usuario.nombre,
                cuentas = listOf(CuentaDashboard(CUENTA_PRINCIPAL, 0.0)),
                movimientos = movimientos.sortedByDescending { it.id },
                meta = null,
            ),
        )
    }
}

/** Nombre de la cuenta que se crea por defecto al registrarse. */
const val CUENTA_PRINCIPAL = "Principal"

/** Repositorio simulado con datos fijos (tests de interfaz y Previews). */
class DashboardFijo(private val datos: Result<DatosDashboard>) : DashboardRepository {
    var cargas = 0
        private set

    override suspend fun cargar(): Result<DatosDashboard> {
        cargas++
        return datos
    }
}

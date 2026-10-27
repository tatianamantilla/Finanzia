package com.finanzia.finanzia.datos

import java.math.BigDecimal
import java.time.Instant

enum class TipoMovimiento { INGRESO, GASTO }

/** Un movimiento activo del usuario, con los nombres ya resueltos para mostrarlo. */
data class MovimientoDashboard(
    val id: Long,
    val tipo: TipoMovimiento,
    /** Monto exacto (BigDecimal): el dinero nunca se calcula con Double (spec 10). */
    val monto: BigDecimal,
    val descripcion: String,
    val fecha: Instant,
    val nombreCuenta: String,
    val nombreCategoria: String?,
    /** Valor de la tabla colores (por ejemplo "#22C55E"), o null si la categoría no tiene color. */
    val colorCategoria: String?,
)

data class CuentaDashboard(val nombre: String, val saldoInicial: BigDecimal)

/** Un aporte a una meta: descuenta del saldo disponible de la cuenta (spec 10). */
data class AporteDashboard(val monto: BigDecimal, val fecha: Instant)

/** La meta activa más reciente con la suma de sus aportes. */
data class MetaDashboard(val nombre: String, val montoObjetivo: BigDecimal, val aportado: BigDecimal)

/** Todo lo que necesita el Dashboard, leído de los datos reales del usuario. */
data class DatosDashboard(
    val nombreUsuario: String,
    val cuentas: List<CuentaDashboard>,
    val movimientos: List<MovimientoDashboard>,
    val meta: MetaDashboard?,
    /** Aportes a metas desde las cuentas activas: también descuentan del saldo (spec 10). */
    val aportes: List<AporteDashboard> = emptyList(),
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
                add(MovimientoDashboard(++id, TipoMovimiento.INGRESO, BigDecimal.valueOf(it.monto), it.descripcion, ahora(), CUENTA_PRINCIPAL, it.frecuencia.nombreVisible, null))
            }
            simulado?.gastos?.forEach {
                add(MovimientoDashboard(++id, TipoMovimiento.GASTO, BigDecimal.valueOf(it.monto), it.descripcion, ahora(), CUENTA_PRINCIPAL, it.categoria.nombreVisible, null))
            }
        }
        return Result.success(
            DatosDashboard(
                nombreUsuario = usuario.nombre,
                cuentas = listOf(CuentaDashboard(CUENTA_PRINCIPAL, BigDecimal.ZERO)),
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

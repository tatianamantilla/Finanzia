package com.finanzia.finanzia.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.DashboardRepository
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MetaDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import com.finanzia.finanzia.validacion.CalculosDashboard
import com.finanzia.finanzia.validacion.FormatosDashboard
import com.finanzia.finanzia.validacion.MayorGasto
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/** Una fila de "Movimientos recientes", con los textos ya formateados. */
data class FilaMovimiento(
    val titulo: String,
    /** "Hoy, 14:32 • Principal" */
    val subtitulo: String,
    /** "+$2,800.00" o "-$64.50" */
    val monto: String,
    val categoria: String?,
    val esIngreso: Boolean,
    val colorCategoria: String?,
)

/** Todo lo que pinta el Dashboard, calculado con los datos reales del usuario. */
data class ResumenDashboard(
    val primerNombre: String,
    /** Nombre de la primera cuenta activa, o null si no tiene cuentas. */
    val nombreCuenta: String?,
    val balance: Double,
    val ingresosMes: Double,
    val gastosMes: Double,
    /** Pastilla "Saludable +X%": null = no se muestra. */
    val ahorroMes: Double?,
    /** Línea de tendencia: con menos de dos puntos no se dibuja. */
    val tendencia: List<Double>,
    /** Consejo: null = "Aún no hay suficiente información…". */
    val mayorGasto: MayorGasto?,
    val recientes: List<FilaMovimiento>,
    val meta: MetaDashboard?,
)

object ResumidorDashboard {
    fun resumir(datos: DatosDashboard, ahora: Instant, zona: ZoneId): ResumenDashboard {
        val hoy = ahora.atZone(zona).toLocalDate()
        val delMes = CalculosDashboard.delMes(datos.movimientos, hoy, zona)
        val ingresosMes = CalculosDashboard.total(delMes, TipoMovimiento.INGRESO)
        val gastosMes = CalculosDashboard.total(delMes, TipoMovimiento.GASTO)
        return ResumenDashboard(
            primerNombre = FormatosDashboard.primerNombre(datos.nombreUsuario),
            nombreCuenta = datos.cuentas.firstOrNull()?.nombre,
            balance = CalculosDashboard.balance(datos),
            ingresosMes = ingresosMes,
            gastosMes = gastosMes,
            ahorroMes = CalculosDashboard.ahorroDelMes(ingresosMes, gastosMes),
            tendencia = CalculosDashboard.puntosTendencia(datos),
            mayorGasto = CalculosDashboard.mayorGasto(delMes),
            recientes = CalculosDashboard.recientes(datos.movimientos).map {
                FilaMovimiento(
                    titulo = it.descripcion,
                    subtitulo = FormatosDashboard.fecha(it.fecha, ahora, zona) + " • " + it.nombreCuenta,
                    monto = FormatosDashboard.conSigno(it),
                    categoria = it.nombreCategoria,
                    esIngreso = it.tipo == TipoMovimiento.INGRESO,
                    colorCategoria = it.colorCategoria,
                )
            },
            meta = datos.meta,
        )
    }
}

sealed interface EstadoDashboard {
    data object Cargando : EstadoDashboard
    data class Error(val mensaje: String) : EstadoDashboard
    data class Listo(val resumen: ResumenDashboard) : EstadoDashboard
}

class DashboardViewModel(
    private val repositorio: DashboardRepository,
    private val auth: AuthRepository,
    private val ahora: () -> Instant = Instant::now,
    private val zona: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    var estado by mutableStateOf<EstadoDashboard>(EstadoDashboard.Cargando)
        private set

    /** El ojo del balance: por defecto se ve el monto. */
    var balanceVisible by mutableStateOf(true)
        private set

    var cerrandoSesion by mutableStateOf(false)
        private set
    var errorCerrarSesion by mutableStateOf<String?>(null)
        private set

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoDashboard.Cargando
        viewModelScope.launch {
            estado = repositorio.cargar().fold(
                onSuccess = { EstadoDashboard.Listo(ResumidorDashboard.resumir(it, ahora(), zona)) },
                onFailure = { EstadoDashboard.Error(it.message ?: "No se pudo cargar tu información. Inténtalo de nuevo.") },
            )
        }
    }

    fun alternarBalance() { balanceVisible = !balanceVisible }

    fun cerrarSesion(alTerminar: () -> Unit) {
        if (cerrandoSesion) return
        cerrandoSesion = true
        errorCerrarSesion = null
        viewModelScope.launch {
            auth.cerrarSesion()
                .onSuccess { alTerminar() }
                .onFailure { errorCerrarSesion = it.message ?: "No se pudo cerrar la sesión. Inténtalo de nuevo." }
            cerrandoSesion = false
        }
    }
}

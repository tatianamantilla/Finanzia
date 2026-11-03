package com.finanzia.finanzia.ui.estadisticas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.DashboardRepository
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.validacion.CalculosEstadisticas
import com.finanzia.finanzia.validacion.ResumenEstadisticas
import com.finanzia.finanzia.validacion.TipoPeriodo
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

sealed interface EstadoEstadisticas {
    data object Cargando : EstadoEstadisticas
    data class Error(val mensaje: String) : EstadoEstadisticas
    data class Listo(val datos: DatosDashboard) : EstadoEstadisticas
}

/**
 * Estadísticas (spec 12). Lee los mismos datos reales del Dashboard (movimientos activos y la meta
 * más reciente con sus aportes) y recalcula la pantalla para el periodo elegido.
 */
class EstadisticasViewModel(
    /** La misma lectura del Dashboard (en la app, SupabaseDashboardRepository): no hay consulta nueva. */
    private val repositorio: DashboardRepository,
    private val auth: AuthRepository,
    private val ahora: () -> Instant = Instant::now,
    private val zona: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    var estado by mutableStateOf<EstadoEstadisticas>(EstadoEstadisticas.Cargando)
        private set

    /** Por defecto: Mes. */
    var tipoPeriodo by mutableStateOf(TipoPeriodo.MES)
        private set

    /** Fechas elegidas en el selector del Rango (null hasta confirmar un rango). */
    var rango by mutableStateOf<Pair<LocalDate, LocalDate>?>(null)
        private set

    /** Grupo tocado en la gráfica; null = el grupo por defecto del periodo. */
    var grupoTocado by mutableStateOf<Int?>(null)
        private set

    var cerrandoSesion by mutableStateOf(false)
        private set
    var errorCerrarSesion by mutableStateOf<String?>(null)
        private set

    val hoy: LocalDate get() = ahora().atZone(zona).toLocalDate()

    /** La pantalla calculada para el periodo actual, o null mientras carga o si hubo error. */
    val resumen: ResumenEstadisticas?
        get() = (estado as? EstadoEstadisticas.Listo)?.let {
            CalculosEstadisticas.resumir(it.datos, tipoPeriodo, hoy, zona, rango)
        }

    init {
        cargar()
    }

    private var yaSeMostro = false

    /** Cada vez que la pantalla vuelve a verse, lee otra vez los datos (spec 12 · Actualización). */
    fun alVolverALaPantalla() {
        if (!yaSeMostro) {
            yaSeMostro = true
            return
        }
        viewModelScope.launch {
            repositorio.cargar()
                .onSuccess { estado = EstadoEstadisticas.Listo(it) }
                .onFailure { if (estado !is EstadoEstadisticas.Listo) estado = EstadoEstadisticas.Error(it.message ?: "No se pudo cargar tu información.") }
        }
    }

    fun cargar() {
        estado = EstadoEstadisticas.Cargando
        viewModelScope.launch {
            estado = repositorio.cargar().fold(
                onSuccess = { EstadoEstadisticas.Listo(it) },
                onFailure = { EstadoEstadisticas.Error(it.message ?: "No se pudo cargar tu información. Inténtalo de nuevo.") },
            )
        }
    }

    /** Semana, Mes o Año. (Rango se elige con [elegirRango].) */
    fun elegirPeriodo(tipo: TipoPeriodo) {
        if (tipo == TipoPeriodo.RANGO) return
        tipoPeriodo = tipo
        grupoTocado = null
    }

    /** Al confirmar el selector de fechas. Si se cancela, no se llama y queda el periodo anterior. */
    fun elegirRango(desde: LocalDate, hasta: LocalDate) {
        rango = minOf(desde, hasta) to maxOf(desde, hasta)
        tipoPeriodo = TipoPeriodo.RANGO
        grupoTocado = null
    }

    /** Tocar un grupo de la gráfica solo cambia cuál se resalta. */
    fun seleccionarGrupo(indice: Int) {
        grupoTocado = indice
    }

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

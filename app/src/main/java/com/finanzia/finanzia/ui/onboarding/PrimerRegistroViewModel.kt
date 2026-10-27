package com.finanzia.finanzia.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.DashboardRepository
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.MontoInvalidoException
import com.finanzia.finanzia.datos.MovimientoNoEncontradoException
import com.finanzia.finanzia.datos.MovimientosRepository
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.datos.SaldoInsuficienteException
import com.finanzia.finanzia.datos.nombreVisible
import com.finanzia.finanzia.validacion.Montos
import com.finanzia.finanzia.validacion.ReglaSaldo
import kotlinx.coroutines.launch
import java.math.RoundingMode

/**
 * Qué movimiento se edita (spec 10). Sus valores actuales se leen del [dashboard] y los cambios
 * se guardan con [movimientos] (un UPDATE sobre el mismo registro).
 */
class EdicionMovimiento(
    val idMovimiento: Long,
    val movimientos: MovimientosRepository,
    val dashboard: DashboardRepository,
)

/**
 * Estado común de las pantallas 4 y 5: un monto y una sola opción seleccionada.
 * [T] es la frecuencia del ingreso o la categoría del gasto.
 * Los errores se muestran en rojo dentro de la pantalla (sin Snackbar).
 */
abstract class PrimerRegistroViewModel<T>(opcionInicial: T, private val edicion: EdicionMovimiento?) : ViewModel() {

    /** Solo dígitos, sin comas ("18500"). */
    var monto by mutableStateOf("")
        private set
    var opcion by mutableStateOf(opcionInicial)
        private set
    var guardando by mutableStateOf(false)
        private set

    /** Al editar: mientras se leen los valores actuales del movimiento. */
    var cargandoEdicion by mutableStateOf(edicion != null)
        private set

    /** Monto vacío, en cero o sin saldo suficiente: va en rojo dentro de la tarjeta del monto. */
    var errorMonto by mutableStateOf<String?>(null)
        private set

    /** Errores al guardar (sin conexión, sesión terminada…), sobre el botón. */
    var errorGeneral by mutableStateOf<String?>(null)
        private set

    fun cambiarMonto(entrada: String) { monto = Montos.limpiar(entrada); errorMonto = null; errorGeneral = null }
    fun seleccionar(nueva: T) { opcion = nueva; errorGeneral = null }

    protected abstract suspend fun persistir(monto: Long, opcion: T): Result<Unit>

    /** La opción que corresponde al nombre de la categoría guardada ("Mensual", "Comida"…). */
    protected abstract fun opcionDeCategoria(nombre: String?): T?

    /** Nombre de la categoría que se guarda para la opción elegida. */
    protected abstract fun nombreDeCategoria(opcion: T): String

    /** Texto que se agrega al error de saldo insuficiente (en el onboarding pide registrar un ingreso). */
    protected open fun pistaSaldoInsuficiente(): String? = null

    init {
        if (edicion != null) cargarValoresActuales(edicion)
    }

    private fun cargarValoresActuales(edicion: EdicionMovimiento) {
        viewModelScope.launch {
            edicion.dashboard.cargar()
                .mapCatching { datos ->
                    datos.movimientos.firstOrNull { it.id == edicion.idMovimiento } ?: throw MovimientoNoEncontradoException()
                }
                .onSuccess { movimiento ->
                    // COP sin decimales: el campo solo acepta dígitos.
                    monto = Montos.limpiar(movimiento.monto.setScale(0, RoundingMode.HALF_UP).toPlainString())
                    opcionDeCategoria(movimiento.nombreCategoria)?.let { opcion = it }
                }
                .onFailure { errorGeneral = it.message ?: "No se pudo cargar el movimiento. Inténtalo de nuevo." }
            cargandoEdicion = false
        }
    }

    fun guardar(alTerminar: () -> Unit) {
        if (guardando || cargandoEdicion) return
        errorMonto = Montos.validar(monto)?.mensaje
        errorGeneral = null
        if (errorMonto != null) return
        guardando = true
        viewModelScope.launch {
            val resultado = if (edicion != null) {
                edicion.movimientos.editar(edicion.idMovimiento, monto.toLong(), nombreDeCategoria(opcion))
            } else {
                persistir(monto.toLong(), opcion)
            }
            resultado
                .onSuccess { alTerminar() }
                .onFailure { e ->
                    when (e) {
                        // La regla del saldo va en rojo en el campo del monto (spec 10).
                        is SaldoInsuficienteException ->
                            errorMonto = listOfNotNull(e.message, pistaSaldoInsuficiente()).joinToString(" ")
                        is MontoInvalidoException -> errorMonto = e.message
                        else -> errorGeneral = e.message ?: "No se pudo guardar. Inténtalo de nuevo."
                    }
                }
            guardando = false
        }
    }
}

/**
 * [desdeDashboard] = true cuando se abre desde el botón "+" del Dashboard: el movimiento se
 * llama "Ingreso", queda en la categoría de la frecuencia elegida y no se cambia la frecuencia del usuario.
 * Con [edicion] se edita un ingreso que ya existe (spec 10).
 */
class PrimerIngresoViewModel(
    private val repositorio: OnboardingRepository,
    private val desdeDashboard: Boolean = false,
    edicion: EdicionMovimiento? = null,
) : PrimerRegistroViewModel<FrecuenciaIngreso>(FrecuenciaIngreso.MENSUAL, edicion) {
    override suspend fun persistir(monto: Long, opcion: FrecuenciaIngreso) =
        if (desdeDashboard) {
            repositorio.guardarIngreso(monto, descripcion = "Ingreso", frecuencia = opcion)
        } else {
            repositorio.guardarPrimerIngreso(PrimerIngreso(monto, opcion))
        }

    override fun opcionDeCategoria(nombre: String?) = FrecuenciaIngreso.entries.firstOrNull { it.nombreVisible == nombre }

    override fun nombreDeCategoria(opcion: FrecuenciaIngreso) = opcion.nombreVisible
}

/**
 * [desdeDashboard] = true cuando se abre desde el botón "+" del Dashboard: el movimiento se llama "Gasto".
 * Con [edicion] se edita un gasto que ya existe (spec 10).
 */
class PrimerGastoViewModel(
    private val repositorio: OnboardingRepository,
    private val desdeDashboard: Boolean = false,
    private val edicion: EdicionMovimiento? = null,
) : PrimerRegistroViewModel<CategoriaGasto>(CategoriaGasto.GASTOS_FIJOS, edicion) {
    override suspend fun persistir(monto: Long, opcion: CategoriaGasto) =
        if (desdeDashboard) {
            repositorio.guardarPrimerGasto(PrimerGasto(monto, opcion, descripcion = "Gasto"))
        } else {
            repositorio.guardarPrimerGasto(PrimerGasto(monto, opcion))
        }

    /** En el onboarding, sin saldo para el primer gasto, se pide registrar primero un ingreso (spec 10). */
    override fun pistaSaldoInsuficiente(): String? =
        if (!desdeDashboard && edicion == null) ReglaSaldo.PISTA_REGISTRAR_INGRESO else null

    override fun opcionDeCategoria(nombre: String?) = CategoriaGasto.entries.firstOrNull { it.nombreVisible == nombre }

    override fun nombreDeCategoria(opcion: CategoriaGasto) = opcion.nombreVisible
}

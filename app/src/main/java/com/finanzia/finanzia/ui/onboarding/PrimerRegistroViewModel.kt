package com.finanzia.finanzia.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.validacion.Montos
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Estado común de las pantallas 4 y 5: un monto y una sola opción seleccionada.
 * [T] es la frecuencia del ingreso o la categoría del gasto.
 */
abstract class PrimerRegistroViewModel<T>(opcionInicial: T) : ViewModel() {

    /** Solo dígitos, sin comas ("18500"). */
    var monto by mutableStateOf("")
        private set
    var opcion by mutableStateOf(opcionInicial)
        private set
    var guardando by mutableStateOf(false)
        private set

    private val canalMensajes = Channel<String>(Channel.BUFFERED)
    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    fun cambiarMonto(entrada: String) { monto = Montos.limpiar(entrada) }
    fun seleccionar(nueva: T) { opcion = nueva }

    protected abstract suspend fun persistir(monto: Long, opcion: T): Result<Unit>

    fun guardar(alTerminar: () -> Unit) {
        if (guardando) return
        val error = Montos.validar(monto)
        if (error != null) {
            canalMensajes.trySend(error.mensaje)
            return
        }
        guardando = true
        viewModelScope.launch {
            persistir(monto.toLong(), opcion)
                .onSuccess { alTerminar() }
                .onFailure { canalMensajes.send(it.message ?: "No se pudo guardar. Inténtalo de nuevo.") }
            guardando = false
        }
    }
}

class PrimerIngresoViewModel(private val repositorio: OnboardingRepository) :
    PrimerRegistroViewModel<FrecuenciaIngreso>(FrecuenciaIngreso.MENSUAL) {
    override suspend fun persistir(monto: Long, opcion: FrecuenciaIngreso) =
        repositorio.guardarPrimerIngreso(PrimerIngreso(monto, opcion))
}

class PrimerGastoViewModel(private val repositorio: OnboardingRepository) :
    PrimerRegistroViewModel<CategoriaGasto>(CategoriaGasto.GASTOS_FIJOS) {
    override suspend fun persistir(monto: Long, opcion: CategoriaGasto) =
        repositorio.guardarPrimerGasto(PrimerGasto(monto, opcion))
}

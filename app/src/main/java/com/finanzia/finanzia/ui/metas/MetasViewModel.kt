package com.finanzia.finanzia.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.validacion.Montos
import kotlinx.coroutines.launch

sealed interface EstadoMetas {
    data object Cargando : EstadoMetas
    data class Error(val mensaje: String) : EstadoMetas
    data class Listo(val metas: List<Meta>) : EstadoMetas
}

/** Mensaje cuando el monto de un aporte está vacío o es 0 (mismo texto de Nueva meta). */
const val ERROR_MONTO_APORTE = "Escribe un monto mayor que 0"

/** Pantalla Metas (spec 07): carga las metas activas al entrar. */
class MetasViewModel(private val repositorio: MetasRepository) : ViewModel() {

    var estado by mutableStateOf<EstadoMetas>(EstadoMetas.Cargando)
        private set

    // --- "+ Aportar" (spec 07) ---

    /** Id de la meta a la que se está aportando (recuadro abierto en su tarjeta), o null. */
    var aportandoA by mutableStateOf<Long?>(null)
        private set
    /** Solo dígitos ("50000"); en pantalla se ve con separadores de miles. */
    var montoAporte by mutableStateOf("")
        private set
    var errorAporte by mutableStateOf<String?>(null)
        private set
    var guardandoAporte by mutableStateOf(false)
        private set

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoMetas.Cargando
        viewModelScope.launch { recargar() }
    }

    /** Al volver a la pantalla (por ejemplo, desde el detalle): recarga en silencio si ya había datos. */
    fun refrescar() {
        if (estado is EstadoMetas.Listo) viewModelScope.launch { recargar() }
    }

    /** Vuelve a leer las metas sin mostrar el estado de carga (después de aportar). */
    private suspend fun recargar() {
        estado = repositorio.cargar().fold(
            onSuccess = { metas -> EstadoMetas.Listo(metas.sortedByDescending { it.fechaCreacion }) },
            onFailure = { EstadoMetas.Error(it.message ?: "No se pudieron cargar tus metas. Inténtalo de nuevo.") },
        )
    }

    fun abrirAporte(idMeta: Long) {
        aportandoA = idMeta
        montoAporte = ""
        errorAporte = null
    }

    fun cerrarAporte() {
        if (!guardandoAporte) aportandoA = null
    }

    fun cambiarMontoAporte(entrada: String) {
        montoAporte = Montos.limpiar(entrada)
        errorAporte = null
    }

    fun guardarAporte() {
        val idMeta = aportandoA ?: return
        if (guardandoAporte) return
        if (Montos.validar(montoAporte) != null) {
            errorAporte = ERROR_MONTO_APORTE
            return
        }
        guardandoAporte = true
        viewModelScope.launch {
            repositorio.aportar(idMeta, montoAporte.toLong())
                .onSuccess {
                    aportandoA = null
                    recargar()
                }
                .onFailure { errorAporte = it.message ?: "No se pudo guardar el aporte. Inténtalo de nuevo." }
            guardandoAporte = false
        }
    }
}

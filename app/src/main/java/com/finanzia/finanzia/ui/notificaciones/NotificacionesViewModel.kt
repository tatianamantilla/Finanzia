package com.finanzia.finanzia.ui.notificaciones

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.Notificacion
import com.finanzia.finanzia.datos.NotificacionesRepository
import com.finanzia.finanzia.datos.TipoNotificacion
import kotlinx.coroutines.launch

sealed interface EstadoNotificaciones {
    data object Cargando : EstadoNotificaciones
    data class Error(val mensaje: String) : EstadoNotificaciones
    data class Listo(val notificaciones: List<Notificacion>) : EstadoNotificaciones
}

class NotificacionesViewModel(private val repositorio: NotificacionesRepository) : ViewModel() {

    var estado by mutableStateOf<EstadoNotificaciones>(EstadoNotificaciones.Cargando)
        private set

    /** Filtro seleccionado: `null` = "Todas" (seleccionado por defecto). */
    var filtro by mutableStateOf<TipoNotificacion?>(null)
        private set

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoNotificaciones.Cargando
        viewModelScope.launch {
            estado = repositorio.cargar().fold(
                onSuccess = { lista -> EstadoNotificaciones.Listo(lista.sortedByDescending { it.fecha }) },
                onFailure = { EstadoNotificaciones.Error(it.message ?: "No se pudieron cargar tus notificaciones. Inténtalo de nuevo.") },
            )
        }
    }

    fun elegirFiltro(nuevo: TipoNotificacion?) { filtro = nuevo }

    /** Todas las notificaciones (para los contadores). Vacía mientras carga o si falló. */
    val todas: List<Notificacion>
        get() = (estado as? EstadoNotificaciones.Listo)?.notificaciones.orEmpty()

    /** Las que se muestran con el filtro actual. */
    val visibles: List<Notificacion>
        get() = filtro?.let { tipo -> todas.filter { it.tipo == tipo } } ?: todas

    val sinRevisar: Int
        get() = todas.count { !it.leida }
}

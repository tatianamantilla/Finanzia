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

/** Filtro por visto, del botón de ajustes del encabezado. */
enum class FiltroLectura(val texto: String) {
    TODAS("Todas"),
    SIN_LEER("Sin leer"),
    LEIDAS("Leídas"),
    ;

    fun incluye(notificacion: Notificacion): Boolean = when (this) {
        TODAS -> true
        SIN_LEER -> !notificacion.leida
        LEIDAS -> notificacion.leida
    }
}

class NotificacionesViewModel(private val repositorio: NotificacionesRepository) : ViewModel() {

    var estado by mutableStateOf<EstadoNotificaciones>(EstadoNotificaciones.Cargando)
        private set

    /** Filtro seleccionado: `null` = "Todas" (seleccionado por defecto). */
    var filtro by mutableStateOf<TipoNotificacion?>(null)
        private set

    /** Filtro por visto (botón de ajustes): Todas por defecto. */
    var filtroLectura by mutableStateOf(FiltroLectura.TODAS)
        private set

    /** Error al marcar como leída, en rojo debajo de los filtros. */
    var errorAccion by mutableStateOf<String?>(null)
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

    fun elegirFiltroLectura(nuevo: FiltroLectura) { filtroLectura = nuevo }

    /** Al tocar una tarjeta sin leer, queda marcada como leída. */
    fun marcarLeida(notificacion: Notificacion) {
        if (!notificacion.leida) marcar(setOf(notificacion.id))
    }

    /** Doble check del encabezado: marca todas como leídas. */
    fun marcarTodasLeidas() = marcar(todas.filter { !it.leida }.map { it.id }.toSet())

    /**
     * Se marca de inmediato en pantalla y luego se guarda en Supabase.
     * Si falla, se devuelven a como estaban y se muestra el error en rojo.
     */
    private fun marcar(ids: Set<Long>) {
        val actual = estado as? EstadoNotificaciones.Listo ?: return
        if (ids.isEmpty()) return
        errorAccion = null
        estado = actual.conLeidas(ids, leida = true)
        viewModelScope.launch {
            repositorio.marcarLeidas(ids.toList()).onFailure { error ->
                (estado as? EstadoNotificaciones.Listo)?.let { estado = it.conLeidas(ids, leida = false) }
                errorAccion = error.message ?: "No se pudo marcar como leída. Inténtalo de nuevo."
            }
        }
    }

    private fun EstadoNotificaciones.Listo.conLeidas(ids: Set<Long>, leida: Boolean) =
        copy(notificaciones = notificaciones.map { if (it.id in ids) it.copy(leida = leida) else it })

    /** Todas las notificaciones (para los contadores). Vacía mientras carga o si falló. */
    val todas: List<Notificacion>
        get() = (estado as? EstadoNotificaciones.Listo)?.notificaciones.orEmpty()

    /** Las que se muestran con el filtro de tipo y el de visto. */
    val visibles: List<Notificacion>
        get() = todas.filter { (filtro == null || it.tipo == filtro) && filtroLectura.incluye(it) }

    val sinRevisar: Int
        get() = todas.count { !it.leida }
}

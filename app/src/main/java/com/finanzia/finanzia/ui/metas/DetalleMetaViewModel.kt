package com.finanzia.finanzia.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private val MESES_CORTOS = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

/** "05 Oct 2026" (fecha de un aporte o de creación de la meta). */
fun fechaCortaMeta(fecha: LocalDate): String = "%02d %s %d".format(fecha.dayOfMonth, MESES_CORTOS[fecha.monthValue - 1], fecha.year)

sealed interface EstadoDetalleMeta {
    data object Cargando : EstadoDetalleMeta
    data class Error(val mensaje: String) : EstadoDetalleMeta
    data class Listo(val meta: Meta) : EstadoDetalleMeta
}

/** Detalle de meta (spec 09): una meta con todos sus aportes. */
class DetalleMetaViewModel(
    private val repositorio: MetasRepository,
    private val idMeta: Long,
    val zona: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    var estado by mutableStateOf<EstadoDetalleMeta>(EstadoDetalleMeta.Cargando)
        private set

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoDetalleMeta.Cargando
        viewModelScope.launch { recargar() }
    }

    private suspend fun recargar() {
        estado = repositorio.cargar().fold(
            onSuccess = { metas ->
                metas.firstOrNull { it.id == idMeta }?.let { EstadoDetalleMeta.Listo(it) }
                    ?: EstadoDetalleMeta.Error("No se encontró esta meta.")
            },
            onFailure = { EstadoDetalleMeta.Error(it.message ?: "No se pudo cargar la meta. Inténtalo de nuevo.") },
        )
    }
}

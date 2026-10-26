package com.finanzia.finanzia.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AporteMeta
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.validacion.Montos
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

    // --- Editar aportes (spec 09) ---

    /** Id del aporte que se está editando (recuadro abierto debajo de su fila), o null. */
    var editando by mutableStateOf<Long?>(null)
        private set
    /** Solo dígitos; empieza con el monto actual del aporte. */
    var montoEdicion by mutableStateOf("")
        private set
    var errorEdicion by mutableStateOf<String?>(null)
        private set
    var guardandoEdicion by mutableStateOf(false)
        private set

    fun abrirEdicion(aporte: AporteMeta) {
        editando = aporte.id
        montoEdicion = aporte.monto.toLong().toString()
        errorEdicion = null
    }

    fun cerrarEdicion() {
        if (!guardandoEdicion) editando = null
    }

    fun cambiarMontoEdicion(entrada: String) {
        montoEdicion = Montos.limpiar(entrada)
        errorEdicion = null
    }

    fun guardarEdicion() {
        val idAporte = editando ?: return
        if (guardandoEdicion) return
        if (Montos.validar(montoEdicion) != null) {
            errorEdicion = ERROR_MONTO_APORTE
            return
        }
        guardandoEdicion = true
        viewModelScope.launch {
            repositorio.editarAporte(idAporte, montoEdicion.toLong())
                .onSuccess {
                    editando = null
                    recargar()
                }
                .onFailure { errorEdicion = it.message ?: "No se pudo editar el aporte. Inténtalo de nuevo." }
            guardandoEdicion = false
        }
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

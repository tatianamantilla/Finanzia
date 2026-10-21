package com.finanzia.finanzia.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import kotlinx.coroutines.launch

sealed interface EstadoMetas {
    data object Cargando : EstadoMetas
    data class Error(val mensaje: String) : EstadoMetas
    data class Listo(val metas: List<Meta>) : EstadoMetas
}

/** Pantalla Metas (spec 07): carga las metas activas al entrar. */
class MetasViewModel(private val repositorio: MetasRepository) : ViewModel() {

    var estado by mutableStateOf<EstadoMetas>(EstadoMetas.Cargando)
        private set

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoMetas.Cargando
        viewModelScope.launch {
            estado = repositorio.cargar().fold(
                onSuccess = { metas -> EstadoMetas.Listo(metas.sortedByDescending { it.fechaCreacion }) },
                onFailure = { EstadoMetas.Error(it.message ?: "No se pudieron cargar tus metas. Inténtalo de nuevo.") },
            )
        }
    }
}

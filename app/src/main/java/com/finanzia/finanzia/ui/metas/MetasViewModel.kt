package com.finanzia.finanzia.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.validacion.CalculosMetas
import com.finanzia.finanzia.validacion.ConsejoMeta
import com.finanzia.finanzia.validacion.FormatosDashboard
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/** Una tarjeta de meta con los textos ya formateados. */
data class TarjetaMetaUi(
    val nombre: String,
    /** "Junio 2025", o null si la meta no tiene fecha límite. */
    val mesLimite: String?,
    val ahorrado: String,
    val objetivo: String,
    val falta: String,
    val porcentaje: Int,
)

/** Todo lo que pinta la pantalla Metas, calculado con los datos reales del usuario. */
data class ResumenMetas(
    val ahorrado: String,
    val objetivo: String,
    /** "[X]% completado": null = no se muestra (sin metas). */
    val porcentaje: Int?,
    /** null = "Aún no hay suficiente información…". */
    val consejo: ConsejoMeta?,
    val metas: List<TarjetaMetaUi>,
)

object ResumidorMetas {
    fun resumir(metas: List<Meta>, ahora: Instant, zona: ZoneId): ResumenMetas {
        val ahorrado = CalculosMetas.totalAhorrado(metas)
        val objetivo = CalculosMetas.totalObjetivo(metas)
        return ResumenMetas(
            ahorrado = FormatosDashboard.montoSinDecimales(ahorrado),
            objetivo = FormatosDashboard.montoSinDecimales(objetivo),
            porcentaje = if (metas.isEmpty()) null else CalculosMetas.porcentaje(ahorrado, objetivo),
            consejo = CalculosMetas.consejo(metas, ahora, zona),
            metas = metas.map {
                TarjetaMetaUi(
                    nombre = it.nombre,
                    mesLimite = it.fechaLimite?.let(CalculosMetas::mesYAnio),
                    ahorrado = FormatosDashboard.montoSinDecimales(it.ahorrado),
                    objetivo = FormatosDashboard.montoSinDecimales(it.montoObjetivo),
                    falta = FormatosDashboard.montoSinDecimales(CalculosMetas.falta(it)),
                    porcentaje = CalculosMetas.porcentaje(it),
                )
            },
        )
    }
}

sealed interface EstadoMetas {
    data object Cargando : EstadoMetas
    data class Error(val mensaje: String) : EstadoMetas
    data class Listo(val resumen: ResumenMetas) : EstadoMetas
}

class MetasViewModel(
    private val repositorio: MetasRepository,
    private val ahora: () -> Instant = Instant::now,
    private val zona: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    var estado by mutableStateOf<EstadoMetas>(EstadoMetas.Cargando)
        private set

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoMetas.Cargando
        viewModelScope.launch {
            estado = repositorio.cargar().fold(
                onSuccess = { EstadoMetas.Listo(ResumidorMetas.resumir(it, ahora(), zona)) },
                onFailure = { EstadoMetas.Error(it.message ?: "No se pudieron cargar tus metas. Inténtalo de nuevo.") },
            )
        }
    }
}

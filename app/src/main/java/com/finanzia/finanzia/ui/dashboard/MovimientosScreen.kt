package com.finanzia.finanzia.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.DashboardRepository
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonAtras
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.validacion.CalculosDashboard
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

sealed interface EstadoMovimientos {
    data object Cargando : EstadoMovimientos
    data class Error(val mensaje: String) : EstadoMovimientos
    data class Listo(val filas: List<FilaMovimiento>) : EstadoMovimientos
}

/** Lista "Ver todos" (spec 10): todos los movimientos activos, del más reciente al más antiguo. */
class MovimientosViewModel(
    private val repositorio: DashboardRepository,
    private val ahora: () -> Instant = Instant::now,
    private val zona: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    var estado by mutableStateOf<EstadoMovimientos>(EstadoMovimientos.Cargando)
        private set

    private var yaSeMostro = false

    init {
        cargar()
    }

    fun cargar() {
        if (estado !is EstadoMovimientos.Listo) estado = EstadoMovimientos.Cargando
        viewModelScope.launch {
            estado = repositorio.cargar().fold(
                onSuccess = { EstadoMovimientos.Listo(filasDe(it)) },
                onFailure = { EstadoMovimientos.Error(it.message ?: "No se pudieron cargar tus movimientos. Inténtalo de nuevo.") },
            )
        }
    }

    /** Al volver de editar un movimiento, la lista se lee otra vez. */
    fun alVolverALaPantalla() {
        if (!yaSeMostro) {
            yaSeMostro = true
            return
        }
        cargar()
    }

    private fun filasDe(datos: DatosDashboard): List<FilaMovimiento> {
        val momento = ahora()
        return CalculosDashboard.recientes(datos.movimientos, cuantos = Int.MAX_VALUE)
            .map { ResumidorDashboard.filaDe(it, momento, zona) }
    }
}

/**
 * Pantalla "Movimientos" (spec 10). No tiene diseño propio: usa el encabezado con flecha atrás de
 * crear_cuenta_4.jpeg y exactamente las mismas filas y tarjeta de "Movimientos recientes".
 */
@Composable
fun MovimientosScreen(
    viewModel: MovimientosViewModel,
    onAtras: () -> Unit,
    onEditarMovimiento: (FilaMovimiento) -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.alVolverALaPantalla() }
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(Etiquetas.PANTALLA_MOVIMIENTOS)) {
        ContenidoDesplazable(margenHorizontal = 18.dp) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonAtras(onAtras)
                Spacer(Modifier.width(14.dp))
                Text("Movimientos", style = estilo(26, FontWeight.ExtraBold, altoLinea = 34, espaciado = -0.4))
            }
            Spacer(Modifier.height(20.dp))
            when (val estado = viewModel.estado) {
                EstadoMovimientos.Cargando -> Cargando()
                is EstadoMovimientos.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                // Sin movimientos, la tarjeta muestra "Aún no tienes movimientos", como en el Dashboard.
                is EstadoMovimientos.Listo -> TarjetaMovimientos(estado.filas, onEditarMovimiento)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@PreviewsFinanzia
@Composable
private fun MovimientosVacioPreview() {
    FinanziaTheme {
        MovimientosScreen(
            MovimientosViewModel(
                DashboardFijo(Result.success(DatosDashboard("Vista Previa", emptyList(), emptyList(), null))),
                ahora = { Instant.EPOCH },
            ),
            onAtras = {},
            onEditarMovimiento = {},
        )
    }
}

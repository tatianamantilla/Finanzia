package com.finanzia.finanzia.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BarraNavegacion
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.PestanaApp
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto

// Colores tomados de docs/design/notificaciones_1.png.
private val AzulPunto = Color(0xFF1D4ED8)
private val AzulIconoClaro = Color(0xFFE3ECFC)
private val AzulTarjeta = Color(0xFF2563EB)
private val VerdeFiltro = Color(0xFFBFF5DE)
private val VerdeIcono = Color(0xFF0E6B4D)

/** Nombres internos que usan los tests de Notificaciones. No se ven en pantalla. */
object EtiquetasNotificaciones {
    const val PANTALLA = "pantalla_notificaciones"
    const val TARJETA_AVISO = "tarjeta_aviso"
}

/** Clases de aviso que se mantienen (spec 05, simplificación aprobada: sin "Oportunidad de Finanzia IA"). */
enum class TipoAviso(
    val titulo: String,
    val origen: String,
    val filtro: String,
    val icono: ImageVector,
    val fondoIcono: Color,
    val colorIcono: Color,
) {
    PRESUPUESTO("Límite de presupuesto", "Presupuestos inteligentes", "Alertas de presupuesto", Icons.Filled.Restaurant, AzulIconoClaro, AzulPunto),
    VENCIMIENTO("Vencimiento de tarjeta", "Próximo vencimiento", "Vencimientos", Icons.Filled.CreditCard, AzulTarjeta, Blanco),
}

/**
 * Notificaciones (spec 05, docs/design/notificaciones_1.png).
 *
 * Supabase todavía no guarda límites de presupuesto, fechas de pago ni avisos de IA, así que
 * ningún aviso tiene datos: cada tarjeta se ve con su diseño y el texto "Aún no hay datos".
 * Nunca se muestran los ejemplos de la imagen.
 */
@Composable
fun NotificacionesScreen(
    onAtras: () -> Unit,
    onPestana: (PestanaApp) -> Unit,
    tipos: List<TipoAviso> = TipoAviso.entries,
) {
    // null = "Todas".
    var filtro by rememberSaveable { mutableStateOf<TipoAviso?>(null) }
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(EtiquetasNotificaciones.PANTALLA)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .consumeWindowInsets(WindowInsets.navigationBars),
            ) {
                ContenidoDesplazable(margenHorizontal = 20.dp) {
                    Encabezado(onAtras)
                    Spacer(Modifier.height(16.dp))
                    Filtros(tipos, filtro, onFiltro = { filtro = it })
                    Spacer(Modifier.height(20.dp))
                    SeccionHoy()
                    Spacer(Modifier.height(12.dp))
                    tipos.filter { filtro == null || it == filtro }.forEach {
                        TarjetaAviso(it)
                        Spacer(Modifier.height(14.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            // Se llega desde la campana de Inicio, así que Inicio queda seleccionada (spec 05).
            BarraNavegacion(seleccionada = PestanaApp.INICIO, onPestana = onPestana)
        }
    }
}

@Composable
private fun Encabezado(onAtras: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Blanco)
                .border(1.dp, BordeTarjeta, CircleShape)
                .clickable(role = Role.Button, onClick = onAtras),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Notificaciones", style = estilo(22, FontWeight.ExtraBold, altoLinea = 28))
            // Sin avisos reales, siempre 0 (spec 05, estado vacío).
            Text("0 alertas activas sin revisar", style = estilo(13, color = GrisTexto))
        }
        // "Marcar todo como leído": sin acción mientras no haya avisos.
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Blanco)
                .border(1.dp, BordeTarjeta, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.DoneAll, contentDescription = "Marcar todo como leído", tint = AzulMarino, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun Filtros(tipos: List<TipoAviso>, filtro: TipoAviso?, onFiltro: (TipoAviso?) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Filtro("Todas (0)", null, seleccionado = filtro == null) { onFiltro(null) }
        tipos.forEach { tipo ->
            Filtro(tipo.filtro, tipo.icono, seleccionado = filtro == tipo) { onFiltro(tipo) }
        }
    }
}

@Composable
private fun Filtro(texto: String, icono: ImageVector?, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (seleccionado) VerdeFiltro else Blanco)
            .border(1.dp, if (seleccionado) VerdeFiltro else BordeTarjeta, CircleShape)
            .selectable(selected = seleccionado, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icono?.let {
            Icon(it, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(texto, style = estilo(13, if (seleccionado) FontWeight.Bold else FontWeight.Medium, if (seleccionado) VerdeIcono else AzulMarino))
    }
}

@Composable
private fun SeccionHoy() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(AzulPunto),
        )
        Spacer(Modifier.width(8.dp))
        Text("HOY", style = estilo(13, FontWeight.Bold, espaciado = 1.0), modifier = Modifier.weight(1f))
        Text("Tiempo real", style = estilo(12, color = GrisSuave))
    }
}

@Composable
private fun TarjetaAviso(tipo: TipoAviso) {
    val forma = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .testTag(EtiquetasNotificaciones.TARJETA_AVISO)
            .shadow(3.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tipo.fondoIcono),
                contentAlignment = Alignment.Center,
            ) {
                Icon(tipo.icono, contentDescription = null, tint = tipo.colorIcono, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tipo.titulo, style = estilo(18, FontWeight.Bold, altoLinea = 24))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AzulPunto),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(tipo.origen, style = estilo(12, color = GrisTexto))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.PieChart, contentDescription = null, tint = GrisSuave, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            // TODO(Supabase): mostrar el aviso real cuando existan sus datos (spec 05, decisiones pendientes).
            Text("Aún no hay datos", style = estilo(14, FontWeight.Medium, GrisTexto))
        }
    }
}

@PreviewsFinanzia
@Composable
private fun NotificacionesPreview() {
    FinanziaTheme { NotificacionesScreen(onAtras = {}, onPestana = {}) }
}

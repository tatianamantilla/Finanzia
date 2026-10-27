package com.finanzia.finanzia.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.AporteMeta
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasSimuladas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.validacion.CalculosMetas
import com.finanzia.finanzia.validacion.FormatosDashboard
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

// Mismos colores de la pantalla Metas (docs/design/metas_1.png).
private val AzulAhorrado = Color(0xFF1D4ED8)
private val PistaProgreso = Color(0xFFE6E9F5)
private val GrisPastilla = Color(0xFFEEF1F6)
private val VerdePorcentaje = Color(0xFF0B8F5E)
private val DegradadoMeta = Brush.horizontalGradient(listOf(Color(0xFF0B3FA8), Color(0xFF10B981)))

/** Detalle de meta (spec 09): todo sobre una meta y la lista de sus aportes. */
@Composable
fun DetalleMetaScreen(viewModel: DetalleMetaViewModel, onAtras: () -> Unit) {
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(EtiquetasAportes.PANTALLA_DETALLE_META)) {
        ContenidoDesplazable(margenHorizontal = 20.dp) {
            Encabezado(onAtras)
            when (val estado = viewModel.estado) {
                EstadoDetalleMeta.Cargando -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AzulAhorrado, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                }
                is EstadoDetalleMeta.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                is EstadoDetalleMeta.Listo -> {
                    Spacer(Modifier.height(16.dp))
                    TarjetaResumen(estado.meta, viewModel.zona)
                    Spacer(Modifier.height(24.dp))
                    ListaAportes(estado.meta.aportes.sortedWith(compareByDescending<AporteMeta> { it.fecha }.thenByDescending { it.id }), viewModel)
                }
            }
            Spacer(Modifier.height(24.dp))
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
        Text("Detalle de meta", style = estilo(20, FontWeight.Bold, altoLinea = 26))
    }
}

@Composable
private fun ErrorCarga(mensaje: String, onReintentar: () -> Unit) {
    Column(Modifier.padding(top = 24.dp)) {
        MensajeError(mensaje)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onReintentar)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reintentar", style = estilo(15, FontWeight.SemiBold, AzulEnlace))
        }
    }
}

@Composable
private fun Tarjeta(contenido: @Composable () -> Unit) {
    val forma = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(3.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .padding(18.dp),
    ) { contenido() }
}

@Composable
private fun TarjetaResumen(meta: Meta, zona: ZoneId) = Tarjeta {
    val falta = CalculosMetas.falta(meta)
    val porcentaje = CalculosMetas.porcentaje(meta.ahorrado, meta.montoObjetivo)
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colorCategoriaMeta(meta.categoria)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconoCategoriaMeta(meta.categoria), contentDescription = null, tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(meta.nombre, style = estilo(19, FontWeight.Bold, altoLinea = 25), maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(
                meta.categoria?.nombre ?: "Sin categoría",
                style = estilo(12, FontWeight.Medium, GrisTexto),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrisPastilla)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
    Spacer(Modifier.height(18.dp))
    Row {
        Dato("Ahorrado", FormatosDashboard.monto(meta.ahorrado), AzulAhorrado, Modifier.weight(1f))
        Dato("Objetivo", "${FormatosDashboard.monto(meta.montoObjetivo)} COP", AzulMarino, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Faltan ${FormatosDashboard.monto(falta)} COP", style = estilo(13, color = GrisTexto), modifier = Modifier.weight(1f))
        Text("$porcentaje%", style = estilo(14, FontWeight.Bold, VerdePorcentaje))
    }
    Spacer(Modifier.height(6.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(PistaProgreso),
    ) {
        if (porcentaje > 0) {
            Box(
                Modifier
                    .fillMaxWidth(porcentaje / 100f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(DegradadoMeta),
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    Linea(Icons.Outlined.Schedule, "Fecha límite", meta.fechaLimite?.let { CalculosMetas.mesYAnio(YearMonth.from(it)) } ?: "Aún no hay datos")
    Linea(Icons.Outlined.CalendarMonth, "Creada el", fechaCortaMeta(meta.fechaCreacion.atZone(zona).toLocalDate()))
    Linea(Icons.Outlined.Savings, "Aportes hechos", meta.aportes.size.toString())
}

@Composable
private fun Dato(titulo: String, valor: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(titulo, style = estilo(12, color = GrisTexto))
        Text(valor, style = estilo(18, FontWeight.ExtraBold, color, altoLinea = 24), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Linea(icono: ImageVector, titulo: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = GrisSuave, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(titulo, style = estilo(13, color = GrisTexto), modifier = Modifier.weight(1f))
        Text(valor, style = estilo(13, FontWeight.SemiBold))
    }
}

@Composable
private fun ListaAportes(aportes: List<AporteMeta>, viewModel: DetalleMetaViewModel) {
    Text("Aportes (${aportes.size})", style = estilo(18, FontWeight.Bold))
    Spacer(Modifier.height(12.dp))
    Tarjeta {
        if (aportes.isEmpty()) {
            Text("Aún no tienes aportes en esta meta.", style = estilo(14, FontWeight.Medium, GrisTexto))
            return@Tarjeta
        }
        aportes.forEachIndexed { i, aporte ->
            if (i > 0) HorizontalDivider(color = BordeTarjeta, modifier = Modifier.padding(vertical = 10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .testTag(EtiquetasAportes.FILA_APORTE),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Savings, contentDescription = null, tint = VerdePorcentaje, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(fechaCortaMeta(aporte.fecha), style = estilo(14, color = GrisTexto), modifier = Modifier.weight(1f))
                Text("+${FormatosDashboard.monto(aporte.monto)}", style = estilo(15, FontWeight.Bold, VerdePorcentaje))
                Spacer(Modifier.width(6.dp))
                // Editar el aporte (spec 09): abre el recuadro debajo de la fila.
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Editar aporte del ${fechaCortaMeta(aporte.fecha)}",
                    tint = AzulAhorrado,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { viewModel.abrirEdicion(aporte) }
                        .testTag(EtiquetasAportes.BOTON_EDITAR_APORTE)
                        .padding(6.dp)
                        .size(20.dp),
                )
            }
            if (viewModel.editando == aporte.id) {
                Spacer(Modifier.height(10.dp))
                FormularioMonto(
                    titulo = "Editar aporte del ${fechaCortaMeta(aporte.fecha)}",
                    monto = viewModel.montoEdicion,
                    error = viewModel.errorEdicion,
                    guardando = viewModel.guardandoEdicion,
                    textoGuardar = "Guardar cambios",
                    onCambio = viewModel::cambiarMontoEdicion,
                    onGuardar = viewModel::guardarEdicion,
                    onCancelar = viewModel::cerrarEdicion,
                )
            }
        }
    }
}

@PreviewsFinanzia
@Composable
private fun DetalleMetaPreview() {
    val meta = Meta(
        id = 1,
        nombre = "Meta de vista previa",
        montoObjetivo = 1_000_000.0,
        fechaLimite = LocalDate.of(2027, 6, 30),
        fechaCreacion = Instant.parse("2026-10-14T12:00:00Z"),
        categoria = CategoriaMeta.TECNOLOGIA,
        frecuencia = FrecuenciaAporte.MENSUAL,
        aportes = listOf(AporteMeta(100_000.0, LocalDate.of(2026, 10, 15), 1), AporteMeta(150_000.0, LocalDate.of(2026, 10, 30), 2)),
    )
    FinanziaTheme {
        DetalleMetaScreen(DetalleMetaViewModel(MetasSimuladas(listOf(meta)), 1, ZoneId.of("America/Bogota")), onAtras = {})
    }
}

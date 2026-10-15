package com.finanzia.finanzia.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.Aporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasSimulado
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BarraNavegacion
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.PestanaApp
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.VerdeClaro
import com.finanzia.finanzia.ui.theme.VerdeTexto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Colores tomados de docs/design/metas_1.png.
private val AzulTitulo = Color(0xFF1D4ED8)
private val AzulBotonMeta = Color(0xFF1E3FAE)
private val AzulBordeBoton = Color(0xFF9DB8F2)
private val AzulIconoClaro = Color(0xFFE3ECFC)
private val AzulConsejo = Color(0xFFEDF3FE)
private val PistaProgreso = Color(0xFFE2E8F0)
private val VerdePastilla = Color(0xFFD5F7E7)
private val GrisRecuadro = Color(0xFFF3F5F9)
private val GrisPastilla = Color(0xFFEDF0F5)
private val DegradadoProgreso = Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF10B981)))
private val DegradadoProgresoMeta = Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF10B981)))

/** Nombres internos que usan los tests de Metas y Nueva meta. No se ven en pantalla. */
object EtiquetasMetas {
    const val PANTALLA_METAS = "pantalla_metas"
    const val BOTON_NUEVA_META = "boton_nueva_meta"
    const val TARJETA_META = "tarjeta_meta"
    const val PANTALLA_NUEVA_META = "pantalla_nueva_meta"
    const val CAMPO_NOMBRE_META = "campo_nombre_meta"
    const val CAMPO_MONTO_META = "campo_monto_meta"
    const val CAMPO_FECHA_META = "campo_fecha_meta"
    const val BOTON_GUARDAR_META = "boton_guardar_meta"
}

/** Metas (spec 07, docs/design/metas_1.png), con la simplificación aprobada. */
@Composable
fun MetasScreen(
    viewModel: MetasViewModel,
    onAtras: () -> Unit,
    onNuevaMeta: () -> Unit,
    onPestana: (PestanaApp) -> Unit,
) {
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(EtiquetasMetas.PANTALLA_METAS)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // La barra inferior ya se encarga de la barra de navegación del sistema.
                    .consumeWindowInsets(WindowInsets.navigationBars),
            ) {
                ContenidoDesplazable(margenHorizontal = 20.dp) {
                    Encabezado(onAtras, onNuevaMeta)
                    when (val estado = viewModel.estado) {
                        EstadoMetas.Cargando -> Cargando()
                        is EstadoMetas.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                        is EstadoMetas.Listo -> Contenido(estado.resumen)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            BarraNavegacion(seleccionada = PestanaApp.METAS, onPestana = onPestana)
        }
    }
}

@Composable
private fun Encabezado(onAtras: () -> Unit, onNuevaMeta: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Metas Financieras",
                    style = estilo(20, FontWeight.Bold, altoLinea = 26),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Verified, contentDescription = null, tint = VerdeTexto, modifier = Modifier.size(18.dp))
            }
            Text(
                "Tu copiloto IA proyecta tu ahorro",
                style = estilo(13, color = GrisTexto, altoLinea = 18),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier
                .clip(CircleShape)
                .border(1.5.dp, AzulBordeBoton, CircleShape)
                .clickable(role = Role.Button, onClick = onNuevaMeta)
                .testTag(EtiquetasMetas.BOTON_NUEVA_META)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = AzulTitulo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Nueva Meta", style = estilo(13, FontWeight.SemiBold, AzulTitulo))
        }
    }
}

@Composable
private fun Cargando() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AzulTitulo, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
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
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reintentar", style = estilo(15, FontWeight.SemiBold, AzulEnlace))
        }
    }
}

@Composable
private fun Contenido(resumen: ResumenMetas) {
    Spacer(Modifier.height(12.dp))
    TarjetaAhorroAcumulado(resumen)
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Flag, contentDescription = null, tint = AzulTitulo, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text("Metas Activas (${resumen.metas.size})", style = estilo(19, FontWeight.Bold))
    }
    Spacer(Modifier.height(14.dp))
    if (resumen.metas.isEmpty()) {
        Text(
            "Aún no tienes metas. Crea la primera con + Nueva Meta.",
            style = estilo(15, FontWeight.Medium, GrisTexto),
            modifier = Modifier.padding(vertical = 8.dp),
        )
    } else {
        resumen.metas.forEach {
            TarjetaMeta(it)
            Spacer(Modifier.height(16.dp))
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
private fun BarraProgreso(porcentaje: Int, degradado: Brush) {
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
                    .background(degradado),
            )
        }
    }
}

@Composable
private fun TarjetaAhorroAcumulado(resumen: ResumenMetas) = Tarjeta {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AzulIconoClaro),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Savings, contentDescription = null, tint = AzulTitulo, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("Ahorro Acumulado en Metas", style = estilo(15, FontWeight.SemiBold, GrisTexto), modifier = Modifier.weight(1f))
        resumen.porcentaje?.let {
            Spacer(Modifier.width(8.dp))
            Text(
                "$it% completado",
                style = estilo(12, FontWeight.SemiBold, VerdeTexto),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VerdePastilla)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(resumen.ahorrado) }
            withStyle(estilo(15, FontWeight.Medium, GrisSuave).toSpanStyle()) { append(" / ${resumen.objetivo} COP") }
        },
        style = estilo(28, FontWeight.ExtraBold, altoLinea = 34),
    )
    Spacer(Modifier.height(12.dp))
    BarraProgreso(resumen.porcentaje ?: 0, DegradadoProgreso)
    Spacer(Modifier.height(16.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AzulConsejo)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Blanco),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AzulTitulo, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Consejo de Finanzia IA ✦", style = estilo(14, FontWeight.Bold, AzulTitulo))
            Spacer(Modifier.height(4.dp))
            Text(textoConsejo(resumen), style = estilo(13, color = GrisTexto, altoLinea = 19))
        }
    }
}

private fun textoConsejo(resumen: ResumenMetas) = buildAnnotatedString {
    val consejo = resumen.consejo
    if (consejo == null) {
        append("Aún no hay suficiente información para darte un consejo. Registra tus aportes para empezar.")
        return@buildAnnotatedString
    }
    append("A tu ritmo actual alcanzarás la meta ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AzulMarino)) { append("\"${consejo.nombreMeta}\"") }
    append(" ${consejo.dias} ${if (consejo.dias == 1L) "día" else "días"} ${if (consejo.antes) "antes" else "después"} de lo previsto.")
}

@Composable
private fun TarjetaMeta(meta: TarjetaMetaUi) {
    Box(Modifier.testTag(EtiquetasMetas.TARJETA_META)) {
        Tarjeta {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AzulIconoClaro),
                    contentAlignment = Alignment.Center,
                ) {
                    // Ícono genérico: la categoría todavía no existe en Supabase (spec 07).
                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = AzulTitulo, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(meta.nombre, style = estilo(18, FontWeight.Bold, altoLinea = 24), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // TODO(Supabase): mostrar la categoría real cuando exista la columna (spec 08).
                        Text(
                            "Sin categoría",
                            style = estilo(12, FontWeight.Medium, GrisTexto),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GrisPastilla)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                        meta.mesLimite?.let {
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = VerdeTexto, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it, style = estilo(12, FontWeight.SemiBold, VerdeTexto))
                        }
                    }
                }
                // Menú de tres puntos sin acción por ahora (spec 07).
                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = GrisSuave, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("Ahorrado", style = estilo(12, color = GrisTexto))
                    Text(meta.ahorrado, style = estilo(22, FontWeight.ExtraBold, AzulTitulo, altoLinea = 28), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Meta", style = estilo(12, color = GrisTexto))
                    Text("${meta.objetivo} COP", style = estilo(15, FontWeight.Bold, altoLinea = 22), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Faltan ${meta.falta} COP", style = estilo(12, color = GrisTexto), modifier = Modifier.weight(1f))
                Text("${meta.porcentaje}%", style = estilo(13, FontWeight.Bold, VerdeTexto))
            }
            Spacer(Modifier.height(6.dp))
            BarraProgreso(meta.porcentaje, DegradadoProgresoMeta)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GrisRecuadro)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                // TODO(Supabase): calcular el próximo aporte cuando exista la frecuencia (spec 08).
                Text("Próximo aporte: aún no hay datos", style = estilo(13, color = GrisTexto))
            }
            Spacer(Modifier.height(14.dp))
            // "+ Aportar" sin acción por ahora (spec 07, decisión pendiente 4).
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AzulBotonMeta)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Blanco, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Aportar", style = estilo(15, FontWeight.Bold, Blanco))
            }
        }
    }
}

// --- Previews: datos de vista previa, nunca los ejemplos del diseño ---

@PreviewsFinanzia
@Composable
private fun MetasVaciaPreview() {
    FinanziaTheme {
        MetasScreen(MetasViewModel(MetasSimulado()), onAtras = {}, onNuevaMeta = {}, onPestana = {})
    }
}

@PreviewsFinanzia
@Composable
private fun MetasConDatosPreview() {
    val ahora = Instant.parse("2026-10-14T12:00:00Z")
    val meta = Meta(
        id = 1,
        nombre = "Meta de vista previa",
        montoObjetivo = 1_000_000.0,
        fechaLimite = LocalDate.of(2027, 6, 30),
        fechaCreacion = ahora,
        aportes = listOf(Aporte(100_000.0, Instant.parse("2026-09-01T12:00:00Z")), Aporte(150_000.0, ahora)),
    )
    FinanziaTheme {
        MetasScreen(
            MetasViewModel(MetasSimulado(listOf(meta)), ahora = { ahora }, zona = ZoneId.of("America/Bogota")),
            onAtras = {},
            onNuevaMeta = {},
            onPestana = {},
        )
    }
}

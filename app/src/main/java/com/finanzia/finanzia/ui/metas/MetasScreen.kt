package com.finanzia.finanzia.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzia.finanzia.datos.AporteMeta
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasSimuladas
import com.finanzia.finanzia.ui.Etiquetas
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
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.validacion.CalculosMetas
import com.finanzia.finanzia.validacion.ConsejoMetas
import com.finanzia.finanzia.validacion.FormatosDashboard
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

// Colores tomados de docs/design/metas_1.png
private val AzulBotonClaro = Color(0xFFE3EBFA)
private val VerdeInsignia = Color(0xFF10B981)
private val AzulBordeBoton = Color(0xFF2F6BF0)
private val AzulCirculo = Color(0xFF2F6BF0)
private val MentaPastilla = Color(0xFFB8F2D8)
private val VerdePastillaTexto = Color(0xFF0D7A55)
private val PistaProgreso = Color(0xFFE6E9F5)
private val DegradadoAcumulado = Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF10B981)))
private val DegradadoMeta = Brush.horizontalGradient(listOf(Color(0xFF0B3FA8), Color(0xFF10B981)))
private val FondoConsejo = Color(0xFFEEF3FC)
private val AzulIconoClaro = Color(0xFFDCE7FA)
private val GrisPastilla = Color(0xFFEEF1F6)
private val VerdeFecha = Color(0xFF0B8F5E)
private val AzulAhorrado = Color(0xFF1D4ED8)
private val VerdePorcentaje = Color(0xFF0B8F5E)
private val GrisRecuadro = Color(0xFFF1F4F9)
private val AzulBotonOscuro = Color(0xFF1E3A8A)
private val AzulProgresoCarga = Color(0xFF1D4ED8)

/** Spec 07 · Metas (docs/design/metas_1.png, con la "Simplificación aprobada"). */
@Composable
fun MetasScreen(
    viewModel: MetasViewModel,
    onAtras: () -> Unit,
    onNuevaMeta: () -> Unit,
    hoy: () -> LocalDate = { LocalDate.now() },
) {
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(Etiquetas.PANTALLA_METAS)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // La barra inferior ya se encarga de la barra de navegación del sistema.
                    .consumeWindowInsets(WindowInsets.navigationBars),
            ) {
                ContenidoDesplazable(margenHorizontal = 20.dp) {
                    Encabezado(onAtras = onAtras, onNuevaMeta = onNuevaMeta)
                    when (val estado = viewModel.estado) {
                        EstadoMetas.Cargando -> Cargando()
                        is EstadoMetas.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                        is EstadoMetas.Listo -> Contenido(estado.metas, hoy())
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            // "Inicio" vuelve al Dashboard (igual que la flecha).
            BarraNavegacion(seleccionada = PestanaApp.METAS, onInicio = onAtras)
        }
    }
}

@Composable
private fun Encabezado(onAtras: () -> Unit, onNuevaMeta: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AzulBotonClaro)
                .clickable(role = Role.Button, onClick = onAtras)
                .testTag(Etiquetas.BOTON_ATRAS),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Metas Financieras",
                    style = estilo(22, FontWeight.ExtraBold, altoLinea = 28, espaciado = -0.3),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Verified, contentDescription = null, tint = VerdeInsignia, modifier = Modifier.size(18.dp))
            }
            Text(
                "Finanzia proyecta tu ahorro",
                style = estilo(13, color = GrisTexto, altoLinea = 18),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Row(
            Modifier
                .clip(CircleShape)
                .border(1.5.dp, AzulBordeBoton, CircleShape)
                .clickable(role = Role.Button, onClick = onNuevaMeta)
                .testTag(Etiquetas.BOTON_NUEVA_META)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = AzulBordeBoton, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(5.dp))
            Text("+ Nueva Meta", style = estilo(13, FontWeight.SemiBold, AzulBordeBoton), maxLines = 1)
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
        CircularProgressIndicator(color = AzulProgresoCarga, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
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
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .testTag(Etiquetas.BOTON_REINTENTAR),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reintentar", style = estilo(15, FontWeight.SemiBold, AzulEnlace))
        }
    }
}

@Composable
private fun Contenido(metas: List<Meta>, hoy: LocalDate) {
    Spacer(Modifier.height(20.dp))
    TarjetaAhorroAcumulado(metas, hoy)
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Flag, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Metas Activas (${metas.size})", style = estilo(18, FontWeight.Bold))
    }
    if (metas.isEmpty()) {
        Text(
            "Aún no tienes metas. Crea la primera con + Nueva Meta.",
            style = estilo(15, color = GrisSuave, altoLinea = 21),
            modifier = Modifier
                .padding(top = 14.dp)
                .testTag(Etiquetas.ESTADO_VACIO_METAS),
        )
    } else {
        metas.forEach {
            Spacer(Modifier.height(14.dp))
            TarjetaMeta(it, hoy)
        }
    }
}

/** Contenedor blanco y redondeado de las tarjetas. */
@Composable
private fun Tarjeta(contenido: @Composable () -> Unit) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .padding(16.dp),
    ) {
        contenido()
    }
}

@Composable
private fun BarraProgreso(proporcion: Double, degradado: Brush, alto: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(alto.dp)
            .clip(CircleShape)
            .background(PistaProgreso),
    ) {
        Box(
            Modifier
                .fillMaxWidth(proporcion.coerceIn(0.0, 1.0).toFloat())
                .height(alto.dp)
                .clip(CircleShape)
                .background(degradado),
        )
    }
}

@Composable
private fun TarjetaAhorroAcumulado(metas: List<Meta>, hoy: LocalDate) {
    val resumen = CalculosMetas.resumen(metas)
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AzulCirculo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Savings, contentDescription = null, tint = Blanco, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Ahorro Acumulado en Metas",
                style = estilo(15, FontWeight.SemiBold, altoLinea = 20),
                modifier = Modifier.weight(1f),
            )
            // Sin metas no hay pastilla de porcentaje (spec 07).
            resumen.porcentaje?.let {
                Spacer(Modifier.width(8.dp))
                Text(
                    "$it% completado",
                    style = estilo(12, FontWeight.SemiBold, VerdePastillaTexto),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MentaPastilla)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(itemVerticalAlignment = Alignment.Bottom) {
            BasicText(
                FormatosDashboard.monto(resumen.ahorrado),
                style = estilo(30, FontWeight.ExtraBold, altoLinea = 36, espaciado = -0.5),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = 30.sp, stepSize = 1.sp),
                modifier = Modifier.testTag(Etiquetas.MONTO_AHORRO_METAS),
            )
            Text(
                " / ${FormatosDashboard.monto(resumen.objetivo)} COP",
                style = estilo(14, color = GrisTexto),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        BarraProgreso(
            proporcion = if (resumen.objetivo > 0) resumen.ahorrado / resumen.objetivo else 0.0,
            degradado = DegradadoAcumulado,
            alto = 10,
        )
        // Sin metas no hay consejo (spec 07).
        if (metas.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            RecuadroConsejo(CalculosMetas.consejo(metas, hoy))
        }
    }
}

@Composable
private fun RecuadroConsejo(consejo: ConsejoMetas?) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FondoConsejo)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(AzulIconoClaro),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Consejo de Finanzia IA ✦", style = estilo(14, FontWeight.SemiBold, AzulEnlace))
            Spacer(Modifier.height(2.dp))
            Text(textoConsejo(consejo), style = estilo(13, color = GrisTexto, altoLinea = 18))
        }
    }
}

/** Consejo con el ritmo real de aportes (spec 07), o el texto de falta de información. */
fun textoConsejo(consejo: ConsejoMetas?) = buildAnnotatedString {
    if (consejo == null) {
        append("Aún no hay suficiente información para darte un consejo. Registra tus aportes para empezar.")
        return@buildAnnotatedString
    }
    append("A tu ritmo actual alcanzarás la meta ")
    withStyle(SpanStyle(color = AzulMarino, fontWeight = FontWeight.Bold)) { append("\"${consejo.nombreMeta}\"") }
    when {
        consejo.dias == 0L -> append(" justo en la fecha prevista.")
        else -> {
            val dias = if (consejo.dias == 1L) "1 día" else "${consejo.dias} días"
            append(" $dias ${if (consejo.antes) "antes" else "después"} de lo previsto.")
        }
    }
}

@Composable
private fun TarjetaMeta(meta: Meta, hoy: LocalDate) {
    val falta = CalculosMetas.falta(meta)
    val porcentaje = CalculosMetas.porcentaje(meta.ahorrado, meta.montoObjetivo)
    Box(Modifier.testTag(Etiquetas.TARJETA_META)) {
        Tarjeta {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AzulIconoClaro),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        iconoCategoriaMeta(meta.categoria),
                        contentDescription = null,
                        tint = colorCategoriaMeta(meta.categoria),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        meta.nombre,
                        style = estilo(20, FontWeight.Bold, altoLinea = 26),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            meta.categoria?.nombre ?: "Sin categoría",
                            style = estilo(12, FontWeight.Medium, GrisTexto),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GrisPastilla)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                        meta.fechaLimite?.let { fecha ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = VerdeFecha, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    CalculosMetas.mesYAnio(YearMonth.from(fecha)),
                                    style = estilo(12, FontWeight.SemiBold, VerdeFecha),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(6.dp))
                // Menú de tres puntos: sin acción por ahora (spec 07).
                Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones", tint = AzulMarino, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("Ahorrado", style = estilo(13, color = GrisTexto))
                    BasicText(
                        FormatosDashboard.monto(meta.ahorrado),
                        style = estilo(24, FontWeight.ExtraBold, AzulAhorrado, altoLinea = 30),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 24.sp, stepSize = 1.sp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Meta", style = estilo(13, color = GrisTexto))
                    BasicText(
                        "${FormatosDashboard.monto(meta.montoObjetivo)} COP",
                        style = estilo(17, FontWeight.Bold, altoLinea = 24),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 17.sp, stepSize = 1.sp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Faltan ${FormatosDashboard.monto(falta)} COP",
                    style = estilo(12, color = GrisTexto),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text("$porcentaje%", style = estilo(13, FontWeight.Bold, VerdePorcentaje))
            }
            Spacer(Modifier.height(6.dp))
            BarraProgreso(
                proporcion = if (meta.montoObjetivo > 0) meta.ahorrado / meta.montoObjetivo else 0.0,
                degradado = DegradadoMeta,
                alto = 8,
            )
            Spacer(Modifier.height(14.dp))
            RecuadroProximoAporte(meta, hoy)
            Spacer(Modifier.height(12.dp))
            // "+ Aportar": sin acción por ahora (spec 07).
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzulBotonOscuro)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Blanco, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aportar", style = estilo(15, FontWeight.Bold, Blanco))
            }
        }
    }
}

@Composable
private fun RecuadroProximoAporte(meta: Meta, hoy: LocalDate) {
    val proximo = CalculosMetas.proximoAporte(meta, hoy)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GrisRecuadro)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        val texto = when {
            proximo != null -> buildAnnotatedString {
                append("Próximo aporte: ")
                withStyle(SpanStyle(color = AzulMarino, fontWeight = FontWeight.Bold)) {
                    append("${FormatosDashboard.monto(proximo.monto)} COP")
                }
                append(" ${proximo.frecuencia.texto} (${CalculosMetas.diaYMesCorto(proximo.fecha)})")
            }
            meta.frecuencia != null && meta.fechaLimite != null && CalculosMetas.falta(meta) <= 0.0 ->
                buildAnnotatedString { append("Próximo aporte: meta completada") }
            else -> buildAnnotatedString {
                withStyle(SpanStyle(color = GrisSuave)) { append("Próximo aporte: aún no hay datos") }
            }
        }
        Text(texto, style = estilo(12, color = GrisTexto, altoLinea = 16), maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// --- Previews: datos de vista previa, nunca los ejemplos del diseño ---

private val hoyVistaPrevia: LocalDate = LocalDate.of(2026, 1, 15)

private val metasVistaPrevia = listOf(
    Meta(
        id = 1, nombre = "Meta de vista previa", montoObjetivo = 1_000.0,
        fechaLimite = LocalDate.of(2026, 6, 30), fechaCreacion = Instant.parse("2026-01-10T12:00:00Z"),
        categoria = CategoriaMeta.VIAJES_OCIO, frecuencia = FrecuenciaAporte.QUINCENAL,
        aportes = listOf(AporteMeta(200.0, LocalDate.of(2025, 12, 15)), AporteMeta(300.0, LocalDate.of(2025, 12, 30))),
    ),
    Meta(
        id = 2, nombre = "Otra meta de vista previa", montoObjetivo = 500.0,
        fechaLimite = null, fechaCreacion = Instant.parse("2026-01-01T12:00:00Z"),
        categoria = null, frecuencia = null, aportes = emptyList(),
    ),
)

@PreviewsFinanzia
@Composable
private fun MetasVacioPreview() {
    FinanziaTheme {
        MetasScreen(viewModel { MetasViewModel(MetasSimuladas()) }, onAtras = {}, onNuevaMeta = {}, hoy = { hoyVistaPrevia })
    }
}

@PreviewsFinanzia
@Composable
private fun MetasConDatosPreview() {
    FinanziaTheme {
        MetasScreen(
            viewModel { MetasViewModel(MetasSimuladas(metasVistaPrevia)) },
            onAtras = {},
            onNuevaMeta = {},
            hoy = { hoyVistaPrevia },
        )
    }
}

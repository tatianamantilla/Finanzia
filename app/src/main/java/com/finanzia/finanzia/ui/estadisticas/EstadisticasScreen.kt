package com.finanzia.finanzia.ui.estadisticas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzia.finanzia.datos.CUENTA_PRINCIPAL
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BarraNavegacion
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.PestanaApp
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.dashboard.Cargando
import com.finanzia.finanzia.ui.dashboard.EncabezadoInicio
import com.finanzia.finanzia.ui.dashboard.ErrorCarga
import com.finanzia.finanzia.ui.theme.AzulClaro
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.RojoTexto
import com.finanzia.finanzia.validacion.FormatosEstadisticas
import com.finanzia.finanzia.validacion.GrupoGrafica
import com.finanzia.finanzia.validacion.ResumenEstadisticas
import com.finanzia.finanzia.validacion.SaludFinanciera
import com.finanzia.finanzia.validacion.TipoPeriodo
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// Colores tomados de docs/design/estadisticas_1.jpeg
private val MoradoDestellos = Color(0xFF7C3AED)
private val FondoSelectorMoneda = Color(0xFFDCE6F5)
private val VerdePunto = Color(0xFF10B981)
private val FondoPeriodos = Color(0xFFE6ECF5)
private val AzulActivo = Color(0xFF1D4ED8)
private val GrisPeriodo = Color(0xFF3A4255)
private val MentaPastilla = Color(0xFF8EF3CF)
private val VerdePastillaTexto = Color(0xFF0D7A55)
private val MentaHalo = Color(0xFFE3FAF0)
private val VerdeDinero = Color(0xFF0B8F5E)
private val FondoRecuadro = Color(0xFFF1F5FB)
private val MentaIngresos = Color(0xFF8EF3CF)
private val VerdeOscuro = Color(0xFF0B6B4F)
private val RosaGastos = Color(0xFFFFD6D6)
private val RojoFlecha = Color(0xFFDC2626)
private val FondoPromedio = Color(0xFFE3EAF5)
private val AzulGastos = Color(0xFF2563EB)
private val GuiaPunteada = Color(0xFFE2E8F0)
private val LineaBase = Color(0xFFCBD5E1)
private val FondoDiagnostico = Color(0xFFE5EDFB)
private val BordeDiagnostico = Color(0xFFD5E1F6)
private val VerdeInsignia = Color(0xFF34D399)

private val DegradadoBarraIngresos = Brush.verticalGradient(listOf(Color(0xFF2FB27F), Color(0xFFA7EACB)))
private val DegradadoBarraGastos = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF5B86E5)))
private val DegradadoSugerencia = Brush.horizontalGradient(listOf(Color(0xFF0B6B4F), Color(0xFF1D5FE0)))

/** Spec 11 · Estadísticas (pestaña Gráficos, docs/design/estadisticas_1.jpeg). */
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel,
    /** Pestaña Inicio: vuelve al Dashboard. */
    onInicio: () -> Unit = {},
    /** Pestaña Metas (spec 07). */
    onMetas: () -> Unit = {},
    /** Campana: igual que en el Dashboard, abre Notificaciones (spec 05). */
    onNotificaciones: () -> Unit = {},
    onSesionCerrada: () -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.alVolverALaPantalla() }
    var selectorRangoAbierto by remember { mutableStateOf(false) }
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(Etiquetas.PANTALLA_ESTADISTICAS)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // La barra inferior ya se encarga de la barra de navegación del sistema.
                    .consumeWindowInsets(WindowInsets.navigationBars),
            ) {
                ContenidoDesplazable(margenHorizontal = 20.dp) {
                    EncabezadoInicio(
                        cerrandoSesion = viewModel.cerrandoSesion,
                        onCerrarSesion = { viewModel.cerrarSesion(onSesionCerrada) },
                        onNotificaciones = onNotificaciones,
                    )
                    viewModel.errorCerrarSesion?.let { MensajeError(it, Modifier.padding(top = 8.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Titulo()
                    Spacer(Modifier.height(18.dp))
                    SelectorPeriodo(
                        seleccionado = viewModel.tipoPeriodo,
                        hoy = viewModel.hoy,
                        onElegir = { tipo ->
                            if (tipo == TipoPeriodo.RANGO) selectorRangoAbierto = true else viewModel.elegirPeriodo(tipo)
                        },
                    )
                    when (val estado = viewModel.estado) {
                        EstadoEstadisticas.Cargando -> Cargando()
                        is EstadoEstadisticas.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                        is EstadoEstadisticas.Listo -> viewModel.resumen?.let { resumen ->
                            Contenido(
                                resumen = resumen,
                                seleccionado = viewModel.grupoTocado ?: resumen.grupoPorDefecto,
                                onGrupo = viewModel::seleccionarGrupo,
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            // Excepción aprobada (spec 11): aquí la pestaña resaltada es Gráficos.
            BarraNavegacion(seleccionada = PestanaApp.GRAFICOS, onInicio = onInicio, onMetas = onMetas)
        }
    }
    if (selectorRangoAbierto) {
        SelectorRango(
            inicial = viewModel.rango,
            onCancelar = { selectorRangoAbierto = false },
            onConfirmar = { desde, hasta ->
                selectorRangoAbierto = false
                viewModel.elegirRango(desde, hasta)
            },
        )
    }
}

@Composable
private fun Titulo() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estadísticas", style = estilo(30, FontWeight.ExtraBold, altoLinea = 38, espaciado = -0.5), modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.AutoGraph, contentDescription = null, tint = MoradoDestellos, modifier = Modifier.size(26.dp))
            }
            Text("Visión global de tus flujos de dinero", style = estilo(16, color = GrisTexto, altoLinea = 22))
        }
        Spacer(Modifier.width(12.dp))
        // La app trabaja solo en pesos colombianos: se ve como en el diseño y no tiene acción (spec 11).
        Row(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(FondoSelectorMoneda)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(VerdePunto))
            Spacer(Modifier.width(8.dp))
            Text("COP ($)", style = estilo(15, FontWeight.Medium))
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = AzulMarino, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SelectorPeriodo(seleccionado: TipoPeriodo, hoy: LocalDate, onElegir: (TipoPeriodo) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(FondoPeriodos)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OpcionPeriodo("Semana", TipoPeriodo.SEMANA, seleccionado, 1f, onElegir)
        OpcionPeriodo(FormatosEstadisticas.opcionMes(hoy), TipoPeriodo.MES, seleccionado, 1.7f, onElegir)
        OpcionPeriodo("Año", TipoPeriodo.ANIO, seleccionado, 0.8f, onElegir)
        OpcionPeriodo("Rango", TipoPeriodo.RANGO, seleccionado, 1f, onElegir)
    }
}

@Composable
private fun RowScope.OpcionPeriodo(
    texto: String,
    tipo: TipoPeriodo,
    seleccionado: TipoPeriodo,
    peso: Float,
    onElegir: (TipoPeriodo) -> Unit,
) {
    val activo = tipo == seleccionado
    val forma = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .weight(peso)
            .then(if (activo) Modifier.shadow(2.dp, forma).clip(forma).background(Blanco) else Modifier.clip(forma))
            .clickable(role = Role.Tab) { onElegir(tipo) }
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag(Etiquetas.OPCION_PERIODO + tipo.name),
        contentAlignment = Alignment.Center,
    ) {
        // Con pantallas angostas o letra grande, el texto se reduce lo justo para caber en una línea.
        BasicText(
            texto,
            style = estilo(15, if (activo) FontWeight.Bold else FontWeight.Normal, if (activo) AzulActivo else GrisPeriodo, alineacion = TextAlign.Center),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 15.sp, stepSize = 0.5.sp),
        )
    }
}

@Composable
private fun ColumnScope.Contenido(resumen: ResumenEstadisticas, seleccionado: Int, onGrupo: (Int) -> Unit) {
    Spacer(Modifier.height(20.dp))
    TarjetaFlujoNeto(resumen)
    Spacer(Modifier.height(20.dp))
    TarjetaEvolucion(resumen, seleccionado, onGrupo)
    Spacer(Modifier.height(20.dp))
    TarjetaDiagnostico(resumen)
}

@Composable
private fun TarjetaFlujoNeto(resumen: ResumenEstadisticas) {
    val forma = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            // Halo verde suave en la esquina superior derecha, como en el diseño.
            .drawBehind { drawCircle(MentaHalo, radius = 72.dp.toPx(), center = Offset(size.width - 12.dp.toPx(), 18.dp.toPx())) }
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Flujo Neto Acumulado", style = estilo(15, color = AzulMarino), modifier = Modifier.weight(1f))
            resumen.variacionNeto?.let { variacion ->
                val baja = variacion.signum() < 0
                Row(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (baja) RosaGastos else MentaPastilla)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag(Etiquetas.PASTILLA_VARIACION_NETO),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val color = if (baja) RojoFlecha else VerdePastillaTexto
                    Icon(
                        if (baja) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(FormatosEstadisticas.variacion(variacion), style = estilo(14, FontWeight.SemiBold, color))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            BasicText(
                FormatosEstadisticas.montoConSigno(resumen.neto),
                style = estilo(36, FontWeight.ExtraBold, altoLinea = 44, espaciado = -0.5),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = 36.sp, stepSize = 1.sp),
                modifier = Modifier.weight(1f, fill = false).testTag(Etiquetas.MONTO_FLUJO_NETO),
            )
            Spacer(Modifier.width(6.dp))
            Text("COP", style = estilo(16, color = GrisTexto), modifier = Modifier.padding(bottom = 6.dp))
        }
        resumen.salud?.let { salud ->
            Spacer(Modifier.height(12.dp))
            val negativa = salud == SaludFinanciera.NEGATIVA
            val color = if (negativa) RojoTexto else VerdeDinero
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (negativa) Icons.Outlined.WarningAmber else Icons.Outlined.Verified, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    when (salud) {
                        SaludFinanciera.EXCELENTE -> "Salud financiera excelente este periodo"
                        SaludFinanciera.ESTABLE -> "Salud financiera estable este periodo"
                        SaludFinanciera.NEGATIVA -> "Tus gastos superan tus ingresos este periodo"
                    },
                    style = estilo(15, FontWeight.Medium, color, altoLinea = 20),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RecuadroMonto(
                titulo = "Ingresos",
                monto = FormatosEstadisticas.monto(resumen.ingresos),
                icono = Icons.Filled.ArrowDownward,
                fondoIcono = MentaIngresos,
                colorIcono = VerdeOscuro,
                etiqueta = Etiquetas.MONTO_INGRESOS_ESTADISTICAS,
                pie = resumen.variacionIngresos?.let { variacion ->
                    "${FormatosEstadisticas.variacion(variacion)} ${FormatosEstadisticas.versus(resumen.anterior)}" to
                        if (variacion.signum() < 0) RojoTexto else VerdeDinero
                },
            )
            RecuadroMonto(
                titulo = "Gastos",
                monto = FormatosEstadisticas.monto(resumen.gastos),
                icono = Icons.Filled.ArrowUpward,
                fondoIcono = RosaGastos,
                colorIcono = RojoFlecha,
                etiqueta = Etiquetas.MONTO_GASTOS_ESTADISTICAS,
                pie = resumen.porcentajeDeIngreso?.let { "${FormatosEstadisticas.porcentaje(it)} de ingreso" to GrisTexto },
            )
        }
    }
}

@Composable
private fun RowScope.RecuadroMonto(
    titulo: String,
    monto: String,
    icono: ImageVector,
    fondoIcono: Color,
    colorIcono: Color,
    etiqueta: String,
    pie: Pair<String, Color>?,
) {
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(FondoRecuadro)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(fondoIcono),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(titulo, style = estilo(15), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        BasicText(
            monto,
            style = estilo(21, FontWeight.Bold, altoLinea = 28),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 21.sp, stepSize = 0.5.sp),
            modifier = Modifier.testTag(etiqueta),
        )
        pie?.let { (texto, color) ->
            Spacer(Modifier.height(4.dp))
            Text(texto, style = estilo(13, FontWeight.Medium, color, altoLinea = 18))
        }
    }
}

@Composable
private fun TarjetaEvolucion(resumen: ResumenEstadisticas, seleccionado: Int, onGrupo: (Int) -> Unit) {
    val forma = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(FormatosEstadisticas.tituloGrafica(resumen.agrupacion), style = estilo(22, FontWeight.SemiBold, altoLinea = 28))
                Text(FormatosEstadisticas.subtituloGrafica(resumen.periodo), style = estilo(13, color = GrisTexto, altoLinea = 18))
            }
            resumen.promedioDiario?.let { promedio ->
                Spacer(Modifier.width(8.dp))
                Text(
                    buildAnnotatedString {
                        append("Prom. ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(FormatosEstadisticas.monto(promedio)) }
                        append("/día")
                    },
                    style = estilo(13, color = AzulMarino),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FondoPromedio)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag(Etiquetas.PASTILLA_PROMEDIO),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Leyenda("Ingresos", VerdeOscuro)
            Spacer(Modifier.width(18.dp))
            Leyenda("Gastos", AzulGastos)
        }
        Spacer(Modifier.height(16.dp))
        if (!resumen.hayMovimientos) {
            Text(
                "Aún no tienes movimientos en este periodo",
                style = estilo(14, color = GrisSuave, alineacion = TextAlign.Center),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
            )
        } else {
            Grafica(resumen.grupos, seleccionado, onGrupo)
        }
        // Sin la nota "*Semana 4 incluye proyección estimada": no hay proyecciones (spec 11).
    }
}

@Composable
private fun Leyenda(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = estilo(15))
    }
}

/** Barras dobles por grupo, proporcionales al valor más alto; líneas guía punteadas detrás. */
@Composable
private fun Grafica(grupos: List<GrupoGrafica>, seleccionado: Int, onGrupo: (Int) -> Unit) {
    val maximo = grupos.filterNot { it.futuro }.flatMap { listOf(it.ingresos, it.gastos) }.maxOrNull()?.takeIf { it.signum() > 0 }
    // Con muchos grupos (rangos largos) solo se escribe una etiqueta de cada tantas, y siempre la seleccionada.
    val cadaCuantas = ((grupos.size + 7) / 8).coerceAtLeast(1)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val anchoGrupo = maxWidth / grupos.size.coerceAtLeast(1)
        val anchoBarra = minOf(18.dp, anchoGrupo * 0.3f)
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.2f),
            ) {
                Canvas(Modifier.matchParentSize().padding(top = 14.dp)) {
                    val guia = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
                    for (i in 0 until 3) {
                        val y = size.height * i / 3f
                        drawLine(GuiaPunteada, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = guia)
                    }
                    drawLine(LineaBase, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                }
                Row(Modifier.matchParentSize()) {
                    grupos.forEachIndexed { indice, grupo ->
                        GrupoBarras(
                            grupo = grupo,
                            maximo = maximo,
                            anchoBarra = anchoBarra,
                            seleccionado = indice == seleccionado,
                            onClick = { onGrupo(indice) },
                            modifier = Modifier.weight(1f).fillMaxHeight().testTag(Etiquetas.GRUPO_GRAFICA + indice),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                grupos.forEachIndexed { indice, grupo ->
                    val esSeleccionado = indice == seleccionado
                    val visible = esSeleccionado || indice % cadaCuantas == 0
                    Box(
                        Modifier
                            .weight(1f)
                            .clickable(role = Role.Button) { onGrupo(indice) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (visible) {
                            BasicText(
                                grupo.etiqueta,
                                style = estilo(
                                    12,
                                    if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                    when {
                                        esSeleccionado -> AzulActivo
                                        grupo.futuro -> GrisSuave.copy(alpha = 0.6f)
                                        else -> GrisTexto
                                    },
                                    alineacion = TextAlign.Center,
                                ),
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 12.sp, stepSize = 0.5.sp),
                                modifier = if (esSeleccionado) Modifier.testTag(Etiquetas.ETIQUETA_GRUPO_SELECCIONADO) else Modifier,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrupoBarras(
    grupo: GrupoGrafica,
    maximo: BigDecimal?,
    anchoBarra: Dp,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier.clickable(role = Role.Button, onClick = onClick)) {
        val espacioPunto = 14.dp
        val altoUtil = maxHeight - espacioPunto
        // Solo para dibujar (alto en pantalla): los montos ya se calcularon exactos.
        fun alto(valor: BigDecimal): Dp {
            if (grupo.futuro || maximo == null || valor.signum() <= 0) return 0.dp
            val proporcion = valor.divide(maximo, 4, RoundingMode.HALF_UP).toFloat()
            // Un valor mayor que cero siempre tiene una barra visible.
            return maxOf(altoUtil * proporcion, 3.dp)
        }
        val altoIngresos = alto(grupo.ingresos)
        val altoGastos = alto(grupo.gastos)
        Row(
            Modifier.align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(anchoBarra * 0.4f),
            verticalAlignment = Alignment.Bottom,
        ) {
            Barra(altoIngresos, anchoBarra, DegradadoBarraIngresos)
            Barra(altoGastos, anchoBarra, DegradadoBarraGastos)
        }
        if (seleccionado) {
            val tope = maxOf(altoIngresos, altoGastos)
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -(tope + 6.dp))
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AzulGastos),
            )
        }
    }
}

@Composable
private fun Barra(alto: Dp, ancho: Dp, fondo: Brush) {
    if (alto <= 0.dp) {
        Spacer(Modifier.width(ancho))
        return
    }
    Box(
        Modifier
            .width(ancho)
            .height(alto)
            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
            .background(fondo),
    )
}

@Composable
private fun TarjetaDiagnostico(resumen: ResumenEstadisticas) {
    val forma = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(FondoDiagnostico)
            .border(1.dp, BordeDiagnostico, forma)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AzulGastos),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Blanco, modifier = Modifier.size(28.dp))
                }
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Blanco)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(VerdeInsignia),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Diagnóstico de tu Copiloto IA", style = estilo(18, FontWeight.Bold, altoLinea = 24))
                Text("Análisis conductual activo", style = estilo(13, FontWeight.Medium, AzulActivo, altoLinea = 18))
            }
            // Sin la pastilla "IA 98%": no hay un valor real detrás (spec 11).
        }
        Spacer(Modifier.height(16.dp))
        Text(textoDiagnostico(resumen), style = estilo(16, color = AzulMarino, altoLinea = 25))
        // La recomendación solo aparece con datos para generarla (meta activa y flujo neto positivo).
        if (resumen.mayorGasto != null) resumen.recomendacion?.let { recomendacion ->
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Blanco)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag(Etiquetas.RECOMENDACION),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoFixHigh, contentDescription = null, tint = VerdeOscuro, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Recomendación sugerida:", style = estilo(15, FontWeight.Bold, VerdeOscuro))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    buildAnnotatedString {
                        val negrita = SpanStyle(fontWeight = FontWeight.Bold)
                        append("Tu flujo neto de este periodo es ")
                        withStyle(negrita) { append("${FormatosEstadisticas.monto(recomendacion.neto)} COP") }
                        append(". Podrías destinar parte a tu meta ")
                        withStyle(negrita) { append("\"${recomendacion.nombreMeta}\"") }
                        append(": te faltan ")
                        withStyle(negrita) { append("${FormatosEstadisticas.monto(recomendacion.faltante)} COP") }
                        append(" para completarla.")
                    },
                    style = estilo(16, color = AzulMarino, altoLinea = 25),
                )
                Spacer(Modifier.height(16.dp))
                BotonSugerencia()
            }
        }
    }
}

/** Diagnóstico con datos reales, sin inteligencia artificial por ahora (spec 11). */
private fun textoDiagnostico(resumen: ResumenEstadisticas) = buildAnnotatedString {
    val mayor = resumen.mayorGasto
    if (mayor == null) {
        append("Aún no hay suficiente información para generar tu diagnóstico. Registra tus ingresos y gastos para empezar.")
        return@buildAnnotatedString
    }
    val negrita = SpanStyle(fontWeight = FontWeight.Bold)
    append("Tu mayor concentración de gasto está en ")
    withStyle(negrita) { append("${mayor.categoria} (${FormatosEstadisticas.porcentaje(mayor.porcentaje)})") }
    append(".")
    resumen.reduccion?.let { reduccion ->
        append(" Redujiste un ")
        withStyle(negrita) { append(FormatosEstadisticas.porcentaje(reduccion.porcentaje)) }
        append(" tu gasto en ${reduccion.categoria} comparado con ${FormatosEstadisticas.comparadoCon(resumen.anterior)}.")
    }
}

@Composable
private fun BotonSugerencia() {
    val forma = RoundedCornerShape(14.dp)
    // Se ve como en el diseño, pero no tiene acción por ahora (spec 11, decisión pendiente 2).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .shadow(8.dp, forma, ambientColor = Color(0x400B6B4F), spotColor = Color(0x400B6B4F))
            .clip(forma)
            .background(DegradadoSugerencia)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag(Etiquetas.BOTON_APLICAR_SUGERENCIA),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("Aplicar sugerencia IA", style = estilo(17, FontWeight.Bold, Blanco), modifier = Modifier.weight(1f, fill = false))
        Spacer(Modifier.width(12.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Blanco, modifier = Modifier.size(22.dp))
    }
}

/** Excepción aprobada (spec 11): selector de rango de fechas de Material 3, con los colores de la app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorRango(
    inicial: Pair<LocalDate, LocalDate>?,
    onCancelar: () -> Unit,
    onConfirmar: (LocalDate, LocalDate) -> Unit,
) {
    // El selector trabaja con fechas en UTC (medianoche): así el día elegido no se corre por la zona horaria.
    fun aMilis(fecha: LocalDate) = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    fun aFecha(milis: Long) = Instant.ofEpochMilli(milis).atZone(ZoneOffset.UTC).toLocalDate()
    val estado = rememberDateRangePickerState(
        initialSelectedStartDateMillis = inicial?.first?.let(::aMilis),
        initialSelectedEndDateMillis = inicial?.second?.let(::aMilis),
    )
    val colores = DatePickerDefaults.colors(
        containerColor = Blanco,
        selectedDayContainerColor = AzulActivo,
        todayDateBorderColor = AzulActivo,
        todayContentColor = AzulActivo,
        dayInSelectionRangeContainerColor = AzulClaro,
    )
    val desde = estado.selectedStartDateMillis
    val hasta = estado.selectedEndDateMillis
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = { if (desde != null && hasta != null) onConfirmar(aFecha(desde), aFecha(hasta)) },
                enabled = desde != null && hasta != null,
            ) { Text("Aceptar", style = estilo(15, FontWeight.SemiBold, AzulEnlace)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", style = estilo(15, FontWeight.SemiBold, AzulEnlace)) }
        },
        colors = colores,
    ) {
        DateRangePicker(
            state = estado,
            colors = colores,
            title = { Text("Rango de fechas", style = estilo(15, FontWeight.SemiBold, GrisTexto), modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
            modifier = Modifier.weight(1f),
        )
    }
}

// --- Previews: datos de vista previa, nunca los ejemplos del diseño ---

private val datosVistaPrevia = DatosDashboard(
    nombreUsuario = "Vista Previa",
    cuentas = listOf(CuentaDashboard(CUENTA_PRINCIPAL, BigDecimal.ZERO)),
    movimientos = emptyList(),
    meta = null,
)

@PreviewsFinanzia
@Composable
private fun EstadisticasVaciaPreview() {
    FinanziaTheme {
        EstadisticasScreen(
            viewModel { EstadisticasViewModel(DashboardFijo(Result.success(datosVistaPrevia)), FakeAuthRepository(), ahora = { Instant.EPOCH }) },
            onSesionCerrada = {},
        )
    }
}

package com.finanzia.finanzia.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.MetasSimuladas
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.MilesVisualTransformation
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.esCompacto
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeCampo
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.ErrorRojo
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.VerdeTexto
import com.finanzia.finanzia.validacion.CalculosMetas
import com.finanzia.finanzia.validacion.Montos
import java.time.LocalDate
import java.time.YearMonth

// Colores tomados de docs/design/nueva_meta_1.png
private val AzulBotonClaro = Color(0xFFE3EBFA)
private val FondoIntro = Color(0xFFEEF2F8)
private val MentaPastilla = Color(0xFFCDF3E2)
private val VerdePastillaTexto = Color(0xFF0B6B4F)
private val MentaSeleccion = Color(0xFF6EE7B7)
private val AzulDolar = Color(0xFF1D4ED8)
private val GrisPastilla = Color(0xFFEEF1F6)
private val FondoCampoFecha = Color(0xFFEEF2FA)
private val AzulIconoClaro = Color(0xFFDCE7FA)
private val AzulBotonOscuro = Color(0xFF1E3A8A)
/** BotonPrincipal recibe un degradado: aquí es de un solo color (estilo de "+ Aportar", spec 08). */
private val FondoGuardar = Brush.horizontalGradient(listOf(AzulBotonOscuro, AzulBotonOscuro))

/** Spec 08 · Nueva meta (docs/design/nueva_meta_1.png, con la "Simplificación aprobada"). */
@Composable
fun NuevaMetaScreen(
    viewModel: NuevaMetaViewModel,
    onAtras: () -> Unit,
    onGuardada: () -> Unit,
) {
    var selectorAbierto by remember { mutableStateOf(false) }
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(Etiquetas.PANTALLA_NUEVA_META)) {
        ContenidoDesplazable(margenHorizontal = 20.dp) {
            Encabezado(onAtras)
            Spacer(Modifier.height(18.dp))
            TarjetaIntroduccion()
            Spacer(Modifier.height(22.dp))
            CampoNombre(viewModel)
            Spacer(Modifier.height(22.dp))
            Categorias(viewModel)
            Spacer(Modifier.height(22.dp))
            TarjetaMonto(viewModel)
            Spacer(Modifier.height(18.dp))
            TarjetaFecha(viewModel, onAbrirSelector = { selectorAbierto = true })
            Spacer(Modifier.height(22.dp))
            SelectorFrecuencia(viewModel)
            Spacer(Modifier.height(26.dp))
            viewModel.errorGeneral?.let { MensajeError(it, Modifier.padding(start = 4.dp, bottom = 12.dp)) }
            BotonPrincipal(
                texto = "Guardar meta",
                fondo = FondoGuardar,
                onClick = { viewModel.guardar(onGuardada) },
                forma = RoundedCornerShape(14.dp),
                colorSombra = Color(0x401E3A8A),
                cargando = viewModel.guardando,
                modifier = Modifier.testTag(Etiquetas.BOTON_GUARDAR_META),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
    if (selectorAbierto) {
        SelectorMes(
            inicial = viewModel.mes ?: viewModel.mesActual.plusMonths(1),
            mesActual = viewModel.mesActual,
            onElegir = {
                viewModel.elegirMes(it)
                selectorAbierto = false
            },
            onCerrar = { selectorAbierto = false },
        )
    }
}

@Composable
private fun Encabezado(onAtras: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clip(CircleShape)
                .background(AzulBotonClaro)
                .clickable(role = Role.Button, onClick = onAtras)
                .testTag(Etiquetas.BOTON_ATRAS),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Text(
            "Nueva Meta",
            style = estilo(19, FontWeight.Bold, alineacion = TextAlign.Center),
            modifier = Modifier.padding(horizontal = 52.dp),
        )
    }
}

@Composable
private fun TarjetaIntroduccion() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FondoIntro)
            .padding(18.dp),
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MentaPastilla)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = VerdePastillaTexto, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Asistido por Finanzia IA", style = estilo(12, FontWeight.SemiBold, VerdePastillaTexto))
        }
        Spacer(Modifier.height(12.dp))
        Text("Define tu objetivo financiero", style = estilo(24, FontWeight.ExtraBold, altoLinea = 30, espaciado = -0.4))
        Spacer(Modifier.height(8.dp))
        Text(
            "Establece tu meta y Finanzia calculará el ritmo de ahorro perfecto según tus ingresos reales.",
            style = estilo(15, color = GrisTexto, altoLinea = 22),
        )
    }
}

/** Etiqueta en negrita a la izquierda y un texto pequeño a la derecha ("Obligatorio", "6 disponibles"). */
@Composable
private fun EtiquetaCampo(texto: String, derecha: String? = null, colorDerecha: Color = GrisTexto) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = estilo(16, FontWeight.Bold), modifier = Modifier.weight(1f))
        derecha?.let { Text(it, style = estilo(12, FontWeight.Medium, colorDerecha)) }
    }
}

@Composable
private fun CampoNombre(viewModel: NuevaMetaViewModel) {
    EtiquetaCampo("Nombre de la meta", "Obligatorio")
    Spacer(Modifier.height(10.dp))
    val conError = viewModel.errorNombre != null
    val forma = RoundedCornerShape(14.dp)
    BasicTextField(
        value = viewModel.nombre,
        onValueChange = viewModel::cambiarNombre,
        singleLine = true,
        textStyle = estilo(16, FontWeight.Medium),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        cursorBrush = SolidColor(AzulMarino),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(Etiquetas.CAMPO_NOMBRE_META),
        decorationBox = { campo ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .clip(forma)
                    .background(Blanco)
                    .border(if (conError) 2.dp else 1.dp, if (conError) ErrorRojo else BordeCampo, forma)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (viewModel.nombre.isEmpty()) {
                        Text("Ej.: Fondo para mi moto", style = estilo(16, color = GrisPlaceholder))
                    }
                    campo()
                }
                if (viewModel.nombre.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.Cancel,
                        contentDescription = "Borrar nombre",
                        tint = GrisTexto,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = viewModel::borrarNombre),
                    )
                }
            }
        },
    )
    viewModel.errorNombre?.let { MensajeError(it, Modifier.padding(top = 6.dp, start = 4.dp)) }
}

@Composable
private fun Categorias(viewModel: NuevaMetaViewModel) {
    EtiquetaCampo("Categoría de la meta", "${CategoriaMeta.entries.size} disponibles", VerdeTexto)
    Spacer(Modifier.height(12.dp))
    BoxWithConstraints {
        // Tres columnas como el diseño; dos si con la letra actual no caben sin partir palabras.
        val columnas = if (esCompacto(maxWidth, minimo = 300.dp)) 2 else 3
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoriaMeta.entries.chunked(columnas).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fila.forEach { categoria ->
                        BotonCategoria(
                            categoria = categoria,
                            seleccionada = viewModel.categoria == categoria,
                            onClick = { viewModel.elegirCategoria(categoria) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Completa la última fila para que los botones conserven su ancho.
                    repeat(columnas - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    viewModel.errorCategoria?.let { MensajeError(it, Modifier.padding(top = 6.dp, start = 4.dp)) }
}

@Composable
private fun BotonCategoria(categoria: CategoriaMeta, seleccionada: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier
            .heightIn(min = 78.dp)
            .clip(forma)
            .background(if (seleccionada) MentaSeleccion else Blanco)
            .then(if (seleccionada) Modifier else Modifier.border(1.dp, BordeTarjeta, forma))
            .selectable(selected = seleccionada, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            iconoCategoriaMeta(categoria),
            contentDescription = null,
            tint = if (seleccionada) AzulMarino else colorCategoriaMeta(categoria),
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            categoria.nombre,
            style = estilo(13, if (seleccionada) FontWeight.Bold else FontWeight.Medium, AzulMarino, alineacion = TextAlign.Center),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Contenedor blanco y redondeado de las tarjetas de monto y fecha. */
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
private fun TarjetaMonto(viewModel: NuevaMetaViewModel) {
    Tarjeta {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Monto objetivo total", style = estilo(16, FontWeight.Bold), modifier = Modifier.weight(1f))
            Text(
                "Moneda: COP ($)",
                style = estilo(11, FontWeight.Medium, GrisTexto),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrisPastilla)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        val conError = viewModel.errorMonto != null
        val estiloMonto = estilo(32, FontWeight.ExtraBold, altoLinea = 40, alineacion = TextAlign.End)
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (conError) Modifier.border(2.dp, ErrorRojo, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp) else Modifier,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$", style = estilo(34, FontWeight.ExtraBold, AzulDolar))
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = viewModel.monto,
                onValueChange = viewModel::cambiarMonto,
                singleLine = true,
                textStyle = estiloMonto,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                visualTransformation = MilesVisualTransformation,
                cursorBrush = SolidColor(AzulMarino),
                modifier = Modifier
                    .weight(1f)
                    .testTag(Etiquetas.CAMPO_MONTO_META),
                decorationBox = { campo ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (viewModel.monto.isEmpty()) Text("0", style = estiloMonto.copy(color = GrisPlaceholder))
                        campo()
                    }
                },
            )
            Spacer(Modifier.width(6.dp))
            Text("COP", style = estilo(13, FontWeight.SemiBold, GrisTexto))
        }
        viewModel.errorMonto?.let { MensajeError(it, Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(14.dp))
        // La fila se desplaza hacia los lados si no cabe (spec 08).
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NuevaMetaViewModel.MONTOS_RAPIDOS.forEach { valor ->
                val elegido = viewModel.montoRapido == valor
                Text(
                    "+$" + Montos.formatear(valor.toString()),
                    style = estilo(13, FontWeight.SemiBold, AzulMarino),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (elegido) MentaSeleccion else GrisPastilla)
                        .clickable(role = Role.Button) { viewModel.sumarMonto(valor) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun TarjetaFecha(viewModel: NuevaMetaViewModel, onAbrirSelector: () -> Unit) {
    Tarjeta {
        Text("Fecha estimada de logro", style = estilo(16, FontWeight.Bold))
        Spacer(Modifier.height(12.dp))
        val conError = viewModel.errorFecha != null
        val forma = RoundedCornerShape(14.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(forma)
                .background(FondoCampoFecha)
                .then(if (conError) Modifier.border(2.dp, ErrorRojo, forma) else Modifier)
                .clickable(role = Role.Button, onClick = onAbrirSelector)
                .testTag(Etiquetas.CAMPO_FECHA_META)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AzulIconoClaro),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = AzulDolar, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                val mes = viewModel.mes
                if (mes == null) {
                    Text("Elige el mes", style = estilo(16, FontWeight.SemiBold, GrisSuave))
                } else {
                    val hoy = viewModel.mesActual.atDay(1)
                    Text(CalculosMetas.mesYAnio(mes), style = estilo(16, FontWeight.Bold))
                    Text(
                        textoRestante(hoy, mes),
                        style = estilo(12, color = GrisTexto),
                    )
                }
            }
            Icon(Icons.Outlined.EditCalendar, contentDescription = "Cambiar fecha", tint = GrisTexto, modifier = Modifier.size(22.dp))
        }
        viewModel.errorFecha?.let { MensajeError(it, Modifier.padding(top = 6.dp)) }
    }
}

/** "8 meses restantes • 16 quincenas". */
private fun textoRestante(hoy: LocalDate, mes: YearMonth): String {
    val meses = CalculosMetas.mesesRestantes(hoy, mes)
    val quincenas = CalculosMetas.quincenasRestantes(hoy, mes)
    val textoMeses = if (meses == 1L) "1 mes restante" else "$meses meses restantes"
    val textoQuincenas = if (quincenas == 1L) "1 quincena" else "$quincenas quincenas"
    return "$textoMeses • $textoQuincenas"
}

@Composable
private fun SelectorFrecuencia(viewModel: NuevaMetaViewModel) {
    EtiquetaCampo("Frecuencia del aporte")
    Spacer(Modifier.height(10.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FondoCampoFecha)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FrecuenciaAporte.entries.forEach { opcion ->
            val elegida = viewModel.frecuencia == opcion
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .then(
                        if (elegida) {
                            Modifier.shadow(2.dp, RoundedCornerShape(11.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
                        } else {
                            Modifier
                        },
                    )
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (elegida) Blanco else Color.Transparent)
                    .selectable(selected = elegida, role = Role.RadioButton) { viewModel.elegirFrecuencia(opcion) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    opcion.opcion,
                    style = estilo(13, if (elegida) FontWeight.Bold else FontWeight.Medium, if (elegida) AzulMarino else GrisTexto, alineacion = TextAlign.Center),
                )
            }
        }
    }
}

/** Selector de mes y año: flechas para cambiar el año y los 12 meses; los meses ya pasados no se pueden elegir. */
@Composable
private fun SelectorMes(
    inicial: YearMonth,
    mesActual: YearMonth,
    onElegir: (YearMonth) -> Unit,
    onCerrar: () -> Unit,
) {
    var anio by remember { mutableIntStateOf(inicial.year) }
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = Blanco,
        title = { Text("Fecha estimada de logro", style = estilo(18, FontWeight.Bold)) },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val puedeRetroceder = anio > mesActual.year
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Año anterior",
                        tint = if (puedeRetroceder) AzulMarino else GrisSuave,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(enabled = puedeRetroceder, role = Role.Button) { anio-- }
                            .padding(6.dp),
                    )
                    Text("$anio", style = estilo(18, FontWeight.Bold, alineacion = TextAlign.Center), modifier = Modifier.weight(1f))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Año siguiente",
                        tint = AzulMarino,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { anio++ }
                            .padding(6.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                (1..12).chunked(3).forEach { fila ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { numero ->
                            val mes = YearMonth.of(anio, numero)
                            val disponible = mes.isAfter(mesActual)
                            val elegido = mes == inicial
                            Text(
                                CalculosMetas.mesYAnio(mes).substringBefore(' ').take(3),
                                style = estilo(
                                    14,
                                    if (elegido) FontWeight.Bold else FontWeight.Medium,
                                    if (disponible) AzulMarino else GrisSuave,
                                    alineacion = TextAlign.Center,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (elegido && disponible) MentaSeleccion else Color.Transparent)
                                    .clickable(enabled = disponible, role = Role.Button) { onElegir(mes) }
                                    .padding(vertical = 10.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar", style = estilo(15, FontWeight.SemiBold, AzulDolar)) }
        },
    )
}

@PreviewsFinanzia
@Composable
private fun NuevaMetaPreview() {
    FinanziaTheme {
        NuevaMetaScreen(
            viewModel { NuevaMetaViewModel(MetasSimuladas(), hoy = { LocalDate.of(2026, 1, 15) }) },
            onAtras = {},
            onGuardada = {},
        )
    }
}

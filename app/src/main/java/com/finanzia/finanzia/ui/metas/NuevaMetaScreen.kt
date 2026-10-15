package com.finanzia.finanzia.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.MetasSimulado
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.MilesVisualTransformation
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.ErrorRojo
import com.finanzia.finanzia.ui.theme.ErrorRosaFondo
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.VerdeTexto
import com.finanzia.finanzia.validacion.CalculosMetas
import com.finanzia.finanzia.validacion.Montos
import com.finanzia.finanzia.validacion.ValidacionNuevaMeta
import androidx.compose.ui.graphics.Brush
import java.time.YearMonth

// Colores tomados de docs/design/nueva_meta_1.png.
private val AzulDolar = Color(0xFF1D4ED8)
private val FondoIntro = Color(0xFFEFF3F9)
private val VerdePastilla = Color(0xFFD5F7E7)
private val VerdeSeleccion = Color(0xFF5EE9B5)
private val GrisPastilla = Color(0xFFE8ECF2)
private val AzulBotonGuardar = Brush.horizontalGradient(listOf(Color(0xFF1E3FAE), Color(0xFF1E3FAE)))

private fun CategoriaMeta.icono(): ImageVector = when (this) {
    CategoriaMeta.VIAJES -> Icons.Filled.Flight
    CategoriaMeta.EMERGENCIA -> Icons.Filled.Shield
    CategoriaMeta.TECNOLOGIA -> Icons.Filled.Laptop
    CategoriaMeta.VEHICULO -> Icons.Filled.DirectionsCar
    CategoriaMeta.VIVIENDA -> Icons.Filled.Home
    CategoriaMeta.EDUCACION -> Icons.Filled.School
}

/** "+$20,000", con el mismo separador de miles de toda la app. */
private fun textoMontoRapido(valor: Long) = "+$" + Montos.formatear(valor.toString())

/** Nueva meta (spec 08, docs/design/nueva_meta_1.png), con la simplificación aprobada. */
@Composable
fun NuevaMetaScreen(viewModel: NuevaMetaViewModel, onAtras: () -> Unit, onGuardada: () -> Unit) {
    var eligiendoMes by remember { mutableStateOf(false) }
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(EtiquetasMetas.PANTALLA_NUEVA_META)) {
        ContenidoDesplazable(margenHorizontal = 20.dp, modifier = Modifier.imePadding()) {
            Encabezado(onAtras)
            Spacer(Modifier.height(16.dp))
            TarjetaIntro()
            Spacer(Modifier.height(22.dp))
            CampoNombre(viewModel)
            Spacer(Modifier.height(22.dp))
            Categorias(viewModel)
            Spacer(Modifier.height(22.dp))
            TarjetaMonto(viewModel)
            Spacer(Modifier.height(18.dp))
            TarjetaFecha(viewModel, onElegir = { eligiendoMes = true })
            Spacer(Modifier.height(18.dp))
            Frecuencia(viewModel)
            Spacer(Modifier.height(24.dp))
            viewModel.errorGeneral?.let {
                MensajeError(it)
                Spacer(Modifier.height(12.dp))
            }
            BotonPrincipal(
                texto = "Guardar meta",
                fondo = AzulBotonGuardar,
                onClick = { viewModel.guardar(onGuardada) },
                cargando = viewModel.guardando,
                colorSombra = Color(0x401E3FAE),
                modifier = Modifier.testTag(EtiquetasMetas.BOTON_GUARDAR_META),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
    if (eligiendoMes) {
        SelectorMes(
            desde = YearMonth.from(viewModel.hoy()).plusMonths(1),
            onElegir = {
                viewModel.elegirMes(it)
                eligiendoMes = false
            },
            onCerrar = { eligiendoMes = false },
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
                .background(Blanco)
                .border(1.dp, BordeTarjeta, CircleShape)
                .clickable(role = Role.Button, onClick = onAtras),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Text("Nueva Meta", style = estilo(19, FontWeight.Bold), modifier = Modifier.padding(horizontal = 56.dp))
    }
}

@Composable
private fun TarjetaIntro() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(FondoIntro)
            .padding(18.dp),
    ) {
        Text(
            "✦ Asistido por Finanzia IA",
            style = estilo(12, FontWeight.SemiBold, VerdeTexto),
            modifier = Modifier
                .clip(CircleShape)
                .background(VerdePastilla)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("Define tu objetivo financiero", style = estilo(22, FontWeight.ExtraBold, altoLinea = 28))
        Spacer(Modifier.height(6.dp))
        Text(
            "Establece tu meta y Finanzia calculará el ritmo de ahorro perfecto según tus ingresos reales.",
            style = estilo(14, color = GrisTexto, altoLinea = 20),
        )
    }
}

@Composable
private fun Etiqueta(texto: String, derecha: String? = null, colorDerecha: Color = GrisSuave, error: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = estilo(16, FontWeight.Bold, if (error) ErrorRojo else AzulMarino), modifier = Modifier.weight(1f))
        derecha?.let { Text(it, style = estilo(12, FontWeight.Medium, colorDerecha)) }
    }
}

/** Marco blanco de los campos; en rojo y rosado cuando tiene error (como en las demás pantallas). */
private fun Modifier.marcoCampo(conError: Boolean, forma: RoundedCornerShape = RoundedCornerShape(16.dp)) = this
    .clip(forma)
    .background(if (conError) ErrorRosaFondo else Blanco)
    .border(if (conError) 2.dp else 1.dp, if (conError) ErrorRojo else BordeTarjeta, forma)

@Composable
private fun CampoNombre(viewModel: NuevaMetaViewModel) {
    val conError = viewModel.errorNombre != null
    Etiqueta("Nombre de la meta", derecha = "Obligatorio", error = conError)
    Spacer(Modifier.height(10.dp))
    BasicTextField(
        value = viewModel.nombre,
        onValueChange = viewModel::cambiarNombre,
        singleLine = true,
        textStyle = estilo(16, FontWeight.Medium),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        cursorBrush = SolidColor(AzulMarino),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(EtiquetasMetas.CAMPO_NOMBRE_META),
        decorationBox = { campo ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .marcoCampo(conError)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = if (conError) ErrorRojo else AzulMarino, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (viewModel.nombre.isEmpty()) {
                        Text("Ej: Mi primera meta", style = estilo(16, color = GrisPlaceholder))
                    }
                    campo()
                }
                if (viewModel.nombre.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = "Borrar nombre",
                        tint = GrisSuave,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = viewModel::borrarNombre),
                    )
                }
            }
        },
    )
    viewModel.errorNombre?.let { MensajeError(it, Modifier.padding(top = 6.dp)) }
}

@Composable
private fun Categorias(viewModel: NuevaMetaViewModel) {
    val conError = viewModel.errorCategoria != null
    Etiqueta("Categoría de la meta", derecha = "${CategoriaMeta.entries.size} disponibles", colorDerecha = VerdeTexto, error = conError)
    Spacer(Modifier.height(10.dp))
    CategoriaMeta.entries.chunked(3).forEach { fila ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            fila.forEach { categoria ->
                val seleccionada = viewModel.categoria == categoria
                val forma = RoundedCornerShape(16.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 76.dp)
                        .clip(forma)
                        .background(if (seleccionada) VerdeSeleccion else Blanco)
                        .border(1.dp, if (conError) ErrorRojo else if (seleccionada) VerdeSeleccion else BordeTarjeta, forma)
                        .selectable(selected = seleccionada, role = Role.RadioButton) { viewModel.elegirCategoria(categoria) }
                        .padding(horizontal = 6.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(categoria.icono(), contentDescription = null, tint = AzulMarino, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        categoria.nombreVisible,
                        style = estilo(13, if (seleccionada) FontWeight.Bold else FontWeight.Medium, alineacion = TextAlign.Center),
                        maxLines = 2,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
    viewModel.errorCategoria?.let { MensajeError(it) }
}

@Composable
private fun TarjetaBlanca(conError: Boolean = false, contenido: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .marcoCampo(conError, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { contenido() }
}

@Composable
private fun TarjetaMonto(viewModel: NuevaMetaViewModel) {
    val conError = viewModel.errorMonto != null
    TarjetaBlanca(conError) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Monto objetivo total", style = estilo(15, FontWeight.Bold, if (conError) ErrorRojo else AzulMarino), modifier = Modifier.weight(1f))
            Text(
                "Moneda: COP ($)",
                style = estilo(11, FontWeight.Medium, GrisTexto),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrisPastilla)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$", style = estilo(30, FontWeight.ExtraBold, AzulDolar))
            Spacer(Modifier.width(10.dp))
            val estiloMonto = estilo(30, FontWeight.ExtraBold, alineacion = TextAlign.End)
            // Al tocar el monto se abre el teclado numérico para escribir cualquier valor (spec 08).
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
                    .testTag(EtiquetasMetas.CAMPO_MONTO_META),
                decorationBox = { campo ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (viewModel.monto.isEmpty()) Text("0", style = estiloMonto.copy(color = GrisPlaceholder))
                        campo()
                    }
                },
            )
            Spacer(Modifier.width(6.dp))
            Text("COP", style = estilo(14, FontWeight.SemiBold, GrisSuave))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ValidacionNuevaMeta.MONTOS_RAPIDOS.forEach { valor ->
                val elegido = viewModel.montoRapido == valor
                Text(
                    textoMontoRapido(valor),
                    style = estilo(13, FontWeight.SemiBold, if (elegido) AzulMarino else GrisTexto),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (elegido) VerdeSeleccion else GrisPastilla)
                        .clickable(role = Role.Button) { viewModel.sumarMonto(valor) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
    viewModel.errorMonto?.let { MensajeError(it, Modifier.padding(top = 6.dp)) }
}

@Composable
private fun TarjetaFecha(viewModel: NuevaMetaViewModel, onElegir: () -> Unit) {
    val conError = viewModel.errorFecha != null
    val mes = viewModel.mes
    TarjetaBlanca(conError) {
        Text("Fecha estimada de logro", style = estilo(15, FontWeight.Bold, if (conError) ErrorRojo else AzulMarino))
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FondoPantalla)
                .clickable(role = Role.Button, onClick = onElegir)
                .testTag(EtiquetasMetas.CAMPO_FECHA_META)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blanco),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = AzulDolar, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                if (mes == null) {
                    Text("Elige el mes", style = estilo(16, FontWeight.SemiBold, GrisPlaceholder))
                } else {
                    val hoy = viewModel.hoy()
                    Text(CalculosMetas.mesYAnio(mes.atDay(1)), style = estilo(16, FontWeight.Bold))
                    Text(
                        CalculosMetas.textoRestante(hoy, mes),
                        style = estilo(12, color = GrisTexto),
                    )
                }
            }
            Icon(Icons.Outlined.EditCalendar, contentDescription = "Cambiar fecha", tint = GrisTexto, modifier = Modifier.size(22.dp))
        }
    }
    viewModel.errorFecha?.let { MensajeError(it, Modifier.padding(top = 6.dp)) }
}

@Composable
private fun Frecuencia(viewModel: NuevaMetaViewModel) {
    Text("Frecuencia del aporte", style = estilo(15, FontWeight.Bold))
    Spacer(Modifier.height(10.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GrisPastilla)
            .padding(4.dp),
    ) {
        FrecuenciaAporte.entries.forEach { opcion ->
            val elegida = viewModel.frecuencia == opcion
            Text(
                opcion.nombreVisible,
                style = estilo(13, if (elegida) FontWeight.Bold else FontWeight.Medium, if (elegida) AzulMarino else GrisTexto, alineacion = TextAlign.Center),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (elegida) Blanco else Color.Transparent)
                    .selectable(selected = elegida, role = Role.RadioButton) { viewModel.elegirFrecuencia(opcion) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
            )
        }
    }
}

/** Lista de meses desde [desde] (el mes siguiente al actual) hasta 10 años después. */
@Composable
private fun SelectorMes(desde: YearMonth, onElegir: (YearMonth) -> Unit, onCerrar: () -> Unit) {
    val meses = remember(desde) { List(120) { desde.plusMonths(it.toLong()) } }
    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", style = estilo(15, FontWeight.SemiBold, AzulDolar)) } },
        title = { Text("Fecha estimada de logro", style = estilo(18, FontWeight.Bold)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                items(meses) { mes ->
                    Text(
                        CalculosMetas.mesYAnio(mes.atDay(1)),
                        style = estilo(16, FontWeight.Medium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onElegir(mes) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                    )
                }
            }
        },
        containerColor = Blanco,
    )
}

@PreviewsFinanzia
@Composable
private fun NuevaMetaPreview() {
    FinanziaTheme {
        NuevaMetaScreen(NuevaMetaViewModel(MetasSimulado()), onAtras = {}, onGuardada = {})
    }
}

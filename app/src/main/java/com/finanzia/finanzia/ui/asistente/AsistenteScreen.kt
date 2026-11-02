package com.finanzia.finanzia.ui.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.AsistenteSimulado
import com.finanzia.finanzia.datos.MensajeChat
import com.finanzia.finanzia.datos.RolMensaje
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.AnchoMaximoContenido
import com.finanzia.finanzia.ui.componentes.BarraNavegacion
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
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.VerdeTexto

// Colores de la tarjeta "Finanzia IA" y del botón "Conversar con Finanzia IA" del Dashboard (spec 04).
private val LilaFondo = Color(0xFFEEF1FA)
private val LilaRobot = Color(0xFFEADDFB)
private val MoradoRobot = Color(0xFF5B3CC4)
private val VerdeInsignia = Color(0xFF0F8A5F)
private val GrisAviso = Color(0xFFEDF0F5)
private val DegradadoConversar = Brush.horizontalGradient(listOf(Color(0xFF0B6B4F), Color(0xFF1D5FE0)))

/** Nombres internos que usan los tests del asistente. No se ven en pantalla. */
object EtiquetasAsistente {
    const val PANTALLA = "pantalla_asistente"
    const val CAMPO_PREGUNTA = "campo_pregunta"
    const val BOTON_ENVIAR = "boton_enviar"
    const val MENSAJE_USUARIO = "mensaje_usuario"
    const val MENSAJE_ASISTENTE = "mensaje_asistente"
    const val ESCRIBIENDO = "asistente_escribiendo"
}

/** Asistente Finanzia IA (spec 11). Sin diseño propio: estilo de la tarjeta Finanzia IA del Dashboard. */
@Composable
fun AsistenteScreen(
    viewModel: AsistenteViewModel,
    onAtras: () -> Unit,
    onInicio: () -> Unit,
    onMetas: () -> Unit,
) {
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(EtiquetasAsistente.PANTALLA)) {
        Column(Modifier.fillMaxSize()) {
            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // La barra inferior ya se encarga de la barra de navegación del sistema.
                    .consumeWindowInsets(WindowInsets.navigationBars)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val anchoBurbuja = maxWidth.coerceAtMost(AnchoMaximoContenido) * 0.8f
                Column(
                    Modifier
                        .widthIn(max = AnchoMaximoContenido)
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    Encabezado(onAtras)
                    Aviso()
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (val estado = viewModel.estado) {
                            EstadoAsistente.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MoradoRobot, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                            }
                            is EstadoAsistente.Error -> ErrorConReintentar(estado.mensaje, viewModel::cargar, Modifier.padding(20.dp))
                            EstadoAsistente.Listo -> Conversacion(viewModel, anchoBurbuja)
                        }
                    }
                    viewModel.errorEnvio?.let {
                        ErrorConReintentar(it, viewModel::reintentar, Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                    }
                    BarraEscribir(viewModel)
                }
            }
            BarraNavegacion(seleccionada = PestanaApp.ASISTENTE, onInicio = onInicio, onMetas = onMetas)
        }
    }
}

/** Robot morado en un cuadro lila con la insignia verde de destellos (tarjeta Finanzia IA del Dashboard). */
@Composable
private fun RobotFinanzia(tamano: Dp) {
    Box {
        Box(
            Modifier
                .size(tamano)
                .clip(RoundedCornerShape(tamano / 3))
                .background(LilaRobot),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MoradoRobot, modifier = Modifier.size(tamano * 0.55f))
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-5).dp)
                .size(tamano * 0.4f)
                .clip(CircleShape)
                .background(VerdeInsignia),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Blanco, modifier = Modifier.size(tamano * 0.24f))
        }
    }
}

@Composable
private fun Encabezado(onAtras: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp),
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
        Spacer(Modifier.width(14.dp))
        RobotFinanzia(46.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Finanzia IA", style = estilo(21, FontWeight.SemiBold, altoLinea = 26))
            Text("Responde con tus datos reales", style = estilo(13, FontWeight.SemiBold, VerdeTexto, altoLinea = 18))
        }
    }
}

@Composable
private fun Aviso() {
    Text(
        "Finanzia IA te orienta con tus datos. No es asesoría financiera profesional.",
        style = estilo(12, color = GrisTexto, altoLinea = 16),
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GrisAviso)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun ErrorConReintentar(mensaje: String, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        MensajeError(mensaje)
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
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
private fun Conversacion(viewModel: AsistenteViewModel, anchoBurbuja: Dp) {
    val lista = rememberLazyListState()
    val total = viewModel.mensajes.size + (if (viewModel.respondiendo) 1 else 0)
    // Al llegar un mensaje nuevo (o la burbuja "está escribiendo…"), baja hasta el final.
    LaunchedEffect(total) {
        if (total > 0) lista.animateScrollToItem(total - 1)
    }
    if (total == 0) {
        Bienvenida(viewModel)
        return
    }
    LazyColumn(
        state = lista,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(viewModel.mensajes) { _, mensaje -> Burbuja(mensaje, anchoBurbuja) }
        if (viewModel.respondiendo) {
            item { BurbujaAsistente(anchoBurbuja, Modifier.testTag(EtiquetasAsistente.ESCRIBIENDO)) {
                Text("Finanzia IA está escribiendo…", style = estilo(15, color = GrisPlaceholder, altoLinea = 22))
            } }
        }
    }
}

@Composable
private fun Bienvenida(viewModel: AsistenteViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(LilaFondo)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RobotFinanzia(46.dp)
            Spacer(Modifier.width(14.dp))
            val saludo = if (viewModel.primerNombre.isBlank()) "¡Hola!" else "¡Hola, ${viewModel.primerNombre}!"
            Text(
                "$saludo Pregúntame sobre tus gastos, ingresos o metas.",
                style = estilo(16, FontWeight.Medium, AzulMarino, altoLinea = 23),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("Puedes empezar con:", style = estilo(13, FontWeight.SemiBold, GrisTexto))
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsistenteViewModel.SUGERENCIAS.forEach { sugerencia ->
                Text(
                    sugerencia,
                    style = estilo(14, FontWeight.SemiBold, AzulMarino),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Blanco)
                        .border(1.dp, BordeTarjeta, CircleShape)
                        .clickable(role = Role.Button) { viewModel.usarSugerencia(sugerencia) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
    }
}

@Composable
private fun Burbuja(mensaje: MensajeChat, anchoBurbuja: Dp) {
    if (mensaje.rol == RolMensaje.USUARIO) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                mensaje.texto,
                style = estilo(15, color = Blanco, altoLinea = 22),
                modifier = Modifier
                    .widthIn(max = anchoBurbuja)
                    .testTag(EtiquetasAsistente.MENSAJE_USUARIO)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                    .background(DegradadoConversar)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    } else {
        BurbujaAsistente(anchoBurbuja, Modifier.testTag(EtiquetasAsistente.MENSAJE_ASISTENTE)) {
            Text(mensaje.texto, style = estilo(15, color = AzulMarino, altoLinea = 22))
        }
    }
}

/** Burbuja blanca a la izquierda, con el robot pequeño en un círculo lila. */
@Composable
private fun BurbujaAsistente(anchoBurbuja: Dp, modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(LilaRobot),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MoradoRobot, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        val forma = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        Box(
            Modifier
                .widthIn(max = anchoBurbuja)
                .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
                .clip(forma)
                .background(Blanco)
                .border(1.dp, BordeTarjeta, forma)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) { contenido() }
    }
}

@Composable
private fun BarraEscribir(viewModel: AsistenteViewModel) {
    val forma = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        BasicTextField(
            value = viewModel.texto,
            onValueChange = viewModel::cambiarTexto,
            textStyle = estilo(15, color = AzulMarino, altoLinea = 22),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
            cursorBrush = SolidColor(AzulMarino),
            modifier = Modifier
                .weight(1f)
                .testTag(EtiquetasAsistente.CAMPO_PREGUNTA),
            decorationBox = { campo ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .clip(forma)
                        .background(Blanco)
                        .border(1.dp, BordeTarjeta, forma)
                        .padding(horizontal = 18.dp, vertical = 13.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (viewModel.texto.isEmpty()) Text("Escribe tu pregunta…", style = estilo(15, color = GrisPlaceholder, altoLinea = 22))
                    campo()
                }
            },
        )
        Spacer(Modifier.width(10.dp))
        val activo = viewModel.puedeEnviar
        Box(
            Modifier
                .size(50.dp)
                .alpha(if (activo) 1f else 0.45f)
                .clip(CircleShape)
                .background(DegradadoConversar)
                .clickable(role = Role.Button, enabled = activo, onClick = viewModel::enviar)
                .testTag(EtiquetasAsistente.BOTON_ENVIAR),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Blanco, modifier = Modifier.size(22.dp))
        }
    }
}

// --- Previews: datos de vista previa, nunca conversaciones de ejemplo del diseño ---

@PreviewsFinanzia
@Composable
private fun AsistenteVacioPreview() {
    FinanziaTheme {
        AsistenteScreen(AsistenteViewModel(AsistenteSimulado(), "Vista Previa"), onAtras = {}, onInicio = {}, onMetas = {})
    }
}

@PreviewsFinanzia
@Composable
private fun AsistenteConMensajesPreview() {
    val historial = listOf(
        MensajeChat(RolMensaje.USUARIO, "Pregunta de vista previa"),
        MensajeChat(RolMensaje.ASISTENTE, "Respuesta de vista previa."),
    )
    FinanziaTheme {
        AsistenteScreen(AsistenteViewModel(AsistenteSimulado(historial), "Vista Previa"), onAtras = {}, onInicio = {}, onMetas = {})
    }
}

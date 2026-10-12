package com.finanzia.finanzia.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.GrisRadio
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.Manrope
import com.finanzia.finanzia.validacion.Montos

/** Estilo base: Manrope con el color y el peso indicados. */
fun estilo(
    tamano: Number,
    peso: FontWeight = FontWeight.Normal,
    color: Color = AzulMarino,
    altoLinea: Number? = null,
    espaciado: Double = 0.0,
    alineacion: TextAlign = TextAlign.Unspecified,
) = TextStyle(
    fontFamily = Manrope,
    fontSize = tamano.toFloat().sp,
    fontWeight = peso,
    color = color,
    lineHeight = (altoLinea ?: (tamano.toFloat() * 1.4f)).toFloat().sp,
    letterSpacing = espaciado.sp,
    textAlign = alineacion,
)

/** Ancho máximo del contenido: en tabletas y plegables se centra en vez de estirarse. */
val AnchoMaximoContenido = 480.dp

/** Pantallas más altas que esto (tabletas) centran el contenido en vertical. */
val AltoMaximoContenido = 960.dp

/**
 * Columna desplazable que usan todas las pantallas:
 * - respeta la barra de estado, la de navegación, los recortes y el teclado (edge-to-edge);
 * - se desplaza cuando el contenido no cabe (teléfono pequeño, pantalla dividida, letra grande);
 * - en pantallas anchas limita el ancho a [AnchoMaximoContenido] y lo centra.
 *
 * Con [llenarAlto] el contenido ocupa al menos el alto visible, para que los `Modifier.weight`
 * repartan el espacio libre como en el diseño (Pantalla 1).
 */
@Composable
fun ContenidoDesplazable(
    margenHorizontal: Dp,
    modifier: Modifier = Modifier,
    llenarAlto: Boolean = false,
    alineacion: Alignment.Horizontal = Alignment.Start,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        val altoVisible = maxHeight
        val esPantallaAlta = altoVisible > AltoMaximoContenido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = altoVisible),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (esPantallaAlta) Arrangement.Center else Arrangement.Top,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = AnchoMaximoContenido)
                    .fillMaxWidth()
                    .then(if (llenarAlto) Modifier.heightIn(min = minOf(altoVisible, AltoMaximoContenido)) else Modifier)
                    .padding(horizontal = margenHorizontal),
                horizontalAlignment = alineacion,
                content = contenido,
            )
        }
    }
}

/**
 * `true` cuando el espacio disponible, medido con la letra actual del sistema, equivale a menos
 * de [minimo] con letra normal. Sirve para pasar a una distribución compacta solo cuando hace falta
 * (letra muy grande o pantalla muy angosta) y no partir palabras. Con letra normal en un teléfono
 * común siempre es `false`, así que el diseño no cambia.
 */
@Composable
fun esCompacto(anchoDisponible: Dp, minimo: Dp): Boolean =
    anchoDisponible / LocalDensity.current.fontScale < minimo

/** Contenedor de cada pantalla: fondo de borde a borde y lugar para el Snackbar. */
@Composable
fun PantallaBase(
    fondo: Modifier,
    snackbar: SnackbarHostState? = null,
    contenido: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(fondo),
    ) {
        contenido()
        if (snackbar != null) {
            SnackbarHost(
                hostState = snackbar,
                // Por encima del teclado y de la barra de navegación.
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
            )
        }
    }
}

/**
 * Botón principal: texto blanco en negrita con flecha "→".
 * [alto] es la altura mínima: con letra grande el botón crece y el texto pasa a otra línea.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    fondo: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alto: Dp = 56.dp,
    forma: Shape = RoundedCornerShape(18.dp),
    colorSombra: Color = Color(0x4010B981),
    tamanoTexto: Int = 18,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = alto)
            .shadow(10.dp, forma, ambientColor = colorSombra, spotColor = colorSombra)
            .clip(forma)
            .background(fondo)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                texto,
                style = estilo(tamanoTexto, FontWeight.Bold, Blanco, espaciado = 0.3, alineacion = TextAlign.Center),
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Blanco,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Pie con una pregunta gris y un enlace azul en negrita. Lo usan Crear cuenta
 * ("¿Ya tienes una cuenta? Iniciar sesión") e Inicio de sesión ("¿No tienes una cuenta? Crear cuenta"),
 * así los dos se ven exactamente igual. Si no cabe en una línea, el enlace pasa a la siguiente.
 */
@Composable
fun PieConEnlace(pregunta: String, enlace: String, onClick: () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$pregunta ", style = estilo(15, color = GrisTexto))
        Text(
            enlace,
            style = estilo(16, FontWeight.Bold, AzulEnlace),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 2.dp, vertical = 4.dp),
        )
    }
}

/** Enlace gris centrado ("Omitir este paso por ahora", "Explorar más tarde…"). */
@Composable
fun EnlaceGris(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, tamano: Int = 15) {
    Text(
        text = texto,
        style = estilo(tamano, FontWeight.Medium, GrisSuave, alineacion = TextAlign.Center),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}

/** Pastilla redondeada de los encabezados ("Paso 2 • Ingresos"). */
@Composable
fun Pastilla(
    texto: String,
    fondo: Color,
    borde: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    tamano: Int = 15,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(fondo)
            .border(1.dp, borde, CircleShape)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(texto, style = estilo(tamano, FontWeight.SemiBold, colorTexto))
    }
}

/** Botón blanco cuadrado con flecha "<" de las pantallas 4 y 5. */
@Composable
fun BotonAtras(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(46.dp)
            .shadow(3.dp, forma, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(Etiquetas.BOTON_ATRAS),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Atrás",
            tint = AzulMarino,
            modifier = Modifier.size(30.dp),
        )
    }
}

/** Ícono pequeño dentro de un cuadro de color (flecha de ingreso o gasto, rayo, etc.). */
@Composable
fun CuadroIcono(
    fondo: Color,
    tamano: Dp,
    modifier: Modifier = Modifier,
    forma: Shape = RoundedCornerShape(10.dp),
    contenido: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(forma)
            .background(fondo),
        contentAlignment = Alignment.Center,
    ) { contenido() }
}

/**
 * Emoji usado como ícono dentro de un cuadro de tamaño fijo. Se mide en dp (no crece con la
 * letra del sistema) para que no se salga del cuadro; con letra normal mide lo mismo que en sp.
 */
@Composable
fun Emoji(emoji: String, tamano: Dp) {
    val tamanoLetra = with(LocalDensity.current) { tamano.toSp() }
    Text(emoji, fontSize = tamanoLetra, lineHeight = tamanoLetra)
}

/** Círculo de selección de las tarjetas: vacío gris o relleno con ✓ blanco. */
@Composable
fun IndicadorSeleccion(seleccionada: Boolean, color: Color) {
    if (seleccionada) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Blanco, modifier = Modifier.size(17.dp))
        }
    } else {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(2.dp, GrisRadio, CircleShape),
        )
    }
}

/**
 * Tarjeta de opción de las pantallas 4 y 5 (frecuencia de ingreso o categoría de gasto).
 * Solo una puede estar seleccionada: la pantalla controla cuál.
 */
@Composable
fun TarjetaOpcion(
    emoji: String,
    fondoEmoji: Color,
    titulo: String,
    descripcion: String,
    seleccionada: Boolean,
    colorSeleccion: Color,
    fondoSeleccion: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(forma)
            .selectable(selected = seleccionada, role = Role.RadioButton, onClick = onClick),
        shape = forma,
        color = if (seleccionada) fondoSeleccion else Blanco,
        border = if (seleccionada) BorderStroke(2.5.dp, colorSeleccion) else BorderStroke(1.dp, BordeTarjeta),
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(start = 14.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                CuadroIcono(fondo = fondoEmoji, tamano = 40.dp) {
                    Emoji(emoji, tamano = 20.dp)
                }
                IndicadorSeleccion(seleccionada, colorSeleccion)
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(14.dp))
            Text(titulo, style = estilo(17, FontWeight.Bold))
            Spacer(Modifier.height(4.dp))
            Text(descripcion, style = estilo(13, color = GrisTexto, altoLinea = 18, espaciado = -0.1))
        }
    }
}

/** Muestra los dígitos del monto con comas de miles mientras se escribe ("18500" → "18,500"). */
object MilesVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formateado = Montos.formatear(original)
        val mapa = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var digitos = 0
                formateado.forEachIndexed { i, c ->
                    if (c != ',') digitos++
                    if (digitos == offset) return i + 1
                }
                return formateado.length
            }

            override fun transformedToOriginal(offset: Int): Int =
                formateado.take(offset.coerceIn(0, formateado.length)).count { it != ',' }
        }
        return TransformedText(AnnotatedString(formateado), mapa)
    }
}

package com.finanzia.finanzia.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

/** Contenedor de cada pantalla: fondo, márgenes del sistema y lugar para el Snackbar. */
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}

/** Botón principal: texto blanco en negrita con flecha "→". */
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
            .height(alto)
            .shadow(10.dp, forma, ambientColor = colorSombra, spotColor = colorSombra)
            .clip(forma)
            .background(fondo)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(texto, style = estilo(tamanoTexto, FontWeight.Bold, Blanco, espaciado = 0.3))
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
                    Text(emoji, fontSize = 20.sp)
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

package com.finanzia.finanzia.ui.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.finanzia.finanzia.ui.PreviewsFinanzia
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.esCompacto
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulClaro
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulEscudo
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.DegradadoCuentaCreada
import com.finanzia.finanzia.ui.theme.FilaTarjeta
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.MoradoClaro
import com.finanzia.finanzia.ui.theme.MoradoTexto
import com.finanzia.finanzia.ui.theme.VerdeMenta
import com.finanzia.finanzia.ui.theme.VerdeTexto

/** Pantalla 2 · Cuenta creada (docs/design/crear_cuenta_2.jpeg). */
@Composable
fun CuentaCreadaScreen(correo: String, onComenzar: () -> Unit) {
    PantallaBase(fondo = Modifier.background(FondoPantalla)) {
        ContenidoDesplazable(margenHorizontal = 20.dp, alineacion = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(20.dp))
            PastillaExito()
            Spacer(Modifier.height(26.dp))
            Text(
                "¡Te damos la bienvenida a bordo!",
                style = estilo(32, FontWeight.ExtraBold, altoLinea = 40, espaciado = -0.5, alineacion = TextAlign.Center),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Tu asistente financiero impulsado por IA está listo para ayudarte a ahorrar, optimizar tus gastos y alcanzar cada meta.",
                style = estilo(16, color = GrisTexto, altoLinea = 23, alineacion = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
            Spacer(Modifier.height(30.dp))
            TarjetaTodoListo()
            Spacer(Modifier.height(18.dp))
            AvisoCorreo(correo)
            Spacer(Modifier.height(26.dp))
            BotonPrincipal(
                texto = "Comenzar ahora",
                fondo = DegradadoCuentaCreada,
                onClick = onComenzar,
                alto = 62.dp,
                forma = RoundedCornerShape(20.dp),
                colorSombra = Color(0x400048D6),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PastillaExito() {
    // TODO: el chevron "<" se ve pero no tiene acción ("Decisiones pendientes" 5).
    Row(
        modifier = Modifier
            .shadow(4.dp, CircleShape, ambientColor = Color(0x221B5FD9), spotColor = Color(0x221B5FD9))
            .clip(CircleShape)
            .background(AzulClaro)
            .padding(start = 18.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🚀", style = estilo(17))
        Spacer(Modifier.width(10.dp))
        Text("¡CUENTA CREADA CON ÉXITO!", style = estilo(14, FontWeight.Bold, VerdeTexto, espaciado = 0.2))
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = AzulEnlace,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun TarjetaTodoListo() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(RoundedCornerShape(22.dp))
            .background(Blanco)
            .padding(16.dp),
    ) {
        // Con letra muy grande en pantalla angosta, las etiquetas pasan debajo del texto
        // para que ninguna palabra se parta. Con letra normal se ve como el diseño.
        val compacto = esCompacto(maxWidth, minimo = 200.dp)
        Column {
            if (compacto) {
                EncabezadoTodoListo()
                Spacer(Modifier.height(8.dp))
                PastillaTresDeTres()
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EncabezadoTodoListo(Modifier.weight(1f))
                    PastillaTresDeTres()
                }
            }
            Spacer(Modifier.height(20.dp))
            FilaListo(
                titulo = "Bóveda y cifrado bancario configurados",
                descripcion = "Protocolo AES-256 de nivel bancario activado.",
                compacto = compacto,
            )
            Spacer(Modifier.height(12.dp))
            FilaListo(
                titulo = "Asistente IA Copilot activado",
                descripcion = "Detección automática de gastos hormiga y suscripciones.",
                compacto = compacto,
            )
            Spacer(Modifier.height(12.dp))
            FilaListo(
                titulo = "Bono de bienvenida desbloqueado",
                descripcion = "Diagnóstico de hábitos financieros y proyección a 12 meses.",
                etiqueta = "GRATIS",
                compacto = compacto,
            )
        }
    }
}

@Composable
private fun EncabezadoTodoListo(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE3ECFB)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = AzulEscudo, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text("Todo listo para comenzar", style = estilo(17, FontWeight.SemiBold), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PastillaTresDeTres() {
    Box(
        Modifier
            .clip(CircleShape)
            .background(VerdeMenta)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text("3 de 3", style = estilo(13, FontWeight.Bold))
    }
}

@Composable
private fun EtiquetaGratis(texto: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MoradoClaro)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(texto, style = estilo(13, FontWeight.Bold, MoradoTexto, espaciado = 0.4))
    }
}

@Composable
private fun FilaListo(titulo: String, descripcion: String, compacto: Boolean, etiqueta: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FilaTarjeta)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(VerdeMenta),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = AzulMarino, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(if (compacto) 8.dp else 12.dp))
        Column(Modifier.weight(1f)) {
            val estiloTitulo = estilo(14, FontWeight.Medium, altoLinea = 20, espaciado = -0.2)
            if (compacto) {
                Text(titulo, style = estiloTitulo)
                if (etiqueta != null) {
                    Spacer(Modifier.height(4.dp))
                    EtiquetaGratis(etiqueta)
                }
            } else {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(titulo, style = estiloTitulo, modifier = Modifier.weight(1f))
                    if (etiqueta != null) {
                        Spacer(Modifier.width(8.dp))
                        EtiquetaGratis(etiqueta)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(descripcion, style = estilo(12.5, color = GrisTexto, altoLinea = 17))
        }
    }
}

@Composable
private fun AvisoCorreo(correo: String) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AzulClaro)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        // Con letra muy grande en pantalla angosta, los íconos van arriba y el texto debajo.
        if (esCompacto(maxWidth, minimo = 230.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoCorreo()
                    Spacer(Modifier.weight(1f))
                    IconoRecarga()
                }
                Spacer(Modifier.height(10.dp))
                TextosCorreo(correo)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCorreo()
                Spacer(Modifier.width(14.dp))
                TextosCorreo(correo, Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                IconoRecarga()
            }
        }
    }
}

@Composable
private fun IconoCorreo() {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Blanco),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.MarkEmailRead, contentDescription = null, tint = AzulEscudo, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun IconoRecarga() {
    // TODO: el ícono de recarga se ve pero no tiene acción ("Decisiones pendientes" 5).
    Icon(Icons.Filled.Sync, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(24.dp))
}

@Composable
private fun TextosCorreo(correo: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("Correo de confirmación enviado", style = estilo(15, FontWeight.Medium))
        Text(
            buildAnnotatedString {
                append("Revisa tu bandeja en ")
                withStyle(SpanStyle(color = AzulEnlace, fontWeight = FontWeight.Bold)) { append(correo) }
            },
            style = estilo(13, color = GrisTexto),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewsFinanzia
@Composable
private fun CuentaCreadaPreview() {
    FinanziaTheme { CuentaCreadaScreen(correo = "paula.gomez@ejemplo.com", onComenzar = {}) }
}

package com.finanzia.finanzia.ui.onboarding

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.componentes.CuadroIcono
import com.finanzia.finanzia.ui.componentes.EnlaceGris
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulIntenso
import com.finanzia.finanzia.ui.theme.AzulMuyClaro
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.DegradadoPrincipal
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.RojoGasto
import com.finanzia.finanzia.ui.theme.RosaClaro
import com.finanzia.finanzia.ui.theme.VerdeBordeClaro
import com.finanzia.finanzia.ui.theme.VerdeClaro
import com.finanzia.finanzia.ui.theme.VerdeMarca
import com.finanzia.finanzia.ui.theme.VerdeTexto

/** Pantalla 3 · Paso 1 de 2 · Primeros pasos (docs/design/crear_cuenta_3.jpeg). */
@Composable
fun PrimerosPasosScreen(
    onPrimerIngreso: () -> Unit,
    onPrimerGasto: () -> Unit,
    onIrAlDashboard: () -> Unit,
) {
    val fondo = Modifier.drawBehind {
        drawRect(FondoPantalla)
        drawRect(
            Brush.radialGradient(
                listOf(Color(0xFFEAF6F3), Color(0x00F6F8FC)),
                center = Offset(size.width / 2, size.height * 0.12f),
                radius = size.width * 0.7f,
            ),
        )
    }
    PantallaBase(fondo = fondo) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            PastillaPaso()
            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FINANZIA", style = estilo(24, FontWeight.ExtraBold, espaciado = -0.3))
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDCEBFB))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text("IA", style = estilo(12, FontWeight.Bold, AzulEnlace))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("¡Hola! Soy Finanzia 💸", style = estilo(28, FontWeight.ExtraBold, espaciado = -0.4))
            Spacer(Modifier.height(4.dp))
            Text("Tu asistente financiero inteligente", style = estilo(17, FontWeight.SemiBold, VerdeTexto))
            Spacer(Modifier.height(14.dp))
            Text(
                "Estoy aquí para acompañarte paso a paso. Quiero conocer cómo se mueven tus finanzas para darte los mejores consejos y hacer crecer tu dinero.",
                style = estilo(17, color = GrisTexto, altoLinea = 28, alineacion = TextAlign.Center),
            )
            Spacer(Modifier.height(26.dp))
            TarjetaPorDondeEmpezar()
            Spacer(Modifier.height(28.dp))
            BotonIngreso(onPrimerIngreso)
            Spacer(Modifier.height(14.dp))
            BotonGasto(onPrimerGasto)
            Spacer(Modifier.height(28.dp))
            EnlaceGris("Explorar más tarde e ir al Dashboard", onIrAlDashboard, tamano = 16)
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = VerdeMarca, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tus datos financieros viajan cifrados de punto a punto", style = estilo(13, color = GrisSuave))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PastillaPaso() {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(VerdeClaro)
            .border(1.5.dp, VerdeBordeClaro, CircleShape)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF2ECC8F)),
        )
        Spacer(Modifier.width(10.dp))
        Text("Paso 1 de 2 • Primeros pasos", style = estilo(15, FontWeight.SemiBold, VerdeTexto))
    }
}

@Composable
private fun TarjetaPorDondeEmpezar() {
    val forma = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        CuadroIcono(fondo = AzulMuyClaro, tamano = 38.dp, forma = RoundedCornerShape(12.dp)) {
            RayoContorno(Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text("¿Por dónde te gustaría empezar?", style = estilo(15, FontWeight.Bold))
            Spacer(Modifier.height(4.dp))
            Text(
                "Comienza registrando tu fuente de dinero o lo que gastas habitualmente para calibrar tu balance.",
                style = estilo(13, color = GrisTexto, altoLinea = 19),
            )
        }
    }
}

/** Rayo solo con contorno, como en el diseño (el ícono de Material es relleno). */
@Composable
private fun RayoContorno(modifier: Modifier) {
    Canvas(modifier) {
        val u = size.width / 24f
        val rayo = Path().apply {
            moveTo(13 * u, 2 * u)
            lineTo(3 * u, 14 * u)
            lineTo(12 * u, 14 * u)
            lineTo(11 * u, 22 * u)
            lineTo(21 * u, 10 * u)
            lineTo(12 * u, 10 * u)
            close()
        }
        drawPath(rayo, AzulIntenso, style = Stroke(width = 2 * u, join = StrokeJoin.Round, cap = StrokeCap.Round))
    }
}

@Composable
private fun BotonIngreso(onClick: () -> Unit) {
    val forma = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, forma, ambientColor = Color(0x4010B981), spotColor = Color(0x4010B981))
            .clip(forma)
            .background(DegradadoPrincipal)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 22.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CuadroIcono(fondo = Color(0x33FFFFFF), tamano = 38.dp, forma = RoundedCornerShape(11.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Blanco, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            "Quiero registrar mi primer ingreso",
            style = estilo(18, FontWeight.Bold, Blanco, altoLinea = 28),
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Blanco, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun BotonGasto(onClick: () -> Unit) {
    val forma = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .border(1.5.dp, BordeTarjeta, forma)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 22.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CuadroIcono(fondo = RosaClaro, tamano = 38.dp, forma = RoundedCornerShape(11.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = null, tint = RojoGasto, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            "Quiero registrar mi primer gasto",
            style = estilo(18, FontWeight.Bold, altoLinea = 28),
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GrisSuave, modifier = Modifier.size(28.dp))
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 900)
@Composable
private fun PrimerosPasosPreview() {
    FinanziaTheme { PrimerosPasosScreen(onPrimerIngreso = {}, onPrimerGasto = {}, onIrAlDashboard = {}) }
}

package com.finanzia.finanzia.ui.sesion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.esCompacto
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.AzulMuyClaro
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.DegradadoPrincipal
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.RojoOscuro
import com.finanzia.finanzia.ui.theme.RosaClaro
import com.finanzia.finanzia.ui.theme.VerdeTexto
import com.finanzia.finanzia.validacion.FormatosDashboard
import com.finanzia.finanzia.validacion.InsightAcceso
import com.finanzia.finanzia.validacion.ResumenAcceso

private val MentaPastilla = Color(0xFFD5F8E8)
private val VerdePastilla = Color(0xFF065F46)
private val AzulIconoClaro = Color(0xFFDCE7FA)
private val GrisIcono = Color(0xFF6B7280)

/**
 * Datos de muestra del diseño. En la app, saldo, variación e insight salen de los datos reales del
 * usuario (ver FinanziaApp y CalculosAcceso). Esto solo se usa en la Preview, para que se vea como
 * la imagen del diseño.
 */
object DatosMuestraAcceso {
    const val SALDO = "$14,250.00"
    const val VARIACION = "+4.8%"
    val RESUMEN = ResumenAcceso(
        saldo = 14_250.0,
        variacion = 4.8,
        insight = InsightAcceso.ComparacionMesAnterior(porcentaje = -12),
    )
}

/** Lo que se muestra mientras se calcula el saldo, o si no se pudo leer. */
const val SALDO_SIN_DATO = "—"

/** Solo el primer nombre: "Paula Gómez" → "Paula". */
fun primerNombre(nombreCompleto: String): String =
    nombreCompleto.trim().split(Regex("\\s+")).firstOrNull().orEmpty()

/**
 * Pantalla 2 · Acceso autorizado (docs/design/olvidar_contraseña_3.jpeg).
 *
 * [resumen] trae el saldo (el mismo balance del Dashboard), la variación del mes y el insight,
 * calculados con los datos reales. `null` mientras se calculan.
 */
@Composable
fun AccesoAutorizadoScreen(nombre: String, resumen: ResumenAcceso? = null, onContinuar: () -> Unit) {
    PantallaBase(fondo = Modifier.background(FondoPantalla)) {
        ContenidoDesplazable(margenHorizontal = 22.dp, alineacion = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            PastillaAcceso()
            Spacer(Modifier.height(28.dp))
            Text(
                "¡Qué alegría tenerte de vuelta, ${primerNombre(nombre)}!",
                style = estilo(32, FontWeight.ExtraBold, altoLinea = 40, espaciado = -0.6, alineacion = TextAlign.Center),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Tus finanzas y metas con IA están al día y sincronizadas en tiempo real.",
                style = estilo(17, color = GrisTexto, altoLinea = 23, alineacion = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(30.dp))
            TarjetaSaldo(resumen)
            Spacer(Modifier.height(38.dp))
            BotonPrincipal(
                texto = "Continuar al Dashboard",
                fondo = DegradadoPrincipal,
                onClick = onContinuar,
                alto = 64.dp,
                forma = RoundedCornerShape(20.dp),
                colorSombra = Color(0x400066F5),
            )
            Spacer(Modifier.height(36.dp))
            PieProteccion()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PastillaAcceso() {
    Row(
        modifier = Modifier
            .shadow(4.dp, CircleShape, ambientColor = Color(0x2210B981), spotColor = Color(0x2210B981))
            .clip(CircleShape)
            .background(MentaPastilla)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Verified, contentDescription = null, tint = VerdePastilla, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "ACCESO AUTORIZADO • SESIÓN LISTA",
            style = estilo(15, FontWeight.SemiBold, VerdePastilla, espaciado = 0.4, alineacion = TextAlign.Center),
        )
    }
}

@Composable
private fun TarjetaSaldo(resumen: ResumenAcceso?) {
    val saldo = resumen?.saldo?.let { FormatosDashboard.monto(it) } ?: SALDO_SIN_DATO
    val forma = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .drawBehind {
                // Brillo suave arriba a la derecha, como en el diseño.
                drawRect(
                    Brush.radialGradient(
                        listOf(Color(0xFFEAF7F4), Color(0x00FFFFFF)),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.6f,
                    ),
                )
            }
            .padding(18.dp),
    ) {
        EncabezadoSaldo(saldo, resumen?.variacion)
        Spacer(Modifier.height(22.dp))
        RecuadroInsight(resumen?.insight)
        Spacer(Modifier.height(22.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = VerdeTexto, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Cifrado AES-256 Activo", style = estilo(14, color = AzulMarino.copy(alpha = 0.85f)))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(VerdeTexto),
                )
                Spacer(Modifier.width(8.dp))
                Text("Conexión Segura", style = estilo(14, color = AzulMarino.copy(alpha = 0.85f)))
            }
        }
    }
}

@Composable
private fun EncabezadoSaldo(saldo: String, variacion: Double?) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Con letra muy grande en pantalla angosta, la pastilla de variación pasa debajo del saldo.
        val compacto = esCompacto(maxWidth, minimo = 300.dp)
        if (compacto) {
            // Íconos arriba y el saldo debajo, a todo el ancho.
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoBilletera()
                    Spacer(Modifier.weight(1f))
                    variacion?.let { PastillaVariacion(it) }
                }
                Spacer(Modifier.height(12.dp))
                TextosSaldo(saldo, compacto = true)
            }
        } else {
            Row(verticalAlignment = Alignment.Top) {
                IconoBilletera(Modifier.padding(top = 12.dp))
                Spacer(Modifier.width(14.dp))
                TextosSaldo(saldo, compacto = false, modifier = Modifier.weight(1f))
                variacion?.let {
                    Spacer(Modifier.width(8.dp))
                    PastillaVariacion(it)
                }
            }
        }
    }
}

@Composable
private fun IconoBilletera(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AzulIconoClaro),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun TextosSaldo(saldo: String, compacto: Boolean, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("SALDO TOTAL DISPONIBLE", style = estilo(12, FontWeight.Medium, GrisTexto, espaciado = 0.6))
        Spacer(Modifier.height(4.dp))
        // En modo compacto el monto se mide en dp (no crece con la letra del sistema) para que quepa
        // completo. Con letra normal mide lo mismo que en el diseño.
        val tamanoMonto = if (compacto) with(LocalDensity.current) { 30.dp.toSp().value } else 30f
        FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
            Text(
                saldo,
                style = estilo(tamanoMonto, FontWeight.ExtraBold, altoLinea = tamanoMonto * 1.2f, espaciado = -0.5),
            )
            Spacer(Modifier.width(12.dp))
            // TODO: el ojo se ve pero la spec 01 no define su acción.
            Icon(Icons.Outlined.Visibility, contentDescription = null, tint = GrisIcono, modifier = Modifier.size(24.dp))
        }
    }
}


/** "+4.8%" en verde si el saldo subió (o quedó igual) en el mes; "-3.1%" en rojo si bajó. */
@Composable
private fun PastillaVariacion(variacion: Double) {
    val sube = variacion >= 0.0
    val fondo = if (sube) MentaPastilla else RosaClaro
    val color = if (sube) VerdePastilla else RojoOscuro
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (sube) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(textoVariacion(variacion), style = estilo(14, FontWeight.SemiBold, color))
    }
}

/** 4.8 → "+4.8%", -3.1 → "-3.1%". */
fun textoVariacion(variacion: Double): String =
    (if (variacion >= 0.0) "+" else "-") + FormatosDashboard.porcentaje(kotlin.math.abs(variacion)) + "%"

@Composable
private fun RecuadroInsight(insight: InsightAcceso?) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AzulMuyClaro)
            .padding(14.dp),
    ) {
        // Con letra muy grande en pantalla angosta, el ícono va arriba y el texto debajo.
        if (esCompacto(maxWidth, minimo = 260.dp)) {
            Column {
                IconoInsight()
                Spacer(Modifier.height(10.dp))
                TextosInsight(insight)
            }
        } else {
            Row(verticalAlignment = Alignment.Top) {
                IconoInsight()
                Spacer(Modifier.width(14.dp))
                TextosInsight(insight, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IconoInsight() {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(AzulIconoClaro),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TextosInsight(insight: InsightAcceso?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("Insight Inteligente de Hoy", style = estilo(15, FontWeight.SemiBold, AzulEnlace))
        Spacer(Modifier.height(2.dp))
        Text(textoInsight(insight), style = estilo(16, color = GrisTexto, altoLinea = 22))
    }
}

/** Frase del insight con la parte importante resaltada, como en el diseño. `null` = aún se calcula. */
fun textoInsight(insight: InsightAcceso?): AnnotatedString = buildAnnotatedString {
    val resaltado = SpanStyle(color = AzulMarino, fontWeight = FontWeight.SemiBold)
    when (insight) {
        null -> append("Analizando tus movimientos…")
        is InsightAcceso.ComparacionMesAnterior -> {
            val p = insight.porcentaje
            if (p == 0) {
                append("Llevas tus gastos ")
                withStyle(resaltado) { append("igual") }
                append(" que el mes pasado.")
            } else {
                append("Llevas tus gastos un ")
                withStyle(resaltado) { append("${kotlin.math.abs(p)}% " + if (p < 0) "por debajo" else "por encima") }
                append(" del mes pasado.")
            }
        }
        is InsightAcceso.MayorGasto -> {
            append("Este mes, ")
            withStyle(resaltado) { append(insight.categoria) }
            append(" es tu mayor gasto: ")
            withStyle(resaltado) { append("${insight.porcentaje}%") }
            append(" de lo que has gastado.")
        }
        InsightAcceso.AhorroCompleto -> {
            append("Has ahorrado el ")
            withStyle(resaltado) { append("100%") }
            append(" de tus ingresos este mes.")
        }
        InsightAcceso.SinDatos ->
            append("Aún no hay suficiente información para darte un consejo. Registra tus ingresos y gastos para empezar.")
    }
}

@Composable
private fun PieProteccion() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = GrisIcono, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "PROTECCIÓN DE GRADO BANCARIO INTERNACIONAL",
                style = estilo(12, FontWeight.Medium, GrisTexto, espaciado = 0.4, alineacion = TextAlign.Center),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Finanzia Mobile App v3.4.1 (Build 2049)",
            style = estilo(13, color = GrisTexto, alineacion = TextAlign.Center),
        )
    }
}

@PreviewsFinanzia
@Composable
private fun AccesoAutorizadoPreview() {
    FinanziaTheme { AccesoAutorizadoScreen(nombre = "Paula Gómez", resumen = DatosMuestraAcceso.RESUMEN, onContinuar = {}) }
}

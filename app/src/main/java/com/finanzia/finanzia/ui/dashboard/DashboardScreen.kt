package com.finanzia.finanzia.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.finanzia.finanzia.datos.CATEGORIA_INGRESOS
import com.finanzia.finanzia.datos.CUENTA_PRINCIPAL
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.MetaDashboard
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BarraNavegacion
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.Emoji
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.RojoTexto
import com.finanzia.finanzia.ui.theme.VerdeTexto
import com.finanzia.finanzia.validacion.FormatosDashboard
import java.time.Instant

// Colores tomados de docs/design/usuario_creado_onboarding.jpeg
private val AzulBoton = Color(0xFF0A4FC9)
private val AzulProgreso = Color(0xFF1D4ED8)
private val VerdeDinero = Color(0xFF0B8F5E)
private val MentaPastilla = Color(0xFF8EF3CF)
private val VerdePastillaTexto = Color(0xFF0D7A55)
private val MentaTendencia = Color(0xFF34E5B8)
private val MentaIngresos = Color(0xFF3AF2B3)
private val RosaGastos = Color(0xFFFFD6D6)
private val RojoFlecha = Color(0xFFDC2626)
private val LilaFondo = Color(0xFFEEF1FA)
private val LilaRobot = Color(0xFFEADDFB)
private val MoradoRobot = Color(0xFF5B3CC4)
private val LilaChip = Color(0xFFE4DCFA)
private val MoradoChip = Color(0xFF4B2BA0)
private val MoradoChevron = Color(0xFF6D4BD8)
private val VerdeInsignia = Color(0xFF0F8A5F)
private val AzulMeta = Color(0xFFDCE7FB)
private val PistaProgreso = Color(0xFFC7D6F5)
private val MentaMeta = Color(0xFFA8F0D3)
private val VerdeIconoMeta = Color(0xFF0E6B4D)
private val FondoIconoNeutro = Color(0xFFEEF2FA)

private val DegradadoLogo = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF1D4ED8)))
private val DegradadoBalance = Brush.linearGradient(
    0.0f to Color(0xFF2F6BF0),
    0.55f to Color(0xFF0B5BD8),
    0.85f to Color(0xFF0A5DC2),
    1.0f to Color(0xFF08704F),
)
private val DegradadoConversar = Brush.horizontalGradient(listOf(Color(0xFF0B6B4F), Color(0xFF1D5FE0)))
private val DegradadoMas = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0B57D0)))
/** Velo suave detrás del menú del "+" abierto. */
private val VeloMenuMas = Color(0x330D1B3E)

/** Dashboard · Inicio (spec 04, docs/design/usuario_creado_onboarding.jpeg). */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    /** Botones "Ingreso" y "Gasto" del menú "+". */
    onIngreso: () -> Unit = {},
    onGasto: () -> Unit = {},
    /** Campana: abre Notificaciones (spec 05). */
    onNotificaciones: () -> Unit = {},
    /** Pestaña Metas de la barra inferior (spec 07). */
    onMetas: () -> Unit = {},
    onSesionCerrada: () -> Unit,
) {
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(Etiquetas.PANTALLA_DASHBOARD)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // La barra inferior ya se encarga de la barra de navegación del sistema.
                    .consumeWindowInsets(WindowInsets.navigationBars),
            ) {
                ContenidoDesplazable(margenHorizontal = 20.dp) {
                    Encabezado(
                        cerrandoSesion = viewModel.cerrandoSesion,
                        onCerrarSesion = { viewModel.cerrarSesion(onSesionCerrada) },
                        onNotificaciones = onNotificaciones,
                    )
                    viewModel.errorCerrarSesion?.let { MensajeError(it, Modifier.padding(top = 8.dp)) }
                    when (val estado = viewModel.estado) {
                        EstadoDashboard.Cargando -> Cargando()
                        is EstadoDashboard.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                        is EstadoDashboard.Listo -> Contenido(
                            resumen = estado.resumen,
                            balanceVisible = viewModel.balanceVisible,
                            onOjo = viewModel::alternarBalance,
                        )
                    }
                    // Espacio para que el botón "+" flotante no tape lo último.
                    Spacer(Modifier.height(88.dp))
                }
                // Menú del "+": al abrirlo, un velo suave cubre el contenido; tocarlo (o "atrás") lo cierra.
                var menuMasAbierto by remember { mutableStateOf(false) }
                BackHandler(enabled = menuMasAbierto) { menuMasAbierto = false }
                val opacidadVelo by animateFloatAsState(if (menuMasAbierto) 1f else 0f, tween(200), label = "veloMas")
                if (opacidadVelo > 0f) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .alpha(opacidadVelo)
                            .background(VeloMenuMas)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { menuMasAbierto = false },
                    )
                }
                BotonMas(
                    abierto = menuMasAbierto,
                    onAlternar = { menuMasAbierto = !menuMasAbierto },
                    onIngreso = { menuMasAbierto = false; onIngreso() },
                    onGasto = { menuMasAbierto = false; onGasto() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 16.dp),
                )
            }
            // Ya estamos en Inicio: tocar "Inicio" no hace nada.
            BarraNavegacion(onMetas = onMetas)
        }
    }
}

@Composable
private fun Encabezado(cerrandoSesion: Boolean, onCerrarSesion: () -> Unit, onNotificaciones: () -> Unit) {
    var menuAbierto by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DegradadoLogo),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Blanco, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Finanzia", style = estilo(21, FontWeight.SemiBold, altoLinea = 26))
            Text("Copiloto IA", style = estilo(14, FontWeight.SemiBold, VerdeTexto, altoLinea = 18))
        }
        // Campana sin punto azul: no hay notificaciones reales (spec 04). Abre Notificaciones (spec 05).
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onNotificaciones)
                .testTag(Etiquetas.BOTON_CAMPANA),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = AzulMarino,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Box {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AzulBoton)
                    .clickable(role = Role.Button) { menuAbierto = true }
                    .testTag(Etiquetas.BOTON_PERFIL),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Blanco, modifier = Modifier.size(24.dp))
            }
            // Excepción aprobada (spec 04): menú con una sola opción, solo al tocar.
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false },
                containerColor = Blanco,
            ) {
                DropdownMenuItem(
                    text = { Text("Cerrar sesión", style = estilo(15, FontWeight.SemiBold)) },
                    enabled = !cerrandoSesion,
                    onClick = {
                        menuAbierto = false
                        onCerrarSesion()
                    },
                )
            }
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
        CircularProgressIndicator(color = AzulProgreso, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
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
private fun ColumnScope.Contenido(resumen: ResumenDashboard, balanceVisible: Boolean, onOjo: () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Saludo(resumen)
    Spacer(Modifier.height(20.dp))
    TarjetaBalance(resumen, balanceVisible, onOjo)
    Spacer(Modifier.height(22.dp))
    TarjetaFinanziaIA(resumen)
    Spacer(Modifier.height(18.dp))
    TarjetaMeta(resumen.meta)
    Spacer(Modifier.height(24.dp))
    MovimientosRecientes(resumen.recientes)
    Spacer(Modifier.height(20.dp))
    BotonConversar()
}

@Composable
private fun Saludo(resumen: ResumenDashboard) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text("¡Hola, ${resumen.primerNombre}! 👋", style = estilo(27, FontWeight.Bold, altoLinea = 34, espaciado = -0.4))
            Text(
                "Aquí tienes el resumen financiero de hoy",
                style = estilo(16, color = GrisTexto, altoLinea = 22),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Solo con ingresos en el mes y ahorro de 0 o más (spec 04).
        resumen.ahorroMes?.let { ahorro ->
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(CircleShape)
                    .background(MentaPastilla)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag(Etiquetas.PASTILLA_SALUDABLE),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null, tint = VerdePastillaTexto, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Saludable +${FormatosDashboard.porcentaje(ahorro)}%", style = estilo(13, FontWeight.SemiBold, VerdePastillaTexto))
            }
        }
    }
}

@Composable
private fun TarjetaBalance(resumen: ResumenDashboard, visible: Boolean, onOjo: () -> Unit) {
    val forma = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, forma, ambientColor = Color(0x402F6BF0), spotColor = Color(0x402F6BF0))
            .clip(forma)
            .background(DegradadoBalance)
            .padding(horizontal = 24.dp, vertical = 22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = Blanco.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("BALANCE TOTAL", style = estilo(15, FontWeight.Medium, Blanco.copy(alpha = 0.85f), espaciado = 0.5), modifier = Modifier.weight(1f))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Blanco.copy(alpha = 0.15f))
                    .clickable(role = Role.Button, onClick = onOjo)
                    .testTag(Etiquetas.BOTON_OJO_BALANCE),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (visible) "Ocultar balance" else "Mostrar balance",
                    tint = Blanco,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                // El balance se ve completo: si un monto largo no cabe, la letra se reduce lo justo
                // (con montos que caben queda del tamaño del diseño).
                BasicText(
                    if (visible) FormatosDashboard.monto(resumen.balance) else "••••••",
                    style = estilo(38, FontWeight.ExtraBold, Blanco, altoLinea = 46, espaciado = -0.5),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = 38.sp, stepSize = 1.sp),
                    modifier = Modifier.testTag(Etiquetas.MONTO_BALANCE),
                )
                // Sin "• • • • 8492": esos dígitos no existen en los datos (spec 04).
                resumen.nombreCuenta?.let { cuenta ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = Blanco.copy(alpha = 0.8f), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cuenta $cuenta", style = estilo(14, color = Blanco.copy(alpha = 0.85f)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (resumen.tendencia.size >= 2) {
                Spacer(Modifier.width(8.dp))
                LineaTendencia(resumen.tendencia, Modifier.width(96.dp).height(46.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Blanco.copy(alpha = 0.18f)))
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RecuadroMonto("Ingresos", FormatosDashboard.ingreso(resumen.ingresosMes), Icons.Filled.ArrowDownward, MentaIngresos, AzulMarino)
            RecuadroMonto("Gastos", FormatosDashboard.gasto(resumen.gastosMes), Icons.Filled.ArrowUpward, RosaGastos, RojoFlecha)
        }
    }
}

@Composable
private fun LineaTendencia(puntos: List<Double>, modifier: Modifier) {
    Canvas(modifier.testTag(Etiquetas.LINEA_TENDENCIA)) {
        val minimo = puntos.min()
        val maximo = puntos.max()
        val rango = (maximo - minimo).takeIf { it > 0.0 } ?: 1.0
        val margen = 6.dp.toPx()
        val ancho = size.width - margen * 2
        val alto = size.height - margen * 2
        val coordenadas = puntos.mapIndexed { i, valor ->
            Offset(
                x = margen + ancho * i / (puntos.size - 1),
                y = margen + alto * (1f - ((valor - minimo) / rango).toFloat()),
            )
        }
        val linea = Path().apply {
            moveTo(coordenadas.first().x, coordenadas.first().y)
            coordenadas.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linea, MentaTendencia, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        val fin = coordenadas.last()
        drawCircle(MentaTendencia, radius = 4.dp.toPx(), center = fin)
        drawCircle(Color(0xFF0B5BD8), radius = 2.dp.toPx(), center = fin)
    }
}

@Composable
private fun RowScope.RecuadroMonto(titulo: String, monto: String, icono: ImageVector, fondoIcono: Color, colorIcono: Color) {
    Row(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Blanco.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(fondoIcono),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = estilo(14, color = Blanco.copy(alpha = 0.85f)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(monto, style = estilo(18, FontWeight.Bold, Blanco), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TarjetaFinanziaIA(resumen: ResumenDashboard) {
    val forma = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(LilaFondo)
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LilaRobot),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MoradoRobot, modifier = Modifier.size(28.dp))
                }
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VerdeInsignia),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Blanco, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Finanzia IA", style = estilo(21, FontWeight.SemiBold, altoLinea = 26), modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Copiloto",
                        style = estilo(13, FontWeight.SemiBold, MoradoChip),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LilaChip)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Text("Análisis predictivo en tiempo real", style = estilo(13, FontWeight.SemiBold, VerdeTexto, altoLinea = 18))
            }
            // Chevron sin acción por ahora (spec 04).
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MoradoChevron, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            textoConsejo(resumen),
            style = estilo(16, color = AzulMarino, altoLinea = 26),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Blanco)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
        // Sin "S N A", "3 suscripciones analizadas" ni "Ver plan →": no hay datos reales detrás (spec 04).
    }
}

/** Consejo con datos reales: la categoría con más gasto del mes, o el texto de falta de información. */
private fun textoConsejo(resumen: ResumenDashboard) = buildAnnotatedString {
    append("💡 ")
    withStyle(SpanStyle(color = Color(0xFF1E4FBF), fontWeight = FontWeight.Bold)) { append("Consejo inteligente:") }
    append(" ")
    val mayor = resumen.mayorGasto
    if (mayor == null) {
        append("Aún no hay suficiente información para darte un consejo. Registra tus ingresos y gastos para empezar.")
    } else {
        val destacado = SpanStyle(color = VerdeDinero, fontWeight = FontWeight.Bold)
        append("Tu categoría con más gasto este mes es ${mayor.categoria}: ")
        withStyle(destacado) { append(FormatosDashboard.monto(mayor.monto)) }
        append(", el ")
        withStyle(destacado) { append("${mayor.porcentaje}%") }
        append(" de tus gastos.")
    }
}

@Composable
private fun TarjetaMeta(meta: MetaDashboard?) {
    val forma = RoundedCornerShape(22.dp)
    val modificador = Modifier
        .fillMaxWidth()
        .clip(forma)
        .background(AzulMeta)
        .border(1.dp, Color(0xFFCFDDF7), forma)
        .padding(horizontal = 18.dp, vertical = 18.dp)
    if (meta == null) {
        // Estado vacío: mismo fondo y borde, solo el texto (spec 04).
        Box(modificador) {
            Text("Aún no tienes metas", style = estilo(15, FontWeight.Medium, GrisTexto))
        }
        return
    }
    val falta = (meta.montoObjetivo - meta.aportado).coerceAtLeast(0.0)
    val progreso = if (meta.montoObjetivo > 0) (meta.aportado / meta.montoObjetivo).coerceIn(0.0, 1.0).toFloat() else 0f
    Row(modificador, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "Meta: ${meta.nombre}",
                style = estilo(13, FontWeight.SemiBold, Blanco),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VerdeInsignia)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Llevas ${FormatosDashboard.montoSinDecimales(meta.aportado)} de ${FormatosDashboard.montoSinDecimales(meta.montoObjetivo)}",
                style = estilo(20, FontWeight.Bold, altoLinea = 26),
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(PistaProgreso),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progreso)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(AzulProgreso),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "¡A solo ${FormatosDashboard.montoSinDecimales(falta)} de lograr tu objetivo!",
                style = estilo(12, color = GrisTexto),
            )
        }
        Spacer(Modifier.width(14.dp))
        Box {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MentaMeta),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PriceCheck, contentDescription = null, tint = VerdeIconoMeta, modifier = Modifier.size(32.dp))
            }
            Icon(
                Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = AzulProgreso,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(16.dp),
            )
        }
    }
}

@Composable
private fun MovimientosRecientes(filas: List<FilaMovimiento>) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Movimientos recientes", style = estilo(20, FontWeight.SemiBold), modifier = Modifier.weight(1f))
        // "Ver todos" sin acción por ahora (spec 04).
        Text("Ver todos", style = estilo(15, FontWeight.SemiBold, Color(0xFF1E4FBF)))
    }
    Spacer(Modifier.height(12.dp))
    val forma = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (filas.isEmpty()) {
            Text(
                "Aún no tienes movimientos",
                style = estilo(14, color = GrisSuave),
                modifier = Modifier.padding(vertical = 18.dp),
            )
        } else {
            filas.forEach { FilaMovimientoReciente(it) }
        }
    }
}

@Composable
private fun FilaMovimientoReciente(fila: FilaMovimiento) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag(Etiquetas.FILA_MOVIMIENTO),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = colorDeCategoria(fila.colorCategoria)
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color?.copy(alpha = 0.25f) ?: FondoIconoNeutro),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconoDeCategoria(fila.categoria), contentDescription = null, tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(fila.titulo, style = estilo(16, FontWeight.SemiBold, altoLinea = 22), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(fila.subtitulo, style = estilo(13, color = GrisTexto, altoLinea = 18), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(max = 150.dp)) {
            Text(
                fila.monto,
                style = estilo(16, FontWeight.Bold, if (fila.esIngreso) VerdeDinero else RojoTexto, altoLinea = 22),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            fila.categoria?.let {
                Text(
                    it,
                    style = estilo(13, if (fila.esIngreso) FontWeight.SemiBold else FontWeight.Normal, if (fila.esIngreso) VerdeDinero else GrisTexto, altoLinea = 18),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Ícono según el nombre de la categoría (spec 04); genérico si no hay uno definido. */
private fun iconoDeCategoria(categoria: String?): ImageVector = when (categoria) {
    "Gastos Fijos" -> Icons.Filled.Home
    "Comida" -> Icons.Filled.ShoppingCart
    "Diversión" -> Icons.Filled.MusicNote
    "Transporte" -> Icons.Filled.DirectionsCar
    CATEGORIA_INGRESOS, "Mensual", "Quincenal", "Semanal", "Diario" -> Icons.Filled.Payments
    else -> Icons.Filled.ReceiptLong
}

/** Color de la tabla colores ("#22C55E", "22C55E" o un nombre como "red"); null si no se entiende. */
private fun colorDeCategoria(valor: String?): Color? {
    val texto = valor?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val conAlmohadilla = if (Regex("^[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$").matches(texto)) "#$texto" else texto
    return runCatching { Color(android.graphics.Color.parseColor(conAlmohadilla)) }.getOrNull()
}

@Composable
private fun BotonConversar() {
    val forma = RoundedCornerShape(20.dp)
    // Sin acción por ahora (spec 04).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .shadow(8.dp, forma, ambientColor = Color(0x400B6B4F), spotColor = Color(0x400B6B4F))
            .clip(forma)
            .background(DegradadoConversar)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = Blanco, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text("Conversar con Finanzia IA", style = estilo(17, FontWeight.Bold, Blanco), modifier = Modifier.weight(1f, fill = false))
        Spacer(Modifier.width(12.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Blanco, modifier = Modifier.size(22.dp))
    }
}

/**
 * Botón flotante "+". Al tocarlo suben dos botones, "Ingreso" y "Gasto", y el "+" gira hasta
 * quedar como una "×" para cerrar. Cada botón cierra el menú y abre su pantalla de registro.
 */
@Composable
private fun BotonMas(
    abierto: Boolean,
    onAlternar: () -> Unit,
    onIngreso: () -> Unit,
    onGasto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val giro by animateFloatAsState(if (abierto) 45f else 0f, tween(250), label = "giroMas")
    Column(
        modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // "Ingreso" (arriba) aparece un instante después de "Gasto", para que se vean subir en cascada.
        OpcionAnimada(visible = abierto, retraso = 60) {
            OpcionMas(
                texto = "Ingreso",
                icono = Icons.Filled.ArrowDownward,
                fondoIcono = MentaIngresos,
                colorIcono = AzulMarino,
                etiqueta = Etiquetas.BOTON_INGRESO,
                onClick = onIngreso,
            )
        }
        OpcionAnimada(visible = abierto, retraso = 0) {
            OpcionMas(
                texto = "Gasto",
                icono = Icons.Filled.ArrowUpward,
                fondoIcono = RosaGastos,
                colorIcono = RojoFlecha,
                etiqueta = Etiquetas.BOTON_GASTO,
                onClick = onGasto,
            )
        }
        Box(
            Modifier
                .testTag(Etiquetas.BOTON_MAS)
                .size(60.dp)
                .shadow(10.dp, CircleShape, ambientColor = Color(0x600B57D0), spotColor = Color(0x600B57D0))
                .clip(CircleShape)
                .background(DegradadoMas)
                .clickable(role = Role.Button, onClick = onAlternar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = if (abierto) "Cerrar" else "Agregar",
                tint = Blanco,
                modifier = Modifier.size(32.dp).rotate(giro),
            )
        }
    }
}

/** Sube desde el "+" mientras aparece, y baja mientras se oculta. */
@Composable
private fun ColumnScope.OpcionAnimada(visible: Boolean, retraso: Int, contenido: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220, delayMillis = retraso)) +
            slideInVertically(tween(220, delayMillis = retraso)) { alto -> alto },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { alto -> alto },
    ) {
        contenido()
    }
}

/** Una opción del menú: etiqueta blanca a la izquierda y botón redondo con su ícono. */
@Composable
private fun OpcionMas(
    texto: String,
    icono: ImageVector,
    fondoIcono: Color,
    colorIcono: Color,
    etiqueta: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .testTag(etiqueta)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            // Centra el botón pequeño (46 dp) con el "+" (60 dp).
            .padding(end = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            texto,
            style = estilo(15, FontWeight.SemiBold, AzulMarino),
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
                .clip(RoundedCornerShape(12.dp))
                .background(Blanco)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .size(46.dp)
                .shadow(6.dp, CircleShape, ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
                .clip(CircleShape)
                .background(fondoIcono),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
        }
    }
}

// --- Previews: datos de vista previa, nunca los ejemplos del diseño ---

private val datosVistaPrevia = DatosDashboard(
    nombreUsuario = "Vista Previa",
    cuentas = listOf(CuentaDashboard(CUENTA_PRINCIPAL, 0.0)),
    movimientos = emptyList(),
    meta = null,
)

@PreviewsFinanzia
@Composable
private fun DashboardVacioPreview() {
    FinanziaTheme {
        DashboardScreen(
            // Igual que en la app: el ViewModel se crea con viewModel { }, no directamente.
            viewModel { DashboardViewModel(DashboardFijo(Result.success(datosVistaPrevia)), FakeAuthRepository(), ahora = { Instant.EPOCH }) },
            onSesionCerrada = {},
        )
    }
}

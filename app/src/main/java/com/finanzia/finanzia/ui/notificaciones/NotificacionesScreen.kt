package com.finanzia.finanzia.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzia.finanzia.datos.ClaseNotificacion
import com.finanzia.finanzia.datos.Notificacion
import com.finanzia.finanzia.datos.NotificacionesFijas
import com.finanzia.finanzia.datos.TipoNotificacion
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BarraNavegacion
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

// Colores tomados de docs/design/notificaciones_1.png
private val AzulBotonClaro = Color(0xFFE3EBFA)
private val MentaFiltro = Color(0xFFCDF3E2)
private val VerdeFiltro = Color(0xFF0B6B4F)
private val AzulPunto = Color(0xFF1D4ED8)
private val GrisPuntoLeida = Color(0xFFCBD5E1)
private val AzulIconoClaro = Color(0xFFDCE7FA)
private val AzulTarjeta = Color(0xFF2563EB)
private val MoradoProgreso = Color(0xFF6D5BD0)
private val PistaProgreso = Color(0xFFE6E9F5)
private val FondoTarjetaIA = Color(0xFFEEF3FC)
private val VerdeIconoIA = Color(0xFF0B6B4F)
private val MentaIA = Color(0xFF8EF3CF)
private val VerdeTextoIA = Color(0xFF0D7A55)
private val AzulProgresoCarga = Color(0xFF1D4ED8)

/** Spec 05 · Notificaciones (docs/design/notificaciones_1.png). Se abre desde la campana del Dashboard. */
@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel,
    onAtras: () -> Unit,
    onMetas: () -> Unit = {},
    ahora: () -> Instant = Instant::now,
    zona: ZoneId = ZoneId.systemDefault(),
) {
    PantallaBase(fondo = Modifier.background(FondoPantalla).testTag(Etiquetas.PANTALLA_NOTIFICACIONES)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // La barra inferior ya se encarga de la barra de navegación del sistema.
                    .consumeWindowInsets(WindowInsets.navigationBars),
            ) {
                ContenidoDesplazable(margenHorizontal = 20.dp) {
                    Encabezado(sinRevisar = viewModel.sinRevisar, onAtras = onAtras)
                    Spacer(Modifier.height(16.dp))
                    Filtros(
                        total = viewModel.todas.size,
                        seleccionado = viewModel.filtro,
                        onElegir = viewModel::elegirFiltro,
                    )
                    when (val estado = viewModel.estado) {
                        EstadoNotificaciones.Cargando -> Cargando()
                        is EstadoNotificaciones.Error -> ErrorCarga(estado.mensaje, onReintentar = viewModel::cargar)
                        is EstadoNotificaciones.Listo -> Lista(viewModel.visibles, ahora(), zona)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            // A esta pantalla se llega desde Inicio: "Inicio" queda seleccionada y vuelve al Dashboard.
            BarraNavegacion(onInicio = onAtras, onMetas = onMetas)
        }
    }
}

@Composable
private fun Encabezado(sinRevisar: Int, onAtras: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AzulBotonClaro)
                .clickable(role = Role.Button, onClick = onAtras)
                .testTag(Etiquetas.BOTON_ATRAS),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AzulMarino, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Notificaciones", style = estilo(22, FontWeight.ExtraBold, altoLinea = 28, espaciado = -0.3))
            Text(
                if (sinRevisar == 1) "1 alerta activa sin revisar" else "$sinRevisar alertas activas sin revisar",
                style = estilo(12, color = GrisTexto, altoLinea = 16),
                modifier = Modifier.testTag(Etiquetas.CONTADOR_NOTIFICACIONES),
            )
        }
        Spacer(Modifier.width(8.dp))
        // Doble check y ajustes: se ven como en el diseño, sin acción por ahora (spec 05).
        BotonCuadrado(Icons.Filled.DoneAll, "Marcar todas como leídas")
        Spacer(Modifier.width(10.dp))
        BotonCuadrado(Icons.Filled.Tune, "Ajustes de notificaciones")
    }
}

@Composable
private fun BotonCuadrado(icono: ImageVector, descripcion: String) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AzulBotonClaro),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = descripcion, tint = AzulMarino, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun Filtros(total: Int, seleccionado: TipoNotificacion?, onElegir: (TipoNotificacion?) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PastillaFiltro("Todas ($total)", icono = null, seleccionada = seleccionado == null) { onElegir(null) }
        TipoNotificacion.entries.forEach { tipo ->
            PastillaFiltro(tipo.filtro, iconoFiltro(tipo), seleccionada = seleccionado == tipo) { onElegir(tipo) }
        }
    }
}

private fun iconoFiltro(tipo: TipoNotificacion): ImageVector = when (tipo) {
    TipoNotificacion.PRESUPUESTO -> Icons.Outlined.DataUsage
    TipoNotificacion.PAGO -> Icons.Outlined.CalendarMonth
    TipoNotificacion.SEGURIDAD_IA -> Icons.Filled.AutoAwesome
}

@Composable
private fun PastillaFiltro(texto: String, icono: ImageVector?, seleccionada: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    val color = if (seleccionada) VerdeFiltro else AzulMarino
    Row(
        modifier = Modifier
            .clip(forma)
            .background(if (seleccionada) MentaFiltro else Blanco)
            .then(if (seleccionada) Modifier else Modifier.border(1.dp, BordeTarjeta, forma))
            .clickable(role = Role.Tab, onClick = onClick)
            .testTag(Etiquetas.FILTRO_NOTIFICACIONES)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icono?.let {
            Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = estilo(14, if (seleccionada) FontWeight.SemiBold else FontWeight.Medium, color), maxLines = 1)
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
        CircularProgressIndicator(color = AzulProgresoCarga, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
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
private fun Lista(notificaciones: List<Notificacion>, ahora: Instant, zona: ZoneId) {
    if (notificaciones.isEmpty()) {
        // Estado vacío (spec 05): texto breve y gris debajo de los filtros.
        Text(
            "No tienes notificaciones por ahora",
            style = estilo(15, color = GrisSuave),
            modifier = Modifier
                .padding(top = 28.dp)
                .testTag(Etiquetas.ESTADO_VACIO_NOTIFICACIONES),
        )
        return
    }
    val hoy = ahora.atZone(zona).toLocalDate()
    val (deHoy, anteriores) = notificaciones.partition { it.fecha.atZone(zona).toLocalDate() == hoy }
    if (deHoy.isNotEmpty()) {
        Spacer(Modifier.height(22.dp))
        EncabezadoSeccion()
        deHoy.forEach {
            Spacer(Modifier.height(14.dp))
            TarjetaNotificacion(it, ahora)
        }
    }
    // TODO: el diseño solo muestra "HOY". Falta definir si hay secciones como "AYER" o "ESTA SEMANA"
    // (spec 05, decisión pendiente 9). Mientras tanto, las anteriores van después, sin encabezado.
    if (anteriores.isNotEmpty()) Spacer(Modifier.height(8.dp))
    anteriores.forEach {
        Spacer(Modifier.height(14.dp))
        TarjetaNotificacion(it, ahora)
    }
}

@Composable
private fun EncabezadoSeccion() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(AzulPunto))
        Spacer(Modifier.width(8.dp))
        Text("HOY", style = estilo(13, FontWeight.Bold, AzulMarino, espaciado = 0.6), modifier = Modifier.weight(1f))
        Text("Tiempo real", style = estilo(12, FontWeight.Medium, AzulEnlace))
    }
}

@Composable
private fun TarjetaNotificacion(notificacion: Notificacion, ahora: Instant) {
    val hace = haceCuanto(notificacion.fecha, ahora)
    when (notificacion.tipo) {
        // Alertas de presupuesto: ícono azul sobre cuadro azul claro (como "Límite de…" del diseño).
        TipoNotificacion.PRESUPUESTO -> Tarjeta {
            CabeceraTarjeta(
                icono = iconoClase(notificacion.clase),
                fondoIcono = AzulIconoClaro,
                colorIcono = AzulTarjeta,
                titulo = notificacion.titulo,
                subtitulo = "$hace • Presupuestos inteligentes",
                leida = notificacion.leida,
            )
            Mensaje(notificacion.mensaje)
        }
        // Recordatorios: ícono blanco sobre cuadro azul (como la tarjeta de vencimiento del diseño).
        TipoNotificacion.PAGO -> Tarjeta {
            CabeceraTarjeta(
                icono = iconoClase(notificacion.clase),
                fondoIcono = AzulTarjeta,
                colorIcono = Blanco,
                titulo = notificacion.titulo,
                subtitulo = "$hace • ${origenRecordatorio(notificacion.clase)}",
                leida = notificacion.leida,
            )
            Mensaje(notificacion.mensaje)
            notificacion.progreso?.let { BarraProgreso(it) }
        }
        TipoNotificacion.SEGURIDAD_IA ->
            if (notificacion.clase == ClaseNotificacion.CONSEJO_IA) {
                // Consejo de IA: tarjeta "Oportunidad de…" del diseño, con la pastilla "IA Alert".
                Tarjeta(fondo = FondoTarjetaIA) {
                    CabeceraTarjeta(
                        icono = Icons.Filled.AutoAwesome,
                        fondoIcono = VerdeIconoIA,
                        colorIcono = Blanco,
                        titulo = notificacion.titulo,
                        subtitulo = "$hace • Finanzia IA",
                        leida = notificacion.leida,
                        extraDerecha = {
                            Text(
                                "IA Alert",
                                style = estilo(12, FontWeight.SemiBold, VerdeTextoIA),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MentaIA)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        },
                    )
                    Mensaje(notificacion.mensaje)
                }
            } else {
                Tarjeta {
                    CabeceraTarjeta(
                        icono = iconoClase(notificacion.clase),
                        fondoIcono = AzulIconoClaro,
                        colorIcono = AzulTarjeta,
                        titulo = notificacion.titulo,
                        subtitulo = "$hace • Seguridad",
                        leida = notificacion.leida,
                    )
                    Mensaje(notificacion.mensaje)
                }
            }
    }
}

@Composable
private fun Mensaje(texto: String) {
    Spacer(Modifier.height(12.dp))
    Text(texto, style = estilo(15, color = GrisTexto, altoLinea = 22))
}

/** Barra morada del diseño; aquí muestra el avance de una meta. */
@Composable
private fun BarraProgreso(progreso: Double) {
    Spacer(Modifier.height(14.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(PistaProgreso),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progreso.coerceIn(0.0, 1.0).toFloat())
                .height(8.dp)
                .clip(CircleShape)
                .background(MoradoProgreso),
        )
    }
}

private fun iconoClase(clase: ClaseNotificacion): ImageVector = when (clase) {
    ClaseNotificacion.GASTO_MAYOR -> Icons.AutoMirrored.Filled.TrendingUp
    ClaseNotificacion.GASTO_INUSUAL -> Icons.Filled.PriorityHigh
    ClaseNotificacion.GASTOS_SUPERAN_INGRESOS -> Icons.Filled.SwapVert
    ClaseNotificacion.SALDO_BAJO -> Icons.Outlined.AccountBalanceWallet
    ClaseNotificacion.REGISTRAR_MOVIMIENTOS -> Icons.Filled.EditNote
    ClaseNotificacion.APORTE_META -> Icons.Filled.Savings
    ClaseNotificacion.DIA_INGRESO -> Icons.Filled.Payments
    ClaseNotificacion.RESUMEN_MES -> Icons.Outlined.CalendarMonth
    ClaseNotificacion.AVANCE_META -> Icons.Filled.Flag
    ClaseNotificacion.META_CUMPLIDA -> Icons.Filled.EmojiEvents
    ClaseNotificacion.INICIO_SESION -> Icons.AutoMirrored.Filled.Login
    ClaseNotificacion.CONTRASENA_CAMBIADA -> Icons.Filled.Lock
    ClaseNotificacion.CONSEJO_IA -> Icons.Filled.AutoAwesome
}

private fun origenRecordatorio(clase: ClaseNotificacion): String = when (clase) {
    ClaseNotificacion.APORTE_META, ClaseNotificacion.AVANCE_META, ClaseNotificacion.META_CUMPLIDA -> "Metas"
    ClaseNotificacion.RESUMEN_MES -> "Resumen mensual"
    else -> "Recordatorio"
}

/** Contenedor blanco y redondeado de cada tarjeta. */
@Composable
private fun Tarjeta(fondo: Color = Blanco, contenido: @Composable () -> Unit) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(fondo)
            .padding(16.dp),
    ) {
        contenido()
    }
}

/** Ícono en cuadro, título en negrita, menú de tres puntos y la línea "• Hace … • origen". */
@Composable
private fun CabeceraTarjeta(
    icono: ImageVector,
    fondoIcono: Color,
    colorIcono: Color,
    titulo: String,
    subtitulo: String?,
    leida: Boolean,
    extraDerecha: (@Composable () -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(fondoIcono),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = estilo(18, FontWeight.SemiBold, altoLinea = 24))
            subtitulo?.let {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            // Punto azul = sin revisar; gris = ya revisada.
                            .background(if (leida) GrisPuntoLeida else AzulPunto),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(it, style = estilo(12, color = GrisTexto, altoLinea = 16))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        if (extraDerecha != null) {
            extraDerecha()
        } else {
            // Menú de tres puntos: sin acción por ahora (spec 05).
            Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones", tint = AzulMarino, modifier = Modifier.size(22.dp))
        }
    }
}

/** "Hace un momento", "Hace 20 min", "Hace 2 horas", "Hace 3 días". */
fun haceCuanto(fecha: Instant, ahora: Instant): String {
    val minutos = Duration.between(fecha, ahora).toMinutes().coerceAtLeast(0)
    return when {
        minutos < 1 -> "Hace un momento"
        minutos < 60 -> "Hace $minutos min"
        minutos < 60 * 24 -> (minutos / 60).let { if (it == 1L) "Hace 1 hora" else "Hace $it horas" }
        else -> (minutos / (60 * 24)).let { if (it == 1L) "Hace 1 día" else "Hace $it días" }
    }
}

// --- Previews: datos de vista previa, nunca los ejemplos del diseño ---

private val ahoraVistaPrevia: Instant = Instant.parse("2026-01-15T15:00:00Z")

private val notificacionesVistaPrevia = listOf(
    Notificacion(
        id = 1, clase = ClaseNotificacion.GASTO_MAYOR, titulo = "Título de vista previa",
        mensaje = "Mensaje de vista previa.", fecha = ahoraVistaPrevia.minusSeconds(20 * 60), leida = false,
    ),
    Notificacion(
        id = 2, clase = ClaseNotificacion.AVANCE_META, titulo = "Título de vista previa",
        mensaje = "Mensaje de vista previa.", fecha = ahoraVistaPrevia.minusSeconds(2 * 3600), leida = false,
        progreso = 0.5,
    ),
    Notificacion(
        id = 3, clase = ClaseNotificacion.CONSEJO_IA, titulo = "Título de vista previa",
        mensaje = "Mensaje de vista previa.", fecha = ahoraVistaPrevia.minusSeconds(3 * 3600), leida = true,
    ),
    Notificacion(
        id = 4, clase = ClaseNotificacion.INICIO_SESION, titulo = "Título de vista previa",
        mensaje = "Mensaje de vista previa.", fecha = ahoraVistaPrevia.minusSeconds(4 * 3600), leida = true,
    ),
)

@PreviewsFinanzia
@Composable
private fun NotificacionesVacioPreview() {
    FinanziaTheme {
        NotificacionesScreen(
            viewModel { NotificacionesViewModel(NotificacionesFijas(Result.success(emptyList()))) },
            onAtras = {},
        )
    }
}

@PreviewsFinanzia
@Composable
private fun NotificacionesConTarjetasPreview() {
    FinanziaTheme {
        NotificacionesScreen(
            viewModel { NotificacionesViewModel(NotificacionesFijas(Result.success(notificacionesVistaPrevia))) },
            onAtras = {},
            ahora = { ahoraVistaPrevia },
            zona = ZoneId.of("UTC"),
        )
    }
}

package com.finanzia.finanzia.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import com.finanzia.finanzia.ui.PreviewsFinanzia
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.componentes.BotonAtras
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.CuadroIcono
import com.finanzia.finanzia.ui.componentes.EnlaceGris
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.MilesVisualTransformation
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.Pastilla
import com.finanzia.finanzia.ui.componentes.TarjetaOpcion
import com.finanzia.finanzia.ui.componentes.esCompacto
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulIntenso
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.DegradadoIngreso
import com.finanzia.finanzia.ui.theme.ErrorRojo
import com.finanzia.finanzia.ui.theme.ErrorRosaFondo
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisEtiqueta
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.RojoGasto
import com.finanzia.finanzia.ui.theme.RojoOscuro
import com.finanzia.finanzia.ui.theme.RojoTexto
import com.finanzia.finanzia.ui.theme.RosaBorde
import com.finanzia.finanzia.ui.theme.RosaBordeSuave
import com.finanzia.finanzia.ui.theme.RosaClaro
import com.finanzia.finanzia.ui.theme.RosaFondo
import com.finanzia.finanzia.ui.theme.VerdeBordeClaro
import com.finanzia.finanzia.ui.theme.VerdeClaro
import com.finanzia.finanzia.ui.theme.VerdeMarca
import com.finanzia.finanzia.ui.theme.VerdeTexto

private val VerdeCampo = Color(0xFF1FA77A)
private val FondoSeleccionIngreso = Color(0xFFEDFBF4)
private val FondoSeleccionGasto = Color(0xFFFFF1F3)

/** Datos de cada tarjeta de opción: emoji, color del cuadro del emoji, título y descripción. */
private data class Opcion<T>(val valor: T, val emoji: String, val fondoEmoji: Color, val titulo: String, val descripcion: String)

private val opcionesIngreso = listOf(
    Opcion(FrecuenciaIngreso.MENSUAL, "🗓️", Color(0xFF1FAF7A), "Mensual", "Sueldo o nómina cada 30 días"),
    Opcion(FrecuenciaIngreso.QUINCENAL, "📅", Color(0xFFEEF2FA), "Quincenal", "Pago los días 15 y 30"),
    Opcion(FrecuenciaIngreso.SEMANAL, "⚡", Color(0xFFEAF0FC), "Semanal", "Cobro cada 7 días o fin de semana"),
    Opcion(FrecuenciaIngreso.DIARIO, "☀️", Color(0xFFFEF6DE), "Diario", "Ventas o ingresos constantes por día"),
)

private val opcionesGasto = listOf(
    Opcion(CategoriaGasto.GASTOS_FIJOS, "🏠", Color(0xFFFDE2E6), "Gastos Fijos", "Renta, servicios, internet, luz, agua"),
    Opcion(CategoriaGasto.COMIDA, "🍔", Color(0xFFFFF4DD), "Comida", "Supermercado, despensa y restaurantes"),
    Opcion(CategoriaGasto.DIVERSION, "🎉", Color(0xFFFCE7F3), "Diversión", "Streaming, salidas, ocio y cine"),
    Opcion(CategoriaGasto.TRANSPORTE, "🚗", Color(0xFFE6EEFB), "Transporte", "Gasolina, transporte público, Uber/DiDi"),
)

/**
 * Pantalla 4 · Paso 2 • Ingresos (docs/design/crear_cuenta_4.jpeg).
 *
 * Con [desdeDashboard] = true es "Nuevo ingreso" (botón "+" del Dashboard): sin "Paso 2", sin
 * "Omitir" y sin la sección de frecuencia (la frecuencia se elige solo en el onboarding).
 */
@Composable
fun PrimerIngresoScreen(
    viewModel: PrimerIngresoViewModel,
    desdeDashboard: Boolean = false,
    onAtras: () -> Unit,
    onTerminar: () -> Unit,
) {
    EstructuraPaso2(
        pastilla = {
            Pastilla(if (desdeDashboard) "Nuevo ingreso" else "Paso 2 • Ingresos", VerdeClaro, VerdeBordeClaro, VerdeTexto)
        },
        icono = Icons.Filled.ArrowDownward,
        colorIcono = VerdeMarca,
        fondoIcono = Color(0xFFDDF6EA),
        titulo = if (desdeDashboard) "Registra un ingreso" else "Registra tu primer ingreso",
        tamanoTitulo = 28,
        descripcion = if (desdeDashboard) {
            "Anota el monto que recibiste. Se sumará a tu saldo."
        } else {
            "Indica la frecuencia con la que recibes dinero y el monto aproximado para proyectar tus metas."
        },
        onAtras = onAtras,
        onOmitir = if (desdeDashboard) null else onTerminar,
    ) {
        TarjetaMonto(
            error = viewModel.errorMonto,
            etiqueta = "MONTO DEL INGRESO (COP)",
            ayuda = "Podrás modificar o agregar más fuentes de ingreso después.",
            iconoAyuda = Icons.Outlined.CheckCircle,
            colorAyuda = VerdeMarca,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$", style = estilo(34, FontWeight.ExtraBold, VerdeMarca))
                Spacer(Modifier.width(14.dp))
                CampoMonto(
                    monto = viewModel.monto,
                    onCambio = viewModel::cambiarMonto,
                    estiloTexto = estilo(28, FontWeight.ExtraBold, altoLinea = 34),
                ) { campo ->
                    val conError = viewModel.errorMonto != null
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .background(if (conError) ErrorRosaFondo else Color.Transparent)
                            .border(2.dp, if (conError) ErrorRojo else VerdeCampo, RoundedCornerShape(2.dp))
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) { campo() }
                }
            }
        }
        // La frecuencia se elige siempre, también desde el botón "+": es la categoría del ingreso,
        // como la categoría de los gastos (Valentina, 2026-10-26).
        Spacer(Modifier.height(24.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text("TIPO / FRECUENCIA DE INGRESO", style = estilo(14, FontWeight.Bold, GrisEtiqueta, espaciado = 0.6))
            Text("Selecciona uno", style = estilo(13, FontWeight.Bold, VerdeTexto))
        }
        Spacer(Modifier.height(14.dp))
        CuadriculaOpciones(
            opciones = opcionesIngreso,
            seleccionada = viewModel.opcion,
            onSeleccionar = viewModel::seleccionar,
            colorSeleccion = VerdeMarca,
            fondoSeleccion = FondoSeleccionIngreso,
        )
        // El consejo sobre la frecuencia solo aplica en el onboarding.
        if (!desdeDashboard) {
            Spacer(Modifier.height(18.dp))
            RecuadroConsejo(
                texto = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF1E4FBF), fontWeight = FontWeight.Bold)) { append("Consejo de Finanzia:") }
                    append(" Configurar tu frecuencia nos permite predecir cuándo tendrás dinero disponible para tus ahorros.")
                },
                fondo = Brush.horizontalGradient(listOf(Color(0xFFEEF5FC), Color(0xFFEAF7F2))),
                borde = Color(0xFFDCE8EE),
                estiloTexto = estilo(14, color = GrisEtiqueta, altoLinea = 21),
            )
        }
        Spacer(Modifier.height(22.dp))
        viewModel.errorGeneral?.let { MensajeError(it, Modifier.padding(start = 4.dp, bottom = 12.dp)) }
        BotonPrincipal(
            texto = if (desdeDashboard) "Guardar ingreso" else "Guardar y continuar",
            fondo = DegradadoIngreso,
            onClick = { viewModel.guardar(onTerminar) },
            cargando = viewModel.guardando,
            alto = 60.dp,
            forma = RoundedCornerShape(20.dp),
            colorSombra = Color(0x402563EB),
        )
    }
}

/**
 * Pantalla 5 · Paso 2 • Gastos (docs/design/crear_cuenta_5.jpeg).
 *
 * Con [desdeDashboard] = true es "Nuevo gasto" (botón "+" del Dashboard): sin "Paso 2" ni "Omitir".
 */
@Composable
fun PrimerGastoScreen(
    viewModel: PrimerGastoViewModel,
    desdeDashboard: Boolean = false,
    onAtras: () -> Unit,
    onTerminar: () -> Unit,
) {
    EstructuraPaso2(
        pastilla = {
            Pastilla(if (desdeDashboard) "Nuevo gasto" else "Paso 2 • Gastos", RosaClaro, RosaBorde, RojoTexto, tamano = 16)
        },
        icono = Icons.Filled.ArrowUpward,
        colorIcono = RojoGasto,
        fondoIcono = Color(0xFFFDE2E6),
        titulo = if (desdeDashboard) "Registra un gasto" else "Registra tu primer gasto",
        tamanoTitulo = 24,
        descripcion = if (desdeDashboard) {
            "Anota el monto y elige la categoría del gasto. Se restará de tu saldo."
        } else {
            "Selecciona la categoría principal y anota cuánto sueles destinar para comenzar a optimizar."
        },
        onAtras = onAtras,
        onOmitir = if (desdeDashboard) null else onTerminar,
    ) {
        TarjetaMonto(
            error = viewModel.errorMonto,
            etiqueta = if (desdeDashboard) "MONTO DEL GASTO (COP)" else "MONTO ESTIMADO DEL GASTO (COP)",
            ayuda = "Finanzia detectará oportunidades de ahorro en esta categoría.",
            iconoAyuda = Icons.Outlined.Info,
            colorAyuda = RojoGasto,
        ) {
            CampoMonto(
                monto = viewModel.monto,
                onCambio = viewModel::cambiarMonto,
                estiloTexto = estilo(30, FontWeight.ExtraBold, altoLinea = 38),
            ) { campo ->
                Column(Modifier.background(if (viewModel.errorMonto != null) ErrorRosaFondo else Color.Transparent)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$", style = estilo(30, FontWeight.ExtraBold, RojoGasto, altoLinea = 38))
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f)) { campo() }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0xFFE0436A)),
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text("CATEGORÍA DE GASTO", style = estilo(14, FontWeight.SemiBold, GrisEtiqueta, espaciado = 0.8))
            // TODO: "Administrar categorías" se ve pero no tiene acción ("Decisiones pendientes" 5).
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Settings, contentDescription = null, tint = AzulMarino, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Administrar categorías", style = estilo(14, FontWeight.SemiBold))
            }
        }
        Spacer(Modifier.height(14.dp))
        CuadriculaOpciones(
            opciones = opcionesGasto,
            seleccionada = viewModel.opcion,
            onSeleccionar = viewModel::seleccionar,
            colorSeleccion = RojoGasto,
            fondoSeleccion = FondoSeleccionGasto,
        )
        Spacer(Modifier.height(18.dp))
        RecuadroConsejo(
            texto = buildAnnotatedString {
                withStyle(SpanStyle(color = RojoOscuro, fontWeight = FontWeight.Bold)) { append("Dato de Finanzia:") }
                append(" Identificar tus gastos fijos es la clave para saber exactamente cuánto dinero libre tienes para ahorrar e invertir.")
            },
            fondo = SolidColor(RosaFondo),
            borde = RosaBordeSuave,
            estiloTexto = estilo(15, color = GrisEtiqueta, altoLinea = 22),
        )
        Spacer(Modifier.height(30.dp))
        viewModel.errorGeneral?.let { MensajeError(it, Modifier.padding(start = 4.dp, bottom = 12.dp)) }
        BotonPrincipal(
            texto = if (desdeDashboard) "Guardar gasto" else "Guardar gasto y finalizar",
            fondo = SolidColor(AzulIntenso),
            onClick = { viewModel.guardar(onTerminar) },
            cargando = viewModel.guardando,
            alto = 58.dp,
            forma = RoundedCornerShape(18.dp),
            colorSombra = Color(0x400B63F0),
        )
    }
}

/**
 * Parte común: botón atrás, pastilla, título con ícono, contenido y "Omitir este paso por ahora"
 * (este último no se muestra si [onOmitir] es null, como al registrar desde el Dashboard).
 */
@Composable
private fun EstructuraPaso2(
    pastilla: @Composable () -> Unit,
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    titulo: String,
    tamanoTitulo: Int,
    descripcion: String,
    onAtras: () -> Unit,
    onOmitir: (() -> Unit)?,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    PantallaBase(fondo = Modifier.background(FondoPantalla)) {
        ContenidoDesplazable(margenHorizontal = 18.dp) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                BotonAtras(onAtras, Modifier.align(Alignment.CenterStart))
                // El margen deja la pastilla centrada sin que nunca tape el botón atrás.
                Box(Modifier.padding(horizontal = 54.dp)) { pastilla() }
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CuadroIcono(fondo = fondoIcono, tamano = 32.dp, forma = RoundedCornerShape(8.dp)) {
                    Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    titulo,
                    style = estilo(tamanoTitulo, FontWeight.ExtraBold, altoLinea = tamanoTitulo + 8, espaciado = -0.4),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(descripcion, style = estilo(15, color = GrisTexto, altoLinea = 22))
            Spacer(Modifier.height(20.dp))
            contenido()
            Spacer(Modifier.height(16.dp))
            onOmitir?.let { EnlaceGris("Omitir este paso por ahora", it, Modifier.align(Alignment.CenterHorizontally)) }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TarjetaMonto(
    error: String?,
    etiqueta: String,
    ayuda: String,
    iconoAyuda: ImageVector,
    colorAyuda: Color,
    campo: @Composable () -> Unit,
) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x10000000), spotColor = Color(0x10000000))
            .clip(forma)
            .background(Blanco)
            .border(1.dp, BordeTarjeta, forma)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(etiqueta, style = estilo(14, FontWeight.SemiBold, if (error != null) ErrorRojo else GrisEtiqueta, espaciado = 0.9))
        Spacer(Modifier.height(12.dp))
        campo()
        if (error != null) MensajeError(error, Modifier.padding(top = 10.dp))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(iconoAyuda, contentDescription = null, tint = colorAyuda, modifier = Modifier.padding(top = 2.dp).size(17.dp))
            Spacer(Modifier.width(10.dp))
            Text(ayuda, style = estilo(13, color = GrisSuave, altoLinea = 19))
        }
    }
}

/** Campo numérico: solo dígitos, comas de miles automáticas y "0" gris cuando está vacío. */
@Composable
private fun CampoMonto(
    monto: String,
    onCambio: (String) -> Unit,
    estiloTexto: TextStyle,
    marco: @Composable (campo: @Composable () -> Unit) -> Unit,
) {
    BasicTextField(
        value = monto,
        onValueChange = onCambio,
        singleLine = true,
        textStyle = estiloTexto,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        visualTransformation = MilesVisualTransformation,
        cursorBrush = SolidColor(AzulMarino),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(Etiquetas.CAMPO_MONTO),
        decorationBox = { campo ->
            marco {
                Box {
                    if (monto.isEmpty()) Text("0", style = estiloTexto.copy(color = GrisPlaceholder))
                    campo()
                }
            }
        },
    )
}

/** Dos filas de dos tarjetas; solo una seleccionada a la vez. */
@Composable
private fun <T> CuadriculaOpciones(
    opciones: List<Opcion<T>>,
    seleccionada: T,
    onSeleccionar: (T) -> Unit,
    colorSeleccion: Color,
    fondoSeleccion: Color,
) {
    BoxWithConstraints {
        // Dos columnas como el diseño; una sola si con la letra actual no caben sin partir palabras.
        val columnas = if (esCompacto(maxWidth, minimo = 260.dp)) 1 else 2
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        opciones.chunked(columnas).forEach { fila ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                fila.forEach { opcion ->
                    TarjetaOpcion(
                        emoji = opcion.emoji,
                        fondoEmoji = opcion.fondoEmoji,
                        titulo = opcion.titulo,
                        descripcion = opcion.descripcion,
                        seleccionada = opcion.valor == seleccionada,
                        colorSeleccion = colorSeleccion,
                        fondoSeleccion = fondoSeleccion,
                        onClick = { onSeleccionar(opcion.valor) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun RecuadroConsejo(texto: AnnotatedString, fondo: Brush, borde: Color, estiloTexto: TextStyle) {
    val forma = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fondo)
            .border(1.dp, borde, forma)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(texto, style = estiloTexto)
    }
}

@PreviewsFinanzia
@Composable
private fun PrimerIngresoPreview() {
    FinanziaTheme { PrimerIngresoScreen(PrimerIngresoViewModel(FakeOnboardingRepository()), onAtras = {}, onTerminar = {}) }
}

@PreviewsFinanzia
@Composable
private fun PrimerGastoPreview() {
    FinanziaTheme { PrimerGastoScreen(PrimerGastoViewModel(FakeOnboardingRepository()), onAtras = {}, onTerminar = {}) }
}

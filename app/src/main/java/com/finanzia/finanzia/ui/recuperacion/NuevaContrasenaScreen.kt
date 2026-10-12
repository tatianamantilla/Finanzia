package com.finanzia.finanzia.ui.recuperacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.esCompacto
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulClaro
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulIntenso
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.AzulMuyClaro
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeCampo
import com.finanzia.finanzia.ui.theme.DegradadoCuentaCreada
import com.finanzia.finanzia.ui.theme.DegradadoPrincipal
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisRadio
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.validacion.RequisitosNuevaContrasena
import com.finanzia.finanzia.validacion.ValidacionNuevaContrasena
import kotlinx.coroutines.flow.collectLatest

// Colores tomados de olvidar_contraseña_2.jpeg y olvidar_contraseña_error.jpeg.
private val VerdeOscuro = Color(0xFF0B7A55)
private val VerdePunto = Color(0xFF0B7A55)
private val MentaActivo = Color(0xFF4ADE9A)
private val MentaPasoFinal = Color(0xFF3FE6A8)
private val SegmentoVacio = Color(0xFFDCE6F5)
private val FondoRequisitos = Color(0xFFEEF3FA)
private val AzulCasilla = Color(0xFF0B4FD0)
private val RojoError = Color(0xFFB91C1C)
private val RojoCirculo = Color(0xFFC81E1E)
private val RosaError = Color(0xFFFDD5D5)
private val RosaBordeError = Color(0xFFF3A5A5)
private val GrisIcono = Color(0xFF6B7280)

/**
 * Pantalla 2 · Paso 2 de 2 (docs/design/olvidar_contraseña_2.jpeg). Si al actualizar las dos
 * contraseñas no son iguales, pasa a su estado de error (olvidar_contraseña_error.jpeg).
 */
@Composable
fun NuevaContrasenaScreen(
    viewModel: NuevaContrasenaViewModel,
    onAtras: () -> Unit,
    onActualizada: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        // collectLatest: un mensaje nuevo reemplaza al anterior en vez de esperar a que se cierre.
        viewModel.mensajes.collectLatest { snackbar.showSnackbar(it) }
    }

    PantallaBase(fondo = Modifier.background(FondoPantalla), snackbar = snackbar) {
        ContenidoDesplazable(margenHorizontal = 22.dp) {
            BarraSuperior(onAtras)
            Spacer(Modifier.height(10.dp))
            if (viewModel.errorNoCoinciden) {
                ContenidoError(viewModel)
            } else {
                ContenidoNormal(viewModel)
            }
            Spacer(Modifier.height(26.dp))
            BotonPrincipal(
                texto = "Actualizar contraseña",
                fondo = if (viewModel.errorNoCoinciden) DegradadoCuentaCreada else DegradadoPrincipal,
                onClick = { viewModel.actualizar(onActualizada) },
                alto = 60.dp,
                forma = RoundedCornerShape(20.dp),
                colorSombra = Color(0x400066F5),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// --- Estado normal (olvidar_contraseña_2.jpeg) ---

@Composable
private fun ContenidoNormal(viewModel: NuevaContrasenaViewModel) {
    val requisitos = viewModel.requisitos
    FilaPaso(texto = "RESTABLECIMIENTO DE ACCESO", mayusculas = true)
    Spacer(Modifier.height(18.dp))
    RecuadroCorreoVerificado(viewModel.correo)
    Spacer(Modifier.height(26.dp))
    EncabezadoCampo("Nueva contraseña") {
        ValidacionNuevaContrasena.textoSeguridad(requisitos.cumplidos)?.let {
            Text(it, style = estilo(13, FontWeight.SemiBold, VerdeOscuro))
        }
    }
    CampoNuevaContrasena(viewModel)
    Spacer(Modifier.height(16.dp))
    BarraFuerza(requisitos.cumplidos)
    Spacer(Modifier.height(18.dp))
    ListaRequisitos(requisitos)
    Spacer(Modifier.height(26.dp))
    EncabezadoCampo("Confirmar nueva contraseña")
    CampoConfirmacion(viewModel, conError = false)
    Spacer(Modifier.height(18.dp))
    TarjetaCerrarSesiones(viewModel.cerrarOtrasSesiones, viewModel::cambiarCerrarOtrasSesiones)
}

@Composable
private fun RecuadroCorreoVerificado(correo: String) {
    val forma = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Brush.verticalGradient(listOf(Color(0xFFF1F5FD), Color(0xFFDDE8FB))))
            .padding(horizontal = 18.dp, vertical = 30.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Blanco)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.GppGood, contentDescription = null, tint = VerdeOscuro, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                buildAnnotatedString {
                    append("Correo verificado: ")
                    withStyle(SpanStyle(color = AzulMarino, fontWeight = FontWeight.SemiBold)) {
                        append(ValidacionNuevaContrasena.enmascararCorreo(correo))
                    }
                },
                style = estilo(14, color = GrisTexto),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(MentaActivo)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text("Activo", style = estilo(13, FontWeight.SemiBold, VerdeOscuro))
            }
        }
    }
}

/** Barra de fuerza: un segmento verde por cada requisito cumplido. */
@Composable
private fun BarraFuerza(cumplidos: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .semantics { stateDescription = "$cumplidos de 4 requisitos" },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(4) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(if (i < cumplidos) VerdeOscuro else SegmentoVacio),
            )
        }
    }
}

@Composable
private fun ListaRequisitos(requisitos: RequisitosNuevaContrasena) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FondoRequisitos)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Requisitos de robustez:", style = estilo(14, FontWeight.SemiBold, AzulMarino))
        FilaRequisito("Mínimo 8 caracteres", requisitos.tieneLongitud)
        FilaRequisito("Al menos 1 número", requisitos.tieneNumero)
        FilaRequisito("Al menos 1 letra mayúscula", requisitos.tieneMayuscula)
        FilaRequisito("Al menos 1 símbolo especial (!@#$)", requisitos.tieneSimbolo)
    }
}

@Composable
private fun FilaRequisito(texto: String, cumplido: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            stateDescription = if (cumplido) Etiquetas.INDICADOR_CUMPLIDO else Etiquetas.INDICADOR_PENDIENTE
        },
    ) {
        IconoRequisito(cumplido, tamano = 20)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = estilo(15, color = if (cumplido) AzulMarino else GrisTexto))
    }
}

@Composable
private fun IconoRequisito(cumplido: Boolean, tamano: Int) {
    if (cumplido) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeOscuro, modifier = Modifier.size(tamano.dp))
    } else {
        Box(
            Modifier
                .size(tamano.dp)
                .padding(1.dp)
                .border(2.dp, GrisRadio, CircleShape),
        )
    }
}

@Composable
private fun TarjetaCerrarSesiones(marcada: Boolean, onCambio: (Boolean) -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .clickable(role = Role.Checkbox) { onCambio(!marcada) }
            .semantics { stateDescription = if (marcada) "Marcada" else "Sin marcar" }
            .testTag(Etiquetas.CASILLA_CERRAR_SESIONES)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // En el diseño la casilla marcada es un cuadro azul relleno.
        val formaCasilla = RoundedCornerShape(6.dp)
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .clip(formaCasilla)
                .background(if (marcada) AzulCasilla else Blanco)
                .border(1.5.dp, if (marcada) AzulCasilla else GrisRadio, formaCasilla),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Cerrar sesión en todos los demás dispositivos", style = estilo(16, FontWeight.SemiBold, AzulMarino, altoLinea = 22))
            Spacer(Modifier.height(4.dp))
            Text(
                "Recomendado para garantizar que solo tú tengas acceso inmediatamente.",
                style = estilo(13, color = GrisTexto, altoLinea = 17),
            )
        }
    }
}

// --- Estado de error (olvidar_contraseña_error.jpeg) ---

@Composable
private fun ColumnScope.ContenidoError(viewModel: NuevaContrasenaViewModel) {
    val requisitos = viewModel.requisitos
    val conError = !viewModel.coinciden
    FilaPaso(texto = "Seguridad de la cuenta", mayusculas = false)
    Spacer(Modifier.height(14.dp))
    TarjetaNoCoinciden()
    Spacer(Modifier.height(20.dp))
    TarjetaPasoFinal()
    Spacer(Modifier.height(24.dp))
    EncabezadoCampo("Nueva contraseña") {
        if (requisitos.cumpleTodos) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = VerdeOscuro, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cumple requisitos", style = estilo(13, FontWeight.Medium, VerdeOscuro))
            }
        } else {
            ValidacionNuevaContrasena.textoSeguridad(requisitos.cumplidos)?.let {
                Text(it, style = estilo(13, FontWeight.SemiBold, VerdeOscuro))
            }
        }
    }
    CampoNuevaContrasena(viewModel)
    Spacer(Modifier.height(12.dp))
    ChipsRequisitos(requisitos)
    Spacer(Modifier.height(22.dp))
    EncabezadoCampo("Confirmar nueva contraseña", colorTitulo = if (conError) RojoError else AzulMarino) {
        if (conError) Text("No coincide", style = estilo(13, FontWeight.Medium, RojoError))
    }
    CampoConfirmacion(viewModel, conError = conError)
    if (conError) {
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = RojoError, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ambas contraseñas deben ser idénticas.", style = estilo(13, FontWeight.SemiBold, RojoError))
        }
    }
    Spacer(Modifier.height(16.dp))
    BotonMostrarAmbas(viewModel::mostrarAmbas, Modifier.align(Alignment.End))
}

@Composable
private fun TarjetaNoCoinciden() {
    val forma = RoundedCornerShape(18.dp)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x22B91C1C), spotColor = Color(0x22B91C1C))
            .clip(forma)
            .background(RosaError)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        // Con letra muy grande en pantalla angosta, el ícono va arriba y el texto debajo.
        if (esCompacto(maxWidth, minimo = 260.dp)) {
            Column {
                IconoAlerta()
                Spacer(Modifier.height(10.dp))
                TextosNoCoinciden()
            }
        } else {
            Row(verticalAlignment = Alignment.Top) {
                IconoAlerta()
                Spacer(Modifier.width(14.dp))
                TextosNoCoinciden(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IconoAlerta() {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(RojoCirculo),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Blanco, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TextosNoCoinciden(modifier: Modifier = Modifier) {
    Column(modifier) {
        val tamanoTitulo = tamanoLimitado(20f)
        Text(
            "Las contraseñas no coinciden",
            style = estilo(tamanoTitulo, FontWeight.SemiBold, RojoError, altoLinea = tamanoTitulo * 1.4f),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "La confirmación debe ser exactamente igual a la nueva contraseña ingresada. Por favor verifica ambos campos.",
            style = estilo(15, color = RojoCirculo, altoLinea = 25),
        )
    }
}

@Composable
private fun TarjetaPasoFinal() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AzulMuyClaro)
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MentaPasoFinal)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.LockReset, contentDescription = null, tint = VerdeOscuro, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("PASO FINAL", style = estilo(12, FontWeight.SemiBold, VerdeOscuro, espaciado = 0.5))
        }
        Spacer(Modifier.height(8.dp))
        Text("Crea una nueva contraseña 🔐", style = estilo(24, FontWeight.Bold, AzulMarino, altoLinea = 30))
        Spacer(Modifier.height(6.dp))
        Text("Tu compañero financiero te ayuda a proteger tus fondos.", style = estilo(15, color = GrisTexto, altoLinea = 21))
    }
}

/** Los cuatro requisitos en chips de dos columnas (estado de error). */
@Composable
private fun ChipsRequisitos(requisitos: RequisitosNuevaContrasena) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FondoRequisitos)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2,
    ) {
        ChipRequisito("8+ caracteres", requisitos.tieneLongitud, Modifier.weight(1f))
        ChipRequisito("Al menos un número", requisitos.tieneNumero, Modifier.weight(1f))
        ChipRequisito("Una mayúscula", requisitos.tieneMayuscula, Modifier.weight(1f))
        ChipRequisito("Un símbolo especial", requisitos.tieneSimbolo, Modifier.weight(1f))
    }
}

@Composable
private fun ChipRequisito(texto: String, cumplido: Boolean, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) {
            stateDescription = if (cumplido) Etiquetas.INDICADOR_CUMPLIDO else Etiquetas.INDICADOR_PENDIENTE
        },
    ) {
        IconoRequisito(cumplido, tamano = 17)
        Spacer(Modifier.width(8.dp))
        Text(texto, style = estilo(13, FontWeight.Medium, if (cumplido) VerdeOscuro else GrisTexto))
    }
}

@Composable
private fun BotonMostrarAmbas(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AzulClaro)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Visibility, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Mostrar ambas contraseñas", style = estilo(15, FontWeight.Medium, AzulEnlace))
    }
}

// --- Partes comunes a los dos estados ---

/**
 * Tamaño de letra que crece con la letra del sistema solo hasta 150 %. Se usa en palabras largas
 * ("RESTABLECIMIENTO", "contraseñas") que con letra al 200 % no caben en un teléfono pequeño.
 * Con letra normal devuelve el mismo tamaño, así que el diseño no cambia.
 */
@Composable
private fun tamanoLimitado(tamano: Float): Float {
    val escala = LocalDensity.current.fontScale
    return if (escala > 1.5f) tamano * 1.5f / escala else tamano
}

/** Flecha "←" a la izquierda y botón azul con persona a la derecha. */
@Composable
private fun BarraSuperior(onAtras: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onAtras)
                .testTag(Etiquetas.BOTON_ATRAS),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = AzulMarino, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.weight(1f))
        // TODO: el botón de persona se ve pero la spec 02 no define su acción.
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AzulCasilla),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Blanco, modifier = Modifier.size(22.dp))
        }
    }
}

/** Punto verde con el texto del paso a la izquierda y la pastilla "PASO 2 DE 2" a la derecha. */
@Composable
private fun FilaPaso(texto: String, mayusculas: Boolean) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(if (mayusculas) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(VerdePunto),
            )
            Spacer(Modifier.width(if (mayusculas) 12.dp else 6.dp))
            Text(
                texto,
                style = if (mayusculas) {
                    estilo(tamanoLimitado(13f), FontWeight.Medium, GrisTexto, espaciado = 0.8)
                } else {
                    estilo(14, color = GrisTexto)
                },
            )
        }
        Box(
            Modifier
                .clip(CircleShape)
                .background(AzulClaro)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text("PASO 2 DE 2", style = estilo(14, FontWeight.SemiBold, AzulEnlace))
        }
    }
}

/** Título de un campo con algo opcional a la derecha ("Seguridad media", "No coincide"…). */
@Composable
private fun EncabezadoCampo(
    titulo: String,
    colorTitulo: Color = AzulMarino,
    derecha: @Composable () -> Unit = {},
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(titulo, style = estilo(16, FontWeight.SemiBold, colorTitulo))
        derecha()
    }
}

@Composable
private fun CampoNuevaContrasena(viewModel: NuevaContrasenaViewModel) {
    CampoContrasena(
        valor = viewModel.nuevaContrasena,
        onCambio = viewModel::cambiarNuevaContrasena,
        placeholder = "••••••••••••",
        icono = Icons.Outlined.Lock,
        mostrar = viewModel.mostrarNueva,
        onAlternarMostrar = viewModel::alternarMostrarNueva,
        etiquetaTest = Etiquetas.CAMPO_CONTRASENA,
        imeAction = ImeAction.Next,
        conError = false,
    )
}

@Composable
private fun CampoConfirmacion(viewModel: NuevaContrasenaViewModel, conError: Boolean) {
    CampoContrasena(
        valor = viewModel.confirmacion,
        onCambio = viewModel::cambiarConfirmacion,
        placeholder = "Repite tu nueva contraseña",
        icono = Icons.Outlined.LockReset,
        mostrar = viewModel.mostrarConfirmacion,
        onAlternarMostrar = viewModel::alternarMostrarConfirmacion,
        etiquetaTest = Etiquetas.CAMPO_CONFIRMACION,
        imeAction = ImeAction.Done,
        conError = conError,
    )
}

@Composable
private fun CampoContrasena(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    mostrar: Boolean,
    onAlternarMostrar: () -> Unit,
    etiquetaTest: String,
    imeAction: ImeAction,
    conError: Boolean,
) {
    val forma = RoundedCornerShape(16.dp)
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        singleLine = true,
        textStyle = estilo(16, FontWeight.Medium, if (conError) RojoError else AzulMarino),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        visualTransformation = if (mostrar) VisualTransformation.None else PasswordVisualTransformation(),
        cursorBrush = SolidColor(if (conError) RojoError else AzulIntenso),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiquetaTest),
        decorationBox = { campo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .shadow(1.dp, forma, ambientColor = Color(0x10000000), spotColor = Color(0x10000000))
                    .clip(forma)
                    .background(if (conError) RosaError else Blanco)
                    .border(1.dp, if (conError) RosaBordeError else BordeCampo.copy(alpha = 0.5f), forma)
                    .padding(start = 18.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icono, contentDescription = null, tint = if (conError) RojoError else AzulMarino, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Box(Modifier.weight(1f)) {
                    if (valor.isEmpty()) {
                        Text(placeholder, style = estilo(16, color = GrisPlaceholder))
                    }
                    campo()
                }
                Icon(
                    if (mostrar) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (mostrar) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = GrisIcono,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onAlternarMostrar)
                        .padding(6.dp)
                        .size(24.dp),
                )
                if (conError) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RojoCirculo),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = Blanco, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
    )
}

@PreviewsFinanzia
@Composable
private fun NuevaContrasenaPreview() {
    FinanziaTheme {
        NuevaContrasenaScreen(
            NuevaContrasenaViewModel(FakeAuthRepository(), correo = "paula@ejemplo.com"),
            onAtras = {},
            onActualizada = {},
        )
    }
}

@PreviewsFinanzia
@Composable
private fun NuevaContrasenaErrorPreview() {
    val viewModel = NuevaContrasenaViewModel(FakeAuthRepository(), correo = "paula@ejemplo.com").apply {
        cambiarNuevaContrasena("Finanzia2026!")
        cambiarConfirmacion("Finanzia2026?")
        actualizar {}
    }
    FinanziaTheme { NuevaContrasenaScreen(viewModel, onAtras = {}, onActualizada = {}) }
}

package com.finanzia.finanzia.ui.registro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.finanzia.finanzia.ui.PreviewsFinanzia
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.IconoAlertaCampo
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.PieConEnlace
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeCampo
import com.finanzia.finanzia.ui.theme.DegradadoPrincipal
import com.finanzia.finanzia.ui.theme.ErrorRojo
import com.finanzia.finanzia.ui.theme.ErrorRosaBorde
import com.finanzia.finanzia.ui.theme.ErrorRosaFondo
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.GrisEtiqueta
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisPunto
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.VerdeMarca
import com.finanzia.finanzia.ui.theme.VerdeTexto

private val GrisIcono = Color(0xFF6B7280)
private val BordeCasilla = Color(0xFFC5CDD8)

/** Pantalla 1 · Crear cuenta (docs/design/crear_cuenta_1.jpeg). */
@Composable
fun CrearCuentaScreen(
    viewModel: CrearCuentaViewModel,
    onCuentaCreada: () -> Unit,
    onIniciarSesion: () -> Unit,
) {
    PantallaBase(fondo = Modifier.drawBehind { fondoCrearCuenta() }) {
        ContenidoDesplazable(margenHorizontal = 28.dp, llenarAlto = true, alineacion = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(12.dp))
                    LogoFinanzia()
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "TU ASISTENTE FINANCIERO CON IA",
                        style = estilo(13, FontWeight.Bold, VerdeTexto, espaciado = 0.6, alineacion = TextAlign.Center),
                    )
                    Spacer(Modifier.height(30.dp))
                    Text("¡Empieza tu viaje! 🚀", style = estilo(22, FontWeight.Bold, alineacion = TextAlign.Center))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Toma el control inteligente de tu dinero en minutos.",
                        style = estilo(15, color = GrisTexto, altoLinea = 22, alineacion = TextAlign.Center),
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    Spacer(Modifier.weight(1f).heightIn(min = 32.dp))

                    Formulario(viewModel)

                    Spacer(Modifier.height(26.dp))
                    viewModel.errorGeneral?.let {
                        MensajeError(it, Modifier.padding(start = 4.dp, bottom = 12.dp))
                    }
                    BotonPrincipal(
                        texto = "Crear cuenta",
                        fondo = DegradadoPrincipal,
                        onClick = { viewModel.crearCuenta(onCuentaCreada) },
                        cargando = viewModel.enviando,
                    )

                    Spacer(Modifier.weight(1f).heightIn(min = 32.dp))

                    PieConEnlace(pregunta = "¿Ya tienes una cuenta?", enlace = "Iniciar sesión", onClick = onIniciarSesion)
                    Spacer(Modifier.height(32.dp))
        }
    }
}

/** Fondo blanco con los brillos verde (abajo a la derecha) y lila (abajo a la izquierda). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.fondoCrearCuenta() {
    drawRect(Blanco)
    drawRect(
        Brush.radialGradient(
            listOf(Color(0xFFC4F2DE), Color(0x00FFFFFF)),
            center = Offset(size.width, size.height),
            radius = size.width * 0.95f,
        ),
    )
    drawRect(
        Brush.radialGradient(
            listOf(Color(0xFFEDEFFD), Color(0x00FFFFFF)),
            center = Offset(0f, size.height * 0.92f),
            radius = size.width * 0.7f,
        ),
    )
}

/** Logo "FINANZIA" con "IA" en verde y la estrella verde arriba a la derecha. */
@Composable
private fun LogoFinanzia() {
    Box {
        Text(
            buildAnnotatedString {
                append("FINANZ")
                withStyle(SpanStyle(color = VerdeMarca)) { append("IA") }
            },
            style = estilo(30, FontWeight.ExtraBold, AzulMarino, altoLinea = 32, espaciado = -0.5),
            modifier = Modifier.padding(end = 26.dp),
        )
        Estrella(
            Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-1).dp)
                .size(20.dp),
        )
    }
}

/** Estrella de cuatro puntas del logo. También la usa la pantalla de inicio de sesión. */
@Composable
internal fun Estrella(modifier: Modifier) {
    Canvas(modifier) {
        val c = size.width / 2
        val curva = size.width * 0.12f
        val path = Path().apply {
            moveTo(c, 0f)
            quadraticTo(c + curva, c - curva, size.width, c)
            quadraticTo(c + curva, c + curva, c, size.height)
            quadraticTo(c - curva, c + curva, 0f, c)
            quadraticTo(c - curva, c - curva, c, 0f)
            close()
        }
        drawPath(path, VerdeMarca)
    }
}

@Composable
private fun Formulario(viewModel: CrearCuentaViewModel) {
    Column(Modifier.fillMaxWidth()) {
        EtiquetaCampo("NOMBRE COMPLETO", conError = viewModel.errorNombre != null)
        CampoFormulario(
            error = viewModel.errorNombre,
            valor = viewModel.nombre,
            onCambio = viewModel::cambiarNombre,
            placeholder = "Paula Gómez",
            icono = Icons.Outlined.Person,
            etiquetaTest = Etiquetas.CAMPO_NOMBRE,
            teclado = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(22.dp))
        EtiquetaCampo("CORREO ELECTRÓNICO", conError = viewModel.errorCorreo != null)
        CampoFormulario(
            error = viewModel.errorCorreo,
            valor = viewModel.correo,
            onCambio = viewModel::cambiarCorreo,
            placeholder = "hola@ejemplo.com",
            icono = Icons.Outlined.MailOutline,
            etiquetaTest = Etiquetas.CAMPO_CORREO,
            teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(22.dp))
        EtiquetaCampo("CONTRASEÑA", conError = viewModel.errorContrasena != null)
        CampoFormulario(
            error = viewModel.errorContrasena,
            valor = viewModel.contrasena,
            onCambio = viewModel::cambiarContrasena,
            placeholder = "Mínimo 8 caracteres",
            icono = Icons.Outlined.Lock,
            etiquetaTest = Etiquetas.CAMPO_CONTRASENA,
            teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            transformacion = if (viewModel.mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            alFinal = {
                Icon(
                    if (viewModel.mostrarContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (viewModel.mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = GrisIcono,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = viewModel::alternarMostrarContrasena)
                        .padding(4.dp)
                        .size(24.dp),
                )
            },
        )
        Spacer(Modifier.height(14.dp))
        IndicadoresContrasena(viewModel)
        Spacer(Modifier.height(24.dp))
        CasillaTerminos(viewModel.aceptaTerminos, viewModel::cambiarAceptaTerminos, viewModel.errorTerminos)
    }
}

@Composable
private fun EtiquetaCampo(texto: String, conError: Boolean = false) {
    Text(
        texto,
        style = estilo(13, FontWeight.Bold, if (conError) ErrorRojo else GrisEtiqueta, espaciado = 1.0),
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
    )
}

@Composable
private fun CampoFormulario(
    error: String?,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    etiquetaTest: String,
    teclado: KeyboardOptions,
    transformacion: VisualTransformation = VisualTransformation.None,
    alFinal: (@Composable () -> Unit)? = null,
) {
    val forma = RoundedCornerShape(18.dp)
    val conError = error != null
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        singleLine = true,
        textStyle = estilo(16, FontWeight.Medium, if (conError) ErrorRojo else AzulMarino),
        keyboardOptions = teclado,
        visualTransformation = transformacion,
        cursorBrush = SolidColor(VerdeMarca),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiquetaTest),
        decorationBox = { campo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .clip(forma)
                    .background(if (conError) ErrorRosaFondo else Blanco)
                    .border(if (conError) 1.5.dp else 1.dp, if (conError) ErrorRosaBorde else BordeCampo, forma)
                    .padding(start = 18.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icono, contentDescription = null, tint = if (conError) ErrorRojo else GrisIcono, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Box(Modifier.weight(1f)) {
                    if (valor.isEmpty()) {
                        Text(placeholder, style = estilo(16, color = GrisPlaceholder))
                    }
                    campo()
                }
                if (conError) {
                    Spacer(Modifier.width(8.dp))
                    IconoAlertaCampo()
                    if (alFinal != null) Spacer(Modifier.width(6.dp))
                }
                if (alFinal != null) alFinal()
            }
        },
    )
    if (error != null) MensajeError(error, Modifier.padding(start = 4.dp, top = 8.dp))
}

@Composable
private fun IndicadoresContrasena(viewModel: CrearCuentaViewModel) {
    val reglas = viewModel.reglas
    // FlowRow: en una fila como el diseño; con letra grande o pantalla angosta pasa a otra línea.
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Indicador("8+ carac.", reglas.tieneLongitud)
        Indicador("1 número", reglas.tieneNumero)
        Indicador("1 mayúscula", reglas.tieneMayuscula)
    }
}

@Composable
private fun Indicador(texto: String, cumplido: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            stateDescription = if (cumplido) Etiquetas.INDICADOR_CUMPLIDO else Etiquetas.INDICADOR_PENDIENTE
        },
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (cumplido) VerdeMarca else GrisPunto),
        )
        Spacer(Modifier.width(7.dp))
        Text(texto, style = estilo(14, color = GrisEtiqueta))
    }
}

@Composable
private fun CasillaTerminos(marcada: Boolean, onCambio: (Boolean) -> Unit, error: String?) {
    val forma = RoundedCornerShape(6.dp)
    val conError = error != null
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(22.dp)
                .clip(forma)
                .background(
                    when {
                        marcada -> VerdeMarca
                        conError -> ErrorRosaFondo
                        else -> Blanco
                    },
                )
                .border(
                    if (conError) 2.dp else 1.5.dp,
                    when {
                        marcada -> VerdeMarca
                        conError -> ErrorRojo
                        else -> BordeCasilla
                    },
                    forma,
                )
                .clickable(role = Role.Checkbox) { onCambio(!marcada) }
                .semantics { stateDescription = if (marcada) "Marcada" else "Sin marcar" }
                .testTag(Etiquetas.CASILLA_TERMINOS),
            contentAlignment = Alignment.Center,
        ) {
            if (marcada) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Blanco, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        // TODO: los enlaces de Términos y Política se ven pero no tienen acción ("Decisiones pendientes" 6).
        Text(
            buildAnnotatedString {
                append("Acepto los ")
                withStyle(SpanStyle(color = AzulEnlace, fontWeight = FontWeight.SemiBold)) { append("Términos de Servicio") }
                append(" y la ")
                withStyle(SpanStyle(color = AzulEnlace, fontWeight = FontWeight.SemiBold)) { append("Política de Privacidad") }
            },
            style = estilo(15, color = GrisTexto, altoLinea = 22),
        )
    }
    if (error != null) MensajeError(error, Modifier.padding(start = 4.dp, top = 8.dp))
}

@PreviewsFinanzia
@Composable
private fun CrearCuentaPreview() {
    FinanziaTheme {
        CrearCuentaScreen(CrearCuentaViewModel(FakeAuthRepository()), onCuentaCreada = {}, onIniciarSesion = {})
    }
}

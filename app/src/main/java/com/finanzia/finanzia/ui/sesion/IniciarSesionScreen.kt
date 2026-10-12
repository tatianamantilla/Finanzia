package com.finanzia.finanzia.ui.sesion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.PieConEnlace
import com.finanzia.finanzia.ui.componentes.esCompacto
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.registro.Estrella
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.DegradadoPrincipal
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisEtiqueta
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.VerdeMarca
import com.finanzia.finanzia.ui.theme.VerdeTexto
import kotlinx.coroutines.flow.collectLatest

private val GrisIcono = Color(0xFF6B7280)
private val GrisPlaceholderClaro = Color(0xFFB8C2CF)

/** Pantalla 1 · Inicio de sesión (docs/design/inicio_sesion_1.jpeg). */
@Composable
fun IniciarSesionScreen(
    viewModel: IniciarSesionViewModel,
    onEntrar: () -> Unit,
    onOlvideContrasena: () -> Unit,
    onCrearCuenta: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        // collectLatest: un mensaje nuevo reemplaza al anterior en vez de esperar a que se cierre.
        viewModel.mensajes.collectLatest { snackbar.showSnackbar(it) }
    }

    PantallaBase(fondo = Modifier.background(FondoPantalla), snackbar = snackbar) {
        // En el diseño todo el bloque queda centrado en vertical: los dos espacios flexibles lo logran.
        ContenidoDesplazable(margenHorizontal = 28.dp, llenarAlto = true, alineacion = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f).heightIn(min = 32.dp))
            LogoFinanzia()
            Spacer(Modifier.height(8.dp))
            Text(
                "Tu asistente financiero con IA",
                style = estilo(16, FontWeight.SemiBold, VerdeTexto, espaciado = -0.3, alineacion = TextAlign.Center),
            )
            Spacer(Modifier.height(26.dp))
            Text("¡Qué bueno verte! 👋", style = estilo(23, FontWeight.Bold, alineacion = TextAlign.Center))
            Spacer(Modifier.height(10.dp))
            Text(
                "Inicia sesión para continuar\ny seguir creciendo.",
                style = estilo(16, color = GrisTexto, altoLinea = 22, alineacion = TextAlign.Center),
            )
            Spacer(Modifier.height(28.dp))

            CampoSesion(
                etiqueta = "CORREO ELECTRÓNICO",
                valor = viewModel.correo,
                onCambio = viewModel::cambiarCorreo,
                placeholder = "tu@correo.com",
                icono = Icons.Outlined.MailOutline,
                etiquetaTest = Etiquetas.CAMPO_CORREO,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(16.dp))
            CampoSesion(
                etiqueta = "CONTRASEÑA",
                valor = viewModel.contrasena,
                onCambio = viewModel::cambiarContrasena,
                placeholder = "••••••••••••",
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
            Spacer(Modifier.height(18.dp))
            Text(
                "¿Olvidaste tu contraseña?",
                style = estilo(16, FontWeight.Medium, AzulEnlace, alineacion = TextAlign.Center),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onOlvideContrasena)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(18.dp))
            BotonPrincipal(
                texto = "Entrar",
                fondo = DegradadoPrincipal,
                onClick = { viewModel.entrar(onEntrar) },
                alto = 56.dp,
                forma = RoundedCornerShape(20.dp),
                colorSombra = Color(0x400066F5),
            )
            Spacer(Modifier.weight(1f).heightIn(min = 32.dp))

            // Cambio aprobado (spec 01): mismo pie, estilo y posición que en Crear cuenta.
            PieConEnlace(pregunta = "¿No tienes una cuenta?", enlace = "Crear cuenta", onClick = onCrearCuenta)
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Logo "FINANZIA" (todo en azul marino en este diseño) con la estrella verde arriba a la derecha. */
@Composable
private fun LogoFinanzia() {
    Box {
        Text(
            "FINANZIA",
            style = estilo(34, FontWeight.ExtraBold, AzulMarino, altoLinea = 38, espaciado = 1.5),
            modifier = Modifier.padding(top = 6.dp, end = 22.dp),
        )
        Estrella(
            Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-6).dp)
                .size(18.dp),
        )
    }
}

/** Campo blanco con el ícono a la izquierda y la etiqueta en mayúsculas dentro del recuadro. */
@Composable
private fun CampoSesion(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    etiquetaTest: String,
    teclado: KeyboardOptions,
    transformacion: VisualTransformation = VisualTransformation.None,
    alFinal: (@Composable () -> Unit)? = null,
) {
    val forma = RoundedCornerShape(20.dp)
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        singleLine = true,
        textStyle = estilo(16, FontWeight.Medium, AzulMarino),
        keyboardOptions = teclado,
        visualTransformation = transformacion,
        cursorBrush = SolidColor(VerdeMarca),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiquetaTest),
        decorationBox = { campo ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 66.dp)
                    .clip(forma)
                    .background(Blanco)
                    .border(1.dp, BordeTarjeta, forma)
                    .padding(start = 18.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                val textos: @Composable (Modifier) -> Unit = { modifier ->
                    Column(modifier) {
                        Text(etiqueta, style = estilo(13, FontWeight.Medium, GrisEtiqueta, espaciado = 0.6))
                        Spacer(Modifier.height(4.dp))
                        Box {
                            if (valor.isEmpty()) {
                                Text(placeholder, style = estilo(16, color = GrisPlaceholderClaro))
                            }
                            campo()
                        }
                    }
                }
                // Con letra muy grande en pantalla angosta, los íconos van arriba y el texto debajo
                // para que la etiqueta no se corte. Con letra normal se ve como el diseño.
                if (esCompacto(maxWidth, minimo = 200.dp)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icono, contentDescription = null, tint = GrisIcono, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.weight(1f))
                            if (alFinal != null) alFinal()
                        }
                        Spacer(Modifier.height(6.dp))
                        textos(Modifier.fillMaxWidth())
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icono, contentDescription = null, tint = GrisIcono, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(28.dp))
                        textos(Modifier.weight(1f))
                        if (alFinal != null) alFinal()
                    }
                }
            }
        },
    )
}

@PreviewsFinanzia
@Composable
private fun IniciarSesionPreview() {
    FinanziaTheme {
        IniciarSesionScreen(IniciarSesionViewModel(FakeAuthRepository()), onEntrar = {}, onOlvideContrasena = {}, onCrearCuenta = {})
    }
}

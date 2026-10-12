package com.finanzia.finanzia.ui.recuperacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PreviewsFinanzia
import com.finanzia.finanzia.ui.componentes.BotonPrincipal
import com.finanzia.finanzia.ui.componentes.ContenidoDesplazable
import com.finanzia.finanzia.ui.componentes.PantallaBase
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulEnlace
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.AzulMuyClaro
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.DegradadoPrincipal
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisTexto
import com.finanzia.finanzia.ui.theme.MoradoTexto
import kotlinx.coroutines.flow.collectLatest

private val FondoCampo = Color(0xFFEEF3FC)
private val FondoPastillaPaso = Color(0xFFE9F0F8)
private val VerdePaso = Color(0xFF065F46)
private val VerdePunto = Color(0xFF34D399)
private val GrisEtiquetaOscuro = Color(0xFF2D3748)
private val GrisIcono = Color(0xFF6B7280)

/** Pantalla 1 · Paso 1 de 2 (docs/design/olvidar_contraseña_1.jpeg). */
@Composable
fun VerificarIdentidadScreen(
    viewModel: VerificarIdentidadViewModel,
    onVerificado: (correo: String) -> Unit,
    onVolver: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        // collectLatest: un mensaje nuevo reemplaza al anterior en vez de esperar a que se cierre.
        viewModel.mensajes.collectLatest { snackbar.showSnackbar(it) }
    }

    PantallaBase(fondo = Modifier.background(FondoPantalla), snackbar = snackbar) {
        ContenidoDesplazable(margenHorizontal = 22.dp, alineacion = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BotonAtrasCircular(onVolver)
                Spacer(Modifier.weight(1f))
                PastillaPaso()
            }
            Spacer(Modifier.height(24.dp))
            LogoConPunto()
            Spacer(Modifier.height(6.dp))
            Text(
                "TU ASISTENTE FINANCIERO CON IA",
                style = estilo(12, FontWeight.SemiBold, GrisEtiquetaOscuro, espaciado = 2.0, alineacion = TextAlign.Center),
            )
            Spacer(Modifier.height(24.dp))
            TarjetaFormulario(viewModel)
            Spacer(Modifier.height(18.dp))
            BotonPrincipal(
                texto = "Verificar y continuar",
                fondo = DegradadoPrincipal,
                onClick = { viewModel.verificar(onVerificado) },
                alto = 62.dp,
                forma = RoundedCornerShape(20.dp),
                colorSombra = Color(0x400066F5),
            )
            Spacer(Modifier.height(14.dp))
            VolverAIniciarSesion(onVolver)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Botón blanco redondo con flecha "←". */
@Composable
private fun BotonAtrasCircular(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .shadow(3.dp, CircleShape, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            .clip(CircleShape)
            .background(Blanco)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(Etiquetas.BOTON_ATRAS),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = AzulMarino, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun PastillaPaso() {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(FondoPastillaPaso)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.LockReset, contentDescription = null, tint = VerdePaso, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("PASO 1 DE 2", style = estilo(14, FontWeight.SemiBold, VerdePaso, espaciado = 0.6))
    }
}

/** Logo "FINANZIA" con el punto verde a la derecha. */
@Composable
private fun LogoConPunto() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("FINANZIA", style = estilo(28, FontWeight.ExtraBold, AzulMarino, altoLinea = 32))
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(VerdePunto),
        )
    }
}

@Composable
private fun TarjetaFormulario(viewModel: VerificarIdentidadViewModel) {
    val forma = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("¿Olvidaste tu contraseña? 🔐", style = estilo(21, FontWeight.Bold, alineacion = TextAlign.Center))
        Spacer(Modifier.height(10.dp))
        Text(
            "Ingresa tu nombre de usuario y correo electrónico registrados para verificar tu identidad y restablecer tu contraseña.",
            style = estilo(16, color = GrisTexto, altoLinea = 25, alineacion = TextAlign.Center),
        )
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth()) {
            EtiquetaCampo("NOMBRE DE USUARIO")
            CampoRecuperacion(
                valor = viewModel.nombreUsuario,
                onCambio = viewModel::cambiarNombreUsuario,
                placeholder = "Ej. carlos_mendoza",
                icono = Icons.Outlined.Person,
                etiquetaTest = Etiquetas.CAMPO_NOMBRE,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(20.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                EtiquetaCampo("CORREO ELECTRÓNICO")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp, bottom = 10.dp),
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = AzulEnlace, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Seguro SSL", style = estilo(13, FontWeight.Medium, AzulEnlace))
                }
            }
            CampoRecuperacion(
                valor = viewModel.correo,
                onCambio = viewModel::cambiarCorreo,
                placeholder = "hola@ejemplo.com",
                icono = Icons.Outlined.MailOutline,
                etiquetaTest = Etiquetas.CAMPO_CORREO,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            )
        }
        Spacer(Modifier.height(22.dp))
        RecuadroSoporte()
    }
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        texto,
        style = estilo(13, FontWeight.Medium, GrisEtiquetaOscuro, espaciado = 0.3),
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
    )
}

@Composable
private fun CampoRecuperacion(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    etiquetaTest: String,
    teclado: KeyboardOptions,
) {
    val forma = RoundedCornerShape(18.dp)
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        singleLine = true,
        textStyle = estilo(17, FontWeight.Medium, AzulMarino),
        keyboardOptions = teclado,
        cursorBrush = SolidColor(AzulEnlace),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiquetaTest),
        decorationBox = { campo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .clip(forma)
                    .background(FondoCampo)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icono, contentDescription = null, tint = GrisIcono, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Box(Modifier.weight(1f)) {
                    if (valor.isEmpty()) {
                        Text(
                            placeholder,
                            style = estilo(17, color = GrisPlaceholder),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    campo()
                }
            }
        },
    )
}

@Composable
private fun RecuadroSoporte() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AzulMuyClaro)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(17.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MoradoTexto),
            contentAlignment = Alignment.Center,
        ) {
            // Medido en dp (no crece con la letra del sistema) para que no se salga del cuadro.
            val tamano = with(LocalDensity.current) { 12.dp.toSp().value }
            Text("?", style = estilo(tamano, FontWeight.Bold, Blanco, altoLinea = tamano * 1.2f))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("¿Cambiaste de correo?", style = estilo(15, color = AzulMarino))
            // TODO: el enlace se ve pero no tiene acción ("Decisiones pendientes" 3 de la spec 02).
            Text("Contactar a soporte de Finanzia", style = estilo(15, FontWeight.Medium, AzulEnlace))
        }
    }
}

@Composable
private fun VolverAIniciarSesion(onVolver: () -> Unit) {
    val forma = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(forma)
            .background(Blanco)
            .clickable(role = Role.Button, onClick = onVolver)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GrisIcono, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text("Volver a Iniciar sesión", style = estilo(16, FontWeight.Medium, AzulMarino, alineacion = TextAlign.Center))
    }
}

@PreviewsFinanzia
@Composable
private fun VerificarIdentidadPreview() {
    FinanziaTheme {
        VerificarIdentidadScreen(VerificarIdentidadViewModel(FakeAuthRepository()), onVerificado = {}, onVolver = {})
    }
}

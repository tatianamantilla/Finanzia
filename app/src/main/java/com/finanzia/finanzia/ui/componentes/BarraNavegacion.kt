package com.finanzia.finanzia.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta

private val AzulSeleccion = Color(0xFF1D4ED8)
private val GrisIconoBarra = Color(0xFF3A4255)
private val AzulPestana = Color(0xFFDCE8FB)

/** Pestañas de la barra inferior. "Gráficos" se quitó por decisión del equipo (spec 05). */
enum class PestanaApp(val texto: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Filled.Home),
    BILLETERA("Billetera", Icons.Outlined.AccountBalanceWallet),
    METAS("Metas", Icons.Outlined.Adjust),
    ASISTENTE("Asistente", Icons.Outlined.SmartToy),
}

/**
 * Barra de navegación inferior fija, con el estilo del Dashboard (spec 04).
 * Tocar la pestaña ya seleccionada no hace nada; las pestañas sin pantalla todavía
 * (Billetera y Asistente) las ignora quien recibe [onPestana].
 */
@Composable
fun BarraNavegacion(
    seleccionada: PestanaApp,
    onPestana: (PestanaApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .testTag(Etiquetas.BARRA_INFERIOR)
            .background(Blanco)
            .border(1.dp, BordeTarjeta)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        PestanaApp.entries.forEach { pestana ->
            Pestana(pestana, seleccionada = pestana == seleccionada) {
                if (pestana != seleccionada) onPestana(pestana)
            }
        }
    }
}

@Composable
private fun RowScope.Pestana(pestana: PestanaApp, seleccionada: Boolean, onClick: () -> Unit) {
    val color = if (seleccionada) AzulSeleccion else GrisIconoBarra
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { selected = seleccionada },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .clip(CircleShape)
                .background(if (seleccionada) AzulPestana else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(pestana.icono, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            pestana.texto,
            style = estilo(12, if (seleccionada) FontWeight.SemiBold else FontWeight.Normal, color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

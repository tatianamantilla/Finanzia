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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta

private val AzulPestanaActiva = Color(0xFF1D4ED8)
private val GrisIconoBarra = Color(0xFF3A4255)
private val AzulPestana = Color(0xFFDCE8FB)

/**
 * Barra de navegación inferior de toda la app: Inicio, Gráficos, Billetera, Metas y Asistente.
 * "Gráficos" se mantiene por decisión del equipo (entrará en función más adelante). Por ahora solo
 * "Inicio" está construida y queda seleccionada; las demás se ven pero no tienen acción hasta que
 * exista su pantalla.
 *
 * [onInicio] = null cuando ya se está en el Dashboard (tocar "Inicio" no hace nada).
 */
@Composable
fun BarraNavegacion(onInicio: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag(Etiquetas.BARRA_INFERIOR)
            .background(Blanco)
            .border(1.dp, BordeTarjeta)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Pestana("Inicio", Icons.Filled.Home, seleccionada = true, onClick = onInicio)
        Pestana("Gráficos", Icons.Filled.BarChart)
        Pestana("Billetera", Icons.Outlined.AccountBalanceWallet)
        Pestana("Metas", Icons.Outlined.Adjust)
        Pestana("Asistente", Icons.Outlined.SmartToy)
    }
}

@Composable
private fun RowScope.Pestana(
    texto: String,
    icono: ImageVector,
    seleccionada: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val color = if (seleccionada) AzulPestanaActiva else GrisIconoBarra
    Column(
        Modifier
            .weight(1f)
            .then(if (onClick != null) Modifier.clickable(role = Role.Tab, onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .clip(CircleShape)
                .background(if (seleccionada) AzulPestana else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            texto,
            style = estilo(12, if (seleccionada) FontWeight.SemiBold else FontWeight.Normal, color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

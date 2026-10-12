package com.finanzia.finanzia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Los diseños solo existen en modo claro y con colores fijos, así que no se usan
// los colores dinámicos de Android ni el modo oscuro.
private val EsquemaFinanzia = lightColorScheme(
    primary = VerdeMarca,
    onPrimary = Blanco,
    secondary = AzulIntenso,
    onSecondary = Blanco,
    background = FondoPantalla,
    onBackground = AzulMarino,
    surface = Blanco,
    onSurface = AzulMarino,
)

@Composable
fun FinanziaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaFinanzia,
        typography = Typography,
        content = content,
    )
}

package com.finanzia.finanzia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.FondoPantalla

/** Pantalla temporal con solo un texto, para que la navegación termine en algún lugar. */
@Composable
fun PantallaProvisional(texto: String, etiquetaTest: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .safeDrawingPadding()
            .testTag(etiquetaTest),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, style = estilo(24, FontWeight.Bold, alineacion = TextAlign.Center))
    }
}

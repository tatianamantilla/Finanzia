package com.finanzia.finanzia.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finanzia.finanzia.ui.componentes.MensajeError
import com.finanzia.finanzia.ui.componentes.MilesVisualTransformation
import com.finanzia.finanzia.ui.componentes.estilo
import com.finanzia.finanzia.ui.theme.AzulMarino
import com.finanzia.finanzia.ui.theme.Blanco
import com.finanzia.finanzia.ui.theme.BordeTarjeta
import com.finanzia.finanzia.ui.theme.ErrorRojo
import com.finanzia.finanzia.ui.theme.ErrorRosaFondo
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.ui.theme.GrisPlaceholder
import com.finanzia.finanzia.ui.theme.GrisSuave
import com.finanzia.finanzia.ui.theme.GrisTexto

private val AzulAccion = Color(0xFF1E3FAE)

/**
 * Recuadro para escribir un monto en pesos, dentro de la misma tarjeta (aportar a una meta o
 * editar un aporte). Va en la pantalla y no en una ventana aparte, para no tapar la meta.
 * El error va en rojo dentro del recuadro, como en el resto de la app.
 */
@Composable
fun FormularioMonto(
    titulo: String,
    monto: String,
    error: String?,
    guardando: Boolean,
    textoGuardar: String,
    onCambio: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val conError = error != null
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FondoPantalla)
            .padding(14.dp),
    ) {
        Text(titulo, style = estilo(14, FontWeight.Bold, if (conError) ErrorRojo else AzulMarino))
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(if (conError) ErrorRosaFondo else Blanco, forma)
                .border(if (conError) 2.dp else 1.dp, if (conError) ErrorRojo else BordeTarjeta, forma)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$", style = estilo(22, FontWeight.ExtraBold, AzulAccion))
            Spacer(Modifier.width(10.dp))
            val estiloMonto = estilo(22, FontWeight.ExtraBold, alineacion = TextAlign.End)
            BasicTextField(
                value = monto,
                onValueChange = onCambio,
                singleLine = true,
                textStyle = estiloMonto,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                visualTransformation = MilesVisualTransformation,
                cursorBrush = SolidColor(AzulMarino),
                modifier = Modifier
                    .weight(1f)
                    .testTag(EtiquetasAportes.CAMPO_MONTO),
                decorationBox = { campo ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (monto.isEmpty()) Text("0", style = estiloMonto.copy(color = GrisPlaceholder))
                        campo()
                    }
                },
            )
            Spacer(Modifier.width(6.dp))
            Text("COP", style = estilo(13, FontWeight.SemiBold, GrisSuave))
        }
        error?.let { MensajeError(it, Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Cancelar",
                style = estilo(14, FontWeight.SemiBold, GrisTexto, alineacion = TextAlign.Center),
                modifier = Modifier
                    .weight(1f)
                    .clip(forma)
                    .border(1.dp, BordeTarjeta, forma)
                    .clickable(role = Role.Button, enabled = !guardando, onClick = onCancelar)
                    .padding(vertical = 12.dp),
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(forma)
                    .background(AzulAccion)
                    .clickable(role = Role.Button, enabled = !guardando, onClick = onGuardar)
                    .testTag(EtiquetasAportes.BOTON_GUARDAR)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Blanco, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text(textoGuardar, style = estilo(14, FontWeight.Bold, Blanco))
                }
            }
        }
    }
}

/** Nombres internos que usan los tests de aportes y del detalle de meta. No se ven en pantalla. */
object EtiquetasAportes {
    const val BOTON_APORTAR = "boton_aportar"
    const val CAMPO_MONTO = "campo_monto_aporte"
    const val BOTON_GUARDAR = "boton_guardar_aporte"
    const val BOTON_DETALLE_META = "boton_detalle_meta"
    const val PANTALLA_DETALLE_META = "pantalla_detalle_meta"
    const val FILA_APORTE = "fila_aporte"
    const val BOTON_EDITAR_APORTE = "boton_editar_aporte"
}

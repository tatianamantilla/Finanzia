package com.finanzia.finanzia.ui.metas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.finanzia.finanzia.datos.CategoriaMeta

/** Ícono de cada categoría (spec 08); sin categoría, una bandera genérica de meta (spec 07). */
fun iconoCategoriaMeta(categoria: CategoriaMeta?): ImageVector = when (categoria) {
    CategoriaMeta.VIAJES_OCIO -> Icons.Filled.Flight
    CategoriaMeta.EMERGENCIA -> Icons.Filled.Shield
    CategoriaMeta.TECNOLOGIA -> Icons.Filled.Computer
    CategoriaMeta.VEHICULO -> Icons.Filled.TwoWheeler
    CategoriaMeta.VIVIENDA -> Icons.Filled.Home
    CategoriaMeta.EDUCACION -> Icons.Filled.School
    null -> Icons.Filled.Flag
}

/** Color del ícono de cada categoría, tomado de docs/design/nueva_meta_1.png. */
fun colorCategoriaMeta(categoria: CategoriaMeta?): Color = when (categoria) {
    CategoriaMeta.VIAJES_OCIO -> Color(0xFF0B6B4F)
    CategoriaMeta.EMERGENCIA -> Color(0xFF1D4ED8)
    CategoriaMeta.TECNOLOGIA -> Color(0xFF4C2A85)
    CategoriaMeta.VEHICULO -> Color(0xFF1D4ED8)
    CategoriaMeta.VIVIENDA -> Color(0xFF0B8F5E)
    CategoriaMeta.EDUCACION -> Color(0xFF6D4BD8)
    null -> Color(0xFF1D4ED8)
}

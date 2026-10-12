package com.finanzia.finanzia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.finanzia.finanzia.R

// Manrope: tipografía oficial de Finanzia. Un solo archivo variable con los pesos 400 a 800.
@OptIn(ExperimentalTextApi::class)
private fun manrope(peso: Int) = Font(
    resId = R.font.manrope,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso)),
)

val Manrope = FontFamily(
    manrope(400),
    manrope(500),
    manrope(600),
    manrope(700),
    manrope(800),
)

private fun TextStyle.conManrope() = copy(fontFamily = Manrope)

val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.conManrope(),
        displayMedium = displayMedium.conManrope(),
        displaySmall = displaySmall.conManrope(),
        headlineLarge = headlineLarge.conManrope(),
        headlineMedium = headlineMedium.conManrope(),
        headlineSmall = headlineSmall.conManrope(),
        titleLarge = titleLarge.conManrope(),
        titleMedium = titleMedium.conManrope(),
        titleSmall = titleSmall.conManrope(),
        bodyLarge = bodyLarge.conManrope(),
        bodyMedium = bodyMedium.conManrope(),
        bodySmall = bodySmall.conManrope(),
        labelLarge = labelLarge.conManrope(),
        labelMedium = labelMedium.conManrope(),
        labelSmall = labelSmall.conManrope(),
    )
}

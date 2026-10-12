package com.finanzia.finanzia.ui

import androidx.compose.ui.tooling.preview.Preview

/**
 * Muestra una pantalla en Android Studio en cuatro variantes:
 * teléfono pequeño, teléfono normal, tableta y teléfono normal con letra grande del sistema.
 */
@Preview(name = "1 · Teléfono pequeño", group = "Tamaños", widthDp = 320, heightDp = 568, showBackground = true)
@Preview(name = "2 · Teléfono normal", group = "Tamaños", widthDp = 411, heightDp = 891, showBackground = true)
@Preview(name = "3 · Tableta", group = "Tamaños", widthDp = 800, heightDp = 1280, showBackground = true)
@Preview(name = "4 · Letra grande (200 %)", group = "Tamaños", widthDp = 411, heightDp = 891, fontScale = 2f, showBackground = true)
annotation class PreviewsFinanzia

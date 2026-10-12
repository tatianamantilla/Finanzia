package com.finanzia.finanzia.validacion

import androidx.compose.ui.text.AnnotatedString
import com.finanzia.finanzia.ui.componentes.MilesVisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Test

/** El campo de monto muestra las comas mientras se escribe, sin cambiar lo que se guarda. */
class MilesVisualTransformationTest {

    private val transformado = MilesVisualTransformation.filter(AnnotatedString("1234567"))

    @Test
    fun muestraComas() {
        assertEquals("1,234,567", transformado.text.text)
    }

    @Test
    fun elCursorQuedaEnElLugarCorrecto() {
        val mapa = transformado.offsetMapping
        assertEquals(0, mapa.originalToTransformed(0))
        assertEquals(1, mapa.originalToTransformed(1)) // 1|,234,567
        assertEquals(3, mapa.originalToTransformed(2)) // 1,2|34,567
        assertEquals(9, mapa.originalToTransformed(7)) // al final
        assertEquals(1, mapa.transformedToOriginal(2)) // justo después de la coma
        assertEquals(7, mapa.transformedToOriginal(9))
    }
}

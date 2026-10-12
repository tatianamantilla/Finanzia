package com.finanzia.finanzia.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MontosTest {

    @Test
    fun soloAceptaNumeros() {
        assertEquals("18500", Montos.limpiar("18,500"))
        assertEquals("3200", Montos.limpiar("3.200"))
        assertEquals("123", Montos.limpiar("1a2-3 "))
        assertEquals("", Montos.limpiar("abc"))
        assertEquals("", Montos.limpiar(""))
    }

    @Test
    fun quitaCerosALaIzquierda() {
        assertEquals("500", Montos.limpiar("00500"))
        assertEquals("0", Montos.limpiar("000"))
    }

    @Test
    fun limitaLaCantidadDeDigitos() {
        assertEquals(Montos.MAX_DIGITOS, Montos.limpiar("1".repeat(20)).length)
    }

    @Test
    fun formateaConComasDeMilesSinDecimales() {
        assertEquals("", Montos.formatear(""))
        assertEquals("5", Montos.formatear("5"))
        assertEquals("999", Montos.formatear("999"))
        assertEquals("3,200", Montos.formatear("3200"))
        assertEquals("18,500", Montos.formatear("18500"))
        assertEquals("100,000", Montos.formatear("100000"))
        assertEquals("1,234,567", Montos.formatear("1234567"))
    }

    @Test
    fun noSePuedeGuardarVacioNiEnCero() {
        assertEquals(ErrorMonto.VACIO, Montos.validar(""))
        assertEquals(ErrorMonto.CERO, Montos.validar("0"))
        assertNull(Montos.validar("18500"))
    }

    @Test
    fun convierteANumero() {
        assertEquals(18500L, Montos.aNumero("18500"))
        assertNull(Montos.aNumero(""))
        assertNull(Montos.aNumero("0"))
    }
}

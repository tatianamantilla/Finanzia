package com.finanzia.finanzia.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionNuevaContrasenaTest {

    // --- Requisitos y barra de fuerza ---

    @Test
    fun requisitoSimbolo() {
        assertFalse(ValidacionNuevaContrasena.requisitos("Abcdefg1").tieneSimbolo)
        assertFalse(ValidacionNuevaContrasena.requisitos("Abc defg1").tieneSimbolo) // el espacio no cuenta
        listOf("!", "@", "#", "$", "_", ".").forEach {
            assertTrue(it, ValidacionNuevaContrasena.requisitos("Abcdefg1$it").tieneSimbolo)
        }
    }

    @Test
    fun cuentaLosRequisitosCumplidos() {
        assertEquals(0, ValidacionNuevaContrasena.requisitos("").cumplidos)
        assertEquals(1, ValidacionNuevaContrasena.requisitos("abc1").cumplidos)
        assertEquals(3, ValidacionNuevaContrasena.requisitos("Finanzia2026").cumplidos) // como en el diseño
        assertEquals(4, ValidacionNuevaContrasena.requisitos("Finanzia2026!").cumplidos)
        assertTrue(ValidacionNuevaContrasena.requisitos("Finanzia2026!").cumpleTodos)
        assertFalse(ValidacionNuevaContrasena.requisitos("Finanzia2026").cumpleTodos)
    }

    @Test
    fun textoDeSeguridad() {
        assertNull(ValidacionNuevaContrasena.textoSeguridad(0))
        assertEquals("Seguridad baja", ValidacionNuevaContrasena.textoSeguridad(2))
        assertEquals("Seguridad media", ValidacionNuevaContrasena.textoSeguridad(3))
        assertEquals("Cumple requisitos", ValidacionNuevaContrasena.textoSeguridad(4))
    }

    // --- Paso 1 ---

    @Test
    fun paso1PideCadaCampo() {
        assertEquals(ErrorRecuperacion.NOMBRE_VACIO, ValidacionNuevaContrasena.validarPaso1(" ", "paula@ejemplo.com"))
        assertEquals(ErrorRecuperacion.CORREO_VACIO, ValidacionNuevaContrasena.validarPaso1("Paula Gómez", ""))
        assertEquals(ErrorRecuperacion.CORREO_INVALIDO, ValidacionNuevaContrasena.validarPaso1("Paula Gómez", "paula@"))
        assertNull(ValidacionNuevaContrasena.validarPaso1("Paula Gómez", "paula@ejemplo.com"))
    }

    // --- Correo enmascarado ---

    @Test
    fun enmascaraElCorreo() {
        assertEquals("p***a@ejemplo.com", ValidacionNuevaContrasena.enmascararCorreo("paula@ejemplo.com"))
        assertEquals("a***@x.com", ValidacionNuevaContrasena.enmascararCorreo("ab@x.com"))
        assertEquals("sin-arroba", ValidacionNuevaContrasena.enmascararCorreo("sin-arroba"))
    }
}

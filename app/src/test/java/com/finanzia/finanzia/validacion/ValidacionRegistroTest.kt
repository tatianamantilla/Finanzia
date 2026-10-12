package com.finanzia.finanzia.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionRegistroTest {

    // --- Correo ---

    @Test
    fun correosValidos() {
        listOf("hola@ejemplo.com", "paula.gomez@empresa.com.co", "a+b@dominio.io", "  hola@ejemplo.com ")
            .forEach { assertTrue(it, ValidacionRegistro.esCorreoValido(it)) }
    }

    @Test
    fun correosInvalidos() {
        listOf("", "hola", "hola@", "@ejemplo.com", "hola@ejemplo", "hola@ejemplo.c", "hola ejemplo@x.com", "hola@@ejemplo.com")
            .forEach { assertFalse(it, ValidacionRegistro.esCorreoValido(it)) }
    }

    // --- Contraseña: cada indicador por separado ---

    @Test
    fun indicadorLongitud() {
        assertFalse(ValidacionRegistro.reglasContrasena("Abc1234").tieneLongitud) // 7
        assertTrue(ValidacionRegistro.reglasContrasena("Abc12345").tieneLongitud) // 8
    }

    @Test
    fun indicadorNumero() {
        assertFalse(ValidacionRegistro.reglasContrasena("Abcdefgh").tieneNumero)
        assertTrue(ValidacionRegistro.reglasContrasena("Abcdefg1").tieneNumero)
    }

    @Test
    fun indicadorMayuscula() {
        assertFalse(ValidacionRegistro.reglasContrasena("abcdefg1").tieneMayuscula)
        assertTrue(ValidacionRegistro.reglasContrasena("abcdefG1").tieneMayuscula)
        assertTrue(ValidacionRegistro.reglasContrasena("ñandú1Ñx").tieneMayuscula)
    }

    @Test
    fun contrasenaVaciaNoCumpleNinguna() {
        val reglas = ValidacionRegistro.reglasContrasena("")
        assertEquals(ReglasContrasena(false, false, false), reglas)
        assertFalse(reglas.cumpleTodas)
    }

    @Test
    fun contrasenaCompletaCumpleTodas() {
        assertTrue(ValidacionRegistro.reglasContrasena("Finanzia2026").cumpleTodas)
    }

    // --- Formulario completo ---

    private fun validar(
        nombre: String = "Paula Gómez",
        correo: String = "paula@ejemplo.com",
        contrasena: String = "Finanzia2026",
        acepta: Boolean = true,
    ) = ValidacionRegistro.validar(nombre, correo, contrasena, acepta)

    @Test
    fun formularioCorrecto() = assertNull(validar())

    @Test
    fun nombreObligatorio() {
        assertEquals(ErrorRegistro.NOMBRE_VACIO, validar(nombre = ""))
        assertEquals(ErrorRegistro.NOMBRE_VACIO, validar(nombre = "   "))
    }

    @Test
    fun correoObligatorioYConFormato() {
        assertEquals(ErrorRegistro.CORREO_VACIO, validar(correo = ""))
        assertEquals(ErrorRegistro.CORREO_INVALIDO, validar(correo = "paula@"))
    }

    @Test
    fun contrasenaObligatoriaYConReglas() {
        assertEquals(ErrorRegistro.CONTRASENA_VACIA, validar(contrasena = ""))
        assertEquals(ErrorRegistro.CONTRASENA_CORTA, validar(contrasena = "Abc1"))
        assertEquals(ErrorRegistro.CONTRASENA_SIN_NUMERO, validar(contrasena = "Abcdefgh"))
        assertEquals(ErrorRegistro.CONTRASENA_SIN_MAYUSCULA, validar(contrasena = "abcdefg1"))
    }

    @Test
    fun terminosObligatorios() {
        assertEquals(ErrorRegistro.TERMINOS_NO_ACEPTADOS, validar(acepta = false))
    }
}

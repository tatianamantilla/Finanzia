package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.navegacion.FinanziaApp
import com.finanzia.finanzia.ui.sesion.primerNombre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Recorre el inicio de sesión, el acceso autorizado y el Dashboard tal como lo describe la spec 01. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class NavegacionInicioSesionTest {

    @get:Rule
    val compose = createComposeRule()

    private val auth = FakeAuthRepository().apply {
        agregarUsuario(nombre = "Paula Gómez", correo = "paula@ejemplo.com", contrasena = "Finanzia2026")
    }

    @Before
    fun abrirInicioDeSesion() {
        compose.setContent { FinanziaApp(auth = auth, onboarding = FakeOnboardingRepository()) }
        clic("Iniciar sesión")
    }

    // --- Ayudantes ---

    private fun clic(texto: String) {
        compose.onNodeWithText(texto, substring = true).performScrollTo().performClick()
    }

    private fun seVe(texto: String) {
        compose.onNodeWithText(texto, substring = true).assertExists()
    }

    private fun escribir(etiqueta: String, texto: String) {
        compose.onNodeWithTag(etiqueta).performScrollTo().performTextInput(texto)
    }

    private fun tocarIcono(descripcion: String) {
        compose.onNode(hasContentDescription(descripcion)).performClick()
    }

    private fun entrarCon(correo: String, contrasena: String) {
        escribir(Etiquetas.CAMPO_CORREO, correo)
        escribir(Etiquetas.CAMPO_CONTRASENA, contrasena)
        clic("Entrar")
    }

    // --- Pantalla 1 · Inicio de sesión ---

    @Test
    fun conCamposVaciosMuestraQueFalta() {
        clic("Entrar")
        seVe("Ingresa tu correo y tu contraseña")
        seVe("¡Qué bueno verte!")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun conContrasenaIncorrectaNoEntra() {
        entrarCon("paula@ejemplo.com", "otraClave1")
        seVe("Correo o contraseña incorrectos")
        seVe("¡Qué bueno verte!")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun conCorreoNoRegistradoNoEntra() {
        entrarCon("nadie@ejemplo.com", "Finanzia2026")
        seVe("Correo o contraseña incorrectos")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun elOjoMuestraYOcultaLaContrasena() {
        // El ícono cambia de "Mostrar" a "Ocultar" y vuelve, según cómo esté la contraseña.
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        compose.onNode(hasContentDescription("Ocultar contraseña")).assertDoesNotExist()
        tocarIcono("Mostrar contraseña")
        compose.onNode(hasContentDescription("Mostrar contraseña")).assertDoesNotExist()
        tocarIcono("Ocultar contraseña")
        compose.onNode(hasContentDescription("Mostrar contraseña")).assertExists()
    }

    @Test
    fun olvidasteTuContrasenaAbreLaRecuperacion() {
        clic("¿Olvidaste tu contraseña?")
        compose.onNodeWithTag(Etiquetas.PANTALLA_OLVIDE_CONTRASENA).assertIsDisplayed()
    }

    // --- Pantalla 2 · Acceso autorizado ---

    @Test
    fun conDatosCorrectosEntraYSaludaConElPrimerNombre() {
        entrarCon("paula@ejemplo.com", "Finanzia2026")
        seVe("ACCESO AUTORIZADO • SESIÓN LISTA")
        seVe("¡Qué alegría tenerte de vuelta, Paula!")
        assertEquals("paula@ejemplo.com", auth.usuarioActual?.correo)
    }

    @Test
    fun elCorreoNoDistingueMayusculas() {
        entrarCon("Paula@Ejemplo.com", "Finanzia2026")
        seVe("¡Qué alegría tenerte de vuelta, Paula!")
    }

    @Test
    fun continuarLlevaAlDashboard() {
        entrarCon("paula@ejemplo.com", "Finanzia2026")
        clic("Continuar al Dashboard")
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
    }

    @Test
    fun primerNombreTomaSoloLaPrimeraPalabra() {
        assertEquals("Paula", primerNombre("  Paula   Gómez "))
        assertEquals("Ana", primerNombre("Ana"))
        assertEquals("", primerNombre(""))
    }

}


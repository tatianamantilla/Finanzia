package com.finanzia.finanzia.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.navegacion.FinanziaApp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Recorre olvidar contraseña (Paso 1, Paso 2 y su error) tal como lo describe la spec 02. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class NavegacionOlvideContrasenaTest {

    @get:Rule
    val compose = createComposeRule()

    private val auth = FakeAuthRepository().apply {
        agregarUsuario(nombre = "Paula Gómez", correo = "paula@ejemplo.com", contrasena = "Finanzia2026")
    }

    @Before
    fun abrirOlvideContrasena() {
        compose.setContent { FinanziaApp(auth = auth, onboarding = FakeOnboardingRepository()) }
        clic("Iniciar sesión")
        clic("¿Olvidaste tu contraseña?")
    }

    // --- Ayudantes ---

    private fun clic(texto: String) {
        compose.onNodeWithText(texto, substring = true).performScrollTo().performClick()
    }

    private fun seVe(texto: String) {
        compose.onNodeWithText(texto, substring = true).assertExists()
    }

    private fun noSeVe(texto: String) {
        compose.onNodeWithText(texto, substring = true).assertDoesNotExist()
    }

    private fun escribir(etiqueta: String, texto: String) {
        compose.onNodeWithTag(etiqueta).performScrollTo().performTextInput(texto)
    }

    private fun requisito(texto: String, cumplido: Boolean) {
        compose.onNodeWithText(texto).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                if (cumplido) Etiquetas.INDICADOR_CUMPLIDO else Etiquetas.INDICADOR_PENDIENTE,
            ),
        )
    }

    private fun irAlPaso2() {
        escribir(Etiquetas.CAMPO_NOMBRE, "Paula Gómez")
        escribir(Etiquetas.CAMPO_CORREO, "paula@ejemplo.com")
        clic("Verificar y continuar")
    }

    private fun escribirContrasenas(nueva: String, confirmacion: String) {
        escribir(Etiquetas.CAMPO_CONTRASENA, nueva)
        escribir(Etiquetas.CAMPO_CONFIRMACION, confirmacion)
    }

    // --- Paso 1 ---

    @Test
    fun abreElPaso1() {
        seVe("PASO 1 DE 2")
        seVe("¿Olvidaste tu contraseña?")
    }

    @Test
    fun paso1ConCamposVaciosDiceQueFalta() {
        clic("Verificar y continuar")
        compose.onNodeWithText("Ingresa tu nombre de usuario").assertExists() // texto exacto del Snackbar
        seVe("PASO 1 DE 2")
    }

    @Test
    fun paso1ConCorreoMalEscritoLoDice() {
        escribir(Etiquetas.CAMPO_NOMBRE, "Paula Gómez")
        escribir(Etiquetas.CAMPO_CORREO, "paula@")
        clic("Verificar y continuar")
        seVe("El correo no tiene un formato válido")
    }

    @Test
    fun paso1ConCorreoNoRegistradoLoDice() {
        escribir(Etiquetas.CAMPO_NOMBRE, "Paula Gómez")
        escribir(Etiquetas.CAMPO_CORREO, "nadie@ejemplo.com")
        clic("Verificar y continuar")
        seVe("Este correo no está registrado")
        seVe("PASO 1 DE 2")
    }

    @Test
    fun paso1ConNombreQueNoCoincideLoDice() {
        escribir(Etiquetas.CAMPO_NOMBRE, "Carlos Mendoza")
        escribir(Etiquetas.CAMPO_CORREO, "paula@ejemplo.com")
        clic("Verificar y continuar")
        seVe("El nombre de usuario no coincide con este correo")
        seVe("PASO 1 DE 2")
    }

    @Test
    fun volverAIniciarSesionRegresaAlLogin() {
        clic("Volver a Iniciar sesión")
        seVe("¡Qué bueno verte!")
    }

    @Test
    fun laFlechaAtrasRegresaAlLogin() {
        compose.onNodeWithTag(Etiquetas.BOTON_ATRAS).performClick()
        seVe("¡Qué bueno verte!")
    }

    // --- Paso 2 ---

    @Test
    fun paso2MuestraElCorreoEnmascarado() {
        irAlPaso2()
        seVe("PASO 2 DE 2")
        seVe("p***a@ejemplo.com")
    }

    @Test
    fun losRequisitosYLaSeguridadReaccionanEnVivo() {
        irAlPaso2()
        requisito("Mínimo 8 caracteres", false)
        requisito("Al menos 1 símbolo especial (!@#$)", false)

        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        requisito("Mínimo 8 caracteres", true)
        requisito("Al menos 1 número", true)
        requisito("Al menos 1 letra mayúscula", true)
        requisito("Al menos 1 símbolo especial (!@#$)", false)
        seVe("Seguridad media")

        escribir(Etiquetas.CAMPO_CONTRASENA, "!")
        requisito("Al menos 1 símbolo especial (!@#$)", true)
        seVe("Cumple requisitos")
    }

    @Test
    fun noSeGuardaSiNoCumpleLosRequisitos() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026", "Finanzia2026")
        clic("Actualizar contraseña")
        seVe("La nueva contraseña no cumple todos los requisitos")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun conConfirmacionDistintaMuestraLaPantallaDeError() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026!", "Finanzia2026?")
        clic("Actualizar contraseña")
        seVe("Las contraseñas no coinciden")
        seVe("No coincide")
        seVe("Ambas contraseñas deben ser idénticas.")
        seVe("PASO FINAL")
        requisito("Un símbolo especial", true)
        assertNull(auth.usuarioActual)
    }

    @Test
    fun mostrarAmbasContrasenasLasMuestra() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026!", "Finanzia2026?")
        clic("Actualizar contraseña")
        clic("Mostrar ambas contraseñas")
        compose.onNode(hasContentDescription("Mostrar contraseña")).assertDoesNotExist()
    }

    @Test
    fun corregirLaConfirmacionYActualizarLlegaAAccesoAutorizado() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026!", "Finanzia2026?")
        clic("Actualizar contraseña")
        compose.onNodeWithTag(Etiquetas.CAMPO_CONFIRMACION).performScrollTo().performTextReplacement("Finanzia2026!")
        noSeVe("No coincide")
        clic("Actualizar contraseña")
        seVe("¡Qué alegría tenerte de vuelta, Paula!")
    }

    @Test
    fun actualizarCambiaLaContrasenaYCierraLasDemasSesiones() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026!", "Finanzia2026!")
        clic("Actualizar contraseña")
        seVe("ACCESO AUTORIZADO • SESIÓN LISTA")
        seVe("¡Qué alegría tenerte de vuelta, Paula!")
        assertEquals(true, auth.cerroOtrasSesiones)
        // La contraseña vieja ya no sirve y la nueva sí.
        runBlocking {
            assertTrue(auth.iniciarSesion("paula@ejemplo.com", "Finanzia2026").isFailure)
            assertTrue(auth.iniciarSesion("paula@ejemplo.com", "Finanzia2026!").isSuccess)
        }
    }

    @Test
    fun desmarcarLaCasillaNoCierraLasDemasSesiones() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026!", "Finanzia2026!")
        compose.onNodeWithTag(Etiquetas.CASILLA_CERRAR_SESIONES).performScrollTo().performClick()
        clic("Actualizar contraseña")
        assertFalse(auth.cerroOtrasSesiones!!)
    }

    @Test
    fun continuarAlDashboardDespuesDeActualizar() {
        irAlPaso2()
        escribirContrasenas("Finanzia2026!", "Finanzia2026!")
        clic("Actualizar contraseña")
        clic("Continuar al Dashboard")
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
    }

    @Test
    fun laFlechaAtrasDelPaso2VuelveAlPaso1() {
        irAlPaso2()
        compose.onNodeWithTag(Etiquetas.BOTON_ATRAS).performClick()
        seVe("PASO 1 DE 2")
    }
}

package com.finanzia.finanzia.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.navegacion.FinanziaApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Recorre las pantallas 1 a 5 y el Dashboard tal como lo describe la spec 03. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class NavegacionCrearCuentaTest {

    @get:Rule
    val compose = createComposeRule()

    private val auth = FakeAuthRepository()
    private val onboarding = FakeOnboardingRepository()

    @Before
    fun abrirApp() {
        compose.setContent { FinanziaApp(auth = auth, onboarding = onboarding) }
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

    private fun seVePantalla(etiqueta: String) {
        compose.onNodeWithTag(etiqueta).assertIsDisplayed()
    }

    private fun escribir(etiqueta: String, texto: String) {
        compose.onNodeWithTag(etiqueta).performScrollTo().performTextInput(texto)
    }

    /** El Snackbar tapa la parte de abajo de la pantalla mientras está visible. */
    private fun esperarQueSeCierreElMensaje() {
        compose.mainClock.advanceTimeBy(6_000)
        compose.waitForIdle()
    }

    private fun llenarFormulario(correo: String = "paula@ejemplo.com") {
        escribir(Etiquetas.CAMPO_NOMBRE, "Paula Gómez")
        escribir(Etiquetas.CAMPO_CORREO, correo)
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
    }

    private fun aceptarTerminos() {
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
    }

    private fun crearCuenta() {
        llenarFormulario()
        aceptarTerminos()
        clic("Crear cuenta")
    }

    private fun irAPrimerosPasos() {
        crearCuenta()
        clic("Comenzar ahora")
    }

    private fun indicador(texto: String, cumplido: Boolean) {
        compose.onNodeWithText(texto).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                if (cumplido) Etiquetas.INDICADOR_CUMPLIDO else Etiquetas.INDICADOR_PENDIENTE,
            ),
        )
    }

    // --- Pantalla 1 · Crear cuenta ---

    @Test
    fun abreEnCrearCuenta() {
        seVe("¡Empieza tu viaje!")
    }

    @Test
    fun losIndicadoresDeContrasenaReaccionanEnVivo() {
        indicador("8+ carac.", false)
        indicador("1 número", false)
        indicador("1 mayúscula", false)

        escribir(Etiquetas.CAMPO_CONTRASENA, "abc")
        indicador("8+ carac.", false)
        indicador("1 número", false)

        escribir(Etiquetas.CAMPO_CONTRASENA, "1")
        indicador("1 número", true)
        indicador("1 mayúscula", false)

        escribir(Etiquetas.CAMPO_CONTRASENA, "Defgh")
        indicador("8+ carac.", true)
        indicador("1 número", true)
        indicador("1 mayúscula", true)
    }

    @Test
    fun sinMarcarLaCasillaNoSeCreaLaCuenta() {
        llenarFormulario()
        clic("Crear cuenta")
        seVe("Debes aceptar los Términos de Servicio y la Política de Privacidad")
        seVe("¡Empieza tu viaje!")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun conCamposVaciosMuestraQueFalta() {
        clic("Crear cuenta")
        seVe("Ingresa tu nombre completo")
        seVe("¡Empieza tu viaje!")
    }

    @Test
    fun conCorreoInvalidoMuestraElMotivo() {
        llenarFormulario(correo = "paula@")
        aceptarTerminos()
        clic("Crear cuenta")
        seVe("El correo no tiene un formato válido")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun conContrasenaDebilMuestraElMotivo() {
        escribir(Etiquetas.CAMPO_NOMBRE, "Paula Gómez")
        escribir(Etiquetas.CAMPO_CORREO, "paula@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "finanzia2026")
        aceptarTerminos()
        clic("Crear cuenta")
        seVe("La contraseña necesita al menos 1 mayúscula")
        assertNull(auth.usuarioActual)
    }

    @Test
    fun crearCuentaLlevaACuentaCreadaYMuestraElCorreoReal() {
        crearCuenta()
        seVe("¡CUENTA CREADA CON ÉXITO!")
        seVe("paula@ejemplo.com")
        assertEquals("paula@ejemplo.com", auth.usuarioActual?.correo)
    }

    @Test
    fun iniciarSesionAbreLaPantallaProvisional() {
        clic("Iniciar sesión")
        noSeVe("¡Empieza tu viaje!")
        seVePantalla(Etiquetas.PANTALLA_INICIAR_SESION)
    }

    // --- Pantallas 2 y 3 ---

    @Test
    fun comenzarAhoraLlevaAPrimerosPasos() {
        irAPrimerosPasos()
        seVe("Paso 1 de 2")
        seVe("¡Hola! Soy Finanzia")
    }

    @Test
    fun explorarMasTardeLlevaAlDashboard() {
        irAPrimerosPasos()
        clic("Explorar más tarde e ir al Dashboard")
        seVePantalla(Etiquetas.PANTALLA_DASHBOARD)
    }

    // --- Pantalla 4 · Primer ingreso ---

    @Test
    fun primerIngresoIdaYVuelta() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer ingreso")
        seVe("Registra tu primer ingreso")
        compose.onNodeWithTag(Etiquetas.BOTON_ATRAS).performClick()
        seVe("Paso 1 de 2")
    }

    @Test
    fun ingresoSoloPermiteUnaOpcion() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer ingreso")
        compose.onNodeWithText("Mensual").assertIsSelected()

        compose.onNodeWithText("Quincenal").performClick()
        compose.onNodeWithText("Quincenal").assertIsSelected()
        compose.onNodeWithText("Mensual").assertIsNotSelected()
        compose.onNodeWithText("Semanal").assertIsNotSelected()
        compose.onNodeWithText("Diario").assertIsNotSelected()

        compose.onNodeWithText("Diario").performClick()
        compose.onNodeWithText("Diario").assertIsSelected()
        compose.onNodeWithText("Quincenal").assertIsNotSelected()
    }

    @Test
    fun ingresoNoSeGuardaVacioNiEnCero() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer ingreso")
        clic("Guardar y continuar")
        seVe("Ingresa un monto")
        esperarQueSeCierreElMensaje()
        escribir(Etiquetas.CAMPO_MONTO, "0")
        clic("Guardar y continuar")
        seVe("Ingresa un monto mayor a cero")
        seVe("Registra tu primer ingreso")
        assertTrue(onboarding.ingresos.isEmpty())
    }

    @Test
    fun guardarIngresoGuardaYLlevaAlDashboard() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer ingreso")
        escribir(Etiquetas.CAMPO_MONTO, "18500")
        seVe("18,500")
        clic("Semanal")
        clic("Guardar y continuar")
        seVePantalla(Etiquetas.PANTALLA_DASHBOARD)
        assertEquals(listOf(PrimerIngreso(18500, FrecuenciaIngreso.SEMANAL)), onboarding.ingresos)
    }

    @Test
    fun omitirIngresoNoGuardaNada() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer ingreso")
        escribir(Etiquetas.CAMPO_MONTO, "18500")
        clic("Omitir este paso por ahora")
        seVePantalla(Etiquetas.PANTALLA_DASHBOARD)
        assertTrue(onboarding.ingresos.isEmpty())
        assertTrue(onboarding.gastos.isEmpty())
    }

    // --- Pantalla 5 · Primer gasto ---

    @Test
    fun primerGastoIdaYVuelta() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer gasto")
        seVe("Registra tu primer gasto")
        compose.onNodeWithTag(Etiquetas.BOTON_ATRAS).performClick()
        seVe("Paso 1 de 2")
    }

    @Test
    fun gastoSoloPermiteUnaOpcion() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer gasto")
        compose.onNodeWithText("Gastos Fijos").assertIsSelected()

        compose.onNodeWithText("Comida").performClick()
        compose.onNodeWithText("Comida").assertIsSelected()
        compose.onNodeWithText("Gastos Fijos").assertIsNotSelected()
        compose.onNodeWithText("Diversión").assertIsNotSelected()
        compose.onNodeWithText("Transporte").assertIsNotSelected()
    }

    @Test
    fun gastoNoSeGuardaVacio() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer gasto")
        clic("Guardar gasto y finalizar")
        seVe("Ingresa un monto")
        seVe("Registra tu primer gasto")
        assertTrue(onboarding.gastos.isEmpty())
    }

    @Test
    fun guardarGastoGuardaYLlevaAlDashboard() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer gasto")
        escribir(Etiquetas.CAMPO_MONTO, "3200")
        seVe("3,200")
        clic("Transporte")
        clic("Guardar gasto y finalizar")
        seVePantalla(Etiquetas.PANTALLA_DASHBOARD)
        assertEquals(listOf(PrimerGasto(3200, CategoriaGasto.TRANSPORTE)), onboarding.gastos)
    }

    @Test
    fun omitirGastoNoGuardaNada() {
        irAPrimerosPasos()
        clic("Quiero registrar mi primer gasto")
        escribir(Etiquetas.CAMPO_MONTO, "3200")
        clic("Omitir este paso por ahora")
        seVePantalla(Etiquetas.PANTALLA_DASHBOARD)
        assertTrue(onboarding.ingresos.isEmpty())
        assertTrue(onboarding.gastos.isEmpty())
    }
}

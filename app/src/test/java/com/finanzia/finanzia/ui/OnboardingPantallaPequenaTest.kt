package com.finanzia.finanzia.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Recorre la app completa (Crear cuenta → Cuenta creada → Primeros pasos → Paso 2 → Dashboard)
 * en pantallas pequeñas, con la navegación real. En cada paso desplaza hasta el botón o enlace,
 * comprueba que queda completo dentro de la pantalla y lo toca.
 */
abstract class OnboardingPantallaPequenaBase {

    @get:Rule
    val compose = createComposeRule()

    private val auth = FakeAuthRepository()
    private val onboarding = FakeOnboardingRepository()

    @Before
    fun abrirApp() {
        compose.setContent { FinanziaApp(auth = auth, onboarding = onboarding) }
    }

    // --- Ayudantes ---

    /** Desplaza hasta el elemento, comprueba que queda completo dentro de la pantalla y lo toca. */
    private fun alcanzarYTocar(nodo: SemanticsNodeInteraction) {
        nodo.performScrollTo()
        val pantalla = compose.onRoot().fetchSemanticsNode().boundsInRoot
        val elemento = nodo.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "El elemento $elemento no queda completo en la pantalla $pantalla",
            elemento.top >= pantalla.top - 1f && elemento.bottom <= pantalla.bottom + 1f,
        )
        nodo.performClick()
        compose.waitForIdle()
    }

    private fun boton(texto: String) = compose.onNode(hasText(texto, substring = true))

    private fun escribir(etiqueta: String, texto: String) {
        compose.onNode(hasTestTag(etiqueta)).performScrollTo().performTextInput(texto)
    }

    private fun seVePantalla(texto: String) {
        compose.onNode(hasText(texto, substring = true)).assertExists()
    }

    private fun seVeDashboard() {
        compose.onNode(hasTestTag(Etiquetas.PANTALLA_DASHBOARD)).assertIsDisplayed()
    }

    private fun irAPrimerosPasos() {
        escribir(Etiquetas.CAMPO_NOMBRE, "Paula Gómez")
        escribir(Etiquetas.CAMPO_CORREO, "nueva@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        alcanzarYTocar(compose.onNode(hasTestTag(Etiquetas.CASILLA_TERMINOS)))
        alcanzarYTocar(compose.onNode(hasText("Crear cuenta")))
        seVePantalla("¡CUENTA CREADA CON ÉXITO!")
        alcanzarYTocar(boton("Comenzar ahora"))
        seVePantalla("Paso 1 de 2")
    }

    // --- Pantalla 2 → Pantalla 3 ---

    @Test
    fun comenzarAhoraLlevaAPrimerosPasos() {
        irAPrimerosPasos()
        seVePantalla("¡Hola! Soy Finanzia")
    }

    // --- Pantalla 4 · Primer ingreso ---

    @Test
    fun guardarYContinuarGuardaElIngresoYLlevaAlDashboard() {
        irAPrimerosPasos()
        alcanzarYTocar(boton("Quiero registrar mi primer ingreso"))
        escribir(Etiquetas.CAMPO_MONTO, "18500")
        alcanzarYTocar(boton("Quincenal"))
        alcanzarYTocar(boton("Guardar y continuar"))

        seVeDashboard()
        assertEquals(listOf(PrimerIngreso(18500, FrecuenciaIngreso.QUINCENAL)), onboarding.ingresos)
    }

    @Test
    fun omitirEnIngresoLlevaAlDashboardSinGuardar() {
        irAPrimerosPasos()
        alcanzarYTocar(boton("Quiero registrar mi primer ingreso"))
        escribir(Etiquetas.CAMPO_MONTO, "18500")
        alcanzarYTocar(boton("Omitir este paso por ahora"))

        seVeDashboard()
        assertTrue(onboarding.ingresos.isEmpty())
        assertTrue(onboarding.gastos.isEmpty())
    }

    // --- Pantalla 5 · Primer gasto ---

    @Test
    fun guardarGastoYFinalizarGuardaElGastoYLlevaAlDashboard() {
        irAPrimerosPasos()
        alcanzarYTocar(boton("Quiero registrar mi primer gasto"))
        escribir(Etiquetas.CAMPO_MONTO, "3200")
        alcanzarYTocar(boton("Comida"))
        alcanzarYTocar(boton("Guardar gasto y finalizar"))

        seVeDashboard()
        assertEquals(listOf(PrimerGasto(3200, CategoriaGasto.COMIDA)), onboarding.gastos)
    }

    @Test
    fun omitirEnGastoLlevaAlDashboardSinGuardar() {
        irAPrimerosPasos()
        alcanzarYTocar(boton("Quiero registrar mi primer gasto"))
        escribir(Etiquetas.CAMPO_MONTO, "3200")
        alcanzarYTocar(boton("Omitir este paso por ahora"))

        seVeDashboard()
        assertTrue(onboarding.ingresos.isEmpty())
        assertTrue(onboarding.gastos.isEmpty())
    }
}

/** Teléfono pequeño (320 × 568 dp). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h568dp")
class OnboardingPantallaPequenaTest : OnboardingPantallaPequenaBase()

/**
 * Espacio que queda en un teléfono pequeño con el teclado abierto (568 dp − ~260 dp de teclado).
 * La app reduce su área visible de la misma forma cuando aparece el teclado, así que esto comprueba
 * que los botones siguen alcanzables. (El teclado real no se puede abrir en estos tests.)
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h300dp")
class OnboardingConTecladoAbiertoTest : OnboardingPantallaPequenaBase()

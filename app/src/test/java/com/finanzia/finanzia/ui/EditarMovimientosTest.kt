package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.navegacion.FinanziaApp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Spec 10: regla del saldo en la interfaz, edición de movimientos y "Ver todos". */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class EditarMovimientosTest {

    @get:Rule
    val compose = createComposeRule()

    private val auth = FakeAuthRepository()
    private val onboarding = FakeOnboardingRepository()

    // --- Ayudantes ---

    private fun clic(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun seVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().assertExists()
    }

    private fun noSeVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).assertCountEquals(0)
    }

    private fun escribirMonto(texto: String) {
        compose.onNodeWithTag(Etiquetas.CAMPO_MONTO).performScrollTo().performTextClearance()
        compose.onNodeWithTag(Etiquetas.CAMPO_MONTO).performTextInput(texto)
        compose.waitForIdle()
    }

    private fun hayErrorEnRojo(cuantos: Int) {
        compose.onAllNodes(hasContentDescription("Error"), useUnmergedTree = true).assertCountEquals(cuantos)
    }

    /** Usuario con sesión iniciada: la app abre en "Acceso autorizado" y desde ahí va al Dashboard. */
    private fun abrirDashboardCon(vararg movimientos: Any) {
        movimientos.forEach {
            when (it) {
                is PrimerIngreso -> onboarding.ingresos += it
                is PrimerGasto -> onboarding.gastos += it
            }
        }
        auth.agregarUsuario("Mateo Marin", "mateo@ejemplo.com", "Finanzia2026")
        runBlocking { auth.iniciarSesion("mateo@ejemplo.com", "Finanzia2026") }
        compose.setContent { FinanziaApp(auth = auth, onboarding = onboarding) }
        compose.waitForIdle()
        clic("Continuar al Dashboard")
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
    }

    // --- Onboarding: el primer gasto sin saldo se bloquea ---

    @Test
    fun elOnboardingBloqueaElPrimerGastoSinSaldoYElErrorDesapareceAlCorregir() {
        compose.setContent { FinanziaApp(auth = auth, onboarding = onboarding) }
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.CAMPO_NOMBRE).performScrollTo().performTextInput("Mateo Marin")
        compose.onNodeWithTag(Etiquetas.CAMPO_CORREO).performScrollTo().performTextInput("mateo@ejemplo.com")
        compose.onNodeWithTag(Etiquetas.CAMPO_CONTRASENA).performScrollTo().performTextInput("Finanzia2026")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        clic("Crear cuenta")
        clic("Comenzar ahora")
        clic("Quiero registrar mi primer gasto")
        escribirMonto("3200")
        clic("Guardar gasto y finalizar")

        seVe("No tienes saldo suficiente. Tu saldo disponible es $0.00. Registra primero un ingreso.")
        hayErrorEnRojo(1)
        seVe("Registra tu primer gasto") // no se fue al Dashboard
        assertTrue("No se guardó nada", onboarding.gastos.isEmpty())

        escribirMonto("320")
        noSeVe("No tienes saldo suficiente")
        hayErrorEnRojo(0)
    }

    // --- "+" del Dashboard: gasto mayor que el saldo ---

    @Test
    fun unGastoNuevoMayorQueElSaldoMuestraElErrorEnElMonto() {
        abrirDashboardCon(PrimerIngreso(10_000, FrecuenciaIngreso.MENSUAL))
        compose.onNodeWithTag(Etiquetas.BOTON_MAS).performClick()
        compose.waitForIdle()
        compose.onNode(hasText("Gasto")).performClick() // la opción del menú "+", no el recuadro "Gastos"
        compose.waitForIdle()
        escribirMonto("40000")
        clic("Guardar gasto")
        seVe("No tienes saldo suficiente. Tu saldo disponible es $10,000.00.")
        noSeVe("Registra primero un ingreso") // esa pista es solo del onboarding
        assertTrue(onboarding.gastos.isEmpty())

        escribirMonto("10000") // igual al saldo: se permite y queda en $0.00
        noSeVe("No tienes saldo suficiente")
        clic("Guardar gasto")
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$0.00")
    }

    // --- Editar desde "Movimientos recientes" ---

    @Test
    fun tocarUnMovimientoAbreSuEdicionConLosValoresCargados() {
        abrirDashboardCon(PrimerIngreso(50_000, FrecuenciaIngreso.QUINCENAL, "Sueldo"))
        clic("Sueldo")
        seVe("Editar ingreso")
        compose.onNodeWithTag(Etiquetas.CAMPO_MONTO).assertTextEquals("50,000")
        compose.onNodeWithText("Quincenal").assertIsSelected()
        seVe("Guardar cambios")
        noSeVe("Omitir este paso por ahora")
    }

    @Test
    fun alGuardarCambiosElDashboardSeActualiza() {
        abrirDashboardCon(PrimerIngreso(50_000, FrecuenciaIngreso.MENSUAL, "Sueldo"), PrimerGasto(10_000, CategoriaGasto.COMIDA, "Mercado"))
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$40,000.00")

        clic("Mercado")
        seVe("Editar gasto")
        compose.onNodeWithTag(Etiquetas.CAMPO_MONTO).assert(hasText("10,000")) // el "$" del gasto está en el mismo campo
        escribirMonto("15000")
        clic("Transporte")
        clic("Guardar cambios")

        // De vuelta en el Dashboard, con el balance, la lista y la categoría al día.
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$35,000.00")
        seVe("-$15,000.00")
        seVe("Transporte")
        // El consejo también se recalcula: ahora la categoría con más gasto es Transporte.
        seVe("Tu categoría con más gasto este mes es Transporte")
        // Se cambió el mismo gasto, no se creó otro.
        assertEquals(listOf(PrimerGasto(15_000, CategoriaGasto.TRANSPORTE, "Mercado")), onboarding.gastos)
    }

    @Test
    fun editarUnGastoPorEncimaDelSaldoSeRechazaSinGuardar() {
        abrirDashboardCon(PrimerIngreso(50_000, FrecuenciaIngreso.MENSUAL, "Sueldo"), PrimerGasto(10_000, CategoriaGasto.COMIDA, "Mercado"))
        clic("Mercado")
        // Saldo 40,000 + los 10,000 que ya descontaba = 50,000 disponibles para este gasto.
        escribirMonto("50001")
        clic("Guardar cambios")
        seVe("No tienes saldo suficiente. Tu saldo disponible es $50,000.00.")
        seVe("Editar gasto")
        assertEquals(10_000L, onboarding.gastos.single().monto)

        escribirMonto("50000")
        noSeVe("No tienes saldo suficiente")
    }

    @Test
    fun reducirUnIngresoPorDebajoDeLoGastadoSeRechaza() {
        abrirDashboardCon(PrimerIngreso(50_000, FrecuenciaIngreso.MENSUAL, "Sueldo"), PrimerGasto(30_000, CategoriaGasto.COMIDA, "Mercado"))
        clic("Sueldo")
        escribirMonto("20000")
        clic("Guardar cambios")
        seVe("No tienes saldo suficiente. Tu saldo disponible es $20,000.00.")
        assertEquals(50_000L, onboarding.ingresos.single().monto)
    }

    @Test
    fun laFlechaAtrasVuelveSinGuardar() {
        abrirDashboardCon(PrimerIngreso(50_000, FrecuenciaIngreso.MENSUAL, "Sueldo"))
        clic("Sueldo")
        escribirMonto("99000")
        compose.onNodeWithTag(Etiquetas.BOTON_ATRAS).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$50,000.00")
        assertEquals(50_000L, onboarding.ingresos.single().monto)
    }

    // --- "Ver todos" ---

    @Test
    fun verTodosAbreLaListaCompletaYSePuedeEditarDesdeAhi() {
        abrirDashboardCon(
            PrimerIngreso(100_000, FrecuenciaIngreso.MENSUAL, "Sueldo"),
            PrimerGasto(1_000, CategoriaGasto.COMIDA, "Gasto uno"),
            PrimerGasto(2_000, CategoriaGasto.COMIDA, "Gasto dos"),
            PrimerGasto(3_000, CategoriaGasto.COMIDA, "Gasto tres"),
            PrimerGasto(4_000, CategoriaGasto.COMIDA, "Gasto cuatro"),
        )
        // El Dashboard muestra solo los 4 más recientes.
        compose.onAllNodesWithTag(Etiquetas.FILA_MOVIMIENTO).assertCountEquals(4)

        clic("Ver todos")
        compose.onNodeWithTag(Etiquetas.PANTALLA_MOVIMIENTOS).assertIsDisplayed()
        compose.onAllNodesWithTag(Etiquetas.FILA_MOVIMIENTO).assertCountEquals(5)
        seVe("Sueldo")

        clic("Gasto dos")
        seVe("Editar gasto")
        escribirMonto("2500")
        clic("Guardar cambios")

        // Vuelve a la lista, ya actualizada.
        compose.onNodeWithTag(Etiquetas.PANTALLA_MOVIMIENTOS).assertIsDisplayed()
        seVe("-$2,500.00")
    }

    @Test
    fun verTodosSinMovimientosMuestraElEstadoVacio() {
        abrirDashboardCon()
        clic("Ver todos")
        compose.onNodeWithTag(Etiquetas.PANTALLA_MOVIMIENTOS).assertIsDisplayed()
        seVe("Aún no tienes movimientos")
        compose.onNodeWithTag(Etiquetas.BOTON_ATRAS).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
    }
}

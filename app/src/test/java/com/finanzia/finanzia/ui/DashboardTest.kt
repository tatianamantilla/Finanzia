package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.MetaDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.SinConexionException
import com.finanzia.finanzia.datos.TipoMovimiento
import com.finanzia.finanzia.navegacion.FinanziaApp
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.ZoneId

/** Ejemplos de las imágenes del diseño: nunca deben aparecer en la pantalla (spec 04). */
val DATOS_DE_EJEMPLO = listOf(
    "Paula", "12,450", "8492", "Mercadona", "Vacaciones", "12% extra", "3 suscripciones",
    "Ver plan", "Nómina", "Spotify", "4,200", "1,850", "Saludable +4.2%",
)

private val ZONA = ZoneId.of("America/Bogota")
private val AHORA = Instant.parse("2026-10-13T20:00:00Z")

private val datosConMovimientos = DatosDashboard(
    nombreUsuario = "Mateo Marin",
    cuentas = listOf(CuentaDashboard("Principal", 0.0)),
    movimientos = listOf(
        MovimientoDashboard(2, TipoMovimiento.GASTO, 350_000.0, "Primer gasto", Instant.parse("2026-10-13T19:32:00Z"), "Principal", "Comida", "#F59E0B"),
        MovimientoDashboard(1, TipoMovimiento.INGRESO, 2_500_000.0, "Primer ingreso", Instant.parse("2026-10-12T14:00:00Z"), "Principal", "Ingresos", "#22C55E"),
    ),
    meta = null,
)

private val datosVacios = DatosDashboard("Mateo Marin", listOf(CuentaDashboard("Principal", 0.0)), emptyList(), null)

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class DashboardTest {

    @get:Rule
    val compose = createComposeRule()

    private fun mostrar(datos: Result<DatosDashboard>): DashboardFijo {
        val repositorio = DashboardFijo(datos)
        compose.setContent {
            FinanziaTheme {
                DashboardScreen(
                    DashboardViewModel(repositorio, FakeAuthRepository(), ahora = { AHORA }, zona = ZONA),
                    onSesionCerrada = {},
                )
            }
        }
        compose.waitForIdle()
        return repositorio
    }

    /** El texto aparece al menos una vez (un monto puede estar en el recuadro y en la fila). */
    private fun seVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().performScrollTo().assertExists()
    }

    private fun noAparece(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true), useUnmergedTree = true).assertCountEquals(0)
    }

    private fun sinDatosDeEjemplo() = DATOS_DE_EJEMPLO.forEach(::noAparece)

    @Test
    fun muestraElNombreElBalanceYLosMovimientosReales() {
        mostrar(Result.success(datosConMovimientos))
        seVe("¡Hola, Mateo! 👋")
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$2,150,000.00")
        seVe("Cuenta Principal")
        seVe("+$2,500,000.00")
        seVe("-$350,000.00")
        compose.onAllNodesWithTag(Etiquetas.FILA_MOVIMIENTO).assertCountEquals(2)
        seVe("Primer ingreso")
        seVe("Primer gasto")
        seVe("Hoy, 14:32 • Principal")
        seVe("Ayer, 09:00 • Principal")
        seVe("Comida")
        // Consejo con la categoría de mayor gasto del mes.
        seVe("Tu categoría con más gasto este mes es Comida")
        seVe("100%")
        // Ahorro del mes: (2,500,000 − 350,000) ÷ 2,500,000 = 86.0 %.
        compose.onNodeWithTag(Etiquetas.PASTILLA_SALUDABLE).assertExists()
        seVe("Saludable +86.0%")
        compose.onNodeWithTag(Etiquetas.LINEA_TENDENCIA).assertExists()
        seVe("Aún no tienes metas")
        sinDatosDeEjemplo()
    }

    @Test
    fun losMovimientosVanDelMasRecienteAlMasAntiguo() {
        mostrar(Result.success(datosConMovimientos))
        // Posición real en la pantalla (aunque esté fuera de la parte visible).
        val gasto = compose.onNodeWithText("Primer gasto").fetchSemanticsNode().positionInRoot.y
        val ingreso = compose.onNodeWithText("Primer ingreso").fetchSemanticsNode().positionInRoot.y
        // El gasto es de hoy y el ingreso de ayer: el gasto va arriba.
        assertTrue("El movimiento más reciente debe ir primero (gasto=$gasto, ingreso=$ingreso)", gasto < ingreso)
    }

    @Test
    fun usuarioSinMovimientosVeLosEstadosVacios() {
        mostrar(Result.success(datosVacios))
        seVe("¡Hola, Mateo! 👋")
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$0.00")
        seVe("+$0.00")
        seVe("-$0.00")
        seVe("Aún no tienes movimientos")
        seVe("Aún no tienes metas")
        seVe("Aún no hay suficiente información para darte un consejo. Registra tus ingresos y gastos para empezar.")
        compose.onNodeWithTag(Etiquetas.PASTILLA_SALUDABLE).assertDoesNotExist()
        compose.onNodeWithTag(Etiquetas.LINEA_TENDENCIA).assertDoesNotExist()
        compose.onAllNodesWithTag(Etiquetas.FILA_MOVIMIENTO).assertCountEquals(0)
        sinDatosDeEjemplo()
    }

    @Test
    fun conUnSoloMovimientoNoSeDibujaLaLineaDeTendencia() {
        mostrar(Result.success(datosConMovimientos.copy(movimientos = datosConMovimientos.movimientos.take(1))))
        compose.onNodeWithTag(Etiquetas.LINEA_TENDENCIA).assertDoesNotExist()
        // Solo gastos en el mes: no hay ingresos, así que la pastilla "Saludable" no se muestra.
        compose.onNodeWithTag(Etiquetas.PASTILLA_SALUDABLE).assertDoesNotExist()
    }

    @Test
    fun conMetaMuestraSuProgresoReal() {
        mostrar(Result.success(datosVacios.copy(meta = MetaDashboard("Viaje a la costa", 2000.0, 500.0))))
        seVe("Meta: Viaje a la costa")
        seVe("Llevas $500 de $2,000")
        seVe("¡A solo $1,500 de lograr tu objetivo!")
        noAparece("Aún no tienes metas")
    }

    @Test
    fun noAparecenLasCuentasNiLosElementosSinDatos() {
        mostrar(Result.success(datosConMovimientos))
        noAparece("• • • •")
        noAparece("suscripciones analizadas")
        noAparece("Ver plan")
    }

    @Test
    fun elOjoOcultaYMuestraElBalance() {
        mostrar(Result.success(datosConMovimientos))
        compose.onNodeWithTag(Etiquetas.BOTON_OJO_BALANCE).performClick()
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("••••••")
        compose.onNodeWithTag(Etiquetas.BOTON_OJO_BALANCE).performClick()
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$2,150,000.00")
    }

    @Test
    fun siFallaLaLecturaMuestraElErrorYPermiteReintentar() {
        val repositorio = mostrar(Result.failure(SinConexionException()))
        seVe("No hay conexión con el servidor")
        compose.onNodeWithTag(Etiquetas.BOTON_REINTENTAR).performClick()
        compose.waitForIdle()
        assertEquals(2, repositorio.cargas)
    }
}

/** Navegación real con los repositorios simulados: onboarding → Dashboard → cerrar sesión. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class DashboardNavegacionTest {

    @get:Rule
    val compose = createComposeRule()

    private val auth = FakeAuthRepository()
    private val onboarding = FakeOnboardingRepository()

    private fun clic(texto: String) {
        compose.onNodeWithText(texto, substring = true).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun escribir(etiqueta: String, texto: String) {
        compose.onNodeWithTag(etiqueta).performScrollTo().performTextInput(texto)
    }

    private fun crearCuentaEIrAPrimerosPasos() {
        compose.setContent { FinanziaApp(auth = auth, onboarding = onboarding) }
        compose.waitForIdle()
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        clic("Crear cuenta")
        clic("Comenzar ahora")
    }

    @Test
    fun despuesDelOnboardingMuestraLoQueSeAcabaDeRegistrar() {
        crearCuentaEIrAPrimerosPasos()
        clic("Quiero registrar mi primer ingreso")
        escribir(Etiquetas.CAMPO_MONTO, "18500")
        clic("Guardar y continuar")

        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithText("¡Hola, Mateo! 👋").assertExists()
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals("$18,500.00")
        compose.onNodeWithText("Primer ingreso", substring = true).performScrollTo().assertExists()
        DATOS_DE_EJEMPLO.forEach {
            compose.onAllNodes(hasText(it, substring = true), useUnmergedTree = true).assertCountEquals(0)
        }
    }

    @Test
    fun cerrarSesionVuelveAlInicioDeSesion() {
        crearCuentaEIrAPrimerosPasos()
        clic("Explorar más tarde e ir al Dashboard")
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()

        compose.onNodeWithTag(Etiquetas.BOTON_PERFIL).performClick()
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("¡Qué bueno verte!", substring = true).assertExists()
        assertNull(auth.usuarioActual)
    }
}

/** En un teléfono pequeño se llega hasta el final y los botones no quedan tapados. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h568dp")
class DashboardPantallaPequenaTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun seLlegaAlFinalSinQueElBotonMasNiLaBarraTapenNada() {
        compose.setContent {
            FinanziaTheme {
                DashboardScreen(
                    DashboardViewModel(DashboardFijo(Result.success(datosConMovimientos)), FakeAuthRepository(), ahora = { AHORA }, zona = ZONA),
                    onSesionCerrada = {},
                )
            }
        }
        compose.waitForIdle()
        // Desplazar hasta el final.
        repeat(8) { compose.onNode(hasScrollAction()).performTouchInput { swipeUp() } }
        compose.waitForIdle()

        val boton = compose.onNodeWithText("Conversar con Finanzia IA").fetchSemanticsNode().boundsInRoot
        val barra = compose.onNodeWithTag(Etiquetas.BARRA_INFERIOR).fetchSemanticsNode().boundsInRoot
        val mas = compose.onNodeWithTag(Etiquetas.BOTON_MAS).fetchSemanticsNode().boundsInRoot
        val pantalla = compose.onRoot().fetchSemanticsNode().boundsInRoot

        assertTrue("El botón se sale de la pantalla", boton.top >= pantalla.top)
        assertTrue("La barra inferior tapa el botón", boton.bottom <= barra.top + 1f)
        assertTrue("El botón + tapa el botón Conversar", boton.bottom <= mas.top + 1f || boton.right <= mas.left)
        // La barra inferior queda fija abajo.
        assertEquals(pantalla.bottom, barra.bottom, 1f)
    }
}

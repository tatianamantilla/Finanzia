package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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
import com.finanzia.finanzia.navegacion.Rutas
import com.finanzia.finanzia.ui.estadisticas.EstadisticasScreen
import com.finanzia.finanzia.ui.estadisticas.EstadisticasViewModel
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.validacion.TipoPeriodo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Ejemplos de docs/design/estadisticas_1.jpeg: nunca deben aparecer en la pantalla (spec 11).
 * Es el único lugar del código donde están, para comprobar que no se muestran.
 */
val DATOS_DE_EJEMPLO_ESTADISTICAS = listOf(
    "Mar 2026", "2.350.000", "4.200.000", "1.850.000", "14.2%", "6.5%", "vs Feb", "44%", "61.600",
    "IA 98%", "98%", "Vacaciones San Andrés", "120.000", "6 días antes", "Gastos Fijos (44%)", "18%",
    "proyección", "Sem 4*", "Excelente autocontrol",
)

private val ZONA = ZoneId.of("America/Bogota")

/** Martes 3 de noviembre de 2026, mediodía en Bogotá. */
private val AHORA = Instant.parse("2026-11-03T17:00:00Z")

private var siguienteId = 0L

private fun mov(tipo: TipoMovimiento, monto: String, fecha: String, categoria: String) =
    MovimientoDashboard(++siguienteId, tipo, BigDecimal(monto), "Prueba", Instant.parse("${fecha}T17:00:00Z"), "Principal", categoria, null)

/** Octubre: neto 1.400.000. Noviembre (hasta hoy): ingresos 2.500.000, gastos 510.000, neto 1.990.000. */
val datosEstadisticas = DatosDashboard(
    nombreUsuario = "Mateo Marin",
    cuentas = listOf(CuentaDashboard("Principal", BigDecimal.ZERO)),
    movimientos = listOf(
        mov(TipoMovimiento.INGRESO, "2000000", "2026-10-05", "Mensual"),
        mov(TipoMovimiento.GASTO, "500000", "2026-10-10", "Comida"),
        mov(TipoMovimiento.GASTO, "100000", "2026-10-20", "Transporte"),
        mov(TipoMovimiento.INGRESO, "2500000", "2026-11-01", "Mensual"),
        mov(TipoMovimiento.GASTO, "420000", "2026-11-02", "Comida"),
        mov(TipoMovimiento.GASTO, "90000", "2026-11-03", "Diversión"),
    ),
    meta = MetaDashboard("Viaje familiar", BigDecimal("3000000"), BigDecimal("1000000")),
)

private val datosSinMovimientos = datosEstadisticas.copy(movimientos = emptyList())

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class EstadisticasTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var viewModel: EstadisticasViewModel

    private fun mostrar(datos: Result<DatosDashboard>): DashboardFijo {
        val repositorio = DashboardFijo(datos)
        viewModel = EstadisticasViewModel(repositorio, FakeAuthRepository(), ahora = { AHORA }, zona = ZONA)
        compose.setContent { FinanziaTheme { EstadisticasScreen(viewModel, onSesionCerrada = {}) } }
        compose.waitForIdle()
        return repositorio
    }

    private fun seVe(texto: String) =
        compose.onAllNodes(hasText(texto, substring = true), useUnmergedTree = true).onFirst().performScrollTo().assertExists()

    private fun noAparece(texto: String) =
        compose.onAllNodes(hasText(texto, substring = true), useUnmergedTree = true).assertCountEquals(0)

    private fun periodo(tipo: TipoPeriodo) {
        compose.onNodeWithTag(Etiquetas.OPCION_PERIODO + tipo.name).performClick()
        compose.waitForIdle()
    }

    @Test
    fun porDefectoMuestraElMesActualConLasCifrasReales() {
        mostrar(Result.success(datosEstadisticas))
        seVe("Mes (Nov 2026)")
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("+$1.990.000")
        compose.onNodeWithTag(Etiquetas.MONTO_INGRESOS_ESTADISTICAS).assertTextEquals("$2.500.000")
        compose.onNodeWithTag(Etiquetas.MONTO_GASTOS_ESTADISTICAS).assertTextEquals("$510.000")
        seVe("+42.1%")
        seVe("Salud financiera excelente este periodo")
        seVe("+25.0% vs Oct")
        seVe("20% de ingreso")
        seVe("Evolución Semanal")
        seVe("Ingresos vs. Gastos (Noviembre)")
        compose.onNodeWithTag(Etiquetas.PASTILLA_PROMEDIO, useUnmergedTree = true).performScrollTo().assertTextEquals("Prom. $170.000/día")
        seVe("Tu mayor concentración de gasto está en Comida (82%).")
        seVe("Redujiste un 16% tu gasto en Comida comparado con Octubre.")
        seVe("Podrías destinar parte a tu meta \"Viaje familiar\": te faltan $2.000.000 COP para completarla.")
        compose.onNodeWithTag(Etiquetas.BOTON_APLICAR_SUGERENCIA).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cambiarDePeriodoRecalculaTodaLaPantalla() {
        mostrar(Result.success(datosEstadisticas))
        // Semana del 2 al 8 de noviembre: el ingreso del domingo 1 queda en la semana anterior.
        periodo(TipoPeriodo.SEMANA)
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("-$510.000")
        compose.onNodeWithTag(Etiquetas.MONTO_INGRESOS_ESTADISTICAS).assertTextEquals("$0")
        seVe("-120.4%")
        seVe("-100.0% vs sem. anterior")
        seVe("Evolución Diaria")
        seVe("Ingresos vs. Gastos (Esta semana)")
        noAparece("de ingreso")
        noAparece("Salud financiera")
        compose.onAllNodes(hasText("Recomendación sugerida", substring = true)).assertCountEquals(0)

        periodo(TipoPeriodo.ANIO)
        compose.onNodeWithTag(Etiquetas.MONTO_INGRESOS_ESTADISTICAS).assertTextEquals("$4.500.000")
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("+$3.390.000")
        seVe("Evolución Mensual")
        seVe("Ingresos vs. Gastos (2026)")
        // 2025 no tiene movimientos: no hay comparaciones.
        compose.onAllNodes(hasText("vs 2025", substring = true)).assertCountEquals(0)
        compose.onAllNodes(hasTestTag(Etiquetas.PASTILLA_VARIACION_NETO)).assertCountEquals(0)

        // Rango (lo que se confirma en el selector de fechas).
        compose.runOnIdle { viewModel.elegirRango(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)) }
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("+$1.400.000")
        seVe("Ingresos vs. Gastos (01 Oct – 31 Oct)")
        assertEquals(TipoPeriodo.RANGO, viewModel.tipoPeriodo)

        periodo(TipoPeriodo.MES)
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("+$1.990.000")
    }

    @Test
    fun usuarioSinMovimientosVeLosEstadosVacios() {
        mostrar(Result.success(datosSinMovimientos))
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("$0")
        compose.onNodeWithTag(Etiquetas.MONTO_INGRESOS_ESTADISTICAS).assertTextEquals("$0")
        compose.onNodeWithTag(Etiquetas.MONTO_GASTOS_ESTADISTICAS).assertTextEquals("$0")
        seVe("Aún no tienes movimientos en este periodo")
        seVe("Aún no hay suficiente información para generar tu diagnóstico.")
        compose.onAllNodes(hasText("Prom.", substring = true)).assertCountEquals(0)
        compose.onAllNodes(hasText("Recomendación sugerida", substring = true)).assertCountEquals(0)
        noAparece("Salud financiera")
        noAparece("de ingreso")
    }

    @Test
    fun noApareceNingunDatoDeEjemplo() {
        mostrar(Result.success(datosEstadisticas))
        DATOS_DE_EJEMPLO_ESTADISTICAS.forEach(::noAparece)
        TipoPeriodo.entries.filter { it != TipoPeriodo.RANGO }.forEach {
            periodo(it)
            DATOS_DE_EJEMPLO_ESTADISTICAS.forEach(::noAparece)
        }
    }

    @Test
    fun noApareceNingunDatoDeEjemploSinMovimientos() {
        mostrar(Result.success(datosSinMovimientos))
        DATOS_DE_EJEMPLO_ESTADISTICAS.forEach(::noAparece)
    }

    @Test
    fun tocarUnGrupoDeLaGraficaLoSeleccionaSinCambiarLasCifras() {
        mostrar(Result.success(datosEstadisticas))
        // Hoy es 3 de noviembre: la Sem 1 queda seleccionada por defecto.
        compose.onNodeWithTag(Etiquetas.ETIQUETA_GRUPO_SELECCIONADO, useUnmergedTree = true).assertTextEquals("Sem 1")
        compose.onNodeWithTag(Etiquetas.GRUPO_GRAFICA + 2).performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.ETIQUETA_GRUPO_SELECCIONADO, useUnmergedTree = true).assertTextEquals("Sem 3")
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals("+$1.990.000")
        // Sin asterisco ni nota de proyección.
        noAparece("*")
        // Cambiar de periodo vuelve al grupo por defecto.
        periodo(TipoPeriodo.SEMANA)
        compose.onNodeWithTag(Etiquetas.ETIQUETA_GRUPO_SELECCIONADO, useUnmergedTree = true).assertTextEquals("Mar")
    }

    @Test
    fun siFallaLaLecturaMuestraElErrorEnRojoConReintentar() {
        val repositorio = mostrar(Result.failure(SinConexionException()))
        compose.onNodeWithTag(Etiquetas.BOTON_REINTENTAR).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Error", useUnmergedTree = true).assertExists()
        val antes = repositorio.cargas
        compose.onNodeWithTag(Etiquetas.BOTON_REINTENTAR).performClick()
        compose.waitForIdle()
        assertEquals(antes + 1, repositorio.cargas)
    }
}

/** Pestañas Inicio ↔ Gráficos dentro de la app completa (spec 11). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class NavegacionEstadisticasTest {

    @get:Rule
    val compose = createComposeRule()

    private fun abrirDashboard() {
        lateinit var navegacion: NavHostController
        compose.setContent {
            navegacion = rememberNavController()
            FinanziaApp(
                auth = FakeAuthRepository(),
                onboarding = FakeOnboardingRepository(),
                dashboard = DashboardFijo(Result.success(datosEstadisticas)),
                navController = navegacion,
            )
        }
        compose.waitForIdle()
        compose.runOnIdle { navegacion.navigate(Rutas.DASHBOARD) }
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
    }

    private fun pestana(texto: String) {
        compose.onNodeWithText(texto).performClick()
        compose.waitForIdle()
    }

    @Test
    fun graficosAbreEstadisticasEInicioVuelveAlDashboard() {
        abrirDashboard()
        pestana("Gráficos")
        compose.onNodeWithTag(Etiquetas.PANTALLA_ESTADISTICAS).assertIsDisplayed()
        pestana("Inicio")
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
        // Otra vuelta, para comprobar que se puede ir y volver varias veces.
        pestana("Gráficos")
        compose.onNodeWithTag(Etiquetas.PANTALLA_ESTADISTICAS).assertIsDisplayed()
    }

    @Test
    fun desdeEstadisticasMetasAbreMetasYGraficosVuelve() {
        abrirDashboard()
        pestana("Gráficos")
        pestana("Metas")
        compose.onNodeWithTag(Etiquetas.PANTALLA_METAS).assertIsDisplayed()
        pestana("Gráficos")
        compose.onNodeWithTag(Etiquetas.PANTALLA_ESTADISTICAS).assertIsDisplayed()
        pestana("Inicio")
        compose.onNodeWithTag(Etiquetas.PANTALLA_DASHBOARD).assertIsDisplayed()
    }

    @Test
    fun billeteraYAsistenteNoHacenNada() {
        abrirDashboard()
        pestana("Gráficos")
        pestana("Billetera")
        pestana("Asistente")
        compose.onNodeWithTag(Etiquetas.PANTALLA_ESTADISTICAS).assertIsDisplayed()
    }
}

/** Pantalla pequeña: se llega hasta el final (spec 11 · Adaptable). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h568dp")
class EstadisticasPantallaPequenaTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun seLlegaHastaElBotonDelFinalYLaBarraQuedaVisible() {
        val viewModel = EstadisticasViewModel(DashboardFijo(Result.success(datosEstadisticas)), FakeAuthRepository(), ahora = { AHORA }, zona = ZONA)
        compose.setContent { FinanziaTheme { EstadisticasScreen(viewModel, onSesionCerrada = {}) } }
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.OPCION_PERIODO + TipoPeriodo.RANGO.name).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(Etiquetas.GRUPO_GRAFICA + 3).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(Etiquetas.BOTON_APLICAR_SUGERENCIA).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(Etiquetas.BARRA_INFERIOR).assertIsDisplayed()
    }
}

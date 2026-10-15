package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.Aporte
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasSimulado
import com.finanzia.finanzia.datos.SinConexionException
import com.finanzia.finanzia.ui.componentes.PestanaApp
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.metas.EtiquetasMetas
import com.finanzia.finanzia.ui.metas.MetasScreen
import com.finanzia.finanzia.ui.metas.MetasViewModel
import com.finanzia.finanzia.ui.metas.NuevaMetaScreen
import com.finanzia.finanzia.ui.metas.NuevaMetaViewModel
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.validacion.ValidacionNuevaMeta
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

/** Ejemplos de metas_1.png y nueva_meta_1.png: nunca deben aparecer (specs 07 y 08). */
private val EJEMPLOS_METAS = listOf(
    "San Andrés", "14,850,000", "22,000,000", "2,800,000", "4,000,000", "200,000 COP quincenal",
    "Vas con paso firme", "Ordenar", "Ajustar regla", "Fondo de Emergencia", "Paso 1 de 2", "Optimizado COP", "Colombia",
)

private val ZONA_METAS = ZoneId.of("America/Bogota")
private val AHORA_METAS = Instant.parse("2026-10-14T17:00:00Z")

@RunWith(AndroidJUnit4::class)
// LEGACY: el modo gráfico NATIVE de Robolectric no carga en algunos computadores con Windows.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(qualifiers = "w411dp-h891dp")
class MetasTest {

    @get:Rule
    val compose = createComposeRule()

    private fun seVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().performScrollTo().assertExists()
    }

    private fun noAparece(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true), useUnmergedTree = true).assertCountEquals(0)
    }

    private fun mostrarMetas(repositorio: MetasSimulado, onNuevaMeta: () -> Unit = {}, onPestana: (PestanaApp) -> Unit = {}) {
        compose.setContent {
            FinanziaTheme {
                MetasScreen(
                    MetasViewModel(repositorio, ahora = { AHORA_METAS }, zona = ZONA_METAS),
                    onAtras = {},
                    onNuevaMeta = onNuevaMeta,
                    onPestana = onPestana,
                )
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun sinMetasMuestraElEstadoVacioYNingunDatoDeEjemplo() {
        mostrarMetas(MetasSimulado())
        seVe("Metas Activas (0)")
        seVe("Aún no tienes metas")
        seVe("Aún no hay suficiente información")
        noAparece("completado")
        EJEMPLOS_METAS.forEach(::noAparece)
    }

    @Test
    fun cadaMetaMuestraSusDatosRealesYLoQueNoTieneDatosLoDice() {
        val meta = Meta(
            id = 1,
            nombre = "Computador nuevo",
            montoObjetivo = 3_000_000.0,
            fechaLimite = LocalDate.of(2027, 6, 30),
            fechaCreacion = AHORA_METAS,
            aportes = listOf(Aporte(1_000_000.0, AHORA_METAS), Aporte(500_000.0, AHORA_METAS)),
        )
        mostrarMetas(MetasSimulado(listOf(meta)))
        seVe("Metas Activas (1)")
        seVe("Computador nuevo")
        seVe("Junio 2027")
        seVe("$1,500,000")
        seVe("$3,000,000 COP")
        seVe("Faltan $1,500,000 COP")
        seVe("50% completado")
        seVe("Sin categoría")
        seVe("Próximo aporte: aún no hay datos")
        seVe("Aportar")
        EJEMPLOS_METAS.forEach(::noAparece)
    }

    @Test
    fun siFallaLaLecturaMuestraElErrorEnRojoYReintentar() {
        mostrarMetas(MetasSimulado().apply { error = SinConexionException() })
        seVe("No hay conexión con el servidor")
        seVe("Reintentar")
    }

    @Test
    fun nuevaMetaAbreDesdeElBotonYLaBarraNoTieneGraficos() {
        var abrio = false
        var pestana: PestanaApp? = null
        mostrarMetas(MetasSimulado(), onNuevaMeta = { abrio = true }, onPestana = { pestana = it })
        compose.onNodeWithTag(EtiquetasMetas.BOTON_NUEVA_META).performClick()
        assertTrue(abrio)
        noAparece("Gráficos")
        compose.onNodeWithText("Inicio").performClick()
        assertEquals(PestanaApp.INICIO, pestana)
    }

    @Test
    fun elDashboardYaNoTieneGraficosYMetasAbreSuPantalla() {
        var pestana: PestanaApp? = null
        compose.setContent {
            FinanziaTheme {
                DashboardScreen(
                    DashboardViewModel(
                        DashboardFijo(Result.success(DatosDashboard("Mateo Marin", listOf(CuentaDashboard("Principal", 0.0)), emptyList(), null))),
                        FakeAuthRepository(),
                    ),
                    onSesionCerrada = {},
                    onPestana = { pestana = it },
                )
            }
        }
        noAparece("Gráficos")
        compose.onNodeWithText("Metas").performClick()
        assertEquals(PestanaApp.METAS, pestana)
    }

    private fun mostrarNuevaMeta(repositorio: MetasSimulado, onGuardada: () -> Unit = {}): NuevaMetaViewModel {
        val viewModel = NuevaMetaViewModel(repositorio, reloj = { LocalDate.of(2026, 10, 14) })
        compose.setContent {
            FinanziaTheme { NuevaMetaScreen(viewModel, onAtras = {}, onGuardada = onGuardada) }
        }
        compose.waitForIdle()
        return viewModel
    }

    @Test
    fun nuevaMetaEsUnSoloPasoConLosTextosAprobados() {
        mostrarNuevaMeta(MetasSimulado())
        seVe("Asistido por Finanzia IA")
        seVe("Define tu objetivo financiero")
        seVe("Establece tu meta y Finanzia calculará el ritmo de ahorro perfecto según tus ingresos reales.")
        listOf("+$20,000", "+$50,000", "+$100,000", "+$150,000", "+$200,000").forEach(::seVe)
        seVe("Guardar meta")
        noAparece("copiloto")
        EJEMPLOS_METAS.forEach(::noAparece)
    }

    @Test
    fun conCamposVaciosNoGuardaYCadaErrorSeVeEnSuCampo() {
        val repositorio = MetasSimulado()
        var guardada = false
        mostrarNuevaMeta(repositorio) { guardada = true }
        compose.onNodeWithTag(EtiquetasMetas.BOTON_GUARDAR_META).performScrollTo().performClick()
        compose.waitForIdle()
        seVe(ValidacionNuevaMeta.ERROR_NOMBRE)
        seVe(ValidacionNuevaMeta.ERROR_CATEGORIA)
        seVe(ValidacionNuevaMeta.ERROR_MONTO)
        seVe(ValidacionNuevaMeta.ERROR_FECHA)
        assertFalse(guardada)
        assertNull(repositorio.ultimaCreada)
    }

    @Test
    fun losMontosRapidosSeSumanAlMontoEscrito() {
        val viewModel = mostrarNuevaMeta(MetasSimulado())
        compose.onNodeWithTag(EtiquetasMetas.CAMPO_MONTO_META).performScrollTo().performTextInput("100000")
        compose.onNodeWithText("+$50,000").performScrollTo().performClick()
        compose.onNodeWithText("+$20,000").performScrollTo().performClick()
        compose.waitForIdle()
        assertEquals("170000", viewModel.monto)
    }

    @Test
    fun conTodoBienGuardaLaMetaYVuelve() {
        val repositorio = MetasSimulado()
        var guardada = false
        val viewModel = mostrarNuevaMeta(repositorio) { guardada = true }
        compose.onNodeWithTag(EtiquetasMetas.CAMPO_NOMBRE_META).performTextInput("  Computador nuevo ")
        compose.onNodeWithText("Tecnología").performScrollTo().performClick()
        compose.onNodeWithTag(EtiquetasMetas.CAMPO_MONTO_META).performScrollTo().performTextInput("3000000")
        // Al tocar la fecha se abre la lista de meses, que empieza en el mes siguiente.
        compose.onNodeWithTag(EtiquetasMetas.CAMPO_FECHA_META).performScrollTo().performClick()
        compose.onNodeWithText("Noviembre 2026").assertExists().performClick()
        seVe("1 mes restante • 2 quincenas")
        compose.runOnIdle { viewModel.elegirMes(YearMonth.of(2027, 6)) }
        seVe("8 meses restantes • 16 quincenas")
        compose.onNodeWithText(FrecuenciaAporte.MENSUAL.nombreVisible).performScrollTo().performClick()
        compose.onNodeWithTag(EtiquetasMetas.BOTON_GUARDAR_META).performScrollTo().performClick()
        compose.waitForIdle()

        assertTrue(guardada)
        val creada = repositorio.ultimaCreada!!
        assertEquals("Computador nuevo", creada.nombre)
        assertEquals(3_000_000L, creada.montoObjetivo)
        assertEquals(LocalDate.of(2027, 6, 30), creada.fechaLimite)
        assertEquals(CategoriaMeta.TECNOLOGIA, creada.categoria)
        assertEquals(FrecuenciaAporte.MENSUAL, creada.frecuencia)
        assertFalse(viewModel.guardando)
    }
}

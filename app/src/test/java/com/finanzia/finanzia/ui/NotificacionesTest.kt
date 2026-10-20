package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.notificaciones.EtiquetasNotificaciones
import com.finanzia.finanzia.ui.notificaciones.NotificacionesScreen
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Ejemplos de notificaciones_1.png: nunca deben aparecer (spec 05). */
private val EJEMPLOS_NOTIFICACIONES = listOf(
    "Restaurantes", "85%", "Bancolombia", "180.000", "210.000", "450.000", "Vence mañana", "Hace 20 min", "16 días",
)

@RunWith(AndroidJUnit4::class)
// LEGACY: el modo gráfico NATIVE de Robolectric no carga en algunos computadores con Windows.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(qualifiers = "w411dp-h891dp")
class NotificacionesTest {

    @get:Rule
    val compose = createComposeRule()

    private fun seVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().performScrollTo().assertExists()
    }

    private fun noAparece(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun laCampanaDelDashboardAbreNotificaciones() {
        var abrio = false
        compose.setContent {
            FinanziaTheme {
                DashboardScreen(
                    DashboardViewModel(
                        DashboardFijo(Result.success(DatosDashboard("Mateo Marin", listOf(CuentaDashboard("Principal", 0.0)), emptyList(), null))),
                        FakeAuthRepository(),
                    ),
                    onSesionCerrada = {},
                    onNotificaciones = { abrio = true },
                )
            }
        }
        compose.onNodeWithContentDescription("Notificaciones").performClick()
        assertTrue(abrio)
    }

    @Test
    fun sinDatosCadaAvisoDiceQueNoHayDatosYNoHayEjemplos() {
        compose.setContent { FinanziaTheme { NotificacionesScreen(onAtras = {}, onPestana = {}) } }
        seVe("Notificaciones")
        seVe("0 alertas activas sin revisar")
        seVe("Todas (0)")
        seVe("HOY")
        compose.onAllNodesWithTag(EtiquetasNotificaciones.TARJETA_AVISO).assertCountEquals(3)
        compose.onAllNodes(hasText("Aún no hay datos")).assertCountEquals(3)
        EJEMPLOS_NOTIFICACIONES.forEach(::noAparece)
        noAparece("Gráficos")
    }

    @Test
    fun unFiltroMuestraSoloEseAviso() {
        compose.setContent { FinanziaTheme { NotificacionesScreen(onAtras = {}, onPestana = {}) } }
        compose.onNodeWithText("Alertas de presupuesto").performScrollTo().performClick()
        compose.onAllNodesWithTag(EtiquetasNotificaciones.TARJETA_AVISO).assertCountEquals(1)
        seVe("Límite de presupuesto")
    }
}

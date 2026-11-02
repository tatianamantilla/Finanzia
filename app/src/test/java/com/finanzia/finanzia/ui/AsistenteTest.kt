package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
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
import com.finanzia.finanzia.datos.AsistenteNoDisponibleException
import com.finanzia.finanzia.datos.AsistenteSimulado
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.MensajeChat
import com.finanzia.finanzia.datos.RolMensaje
import com.finanzia.finanzia.ui.asistente.AsistenteScreen
import com.finanzia.finanzia.ui.asistente.AsistenteViewModel
import com.finanzia.finanzia.ui.asistente.EtiquetasAsistente
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.SQLiteMode

/** Pruebas del asistente Finanzia IA (spec 11). */
@RunWith(AndroidJUnit4::class)
// LEGACY: los modos NATIVE de Robolectric (gráficos y SQLite) no cargan en algunos computadores con Windows.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@SQLiteMode(SQLiteMode.Mode.LEGACY)
@Config(qualifiers = "w411dp-h891dp")
class AsistenteTest {

    @get:Rule
    val compose = createComposeRule()

    private fun seVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().assertExists()
    }

    private fun mostrar(repositorio: AsistenteSimulado): AsistenteViewModel {
        val viewModel = AsistenteViewModel(repositorio, "Mateo Marin")
        compose.setContent {
            FinanziaTheme { AsistenteScreen(viewModel, onAtras = {}, onInicio = {}, onMetas = {}) }
        }
        compose.waitForIdle()
        return viewModel
    }

    @Test
    fun sinMensajesSaludaConElNombreRealYMuestraLasSugerencias() {
        mostrar(AsistenteSimulado())
        seVe("Finanzia IA")
        seVe("Responde con tus datos reales")
        seVe("No es asesoría financiera profesional")
        seVe("¡Hola, Mateo! Pregúntame sobre tus gastos, ingresos o metas.")
        AsistenteViewModel.SUGERENCIAS.forEach(::seVe)
        compose.onAllNodes(hasText("copiloto", substring = true, ignoreCase = true)).assertCountEquals(0)
    }

    @Test
    fun conElCampoVacioNoSePuedeEnviar() {
        mostrar(AsistenteSimulado())
        compose.onNodeWithTag(EtiquetasAsistente.BOTON_ENVIAR).assertIsNotEnabled()
    }

    @Test
    fun enviarMuestraLaPreguntaYLaRespuesta() {
        val repositorio = AsistenteSimulado(responder = { Result.success("Este mes gastaste más en Comida.") })
        val viewModel = mostrar(repositorio)
        compose.onNodeWithTag(EtiquetasAsistente.CAMPO_PREGUNTA).performTextInput("  ¿En qué gasto más?  ")
        compose.onNodeWithTag(EtiquetasAsistente.BOTON_ENVIAR).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(EtiquetasAsistente.MENSAJE_USUARIO).assertTextEquals("¿En qué gasto más?")
        compose.onNodeWithTag(EtiquetasAsistente.MENSAJE_ASISTENTE, useUnmergedTree = true).assertExists()
        seVe("Este mes gastaste más en Comida.")
        // El campo queda vacío (en pantalla solo se ve el texto de ayuda).
        assertEquals("", viewModel.texto)
        assertEquals(2, repositorio.mensajes.size)
    }

    @Test
    fun unaSugerenciaSeEnviaComoPregunta() {
        val repositorio = AsistenteSimulado(responder = { Result.success("Vas bien.") })
        mostrar(repositorio)
        compose.onNodeWithText("¿Cómo voy con mis metas?").performClick()
        compose.waitForIdle()
        assertEquals(MensajeChat(RolMensaje.USUARIO, "¿Cómo voy con mis metas?"), repositorio.mensajes.first())
        seVe("Vas bien.")
    }

    @Test
    fun siNoEstaDisponibleElErrorSeVeEnRojoYLaPreguntaNoSePierde() {
        val repositorio = AsistenteSimulado(responder = { Result.failure(AsistenteNoDisponibleException()) })
        mostrar(repositorio)
        compose.onNodeWithTag(EtiquetasAsistente.CAMPO_PREGUNTA).performTextInput("¿Cuánto ahorré?")
        compose.onNodeWithTag(EtiquetasAsistente.BOTON_ENVIAR).performClick()
        compose.waitForIdle()
        seVe("Finanzia IA todavía no está disponible. Inténtalo más tarde.")
        seVe("Reintentar")
        compose.onNodeWithTag(EtiquetasAsistente.CAMPO_PREGUNTA).assertTextEquals("¿Cuánto ahorré?")
        compose.onAllNodesWithTag(EtiquetasAsistente.MENSAJE_USUARIO).assertCountEquals(0)
    }

    @Test
    fun alAbrirSeVeElHistorialGuardado() {
        mostrar(
            AsistenteSimulado(
                listOf(
                    MensajeChat(RolMensaje.USUARIO, "Primera pregunta guardada"),
                    MensajeChat(RolMensaje.ASISTENTE, "Primera respuesta guardada"),
                ),
            ),
        )
        seVe("Primera pregunta guardada")
        seVe("Primera respuesta guardada")
        compose.onAllNodes(hasText("Puedes empezar con:")).assertCountEquals(0)
    }

    @Test
    fun elBotonConversarDelDashboardAbreElAsistente() {
        var abrio = false
        compose.setContent {
            FinanziaTheme {
                DashboardScreen(
                    DashboardViewModel(
                        DashboardFijo(Result.success(DatosDashboard("Mateo Marin", listOf(CuentaDashboard("Principal", java.math.BigDecimal.ZERO)), emptyList(), null))),
                        FakeAuthRepository(),
                    ),
                    onAsistente = { abrio = true },
                    onSesionCerrada = {},
                )
            }
        }
        compose.onNodeWithText("Conversar con Finanzia IA").performScrollTo().performClick()
        assertTrue(abrio)
    }
}

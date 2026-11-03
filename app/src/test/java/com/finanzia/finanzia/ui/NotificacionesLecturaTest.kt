package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.ClaseNotificacion
import com.finanzia.finanzia.datos.Notificacion
import com.finanzia.finanzia.datos.NotificacionesFijas
import com.finanzia.finanzia.ui.notificaciones.NotificacionesScreen
import com.finanzia.finanzia.ui.notificaciones.NotificacionesViewModel
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.ZoneId

/** Notificaciones: marcar como leída (tarjeta y doble check) y filtrar por visto (botón de ajustes). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class NotificacionesLecturaTest {

    @get:Rule
    val compose = createComposeRule()

    private val ahora = Instant.parse("2026-10-13T20:00:00Z")

    private fun notificacion(id: Long, leida: Boolean) = Notificacion(
        id = id,
        clase = ClaseNotificacion.GASTO_MAYOR,
        titulo = "Aviso $id",
        mensaje = "Mensaje $id",
        // La 1 es la más reciente y queda de primera en la lista.
        fecha = ahora.minusSeconds(id * 60),
        leida = leida,
    )

    private fun mostrar(
        lista: List<Notificacion>,
        resultadoMarcar: Result<Unit> = Result.success(Unit),
    ): NotificacionesFijas {
        val repositorio = NotificacionesFijas(Result.success(lista), resultadoMarcar)
        val viewModel = NotificacionesViewModel(repositorio)
        compose.setContent {
            FinanziaTheme {
                NotificacionesScreen(viewModel, onAtras = {}, ahora = { ahora }, zona = ZoneId.of("America/Bogota"))
            }
        }
        return repositorio
    }

    private fun contador() = compose.onNodeWithTag(Etiquetas.CONTADOR_NOTIFICACIONES)

    @Test
    fun alTocarUnaTarjetaQuedaLeida() {
        val repositorio = mostrar(listOf(notificacion(1, leida = false), notificacion(2, leida = false)))
        contador().assertTextEquals("2 alertas activas sin revisar")

        compose.onAllNodesWithTag(Etiquetas.TARJETA_NOTIFICACION)[0].performClick()

        contador().assertTextEquals("1 alerta activa sin revisar")
        assertEquals(listOf(1L), repositorio.marcadas)
    }

    @Test
    fun elDobleCheckMarcaTodasComoLeidas() {
        val repositorio = mostrar(
            listOf(notificacion(1, leida = false), notificacion(2, leida = true), notificacion(3, leida = false)),
        )

        compose.onNodeWithTag(Etiquetas.BOTON_MARCAR_TODAS_LEIDAS).performClick()

        contador().assertTextEquals("0 alertas activas sin revisar")
        assertEquals(setOf(1L, 3L), repositorio.marcadas.toSet())
    }

    @Test
    fun elBotonDeAjustesFiltraPorVisto() {
        mostrar(listOf(notificacion(1, leida = false), notificacion(2, leida = true)))
        compose.onAllNodesWithTag(Etiquetas.TARJETA_NOTIFICACION).assertCountEquals(2)

        compose.onNodeWithTag(Etiquetas.BOTON_FILTRO_LECTURA).performClick()
        compose.onNodeWithText("Sin leer").performClick()
        compose.onAllNodesWithTag(Etiquetas.TARJETA_NOTIFICACION).assertCountEquals(1)
        compose.onNodeWithText("Aviso 1").assertIsDisplayed()

        compose.onNodeWithTag(Etiquetas.BOTON_FILTRO_LECTURA).performClick()
        compose.onNodeWithText("Leídas").performClick()
        compose.onAllNodesWithTag(Etiquetas.TARJETA_NOTIFICACION).assertCountEquals(1)
        compose.onNodeWithText("Aviso 2").assertIsDisplayed()
    }

    @Test
    fun sinNotificacionesSinLeerMuestraElTextoVacio() {
        mostrar(listOf(notificacion(1, leida = true)))

        compose.onNodeWithTag(Etiquetas.BOTON_FILTRO_LECTURA).performClick()
        compose.onNodeWithText("Sin leer").performClick()

        compose.onNodeWithTag(Etiquetas.ESTADO_VACIO_NOTIFICACIONES)
            .assertTextEquals("No tienes notificaciones sin leer")
    }

    @Test
    fun siNoSePuedeGuardarVuelveASinLeerYMuestraElError() {
        mostrar(
            listOf(notificacion(1, leida = false)),
            resultadoMarcar = Result.failure(Exception("No se pudo marcar como leída. Inténtalo de nuevo.")),
        )

        compose.onAllNodesWithTag(Etiquetas.TARJETA_NOTIFICACION)[0].performClick()

        contador().assertTextEquals("1 alerta activa sin revisar")
        compose.onNodeWithTag(Etiquetas.ERROR_NOTIFICACIONES).assertIsDisplayed()
    }
}

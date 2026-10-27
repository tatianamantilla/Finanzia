package com.finanzia.finanzia.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.AporteMeta
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.Meta
import com.finanzia.finanzia.datos.MetasSimuladas
import com.finanzia.finanzia.ui.metas.DetalleMetaScreen
import com.finanzia.finanzia.ui.metas.DetalleMetaViewModel
import com.finanzia.finanzia.ui.metas.ERROR_MONTO_APORTE
import com.finanzia.finanzia.ui.metas.EtiquetasAportes
import com.finanzia.finanzia.ui.metas.MetasScreen
import com.finanzia.finanzia.ui.metas.MetasViewModel
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.SQLiteMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val HOY_APORTES = LocalDate.of(2026, 10, 26)
private val AHORA_APORTES = Instant.parse("2026-10-26T17:00:00Z")
private val ZONA_APORTES = ZoneId.of("America/Bogota")

/** Pruebas de "+ Aportar" (spec 07), "Detalle de meta" y "Editar aportes" (spec 09). */
@RunWith(AndroidJUnit4::class)
// LEGACY: los modos NATIVE de Robolectric (gráficos y SQLite) no cargan en algunos computadores con Windows.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@SQLiteMode(SQLiteMode.Mode.LEGACY)
@Config(qualifiers = "w411dp-h891dp")
class AportesMetasTest {

    @get:Rule
    val compose = createComposeRule()

    private fun seVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).onFirst().performScrollTo().assertExists()
    }

    private fun noAparece(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true), useUnmergedTree = true).assertCountEquals(0)
    }

    private fun meta(aportes: List<AporteMeta> = emptyList()) = Meta(
        id = 1,
        nombre = "Computador nuevo",
        montoObjetivo = 3_000_000.0,
        fechaLimite = LocalDate.of(2027, 6, 30),
        fechaCreacion = AHORA_APORTES,
        categoria = CategoriaMeta.TECNOLOGIA,
        frecuencia = FrecuenciaAporte.MENSUAL,
        aportes = aportes,
    )

    @Test
    fun aportarSumaElMontoALaMeta() {
        val repositorio = MetasSimuladas(listOf(meta()), ahora = { AHORA_APORTES })
        compose.setContent {
            FinanziaTheme { MetasScreen(MetasViewModel(repositorio), onAtras = {}, onNuevaMeta = {}, hoy = { HOY_APORTES }) }
        }
        seVe("Faltan $3,000,000.00 COP")
        compose.onNodeWithTag(EtiquetasAportes.BOTON_APORTAR).performScrollTo().performClick()
        // Sin monto no se guarda y el error se ve en rojo dentro del recuadro.
        compose.onNodeWithTag(EtiquetasAportes.BOTON_GUARDAR).performScrollTo().performClick()
        seVe(ERROR_MONTO_APORTE)
        compose.onNodeWithTag(EtiquetasAportes.CAMPO_MONTO).performScrollTo().performTextInput("500000")
        compose.onNodeWithTag(EtiquetasAportes.BOTON_GUARDAR).performScrollTo().performClick()
        compose.waitForIdle()
        seVe("Faltan $2,500,000.00 COP")
        seVe("17%")
        noAparece("Guardar aporte")
    }

    @Test
    fun detalleDeMetaSeAbreDesdeLaTarjeta() {
        var abierta: Long? = null
        compose.setContent {
            FinanziaTheme {
                MetasScreen(
                    MetasViewModel(MetasSimuladas(listOf(meta()))),
                    onAtras = {},
                    onNuevaMeta = {},
                    hoy = { HOY_APORTES },
                    onDetalle = { abierta = it },
                )
            }
        }
        compose.onNodeWithTag(EtiquetasAportes.BOTON_DETALLE_META).performScrollTo().performClick()
        assertEquals(1L, abierta)
    }

    private fun mostrarDetalle(meta: Meta) {
        compose.setContent {
            FinanziaTheme { DetalleMetaScreen(DetalleMetaViewModel(MetasSimuladas(listOf(meta)), meta.id, ZONA_APORTES), onAtras = {}) }
        }
        compose.waitForIdle()
    }

    @Test
    fun detalleDeMetaMuestraLosDatosYLosAportesReales() {
        mostrarDetalle(
            meta(
                listOf(
                    AporteMeta(1_000_000.0, LocalDate.of(2026, 10, 5), 1),
                    AporteMeta(500_000.0, LocalDate.of(2026, 10, 12), 2),
                ),
            ).copy(fechaCreacion = Instant.parse("2026-10-01T17:00:00Z")),
        )
        seVe("Detalle de meta")
        seVe("Computador nuevo")
        seVe("Tecnología")
        seVe("$1,500,000.00")
        seVe("$3,000,000.00 COP")
        seVe("Faltan $1,500,000.00 COP")
        seVe("50%")
        seVe("Junio 2027")
        seVe("01 Oct 2026")
        seVe("Aportes (2)")
        compose.onAllNodesWithTag(EtiquetasAportes.FILA_APORTE).assertCountEquals(2)
        // Del más reciente al más antiguo.
        val reciente = compose.onNodeWithText("12 Oct 2026").fetchSemanticsNode().positionInRoot.y
        val antiguo = compose.onNodeWithText("05 Oct 2026").fetchSemanticsNode().positionInRoot.y
        assertTrue(reciente < antiguo)
        seVe("+$500,000.00")
    }

    @Test
    fun editarUnAporteCambiaLosTotales() {
        mostrarDetalle(meta(listOf(AporteMeta(1_000_000.0, LocalDate.of(2026, 10, 5), 1))))
        seVe("Faltan $2,000,000.00 COP")
        compose.onNodeWithTag(EtiquetasAportes.BOTON_EDITAR_APORTE).performScrollTo().performClick()
        seVe("Editar aporte del 05 Oct 2026")
        // El recuadro empieza con el monto actual; se borra y se escribe uno nuevo.
        compose.onNodeWithTag(EtiquetasAportes.CAMPO_MONTO).performScrollTo().performTextReplacement("")
        compose.onNodeWithTag(EtiquetasAportes.BOTON_GUARDAR).performScrollTo().performClick()
        seVe(ERROR_MONTO_APORTE)
        compose.onNodeWithTag(EtiquetasAportes.CAMPO_MONTO).performScrollTo().performTextInput("1500000")
        compose.onNodeWithTag(EtiquetasAportes.BOTON_GUARDAR).performScrollTo().performClick()
        compose.waitForIdle()
        seVe("Faltan $1,500,000.00 COP")
        seVe("+$1,500,000.00")
        noAparece("Guardar cambios")
    }

    @Test
    fun detalleSinAportesLoDice() {
        mostrarDetalle(meta().copy(fechaLimite = null))
        seVe("Aún no tienes aportes en esta meta.")
        seVe("Aún no hay datos")
    }
}

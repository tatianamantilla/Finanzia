package com.finanzia.finanzia.integracion.manual

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.CredencialesIncorrectasException
import com.finanzia.finanzia.datos.DashboardFijo
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.PrimerGasto
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.datos.TipoMovimiento
import com.finanzia.finanzia.datos.supabase.SupabaseAuthRepository
import com.finanzia.finanzia.datos.supabase.SupabaseDashboardRepository
import com.finanzia.finanzia.datos.supabase.SupabaseOnboardingRepository
import com.finanzia.finanzia.datos.supabase.crearClienteSupabase
import com.finanzia.finanzia.ui.DATOS_DE_EJEMPLO
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.dashboard.ResumidorDashboard
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.validacion.CalculosDashboard
import com.finanzia.finanzia.validacion.FormatosDashboard
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.Properties

/**
 * PRUEBA MANUAL contra Supabase real con el usuario de DEMOSTRACIÓN "Mateo Marin".
 *
 *   ./gradlew :app:testDebugUnitTest -Pdemo
 *
 * - Escribe datos reales y NO los borra (para verlos en la app y en Supabase).
 * - Es idempotente: si Mateo ya existe inicia sesión con él y no duplica usuario, cuenta,
 *   categorías ni movimientos.
 * - Los movimientos llevan "DEMO Mateo Marin" en la descripción.
 * - La contraseña se lee de local.properties (DEMO_PASSWORD); nunca se escribe en el código.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class DemoMateoMarinTest {

    @get:Rule
    val compose = createComposeRule()

    private companion object {
        const val NOMBRE = "Mateo Marin"
        /** Dominio reservado para pruebas (example.com): no es el correo de ninguna persona real. */
        const val CORREO = "mateo.marin@example.com"
        const val MONTO_INGRESO = 2_500_000L
        const val MONTO_GASTO = 350_000L
        const val DESCRIPCION_INGRESO = "Primer ingreso · DEMO Mateo Marin"
        const val DESCRIPCION_GASTO = "Primer gasto · DEMO Mateo Marin"
    }

    private fun contrasenaDemo(): String {
        val archivo = listOf(File("local.properties"), File("../local.properties")).firstOrNull { it.exists() }
            ?: error("No se encontró local.properties")
        val propiedades = Properties().apply { archivo.inputStream().use { load(it) } }
        return propiedades.getProperty("DEMO_PASSWORD")?.trim()?.takeIf { it.isNotEmpty() }
            ?: error("Falta DEMO_PASSWORD en local.properties")
    }

    @Serializable
    private data class FilaCategoria(val nombre: String, val tipo: String, @SerialName("id_color") val idColor: Long? = null)

    @Serializable
    private data class FilaFrecuencia(@SerialName("frecuencia_ingreso") val frecuenciaIngreso: String? = null)

    @Serializable
    private data class FilaColor(@SerialName("id_color") val idColor: Long)

    @Test
    fun onboardingCompletoCerrarSesionVolverAEntrarYVerElDashboard() {
        val contrasena = contrasenaDemo()
        val cliente = crearClienteSupabase(guardarSesionEnElTelefono = false)
        val auth = SupabaseAuthRepository(cliente)
        val onboarding = SupabaseOnboardingRepository(cliente)
        val dashboard = SupabaseDashboardRepository(cliente)

        val datos: DatosDashboard = runBlocking {
            // 1. Si Mateo ya existe, entra; si no, se registra (sin duplicar).
            val entrada = auth.iniciarSesion(CORREO, contrasena)
            if (entrada.isSuccess) {
                println("DEMO: Mateo Marin ya existía; se inició sesión con él.")
            } else {
                val error = entrada.exceptionOrNull()
                check(error is CredencialesIncorrectasException) { "No se pudo iniciar sesión: ${error?.message}" }
                auth.crearCuenta(NOMBRE, CORREO, contrasena).getOrElse {
                    error("No se pudo registrar a Mateo Marin: ${it.message}")
                }
                println("DEMO: Mateo Marin registrado.")
            }

            // 2. Fila en usuarios, cuenta Principal y categorías (con color de la tabla colores).
            val categorias = cliente.from("categorias").select().decodeList<FilaCategoria>()
            val colores = cliente.from("colores").select().decodeList<FilaColor>()
            println("DEMO: categorías = ${categorias.map { "${it.nombre} (${it.tipo}, color ${it.idColor})" }}")
            println("DEMO: filas en la tabla colores = ${colores.size}")
            listOf("Gastos Fijos", "Comida", "Diversión", "Transporte", "Ingresos").forEach { nombre ->
                assertEquals("La categoría $nombre debe existir una sola vez", 1, categorias.count { it.nombre == nombre })
            }
            if (colores.isNotEmpty()) {
                assertTrue("Todas las categorías deben tener color", categorias.all { it.idColor != null })
            }

            // 3. Primer ingreso (mensual) y primer gasto (Comida), solo si todavía no existen.
            val antes = dashboard.cargar().getOrThrow()
            if (antes.movimientos.none { it.descripcion == DESCRIPCION_INGRESO }) {
                onboarding.guardarPrimerIngreso(PrimerIngreso(MONTO_INGRESO, FrecuenciaIngreso.MENSUAL, DESCRIPCION_INGRESO)).getOrThrow()
                println("DEMO: ingreso guardado.")
            }
            if (antes.movimientos.none { it.descripcion == DESCRIPCION_GASTO }) {
                onboarding.guardarPrimerGasto(PrimerGasto(MONTO_GASTO, CategoriaGasto.COMIDA, DESCRIPCION_GASTO)).getOrThrow()
                println("DEMO: gasto guardado.")
            }
            val frecuencia = cliente.from("usuarios").select().decodeSingle<FilaFrecuencia>().frecuenciaIngreso
            assertEquals("mensual", frecuencia)

            // 4. Cerrar sesión y volver a entrar con las mismas credenciales.
            auth.cerrarSesion().getOrThrow()
            assertNull("La sesión debe quedar cerrada", cliente.auth.currentUserOrNull())
            auth.iniciarSesion(CORREO, contrasena).getOrThrow()
            dashboard.cargar().getOrThrow()
        }

        // 5. Lo que se lee después de volver a entrar.
        assertEquals(NOMBRE, datos.nombreUsuario)
        assertEquals("Principal", datos.cuentas.first().nombre)
        val ingresos = datos.movimientos.filter { it.descripcion == DESCRIPCION_INGRESO }
        val gastos = datos.movimientos.filter { it.descripcion == DESCRIPCION_GASTO }
        assertEquals("El ingreso de demostración no debe duplicarse", 1, ingresos.size)
        assertEquals("El gasto de demostración no debe duplicarse", 1, gastos.size)
        assertEquals(TipoMovimiento.INGRESO, ingresos.single().tipo)
        assertEquals(MONTO_INGRESO.toDouble(), ingresos.single().monto, 0.0)
        assertEquals("Ingresos", ingresos.single().nombreCategoria)
        assertEquals(TipoMovimiento.GASTO, gastos.single().tipo)
        assertEquals(MONTO_GASTO.toDouble(), gastos.single().monto, 0.0)
        assertEquals("Comida", gastos.single().nombreCategoria)
        val saldoInicial = datos.cuentas.sumOf { it.saldoInicial }
        val balanceEsperado = saldoInicial + datos.movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto } -
            datos.movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
        assertEquals(balanceEsperado, CalculosDashboard.balance(datos), 0.001)
        assertNull("Mateo no tiene metas", datos.meta)
        println("DEMO: balance = ${FormatosDashboard.monto(CalculosDashboard.balance(datos))}")

        // 6. El Dashboard muestra sus datos reales.
        val ahora = Instant.now()
        val zona = ZoneId.systemDefault()
        val resumen = ResumidorDashboard.resumir(datos, ahora, zona)
        compose.setContent {
            FinanziaTheme {
                DashboardScreen(
                    DashboardViewModel(DashboardFijo(Result.success(datos)), FakeAuthRepository(), ahora = { ahora }, zona = zona),
                    onSesionCerrada = {},
                )
            }
        }
        compose.waitForIdle()
        fun seVe(texto: String) = compose.onAllNodes(hasText(texto, substring = true)).onFirst().performScrollTo().assertNotNullNode()
        seVe("¡Hola, Mateo! 👋")
        compose.onNodeWithTag(Etiquetas.MONTO_BALANCE).assertTextEquals(FormatosDashboard.monto(CalculosDashboard.balance(datos)))
        seVe(FormatosDashboard.ingreso(resumen.ingresosMes))
        seVe(FormatosDashboard.gasto(resumen.gastosMes))
        seVe(DESCRIPCION_INGRESO)
        seVe(DESCRIPCION_GASTO)
        seVe("Comida")
        seVe("Aún no tienes metas")
        DATOS_DE_EJEMPLO.forEach {
            compose.onAllNodes(hasText(it, substring = true), useUnmergedTree = true).assertCountEquals(0)
        }
        assertNotNull(resumen.primerNombre)
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertNotNullNode() = assertExists()
}

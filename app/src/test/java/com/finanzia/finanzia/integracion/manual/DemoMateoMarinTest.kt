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
import com.finanzia.finanzia.datos.SaldoInsuficienteException
import com.finanzia.finanzia.datos.TipoMovimiento
import com.finanzia.finanzia.datos.nombreVisible
import com.finanzia.finanzia.datos.supabase.SupabaseAuthRepository
import com.finanzia.finanzia.datos.supabase.SupabaseDashboardRepository
import com.finanzia.finanzia.datos.supabase.SupabaseMovimientosRepository
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.util.Properties
import com.finanzia.finanzia.datos.supabase.DatosInicialesSupabase
import com.finanzia.finanzia.datos.supabase.Dinero
import com.finanzia.finanzia.ui.DATOS_DE_EJEMPLO_ESTADISTICAS
import com.finanzia.finanzia.ui.estadisticas.EstadisticasScreen
import com.finanzia.finanzia.ui.estadisticas.EstadisticasViewModel
import com.finanzia.finanzia.validacion.CalculosEstadisticas
import com.finanzia.finanzia.validacion.FormatosEstadisticas
import com.finanzia.finanzia.validacion.TipoPeriodo
import java.time.LocalDate
import java.time.OffsetDateTime

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
        /** Spec 10: la prueba alterna el gasto entre estos dos valores en cada ejecución. */
        const val MONTO_GASTO_EDITADO = 420_000L
        const val DESCRIPCION_INTENTO = "Intento sin saldo · DEMO Mateo Marin"
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
        val movimientos = SupabaseMovimientosRepository(cliente)
        var montoGastoEsperado = MONTO_GASTO

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
            listOf("Gastos Fijos", "Comida", "Diversión", "Transporte", "Ingresos", "Mensual", "Quincenal", "Semanal", "Diario").forEach { nombre ->
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

            // 3b. Spec 10 · un gasto mayor que el saldo disponible NO se guarda.
            val conGasto = dashboard.cargar().getOrThrow()
            val saldo = CalculosDashboard.balance(conGasto)
            assertTrue("El saldo de Mateo no debe estar en negativo antes de empezar", saldo.signum() >= 0)
            val masQueElSaldo = saldo.setScale(0, RoundingMode.FLOOR).longValueExact() + 1
            val intento = onboarding.guardarPrimerGasto(PrimerGasto(masQueElSaldo, CategoriaGasto.COMIDA, DESCRIPCION_INTENTO))
            assertTrue("Un gasto mayor que el saldo debe rechazarse", intento.exceptionOrNull() is SaldoInsuficienteException)
            assertTrue(
                "El gasto rechazado no debe quedar guardado",
                dashboard.cargar().getOrThrow().movimientos.none { it.descripcion == DESCRIPCION_INTENTO },
            )
            println("DEMO: gasto de ${FormatosDashboard.monto(BigDecimal.valueOf(masQueElSaldo))} rechazado (saldo ${FormatosDashboard.monto(saldo)}).")

            // 3c. Spec 10 · editar el gasto de demostración (UPDATE del mismo registro).
            val gasto = conGasto.movimientos.single { it.descripcion == DESCRIPCION_GASTO }
            // Editarlo a más que el saldo + lo que ya había descontado → rechazado y sin cambios.
            val imposible = (saldo + gasto.monto).setScale(0, RoundingMode.FLOOR).longValueExact() + 1
            assertTrue(
                "Editar el gasto por encima del saldo disponible debe rechazarse",
                movimientos.editar(gasto.id, imposible, "Comida").exceptionOrNull() is SaldoInsuficienteException,
            )
            val nuevoMonto = if (gasto.monto.compareTo(BigDecimal.valueOf(MONTO_GASTO)) == 0) MONTO_GASTO_EDITADO else MONTO_GASTO
            movimientos.editar(gasto.id, nuevoMonto, "Comida").getOrThrow()
            val despues = dashboard.cargar().getOrThrow()
            val gastoEditado = despues.movimientos.single { it.descripcion == DESCRIPCION_GASTO }
            assertEquals("Se edita el mismo registro", gasto.id, gastoEditado.id)
            assertEquals(0, BigDecimal.valueOf(nuevoMonto).compareTo(gastoEditado.monto))
            assertEquals("Editar no crea movimientos nuevos", conGasto.movimientos.size, despues.movimientos.size)
            val balanceEsperado = saldo + gasto.monto - BigDecimal.valueOf(nuevoMonto)
            assertEquals("El balance en Supabase refleja la edición", 0, balanceEsperado.compareTo(CalculosDashboard.balance(despues)))
            montoGastoEsperado = nuevoMonto

            // 3d. El ingreso de Mateo se creó antes de que la frecuencia fuera su categoría
            // (cambio del 2026-10-26): se corrige a "Mensual" con la edición, sin cambiar el monto.
            val ingreso = despues.movimientos.single { it.descripcion == DESCRIPCION_INGRESO }
            if (ingreso.nombreCategoria != FrecuenciaIngreso.MENSUAL.nombreVisible) {
                movimientos.editar(ingreso.id, ingreso.monto.longValueExact(), FrecuenciaIngreso.MENSUAL.nombreVisible).getOrThrow()
                println("DEMO: categoría del ingreso corregida de ${ingreso.nombreCategoria} a Mensual.")
            }
            println("DEMO: gasto editado de ${FormatosDashboard.monto(gasto.monto)} a ${FormatosDashboard.monto(BigDecimal.valueOf(nuevoMonto))}; balance ${FormatosDashboard.monto(CalculosDashboard.balance(despues))}.")

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
        assertEquals(0, java.math.BigDecimal.valueOf(MONTO_INGRESO).compareTo(ingresos.single().monto))
        // El ingreso queda en la categoría de su frecuencia (Mensual).
        assertEquals("Mensual", ingresos.single().nombreCategoria)
        assertEquals(TipoMovimiento.GASTO, gastos.single().tipo)
        assertEquals(0, java.math.BigDecimal.valueOf(montoGastoEsperado).compareTo(gastos.single().monto))
        assertEquals("Comida", gastos.single().nombreCategoria)
        // Saldo inicial + ingresos − gastos − aportes, todo con BigDecimal (spec 10).
        val balanceEsperado = CalculosDashboard.suma(datos.cuentas.map { it.saldoInicial }) +
            CalculosDashboard.total(datos.movimientos, TipoMovimiento.INGRESO) -
            CalculosDashboard.total(datos.movimientos, TipoMovimiento.GASTO) -
            CalculosDashboard.suma(datos.aportes.map { it.monto })
        assertEquals(0, balanceEsperado.compareTo(CalculosDashboard.balance(datos)))
        assertTrue("El saldo de Mateo nunca debe quedar negativo", CalculosDashboard.balance(datos).signum() >= 0)
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
        // "Movimientos recientes" muestra los 4 más recientes de Mateo (con los de Estadísticas ya no
        // son el primer ingreso y el primer gasto): cada uno aparece con su descripción real.
        assertEquals(4, resumen.recientes.size)
        resumen.recientes.forEach { seVe(it.titulo) }
        seVe("Aún no tienes metas")
        DATOS_DE_EJEMPLO.forEach {
            compose.onAllNodes(hasText(it, substring = true), useUnmergedTree = true).assertCountEquals(0)
        }
        assertNotNull(resumen.primerNombre)
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertNotNullNode() = assertExists()

    // --- Spec 11 · Estadísticas ---

    /** Un movimiento de demostración con su fecha (se guarda a mediodía en Bogotá = 17:00 UTC). */
    private data class MovimientoDemo(val tipo: String, val monto: Long, val fecha: String, val categoria: String, val descripcion: String)

    /**
     * Movimientos de demostración en distintas semanas de octubre y en la primera de noviembre de 2026,
     * con distintas categorías. Los montos no coinciden con los ejemplos del diseño.
     */
    private val movimientosEstadisticas = listOf(
        MovimientoDemo("ingreso", 3_000_000, "2026-10-01", "Mensual", "Sueldo octubre · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 180_000, "2026-10-03", "Comida", "Mercado semana 1 · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 95_000, "2026-10-09", "Transporte", "Bus y taxi semana 2 · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 850_000, "2026-10-15", "Gastos Fijos", "Arriendo y servicios · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 130_000, "2026-10-24", "Diversión", "Cine y salida semana 4 · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 210_000, "2026-10-28", "Comida", "Mercado semana 4 · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 60_000, "2026-11-02", "Transporte", "Transporte noviembre · DEMO Mateo Marin"),
        MovimientoDemo("gasto", 75_000, "2026-11-03", "Comida", "Almuerzos noviembre · DEMO Mateo Marin"),
    )

    @Serializable
    private data class NuevoMovimientoConFecha(
        @SerialName("id_usuario") val idUsuario: Long,
        @SerialName("id_cuenta") val idCuenta: Long,
        @SerialName("id_categoria") val idCategoria: Long,
        val tipo: String,
        val monto: Long,
        val descripcion: String,
        val fecha: String,
        val activo: Boolean = true,
    )

    /** Fila tal como está en la tabla movimientos, para comparar sin pasar por los cálculos de la app. */
    @Serializable
    private data class FilaMovimientoBase(
        val tipo: String,
        val monto: Dinero,
        val fecha: String,
        val activo: Boolean,
    )

    @Test
    fun estadisticasCoincidenConLosMovimientosDeLaBaseDeDatos() {
        val contrasena = contrasenaDemo()
        val cliente = crearClienteSupabase(guardarSesionEnElTelefono = false)
        val auth = SupabaseAuthRepository(cliente)
        val dashboard = SupabaseDashboardRepository(cliente)
        val zona = ZoneId.of("America/Bogota")

        val (datos, filas) = runBlocking {
            auth.iniciarSesion(CORREO, contrasena).getOrElse {
                error("No se pudo iniciar sesión con Mateo Marin (corre primero la otra prueba de este archivo): ${it.message}")
            }
            // 1. Agrega los movimientos de demostración que falten, sin duplicar. Primero el ingreso,
            //    para que la regla del saldo (spec 10) permita los gastos.
            val ids = DatosInicialesSupabase.asegurar(cliente)
            val existentes = dashboard.cargar().getOrThrow().movimientos.map { it.descripcion }.toSet()
            movimientosEstadisticas.sortedBy { if (it.tipo == "ingreso") 0 else 1 }.forEach { m ->
                if (m.descripcion in existentes) return@forEach
                cliente.from("movimientos").insert(
                    NuevoMovimientoConFecha(
                        idUsuario = ids.idUsuario,
                        idCuenta = ids.idCuentaPrincipal,
                        idCategoria = ids.categorias[m.categoria] ?: error("No existe la categoría ${m.categoria}"),
                        tipo = m.tipo,
                        monto = m.monto,
                        descripcion = m.descripcion,
                        fecha = "${m.fecha}T17:00:00Z",
                    ),
                )
                println("DEMO: agregado '${m.descripcion}' (${m.fecha}, ${m.categoria}).")
            }
            // 2. Lo que lee la app y, aparte, las filas tal como están en la tabla.
            val leidos = dashboard.cargar().getOrThrow()
            val crudas = cliente.from("movimientos").select { filter { eq("id_usuario", ids.idUsuario) } }
                .decodeList<FilaMovimientoBase>()
            leidos to crudas.filter { it.activo }
        }

        // 3. Sin duplicados.
        movimientosEstadisticas.forEach { m ->
            assertEquals("'${m.descripcion}' no debe duplicarse", 1, datos.movimientos.count { it.descripcion == m.descripcion })
        }

        // 4. Sumas directas de la tabla (sin usar CalculosEstadisticas) entre dos fechas.
        fun sumaBase(tipo: String, desde: LocalDate, hasta: LocalDate): BigDecimal =
            filas.filter { it.tipo == tipo }
                .filter {
                    val dia = OffsetDateTime.parse(it.fecha).atZoneSameInstant(zona).toLocalDate()
                    !dia.isBefore(desde) && !dia.isAfter(hasta)
                }
                .fold(BigDecimal.ZERO) { total, fila -> total + fila.monto }

        val hoy = Instant.now().atZone(zona).toLocalDate()
        val octubre = LocalDate.of(2026, 10, 1) to LocalDate.of(2026, 10, 31)
        val resumenOctubre = CalculosEstadisticas.resumir(datos, TipoPeriodo.RANGO, hoy, zona, octubre)
        val ingresosOct = sumaBase("ingreso", octubre.first, octubre.second)
        val gastosOct = sumaBase("gasto", octubre.first, octubre.second)
        assertEquals(0, ingresosOct.compareTo(resumenOctubre.ingresos))
        assertEquals(0, gastosOct.compareTo(resumenOctubre.gastos))
        assertEquals(0, (ingresosOct - gastosOct).compareTo(resumenOctubre.neto))
        // Cada semana de la gráfica coincide con la suma de la tabla en esas fechas.
        assertEquals(listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5"), resumenOctubre.grupos.map { it.etiqueta })
        resumenOctubre.grupos.forEach { grupo ->
            assertEquals("Ingresos de ${grupo.etiqueta}", 0, sumaBase("ingreso", grupo.inicio, grupo.fin).compareTo(grupo.ingresos))
            assertEquals("Gastos de ${grupo.etiqueta}", 0, sumaBase("gasto", grupo.inicio, grupo.fin).compareTo(grupo.gastos))
            println("DEMO: ${grupo.etiqueta} (${grupo.inicio} a ${grupo.fin}): ingresos ${grupo.ingresos.toPlainString()}, gastos ${grupo.gastos.toPlainString()}")
        }
        println("DEMO: octubre 2026 · ingresos ${ingresosOct.toPlainString()}, gastos ${gastosOct.toPlainString()}, neto ${(ingresosOct - gastosOct).toPlainString()}")
        println("DEMO: categoría con más gasto en octubre = ${resumenOctubre.mayorGasto}")

        // 5. El mes actual (el que se ve por defecto) también coincide con la tabla.
        val mes = CalculosEstadisticas.periodo(TipoPeriodo.MES, hoy)
        val resumenMes = CalculosEstadisticas.resumir(datos, TipoPeriodo.MES, hoy, zona)
        assertEquals(0, sumaBase("ingreso", mes.inicio, mes.fin).compareTo(resumenMes.ingresos))
        assertEquals(0, sumaBase("gasto", mes.inicio, mes.fin).compareTo(resumenMes.gastos))
        println("DEMO: mes actual (${mes.inicio} a ${mes.fin}) · ingresos ${resumenMes.ingresos.toPlainString()}, gastos ${resumenMes.gastos.toPlainString()}")

        // 6. La pantalla de Estadísticas muestra esas cifras, sin ningún dato de ejemplo.
        val ahora = Instant.now()
        val viewModel = EstadisticasViewModel(DashboardFijo(Result.success(datos)), FakeAuthRepository(), ahora = { ahora }, zona = zona)
        compose.setContent { FinanziaTheme { EstadisticasScreen(viewModel, onSesionCerrada = {}) } }
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.MONTO_INGRESOS_ESTADISTICAS).assertTextEquals(FormatosEstadisticas.monto(resumenMes.ingresos))
        compose.onNodeWithTag(Etiquetas.MONTO_GASTOS_ESTADISTICAS).assertTextEquals(FormatosEstadisticas.monto(resumenMes.gastos))
        compose.runOnIdle { viewModel.elegirRango(octubre.first, octubre.second) }
        compose.waitForIdle()
        compose.onNodeWithTag(Etiquetas.MONTO_FLUJO_NETO).assertTextEquals(FormatosEstadisticas.montoConSigno(ingresosOct - gastosOct))
        compose.onNodeWithTag(Etiquetas.MONTO_INGRESOS_ESTADISTICAS).assertTextEquals(FormatosEstadisticas.monto(ingresosOct))
        compose.onNodeWithTag(Etiquetas.MONTO_GASTOS_ESTADISTICAS).assertTextEquals(FormatosEstadisticas.monto(gastosOct))
        DATOS_DE_EJEMPLO_ESTADISTICAS.forEach {
            compose.onAllNodes(hasText(it, substring = true), useUnmergedTree = true).assertCountEquals(0)
        }
    }
}

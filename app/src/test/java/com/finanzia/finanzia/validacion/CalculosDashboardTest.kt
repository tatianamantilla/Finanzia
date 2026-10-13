package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalculosDashboardTest {

    private val zona = ZoneId.of("America/Bogota")
    private val hoy = LocalDate.of(2026, 10, 13)

    private fun movimiento(
        id: Long,
        tipo: TipoMovimiento,
        monto: Double,
        fecha: String = "2026-10-10T15:00:00Z",
        categoria: String? = null,
    ) = MovimientoDashboard(id, tipo, monto, "Movimiento $id", Instant.parse(fecha), "Principal", categoria, null)

    private fun datos(saldoInicial: Double = 0.0, vararg movimientos: MovimientoDashboard) =
        DatosDashboard("Usuario Prueba", listOf(CuentaDashboard("Principal", saldoInicial)), movimientos.toList(), null)

    @Test
    fun elBalanceEsSaldoInicialMasIngresosMenosGastos() {
        val d = datos(
            1000.0,
            movimiento(1, TipoMovimiento.INGRESO, 2500.0),
            movimiento(2, TipoMovimiento.GASTO, 350.5),
            movimiento(3, TipoMovimiento.GASTO, 49.5),
        )
        assertEquals(3100.0, CalculosDashboard.balance(d), 0.001)
    }

    @Test
    fun sinMovimientosElBalanceEsElSaldoInicial() {
        assertEquals(0.0, CalculosDashboard.balance(datos()), 0.0)
    }

    @Test
    fun ingresosYGastosSeSumanPorSeparado() {
        val movs = listOf(
            movimiento(1, TipoMovimiento.INGRESO, 100.0),
            movimiento(2, TipoMovimiento.INGRESO, 50.0),
            movimiento(3, TipoMovimiento.GASTO, 30.0),
        )
        assertEquals(150.0, CalculosDashboard.total(movs, TipoMovimiento.INGRESO), 0.0)
        assertEquals(30.0, CalculosDashboard.total(movs, TipoMovimiento.GASTO), 0.0)
    }

    @Test
    fun soloCuentanLosMovimientosDelMesActualEnLaZonaDelTelefono() {
        val movs = listOf(
            movimiento(1, TipoMovimiento.INGRESO, 100.0, "2026-10-01T05:30:00Z"), // 1 oct 00:30 en Bogotá
            movimiento(2, TipoMovimiento.INGRESO, 70.0, "2026-10-01T04:30:00Z"), // 30 sep 23:30 en Bogotá
            movimiento(3, TipoMovimiento.GASTO, 20.0, "2025-10-05T12:00:00Z"), // otro año
        )
        val delMes = CalculosDashboard.delMes(movs, hoy, zona)
        assertEquals(listOf(1L), delMes.map { it.id })
    }

    @Test
    fun ahorroDelMes() {
        assertEquals(4.2, CalculosDashboard.ahorroDelMes(1000.0, 958.0)!!, 0.0)
        assertEquals(86.0, CalculosDashboard.ahorroDelMes(2_500_000.0, 350_000.0)!!, 0.0)
        assertEquals(0.0, CalculosDashboard.ahorroDelMes(500.0, 500.0)!!, 0.0)
        assertEquals(33.3, CalculosDashboard.ahorroDelMes(3.0, 2.0)!!, 0.0)
    }

    @Test
    fun laPastillaNoSeMuestraSinIngresosOConAhorroNegativo() {
        assertNull(CalculosDashboard.ahorroDelMes(0.0, 0.0))
        assertNull(CalculosDashboard.ahorroDelMes(0.0, 100.0))
        assertNull(CalculosDashboard.ahorroDelMes(100.0, 150.0))
    }

    @Test
    fun categoriaConMasGastoYSuPorcentaje() {
        val movs = listOf(
            movimiento(1, TipoMovimiento.GASTO, 300.0, categoria = "Comida"),
            movimiento(2, TipoMovimiento.GASTO, 100.0, categoria = "Transporte"),
            movimiento(3, TipoMovimiento.GASTO, 100.0, categoria = "Comida"),
            movimiento(4, TipoMovimiento.INGRESO, 9000.0, categoria = "Ingresos"),
        )
        assertEquals(MayorGasto("Comida", 400.0, 80), CalculosDashboard.mayorGasto(movs))
    }

    @Test
    fun sinGastosNoHayCategoriaDeMayorGasto() {
        assertNull(CalculosDashboard.mayorGasto(emptyList()))
        assertNull(CalculosDashboard.mayorGasto(listOf(movimiento(1, TipoMovimiento.INGRESO, 10.0))))
    }

    @Test
    fun laTendenciaEsElBalanceAcumuladoEnOrdenDeFecha() {
        val d = datos(
            100.0,
            movimiento(2, TipoMovimiento.GASTO, 30.0, "2026-10-12T10:00:00Z"),
            movimiento(1, TipoMovimiento.INGRESO, 50.0, "2026-10-11T10:00:00Z"),
        )
        assertEquals(listOf(150.0, 120.0), CalculosDashboard.puntosTendencia(d))
    }

    @Test
    fun conUnSoloMovimientoLaTendenciaTieneUnPunto() {
        assertEquals(1, CalculosDashboard.puntosTendencia(datos(0.0, movimiento(1, TipoMovimiento.INGRESO, 5.0))).size)
        assertTrue(CalculosDashboard.puntosTendencia(datos()).isEmpty())
    }

    @Test
    fun recientesSonLosUltimos4DelMasNuevoAlMasViejo() {
        val movs = (1L..6L).map { movimiento(it, TipoMovimiento.GASTO, 1.0, "2026-10-0${it}T10:00:00Z") }
        assertEquals(listOf(6L, 5L, 4L, 3L), CalculosDashboard.recientes(movs.shuffled()).map { it.id })
    }
}

class FormatosDashboardTest {

    private val zona = ZoneId.of("America/Bogota")
    private val ahora = Instant.parse("2026-10-13T20:00:00Z") // 13 oct 15:00 en Bogotá

    @Test
    fun montosConSignoComaDeMilesYDosDecimales() {
        assertEquals("$0.00", FormatosDashboard.monto(0.0))
        assertEquals("$2,150,000.00", FormatosDashboard.monto(2_150_000.0))
        assertEquals("$64.50", FormatosDashboard.monto(64.5))
        assertEquals("-$1,850.25", FormatosDashboard.monto(-1850.25))
        assertEquals("+$0.00", FormatosDashboard.ingreso(0.0))
        assertEquals("-$0.00", FormatosDashboard.gasto(0.0))
        assertEquals("+$2,500,000.00", FormatosDashboard.ingreso(2_500_000.0))
        assertEquals("-$350,000.00", FormatosDashboard.gasto(350_000.0))
    }

    @Test
    fun montoSinDecimalesParaLaMeta() {
        assertEquals("$1,800", FormatosDashboard.montoSinDecimales(1800.0))
        assertEquals("$0", FormatosDashboard.montoSinDecimales(0.0))
    }

    @Test
    fun porcentajeConUnDecimal() {
        assertEquals("4.2", FormatosDashboard.porcentaje(4.2))
        assertEquals("86.0", FormatosDashboard.porcentaje(86.0))
    }

    @Test
    fun fechaHoyAyerYOtroDia() {
        assertEquals("Hoy, 14:32", FormatosDashboard.fecha(Instant.parse("2026-10-13T19:32:00Z"), ahora, zona))
        assertEquals("Ayer, 09:00", FormatosDashboard.fecha(Instant.parse("2026-10-12T14:00:00Z"), ahora, zona))
        assertEquals("12 Mar", FormatosDashboard.fecha(Instant.parse("2026-03-12T15:00:00Z"), ahora, zona))
        assertEquals("05 Ene", FormatosDashboard.fecha(Instant.parse("2026-01-05T15:00:00Z"), ahora, zona))
    }

    @Test
    fun laFechaUsaLaZonaDelTelefono() {
        // 13 oct 03:00 UTC = 12 oct 22:00 en Bogotá → "Ayer".
        assertEquals("Ayer, 22:00", FormatosDashboard.fecha(Instant.parse("2026-10-13T03:00:00Z"), ahora, zona))
    }

    @Test
    fun primerNombre() {
        assertEquals("Mateo", FormatosDashboard.primerNombre("Mateo Marin"))
        assertEquals("Ana", FormatosDashboard.primerNombre("  Ana   María López "))
        assertEquals("Luis", FormatosDashboard.primerNombre("Luis"))
    }
}

class ValidacionCamposRegistroTest {

    @Test
    fun marcaTodosLosCamposConProblemaALaVez() {
        val errores = ValidacionRegistro.validarCampos("", "paula@", "abc", aceptaTerminos = false)
        assertEquals(
            listOf(ErrorRegistro.NOMBRE_VACIO, ErrorRegistro.CORREO_INVALIDO, ErrorRegistro.CONTRASENA_CORTA, ErrorRegistro.TERMINOS_NO_ACEPTADOS),
            errores,
        )
    }

    @Test
    fun formularioCorrectoSinErrores() {
        assertTrue(ValidacionRegistro.validarCampos("Mateo Marin", "mateo@ejemplo.com", "Finanzia2026", true).isEmpty())
    }
}

package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.AporteDashboard
import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Montos exactos para los tests ("350.50" → 350.50 sin redondeos de Double). */
fun dinero(texto: String) = BigDecimal(texto)

/** Compara montos por valor (2500.00 == 2500). */
fun assertDinero(esperado: String, real: BigDecimal) =
    assertEquals("esperado $esperado y fue ${real.toPlainString()}", 0, BigDecimal(esperado).compareTo(real))

class CalculosDashboardTest {

    private val zona = ZoneId.of("America/Bogota")
    private val hoy = LocalDate.of(2026, 10, 13)

    private fun movimiento(
        id: Long,
        tipo: TipoMovimiento,
        monto: String,
        fecha: String = "2026-10-10T15:00:00Z",
        categoria: String? = null,
    ) = MovimientoDashboard(id, tipo, dinero(monto), "Movimiento $id", Instant.parse(fecha), "Principal", categoria, null)

    private fun datos(saldoInicial: String = "0", vararg movimientos: MovimientoDashboard) =
        DatosDashboard("Usuario Prueba", listOf(CuentaDashboard("Principal", dinero(saldoInicial))), movimientos.toList(), null)

    @Test
    fun elBalanceEsSaldoInicialMasIngresosMenosGastos() {
        val d = datos(
            "1000",
            movimiento(1, TipoMovimiento.INGRESO, "2500"),
            movimiento(2, TipoMovimiento.GASTO, "350.50"),
            movimiento(3, TipoMovimiento.GASTO, "49.50"),
        )
        assertDinero("3100.00", CalculosDashboard.balance(d))
    }

    @Test
    fun losAportesAMetasDescuentanDelBalance() {
        // Spec 10: un aporte es un ahorro y sale del saldo disponible.
        val d = datos("0", movimiento(1, TipoMovimiento.INGRESO, "10000"))
            .copy(aportes = listOf(AporteDashboard(dinero("2500"), Instant.parse("2026-10-11T15:00:00Z"))))
        assertDinero("7500", CalculosDashboard.balance(d))
    }

    @Test
    fun losCentavosNoSePierden() {
        // 0.1 + 0.2 con Double da 0.30000000000000004; con BigDecimal es exacto.
        val d = datos("0", movimiento(1, TipoMovimiento.INGRESO, "0.10"), movimiento(2, TipoMovimiento.INGRESO, "0.20"))
        assertDinero("0.30", CalculosDashboard.balance(d))
    }

    @Test
    fun sinMovimientosElBalanceEsElSaldoInicial() {
        assertDinero("0", CalculosDashboard.balance(datos()))
    }

    @Test
    fun ingresosYGastosSeSumanPorSeparado() {
        val movs = listOf(
            movimiento(1, TipoMovimiento.INGRESO, "100"),
            movimiento(2, TipoMovimiento.INGRESO, "50"),
            movimiento(3, TipoMovimiento.GASTO, "30"),
        )
        assertDinero("150", CalculosDashboard.total(movs, TipoMovimiento.INGRESO))
        assertDinero("30", CalculosDashboard.total(movs, TipoMovimiento.GASTO))
    }

    @Test
    fun soloCuentanLosMovimientosDelMesActualEnLaZonaDelTelefono() {
        val movs = listOf(
            movimiento(1, TipoMovimiento.INGRESO, "100", "2026-10-01T05:30:00Z"), // 1 oct 00:30 en Bogotá
            movimiento(2, TipoMovimiento.INGRESO, "70", "2026-10-01T04:30:00Z"), // 30 sep 23:30 en Bogotá
            movimiento(3, TipoMovimiento.GASTO, "20", "2025-10-05T12:00:00Z"), // otro año
        )
        val delMes = CalculosDashboard.delMes(movs, hoy, zona)
        assertEquals(listOf(1L), delMes.map { it.id })
    }

    @Test
    fun ahorroDelMes() {
        assertEquals(4.2, CalculosDashboard.ahorroDelMes(dinero("1000"), dinero("958"))!!, 0.0)
        assertEquals(86.0, CalculosDashboard.ahorroDelMes(dinero("2500000"), dinero("350000"))!!, 0.0)
        assertEquals(0.0, CalculosDashboard.ahorroDelMes(dinero("500"), dinero("500"))!!, 0.0)
        assertEquals(33.3, CalculosDashboard.ahorroDelMes(dinero("3"), dinero("2"))!!, 0.0)
    }

    @Test
    fun laPastillaNoSeMuestraSinIngresosOConAhorroNegativo() {
        assertNull(CalculosDashboard.ahorroDelMes(dinero("0"), dinero("0")))
        assertNull(CalculosDashboard.ahorroDelMes(dinero("0"), dinero("100")))
        assertNull(CalculosDashboard.ahorroDelMes(dinero("100"), dinero("150")))
    }

    @Test
    fun categoriaConMasGastoYSuPorcentaje() {
        val movs = listOf(
            movimiento(1, TipoMovimiento.GASTO, "300", categoria = "Comida"),
            movimiento(2, TipoMovimiento.GASTO, "100", categoria = "Transporte"),
            movimiento(3, TipoMovimiento.GASTO, "100", categoria = "Comida"),
            movimiento(4, TipoMovimiento.INGRESO, "9000", categoria = "Mensual"),
        )
        val mayor = CalculosDashboard.mayorGasto(movs)!!
        assertEquals("Comida", mayor.categoria)
        assertDinero("400", mayor.monto)
        assertEquals(80, mayor.porcentaje)
    }

    @Test
    fun sinGastosNoHayCategoriaDeMayorGasto() {
        assertNull(CalculosDashboard.mayorGasto(emptyList()))
        assertNull(CalculosDashboard.mayorGasto(listOf(movimiento(1, TipoMovimiento.INGRESO, "10"))))
    }

    @Test
    fun laTendenciaEsElBalanceAcumuladoEnOrdenDeFecha() {
        val d = datos(
            "100",
            movimiento(2, TipoMovimiento.GASTO, "30", "2026-10-12T10:00:00Z"),
            movimiento(1, TipoMovimiento.INGRESO, "50", "2026-10-11T10:00:00Z"),
        )
        assertEquals(listOf("150", "120"), CalculosDashboard.puntosTendencia(d).map { it.stripTrailingZeros().toPlainString() })
    }

    @Test
    fun laTendenciaIncluyeLosAportes() {
        val d = datos("0", movimiento(1, TipoMovimiento.INGRESO, "100", "2026-10-11T10:00:00Z"))
            .copy(aportes = listOf(AporteDashboard(dinero("40"), Instant.parse("2026-10-12T10:00:00Z"))))
        assertEquals(listOf("100", "60"), CalculosDashboard.puntosTendencia(d).map { it.stripTrailingZeros().toPlainString() })
    }

    @Test
    fun conUnSoloMovimientoLaTendenciaTieneUnPunto() {
        assertEquals(1, CalculosDashboard.puntosTendencia(datos("0", movimiento(1, TipoMovimiento.INGRESO, "5"))).size)
        assertTrue(CalculosDashboard.puntosTendencia(datos()).isEmpty())
    }

    @Test
    fun recientesSonLosUltimos4DelMasNuevoAlMasViejo() {
        val movs = (1L..6L).map { movimiento(it, TipoMovimiento.GASTO, "1", "2026-10-0${it}T10:00:00Z") }
        assertEquals(listOf(6L, 5L, 4L, 3L), CalculosDashboard.recientes(movs.shuffled()).map { it.id })
    }
}

class FormatosDashboardTest {

    private val zona = ZoneId.of("America/Bogota")
    private val ahora = Instant.parse("2026-10-13T20:00:00Z") // 13 oct 15:00 en Bogotá

    @Test
    fun montosConSignoComaDeMilesYDosDecimales() {
        assertEquals("$0.00", FormatosDashboard.monto(dinero("0")))
        assertEquals("$2,150,000.00", FormatosDashboard.monto(dinero("2150000")))
        assertEquals("$64.50", FormatosDashboard.monto(dinero("64.5")))
        assertEquals("-$1,850.25", FormatosDashboard.monto(dinero("-1850.25")))
        assertEquals("+$0.00", FormatosDashboard.ingreso(dinero("0")))
        assertEquals("-$0.00", FormatosDashboard.gasto(dinero("0")))
        assertEquals("+$2,500,000.00", FormatosDashboard.ingreso(dinero("2500000")))
        assertEquals("-$350,000.00", FormatosDashboard.gasto(dinero("350000")))
    }

    @Test
    fun montoSinDecimalesParaLaMeta() {
        assertEquals("$1,800", FormatosDashboard.montoSinDecimales(dinero("1800")))
        assertEquals("$0", FormatosDashboard.montoSinDecimales(dinero("0")))
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

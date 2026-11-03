package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.CuentaDashboard
import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MetaDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val ZONA = ZoneId.of("America/Bogota")

/** Martes 3 de noviembre de 2026. */
private val HOY = LocalDate.of(2026, 11, 3)

private var siguienteId = 0L

private fun mov(tipo: TipoMovimiento, monto: String, fecha: String, categoria: String?) =
    MovimientoDashboard(
        id = ++siguienteId,
        tipo = tipo,
        monto = BigDecimal(monto),
        descripcion = "Prueba",
        // Mediodía en Bogotá (17:00 UTC) para que la fecha no dependa de la zona.
        fecha = Instant.parse("${fecha}T17:00:00Z"),
        nombreCuenta = "Principal",
        nombreCategoria = categoria,
        colorCategoria = null,
    )

private fun ingreso(monto: String, fecha: String) = mov(TipoMovimiento.INGRESO, monto, fecha, "Mensual")
private fun gasto(monto: String, fecha: String, categoria: String) = mov(TipoMovimiento.GASTO, monto, fecha, categoria)

private fun bd(valor: String) = BigDecimal(valor)

private fun assertMonto(esperado: String, real: BigDecimal?) {
    assertTrue("Esperado $esperado, real $real", real != null && BigDecimal(esperado).compareTo(real) == 0)
}

class CalculosEstadisticasTest {

    // --- Límites de cada periodo y su periodo anterior ---

    @Test
    fun semanaVaDeLunesADomingoYLaAnteriorEsLaSemanaPrevia() {
        val semana = CalculosEstadisticas.periodo(TipoPeriodo.SEMANA, HOY)
        assertEquals(LocalDate.of(2026, 11, 2), semana.inicio)
        assertEquals(LocalDate.of(2026, 11, 8), semana.fin)
        val anterior = CalculosEstadisticas.anterior(semana)
        assertEquals(LocalDate.of(2026, 10, 26), anterior.inicio)
        assertEquals(LocalDate.of(2026, 11, 1), anterior.fin)
    }

    @Test
    fun elDomingoYElLunesPertenecenALaMismaSemana() {
        val delDomingo = CalculosEstadisticas.periodo(TipoPeriodo.SEMANA, LocalDate.of(2026, 11, 8))
        val delLunes = CalculosEstadisticas.periodo(TipoPeriodo.SEMANA, LocalDate.of(2026, 11, 2))
        assertEquals(delLunes, delDomingo)
    }

    @Test
    fun mesEsElMesCalendarioYElAnteriorEsElMesPrevioCompleto() {
        val mes = CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY)
        assertEquals(LocalDate.of(2026, 11, 1), mes.inicio)
        assertEquals(LocalDate.of(2026, 11, 30), mes.fin)
        val anterior = CalculosEstadisticas.anterior(mes)
        assertEquals(LocalDate.of(2026, 10, 1), anterior.inicio)
        assertEquals(LocalDate.of(2026, 10, 31), anterior.fin)
        // Desde el 31 de marzo, el anterior es febrero completo (28 días).
        val febrero = CalculosEstadisticas.anterior(CalculosEstadisticas.periodo(TipoPeriodo.MES, LocalDate.of(2026, 3, 31)))
        assertEquals(LocalDate.of(2026, 2, 1), febrero.inicio)
        assertEquals(LocalDate.of(2026, 2, 28), febrero.fin)
        // Desde enero, el anterior es diciembre del año pasado.
        val diciembre = CalculosEstadisticas.anterior(CalculosEstadisticas.periodo(TipoPeriodo.MES, LocalDate.of(2027, 1, 15)))
        assertEquals(LocalDate.of(2026, 12, 1), diciembre.inicio)
        assertEquals(LocalDate.of(2026, 12, 31), diciembre.fin)
    }

    @Test
    fun anioEsElAnioCalendarioYElAnteriorEsElAnioPasado() {
        val anio = CalculosEstadisticas.periodo(TipoPeriodo.ANIO, HOY)
        assertEquals(LocalDate.of(2026, 1, 1), anio.inicio)
        assertEquals(LocalDate.of(2026, 12, 31), anio.fin)
        val anterior = CalculosEstadisticas.anterior(anio)
        assertEquals(LocalDate.of(2025, 1, 1), anterior.inicio)
        assertEquals(LocalDate.of(2025, 12, 31), anterior.fin)
    }

    @Test
    fun rangoUsaLasFechasElegidasYElAnteriorTieneLaMismaDuracion() {
        val rango = CalculosEstadisticas.periodo(TipoPeriodo.RANGO, HOY, LocalDate.of(2026, 10, 10) to LocalDate.of(2026, 10, 19))
        assertEquals(10L, rango.dias)
        val anterior = CalculosEstadisticas.anterior(rango)
        assertEquals(LocalDate.of(2026, 9, 30), anterior.inicio)
        assertEquals(LocalDate.of(2026, 10, 9), anterior.fin)
        assertEquals(rango.dias, anterior.dias)
        // Fechas al revés: se ordenan.
        val alReves = CalculosEstadisticas.periodo(TipoPeriodo.RANGO, HOY, LocalDate.of(2026, 10, 19) to LocalDate.of(2026, 10, 10))
        assertEquals(rango, alReves)
    }

    @Test
    fun losMovimientosSeUbicanPorSuFechaEnLaZonaDelTelefono() {
        // 1 de noviembre a las 03:00 UTC = 31 de octubre a las 22:00 en Bogotá.
        val tarde = mov(TipoMovimiento.GASTO, "1000", "2026-11-01", "Comida").copy(fecha = Instant.parse("2026-11-01T03:00:00Z"))
        val noviembre = CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY)
        assertTrue(CalculosEstadisticas.delPeriodo(listOf(tarde), noviembre, ZONA).isEmpty())
        assertEquals(1, CalculosEstadisticas.delPeriodo(listOf(tarde), CalculosEstadisticas.anterior(noviembre), ZONA).size)
    }

    // --- Variaciones, salud financiera y porcentajes ---

    @Test
    fun variacionConUnDecimalYSinPeriodoAnteriorNoSeCalcula() {
        assertMonto("15.0", CalculosEstadisticas.variacion(bd("2300000"), bd("2000000")))
        assertMonto("-25.0", CalculosEstadisticas.variacion(bd("1500000"), bd("2000000")))
        // Con un neto anterior negativo se divide por su valor absoluto.
        assertMonto("150.0", CalculosEstadisticas.variacion(bd("100"), bd("-200")))
        assertMonto("14.3", CalculosEstadisticas.variacion(bd("114.25"), bd("100")))
        assertNull(CalculosEstadisticas.variacion(bd("100"), BigDecimal.ZERO))
    }

    @Test
    fun saludFinancieraSegunElAhorroDelPeriodo() {
        assertEquals(SaludFinanciera.EXCELENTE, CalculosEstadisticas.salud(bd("1000"), bd("800")))
        assertEquals(SaludFinanciera.ESTABLE, CalculosEstadisticas.salud(bd("1000"), bd("800.01")))
        assertEquals(SaludFinanciera.ESTABLE, CalculosEstadisticas.salud(bd("1000"), bd("1000")))
        assertEquals(SaludFinanciera.NEGATIVA, CalculosEstadisticas.salud(bd("1000"), bd("1000.01")))
        assertNull("Sin ingresos no se muestra", CalculosEstadisticas.salud(BigDecimal.ZERO, bd("500")))
    }

    @Test
    fun porcentajeDeIngresoSinDecimales() {
        assertEquals(37, CalculosEstadisticas.porcentajeDeIngreso(bd("1110000"), bd("3000000")))
        assertEquals(0, CalculosEstadisticas.porcentajeDeIngreso(BigDecimal.ZERO, bd("100")))
        assertNull(CalculosEstadisticas.porcentajeDeIngreso(bd("100"), BigDecimal.ZERO))
    }

    // --- Promedio diario ---

    @Test
    fun promedioDiarioCuentaSoloLosDiasTranscurridosSiElPeriodoEstaEnCurso() {
        val noviembre = CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY)
        // Del 1 al 3 de noviembre: 3 días.
        assertMonto("100000", CalculosEstadisticas.promedioDiario(bd("300000"), noviembre, HOY))
        assertMonto("166666.67", CalculosEstadisticas.promedioDiario(bd("500000"), noviembre, HOY))
    }

    @Test
    fun promedioDiarioDeUnPeriodoTerminadoUsaTodosSusDias() {
        val octubre = CalculosEstadisticas.anterior(CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY))
        assertMonto("10000", CalculosEstadisticas.promedioDiario(bd("310000"), octubre, HOY))
    }

    @Test
    fun promedioDiarioDeUnPeriodoQueNoHaEmpezadoNoSeMuestra() {
        val futuro = CalculosEstadisticas.periodo(TipoPeriodo.RANGO, HOY, LocalDate.of(2026, 12, 1) to LocalDate.of(2026, 12, 5))
        assertNull(CalculosEstadisticas.promedioDiario(bd("1000"), futuro, HOY))
    }

    // --- Gráfica ---

    @Test
    fun mesSeAgrupaEnCuatroSemanasYLaCuartaLlegaHastaFinDeMes() {
        val noviembre = CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY)
        val movimientos = listOf(
            gasto("100", "2026-11-07", "Comida"),
            gasto("200", "2026-11-08", "Comida"),
            ingreso("1000", "2026-11-21"),
            gasto("300", "2026-11-22", "Comida"),
            gasto("400", "2026-11-30", "Comida"),
        )
        val grupos = CalculosEstadisticas.grupos(movimientos, noviembre, LocalDate.of(2026, 11, 30), ZONA)
        assertEquals(listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4"), grupos.map { it.etiqueta })
        assertEquals(listOf(1, 8, 15, 22), grupos.map { it.inicio.dayOfMonth })
        assertEquals(listOf(7, 14, 21, 30), grupos.map { it.fin.dayOfMonth })
        assertMonto("100", grupos[0].gastos)
        assertMonto("200", grupos[1].gastos)
        assertMonto("1000", grupos[2].ingresos)
        assertMonto("700", grupos[3].gastos)
        // En febrero (28 días) la Sem 4 va del 22 al 28.
        val febrero = CalculosEstadisticas.tramos(CalculosEstadisticas.periodo(TipoPeriodo.MES, LocalDate.of(2026, 2, 10)))
        assertEquals(28, febrero.last().third.dayOfMonth)
    }

    @Test
    fun losGruposFuturosSeMarcanYElSeleccionadoPorDefectoEsElDeHoy() {
        val noviembre = CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY)
        val grupos = CalculosEstadisticas.grupos(emptyList(), noviembre, HOY, ZONA)
        assertEquals(listOf(false, true, true, true), grupos.map { it.futuro })
        assertEquals(0, CalculosEstadisticas.grupoPorDefecto(grupos, HOY))
        assertEquals(2, CalculosEstadisticas.grupoPorDefecto(grupos, LocalDate.of(2026, 11, 18)))
        // Periodo terminado: el último grupo.
        val octubre = CalculosEstadisticas.anterior(noviembre)
        val gruposOctubre = CalculosEstadisticas.grupos(emptyList(), octubre, HOY, ZONA)
        assertEquals(3, CalculosEstadisticas.grupoPorDefecto(gruposOctubre, HOY))
        assertTrue(gruposOctubre.none { it.futuro })
    }

    @Test
    fun semanaSeAgrupaPorDiasYAnioPorMeses() {
        val semana = CalculosEstadisticas.tramos(CalculosEstadisticas.periodo(TipoPeriodo.SEMANA, HOY))
        assertEquals(listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"), semana.map { it.first })
        val anio = CalculosEstadisticas.tramos(CalculosEstadisticas.periodo(TipoPeriodo.ANIO, HOY))
        assertEquals(listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"), anio.map { it.first })
    }

    @Test
    fun rangoSeAgrupaPorDiasSemanasOMesesSegunSuDuracion() {
        fun rango(dias: Long) = CalculosEstadisticas.periodo(TipoPeriodo.RANGO, HOY, LocalDate.of(2026, 1, 1) to LocalDate.of(2026, 1, 1).plusDays(dias - 1))
        assertEquals(Agrupacion.DIAS, CalculosEstadisticas.agrupacion(rango(14)))
        assertEquals(14, CalculosEstadisticas.tramos(rango(14)).size)
        assertEquals(Agrupacion.SEMANAS, CalculosEstadisticas.agrupacion(rango(15)))
        // 15 días: Sem 1 (7), Sem 2 (7) y Sem 3 (1 día).
        val quince = CalculosEstadisticas.tramos(rango(15))
        assertEquals(listOf("Sem 1", "Sem 2", "Sem 3"), quince.map { it.first })
        assertEquals(LocalDate.of(2026, 1, 15), quince.last().second)
        assertEquals(LocalDate.of(2026, 1, 15), quince.last().third)
        assertEquals(Agrupacion.SEMANAS, CalculosEstadisticas.agrupacion(rango(120)))
        assertEquals(Agrupacion.MESES, CalculosEstadisticas.agrupacion(rango(121)))
        assertEquals(listOf("Ene", "Feb", "Mar", "Abr", "May"), CalculosEstadisticas.tramos(rango(121)).map { it.first })
    }

    // --- Diagnóstico y recomendación ---

    @Test
    fun categoriaConMasGastoYSuPorcentaje() {
        val mayor = CalculosEstadisticas.mayorGasto(
            listOf(gasto("300", "2026-11-02", "Comida"), gasto("100", "2026-11-02", "Transporte"), ingreso("5000", "2026-11-01")),
        )
        assertEquals(CategoriaDestacada("Comida", 75), mayor)
        // Empate: la primera en orden alfabético.
        val empate = CalculosEstadisticas.mayorGasto(listOf(gasto("100", "2026-11-02", "Transporte"), gasto("100", "2026-11-02", "Comida")))
        assertEquals("Comida", empate?.categoria)
        assertNull("Sin gastos no hay categoría", CalculosEstadisticas.mayorGasto(listOf(ingreso("100", "2026-11-01"))))
    }

    @Test
    fun mayorReduccionSoloConCategoriasEnAmbosPeriodosYDeAlMenosUnoPorCiento() {
        val actuales = listOf(
            gasto("420", "2026-11-02", "Comida"),
            gasto("99.5", "2026-11-02", "Transporte"),
            gasto("50", "2026-11-02", "Diversión"),
        )
        val anteriores = listOf(gasto("500", "2026-10-02", "Comida"), gasto("100", "2026-10-02", "Transporte"))
        // Comida bajó 16%; Transporte solo 0.5%; Diversión no tenía gastos antes.
        assertEquals(CategoriaDestacada("Comida", 16), CalculosEstadisticas.mayorReduccion(actuales, anteriores))
        assertNull(CalculosEstadisticas.mayorReduccion(actuales, emptyList()))
        assertNull(
            "Si ninguna bajó, no hay frase",
            CalculosEstadisticas.mayorReduccion(listOf(gasto("600", "2026-11-02", "Comida")), anteriores),
        )
    }

    @Test
    fun recomendacionSoloConMetaActivaYFlujoNetoPositivo() {
        val meta = MetaDashboard("Viaje familiar", bd("3000000"), bd("1000000"))
        assertEquals(Recomendacion(bd("500000"), "Viaje familiar", bd("2000000")), CalculosEstadisticas.recomendacion(meta, bd("500000")))
        assertNull(CalculosEstadisticas.recomendacion(null, bd("500000")))
        assertNull(CalculosEstadisticas.recomendacion(meta, BigDecimal.ZERO))
        assertNull(CalculosEstadisticas.recomendacion(meta, bd("-1")))
        assertNull("Meta completa: no se recomienda", CalculosEstadisticas.recomendacion(meta.copy(aportado = bd("3000000")), bd("500000")))
    }

    // --- Pantalla completa ---

    private val datos = DatosDashboard(
        nombreUsuario = "Prueba",
        cuentas = listOf(CuentaDashboard("Principal", BigDecimal.ZERO)),
        movimientos = listOf(
            ingreso("2000000", "2026-10-05"),
            gasto("500000", "2026-10-10", "Comida"),
            gasto("100000", "2026-10-20", "Transporte"),
            ingreso("2500000", "2026-11-01"),
            gasto("420000", "2026-11-02", "Comida"),
            gasto("90000", "2026-11-03", "Diversión"),
        ),
        meta = MetaDashboard("Viaje familiar", bd("3000000"), bd("1000000")),
    )

    @Test
    fun resumenDelMesConComparacionesContraElMesAnterior() {
        val r = CalculosEstadisticas.resumir(datos, TipoPeriodo.MES, HOY, ZONA)
        assertMonto("2500000", r.ingresos)
        assertMonto("510000", r.gastos)
        assertMonto("1990000", r.neto)
        // Octubre: neto 1.400.000 → (1.990.000 − 1.400.000) ÷ 1.400.000 = 42.142…%
        assertMonto("42.1", r.variacionNeto)
        assertMonto("25.0", r.variacionIngresos)
        assertEquals(20, r.porcentajeDeIngreso)
        assertEquals(SaludFinanciera.EXCELENTE, r.salud)
        // 510.000 ÷ 3 días transcurridos.
        assertMonto("170000", r.promedioDiario)
        assertEquals(CategoriaDestacada("Comida", 82), r.mayorGasto)
        assertEquals(CategoriaDestacada("Comida", 16), r.reduccion)
        assertEquals("Viaje familiar", r.recomendacion?.nombreMeta)
        assertTrue(r.hayMovimientos)
    }

    @Test
    fun sinPeriodoAnteriorNoHayComparaciones() {
        val r = CalculosEstadisticas.resumir(datos, TipoPeriodo.ANIO, HOY, ZONA)
        assertMonto("4500000", r.ingresos)
        assertNull("2025 no tiene movimientos", r.variacionNeto)
        assertNull(r.variacionIngresos)
        assertNull(r.reduccion)
    }

    @Test
    fun periodoSinMovimientos() {
        val r = CalculosEstadisticas.resumir(datos.copy(movimientos = emptyList()), TipoPeriodo.MES, HOY, ZONA)
        assertMonto("0", r.neto)
        assertFalse(r.hayMovimientos)
        assertNull(r.promedioDiario)
        assertNull(r.salud)
        assertNull(r.mayorGasto)
        assertNull("Sin flujo neto positivo no hay recomendación", r.recomendacion)
    }
}

class FormatosEstadisticasTest {

    @Test
    fun montosConPuntoParaMilesYSinDecimales() {
        assertEquals("$3.750.000", FormatosEstadisticas.monto(bd("3750000")))
        assertEquals("$48.300", FormatosEstadisticas.monto(bd("48300.49")))
        assertEquals("$48.301", FormatosEstadisticas.monto(bd("48300.5")))
        assertEquals("$0", FormatosEstadisticas.monto(BigDecimal.ZERO))
        assertEquals("$999", FormatosEstadisticas.monto(bd("999")))
        assertEquals("$1.000", FormatosEstadisticas.monto(bd("999.5")))
    }

    @Test
    fun montoConSignoMasOMenos() {
        assertEquals("+$1.980.000", FormatosEstadisticas.montoConSigno(bd("1980000")))
        assertEquals("-$75.000", FormatosEstadisticas.montoConSigno(bd("-75000")))
        assertEquals("$0", FormatosEstadisticas.montoConSigno(BigDecimal.ZERO))
        assertEquals("$0", FormatosEstadisticas.montoConSigno(bd("-0.4")))
    }

    @Test
    fun variacionesConUnDecimalPuntoYSigno() {
        assertEquals("+12.4%", FormatosEstadisticas.variacion(bd("12.4")))
        assertEquals("+7.5%", FormatosEstadisticas.variacion(bd("7.45")))
        assertEquals("-3.0%", FormatosEstadisticas.variacion(bd("-3")))
        assertEquals("+100.0%", FormatosEstadisticas.variacion(bd("100")))
        assertEquals("0.0%", FormatosEstadisticas.variacion(BigDecimal.ZERO))
        assertEquals("+1234.5%", FormatosEstadisticas.variacion(bd("1234.5")))
    }

    @Test
    fun porcentajeDeIngresoSinSignoNiDecimales() {
        assertEquals("37%", FormatosEstadisticas.porcentaje(37))
    }

    @Test
    fun textosDeMesesYPeriodos() {
        assertEquals("Mes (Nov 2026)", FormatosEstadisticas.opcionMes(HOY))
        assertEquals("Mes (Ene 2027)", FormatosEstadisticas.opcionMes(LocalDate.of(2027, 1, 1)))
        val mes = CalculosEstadisticas.periodo(TipoPeriodo.MES, HOY)
        assertEquals("vs Oct", FormatosEstadisticas.versus(CalculosEstadisticas.anterior(mes)))
        assertEquals("Octubre", FormatosEstadisticas.comparadoCon(CalculosEstadisticas.anterior(mes)))
        assertEquals("Ingresos vs. Gastos (Noviembre)", FormatosEstadisticas.subtituloGrafica(mes))
        val semana = CalculosEstadisticas.periodo(TipoPeriodo.SEMANA, HOY)
        assertEquals("vs sem. anterior", FormatosEstadisticas.versus(CalculosEstadisticas.anterior(semana)))
        assertEquals("la semana anterior", FormatosEstadisticas.comparadoCon(CalculosEstadisticas.anterior(semana)))
        assertEquals("Ingresos vs. Gastos (Esta semana)", FormatosEstadisticas.subtituloGrafica(semana))
        val anio = CalculosEstadisticas.periodo(TipoPeriodo.ANIO, HOY)
        assertEquals("vs 2025", FormatosEstadisticas.versus(CalculosEstadisticas.anterior(anio)))
        assertEquals("2025", FormatosEstadisticas.comparadoCon(CalculosEstadisticas.anterior(anio)))
        val rango = CalculosEstadisticas.periodo(TipoPeriodo.RANGO, HOY, LocalDate.of(2026, 10, 1) to LocalDate.of(2026, 10, 31))
        assertEquals("vs periodo anterior", FormatosEstadisticas.versus(CalculosEstadisticas.anterior(rango)))
        assertEquals("el periodo anterior", FormatosEstadisticas.comparadoCon(CalculosEstadisticas.anterior(rango)))
        assertEquals("Ingresos vs. Gastos (01 Oct – 31 Oct)", FormatosEstadisticas.subtituloGrafica(rango))
        assertEquals("Evolución Semanal", FormatosEstadisticas.tituloGrafica(Agrupacion.SEMANAS))
    }
}

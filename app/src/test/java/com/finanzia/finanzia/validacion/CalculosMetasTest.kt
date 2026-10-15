package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.Aporte
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.Meta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalculosMetasTest {

    private val zona = ZoneId.of("America/Bogota")
    private val ahora = Instant.parse("2026-10-14T17:00:00Z")

    private fun meta(objetivo: Double, aportes: List<Aporte> = emptyList(), limite: LocalDate? = LocalDate.of(2027, 6, 30), nombre: String = "Meta") =
        Meta(1, nombre, objetivo, limite, ahora, aportes)

    @Test
    fun loAhorradoEsLaSumaDeLosAportesYLoQueFaltaNuncaEsNegativo() {
        val m = meta(1_000_000.0, listOf(Aporte(300_000.0, ahora), Aporte(400_000.0, ahora)))
        assertEquals(700_000.0, m.ahorrado, 0.0)
        assertEquals(300_000.0, CalculosMetas.falta(m), 0.0)
        assertEquals(70, CalculosMetas.porcentaje(m))
        val superada = meta(100.0, listOf(Aporte(150.0, ahora)))
        assertEquals(0.0, CalculosMetas.falta(superada), 0.0)
        assertEquals(100, CalculosMetas.porcentaje(superada))
    }

    @Test
    fun elPorcentajeConObjetivoCeroEsCero() {
        assertEquals(0, CalculosMetas.porcentaje(0.0, 0.0))
    }

    @Test
    fun losTotalesSumanTodasLasMetas() {
        val metas = listOf(meta(1_000.0, listOf(Aporte(500.0, ahora))), meta(3_000.0, listOf(Aporte(1_000.0, ahora))))
        assertEquals(1_500.0, CalculosMetas.totalAhorrado(metas), 0.0)
        assertEquals(4_000.0, CalculosMetas.totalObjetivo(metas), 0.0)
        assertEquals(38, CalculosMetas.porcentaje(1_500.0, 4_000.0))
    }

    @Test
    fun sinDosAportesNoHayConsejo() {
        assertNull(CalculosMetas.consejo(listOf(meta(1_000.0, listOf(Aporte(100.0, ahora)))), ahora, zona))
        assertNull(CalculosMetas.consejo(emptyList(), ahora, zona))
    }

    @Test
    fun elConsejoComparaElRitmoRealConLaFechaLimite() {
        // 200,000 en 100 días = 2,000 por día; faltan 800,000 → 400 días: después del límite.
        val inicio = ahora.minusSeconds(100L * 24 * 3600)
        val lenta = meta(1_000_000.0, listOf(Aporte(100_000.0, inicio), Aporte(100_000.0, ahora)), LocalDate.of(2027, 6, 30), "Viaje")
        val consejo = CalculosMetas.consejo(listOf(lenta), ahora, zona)
        assertNotNull(consejo)
        consejo!!
        assertEquals("Viaje", consejo.nombreMeta)
        assertFalse(consejo.antes)
        // Proyectada: 2026-10-14 + 400 días = 2027-11-18; el límite es 2027-06-30 → 141 días después.
        assertEquals(141L, consejo.dias)

        val rapida = meta(300_000.0, listOf(Aporte(100_000.0, inicio), Aporte(100_000.0, ahora)), LocalDate.of(2027, 6, 30))
        assertTrue(CalculosMetas.consejo(listOf(rapida), ahora, zona)!!.antes)
    }

    @Test
    fun mesesYQuincenasRestantes() {
        val hoy = LocalDate.of(2026, 10, 14)
        assertEquals(1L, CalculosMetas.mesesRestantes(hoy, YearMonth.of(2026, 11)))
        assertEquals(8L, CalculosMetas.mesesRestantes(hoy, YearMonth.of(2027, 6)))
        assertEquals(16L, CalculosMetas.quincenasRestantes(hoy, YearMonth.of(2027, 6)))
        assertEquals("Junio 2027", CalculosMetas.mesYAnio(LocalDate.of(2027, 6, 1)))
    }

    @Test
    fun validacionDeNuevaMeta() {
        val hoy = LocalDate.of(2026, 10, 14)
        assertEquals(ValidacionNuevaMeta.ERROR_NOMBRE, ValidacionNuevaMeta.nombre("   "))
        assertNull(ValidacionNuevaMeta.nombre(" Viaje "))
        assertEquals(ValidacionNuevaMeta.ERROR_CATEGORIA, ValidacionNuevaMeta.categoria(null))
        assertNull(ValidacionNuevaMeta.categoria(CategoriaMeta.VIAJES))
        assertEquals(ValidacionNuevaMeta.ERROR_MONTO, ValidacionNuevaMeta.monto(""))
        assertEquals(ValidacionNuevaMeta.ERROR_MONTO, ValidacionNuevaMeta.monto("0"))
        assertNull(ValidacionNuevaMeta.monto("3500000"))
        assertEquals(ValidacionNuevaMeta.ERROR_FECHA, ValidacionNuevaMeta.fecha(null, hoy))
        assertEquals(ValidacionNuevaMeta.ERROR_FECHA, ValidacionNuevaMeta.fecha(YearMonth.of(2026, 10), hoy))
        assertNull(ValidacionNuevaMeta.fecha(YearMonth.of(2026, 11), hoy))
    }

    @Test
    fun losMontosRapidosSeSuman() {
        assertEquals(listOf(20_000L, 50_000L, 100_000L, 150_000L, 200_000L), ValidacionNuevaMeta.MONTOS_RAPIDOS)
        assertEquals("20000", ValidacionNuevaMeta.sumar("", 20_000))
        assertEquals("170000", ValidacionNuevaMeta.sumar("150000", 20_000))
    }
}

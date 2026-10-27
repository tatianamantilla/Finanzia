package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.validacion.ReglaSaldo.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regla del saldo (spec 10): ningún movimiento nuevo o editado deja el saldo por debajo de $0.00. */
class ReglaSaldoTest {

    private fun permitido(r: Resultado, quedaEn: String) {
        assertTrue("Debía permitirse y fue $r", r is Resultado.Permitido)
        assertDinero(quedaEn, (r as Resultado.Permitido).saldoResultante)
    }

    private fun insuficiente(r: Resultado, disponible: String) {
        assertTrue("Debía rechazarse por saldo y fue $r", r is Resultado.SaldoInsuficiente)
        assertDinero(disponible, (r as Resultado.SaldoInsuficiente).disponible)
    }

    @Test
    fun saldoDisponibleIncluyeIngresosGastosYAportes() {
        assertDinero("6500", ReglaSaldo.saldoDisponible(dinero("1000"), dinero("10000"), dinero("3000"), dinero("1500")))
    }

    @Test
    fun saldo10000YGasto40000SeRechaza() {
        insuficiente(ReglaSaldo.validarSalida(dinero("10000"), dinero("40000")), disponible = "10000")
    }

    @Test
    fun gastoIgualAlSaldoSePermiteYQuedaEnCero() {
        permitido(ReglaSaldo.validarSalida(dinero("10000"), dinero("10000")), quedaEn = "0.00")
    }

    @Test
    fun montoCeroONegativoSeRechaza() {
        assertEquals(Resultado.MontoInvalido, ReglaSaldo.validarSalida(dinero("10000"), dinero("0")))
        assertEquals(Resultado.MontoInvalido, ReglaSaldo.validarSalida(dinero("10000"), dinero("-5")))
        assertEquals(Resultado.MontoInvalido, ReglaSaldo.validarEntrada(dinero("10000"), dinero("0")))
        assertEquals(Resultado.MontoInvalido, ReglaSaldo.validarEdicionSalida(dinero("10000"), dinero("5000"), dinero("0")))
        assertEquals(Resultado.MontoInvalido, ReglaSaldo.validarEdicionEntrada(dinero("10000"), dinero("5000"), dinero("-1")))
    }

    @Test
    fun unIngresoNuevoSiempreSePermite() {
        permitido(ReglaSaldo.validarEntrada(dinero("0"), dinero("2500")), quedaEn = "2500")
    }

    @Test
    fun editarGastoPorEncimaDelSaldoMasLoYaDescontadoSeRechaza() {
        // Saldo actual 2,000 con un gasto de 5,000 ya descontado: lo máximo para ese gasto es 7,000.
        insuficiente(ReglaSaldo.validarEdicionSalida(dinero("2000"), dinero("5000"), dinero("7001")), disponible = "7000")
    }

    @Test
    fun editarGastoHastaElSaldoMasLoYaDescontadoSePermite() {
        permitido(ReglaSaldo.validarEdicionSalida(dinero("2000"), dinero("5000"), dinero("7000")), quedaEn = "0")
    }

    @Test
    fun editarGastoAUnValorMenorSePermite() {
        permitido(ReglaSaldo.validarEdicionSalida(dinero("2000"), dinero("5000"), dinero("3000")), quedaEn = "4000")
    }

    @Test
    fun reducirUnIngresoPorDebajoDeLoYaGastadoSeRechaza() {
        // Ingreso de 10,000 y gastos de 8,000: saldo 2,000. Bajar el ingreso a 7,000 dejaría -1,000.
        insuficiente(ReglaSaldo.validarEdicionEntrada(dinero("2000"), dinero("10000"), dinero("7000")), disponible = "2000")
    }

    @Test
    fun reducirUnIngresoSinPasarDeCeroSePermite() {
        permitido(ReglaSaldo.validarEdicionEntrada(dinero("2000"), dinero("10000"), dinero("8000")), quedaEn = "0")
    }

    @Test
    fun cuentaYaNegativaSoloPermiteCambiosQueNoEmpeoren() {
        val negativo = dinero("-500")
        // Bajar un gasto (sube el saldo) aunque siga en negativo: se permite.
        permitido(ReglaSaldo.validarEdicionSalida(negativo, dinero("5000"), dinero("4800")), quedaEn = "-300")
        // Subir un ingreso: se permite.
        permitido(ReglaSaldo.validarEdicionEntrada(negativo, dinero("1000"), dinero("2000")), quedaEn = "500")
        // Dejar el mismo monto (corregir solo la categoría): no empeora, se permite.
        permitido(ReglaSaldo.validarEdicionSalida(negativo, dinero("5000"), dinero("5000")), quedaEn = "-500")
        // Subir un gasto o registrar uno nuevo: empeora, se rechaza.
        insuficiente(ReglaSaldo.validarEdicionSalida(negativo, dinero("5000"), dinero("5100")), disponible = "4500")
        insuficiente(ReglaSaldo.validarSalida(negativo, dinero("1")), disponible = "-500")
        // Bajar un ingreso: empeora, se rechaza.
        insuficiente(ReglaSaldo.validarEdicionEntrada(negativo, dinero("1000"), dinero("900")), disponible = "-500")
    }

    @Test
    fun unAporteAMetaSigueLaMismaRegla() {
        insuficiente(ReglaSaldo.validarSalida(dinero("10000"), dinero("10001")), disponible = "10000")
        permitido(ReglaSaldo.validarSalida(dinero("10000"), dinero("10000")), quedaEn = "0")
    }

    @Test
    fun losCentavosSeCalculanExactos() {
        permitido(ReglaSaldo.validarSalida(dinero("0.30"), dinero("0.10").add(dinero("0.20"))), quedaEn = "0")
    }

    @Test
    fun mensajeDelError() {
        assertEquals(
            "No tienes saldo suficiente. Tu saldo disponible es $10,000.00.",
            ReglaSaldo.mensajeSaldoInsuficiente(dinero("10000")),
        )
    }
}

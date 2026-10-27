package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Frase del "Insight Inteligente de Hoy" en la pantalla Acceso autorizado. */
sealed interface InsightAcceso {
    /**
     * Gastos de este mes comparados con el mismo período del mes pasado.
     * [porcentaje] negativo = por debajo; positivo = por encima; 0 = igual.
     */
    data class ComparacionMesAnterior(val porcentaje: Int) : InsightAcceso

    /** La categoría con más gasto del mes y qué parte del gasto representa. */
    data class MayorGasto(val categoria: String, val porcentaje: Int) : InsightAcceso

    /** Hubo ingresos este mes pero ningún gasto: se ahorró todo. */
    data object AhorroCompleto : InsightAcceso

    /** No hay movimientos este mes (o no se pudieron leer). */
    data object SinDatos : InsightAcceso
}

/** Lo que muestra la tarjeta de Acceso autorizado, calculado con los datos reales del usuario. */
data class ResumenAcceso(
    /** Saldo total disponible (el mismo balance del Dashboard), o null si no se pudo leer. */
    val saldo: BigDecimal?,
    /** Pastilla de variación del saldo en el mes, en %. null = no se muestra. */
    val variacion: Double?,
    val insight: InsightAcceso,
) {
    companion object {
        /** Cuando no se pudieron leer los datos. */
        val SIN_DATOS = ResumenAcceso(saldo = null, variacion = null, insight = InsightAcceso.SinDatos)
    }
}

/** Cálculos de la tarjeta de Acceso autorizado. Todo sale de los datos reales; nada es de muestra. */
object CalculosAcceso {

    fun resumir(datos: DatosDashboard, ahora: Instant, zona: ZoneId): ResumenAcceso {
        val hoy = ahora.atZone(zona).toLocalDate()
        return ResumenAcceso(
            saldo = CalculosDashboard.balance(datos),
            variacion = variacionDelMes(datos, hoy, zona),
            insight = insight(datos.movimientos, hoy, zona),
        )
    }

    /**
     * Saldo justo antes del día [dia]: saldo inicial de las cuentas + movimientos anteriores a ese día
     * − aportes a metas anteriores a ese día (los aportes descuentan del saldo, spec 10).
     */
    fun saldoAntesDe(datos: DatosDashboard, dia: LocalDate, zona: ZoneId): BigDecimal {
        val anteriores = datos.movimientos.filter { it.fecha.atZone(zona).toLocalDate() < dia }
        val aportesAnteriores = datos.aportes.filter { it.fecha.atZone(zona).toLocalDate() < dia }
        return ReglaSaldo.saldoDisponible(
            saldoInicial = CalculosDashboard.suma(datos.cuentas.map { it.saldoInicial }),
            ingresos = CalculosDashboard.total(anteriores, TipoMovimiento.INGRESO),
            gastos = CalculosDashboard.total(anteriores, TipoMovimiento.GASTO),
            aportes = CalculosDashboard.suma(aportesAnteriores.map { it.monto }),
        )
    }

    /**
     * Pastilla de variación: (saldo de hoy − saldo al empezar el mes) ÷ saldo al empezar el mes × 100,
     * a un decimal. `null` (no se muestra) si el saldo al empezar el mes era 0 o negativo, porque
     * ahí el porcentaje no tiene sentido (por ejemplo, una cuenta recién creada).
     */
    fun variacionDelMes(datos: DatosDashboard, hoy: LocalDate, zona: ZoneId): Double? {
        val inicioMes = saldoAntesDe(datos, hoy.withDayOfMonth(1), zona)
        if (inicioMes.signum() <= 0) return null
        val actual = CalculosDashboard.balance(datos)
        return (actual - inicioMes).multiply(BigDecimal(100)).divide(inicioMes, 1, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Elige la frase del Insight, en este orden:
     * 1. Gastos de este mes vs. el mismo período del mes pasado (si hay gastos en ambos).
     * 2. La categoría con más gasto del mes.
     * 3. Ingresos sin gastos: se ahorró el 100 %.
     * 4. Sin información.
     */
    fun insight(movimientos: List<MovimientoDashboard>, hoy: LocalDate, zona: ZoneId): InsightAcceso {
        val delMes = CalculosDashboard.delMes(movimientos, hoy, zona)
        val gastosMes = CalculosDashboard.total(delMes, TipoMovimiento.GASTO)

        comparacionMesAnterior(movimientos, hoy, zona)?.let { return InsightAcceso.ComparacionMesAnterior(it) }
        CalculosDashboard.mayorGasto(delMes)?.let { return InsightAcceso.MayorGasto(it.categoria, it.porcentaje) }
        if (gastosMes.signum() <= 0 && CalculosDashboard.total(delMes, TipoMovimiento.INGRESO).signum() > 0) {
            return InsightAcceso.AhorroCompleto
        }
        return InsightAcceso.SinDatos
    }

    /**
     * % de diferencia entre lo gastado del 1 a hoy de este mes y lo gastado en los mismos días del
     * mes pasado (si el mes pasado fue más corto, hasta su último día). Redondeado a entero.
     * `null` si no hubo gastos en alguno de los dos períodos.
     */
    fun comparacionMesAnterior(movimientos: List<MovimientoDashboard>, hoy: LocalDate, zona: ZoneId): Int? {
        val inicioActual = hoy.withDayOfMonth(1)
        val inicioAnterior = inicioActual.minusMonths(1)
        val finAnterior = inicioAnterior.withDayOfMonth(minOf(hoy.dayOfMonth, inicioAnterior.lengthOfMonth()))

        val gastoActual = gastosEntre(movimientos, inicioActual, hoy, zona)
        val gastoAnterior = gastosEntre(movimientos, inicioAnterior, finAnterior, zona)
        if (gastoActual.signum() <= 0 || gastoAnterior.signum() <= 0) return null

        return (gastoActual - gastoAnterior).multiply(BigDecimal(100)).divide(gastoAnterior, 0, RoundingMode.HALF_UP).toInt()
    }

    /** Suma de gastos con fecha entre [desde] y [hasta], ambos incluidos. */
    private fun gastosEntre(movimientos: List<MovimientoDashboard>, desde: LocalDate, hasta: LocalDate, zona: ZoneId): BigDecimal =
        CalculosDashboard.suma(movimientos
            .filter { it.tipo == TipoMovimiento.GASTO }
            .filter {
                val fecha = it.fecha.atZone(zona).toLocalDate()
                fecha >= desde && fecha <= hasta
            }
            .map { it.monto })
}

package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** Categoría con más gasto del mes, para el "Consejo inteligente". */
data class MayorGasto(val categoria: String, val monto: Double, val porcentaje: Int)

/** Cálculos del Dashboard (spec 04). Todo sale de los datos reales; nada es de muestra. */
object CalculosDashboard {

    /** Saldo inicial de las cuentas + ingresos − gastos (de todos los movimientos activos). */
    fun balance(datos: DatosDashboard): Double =
        datos.cuentas.sumOf { it.saldoInicial } + total(datos.movimientos, TipoMovimiento.INGRESO) -
            total(datos.movimientos, TipoMovimiento.GASTO)

    fun total(movimientos: List<MovimientoDashboard>, tipo: TipoMovimiento): Double =
        movimientos.filter { it.tipo == tipo }.sumOf { it.monto }

    /** Movimientos del mes calendario de [hoy] (en la zona horaria del teléfono). */
    fun delMes(movimientos: List<MovimientoDashboard>, hoy: LocalDate, zona: ZoneId): List<MovimientoDashboard> =
        movimientos.filter {
            val fecha = it.fecha.atZone(zona).toLocalDate()
            fecha.year == hoy.year && fecha.month == hoy.month
        }

    /**
     * Pastilla "Saludable +X%": (ingresos del mes − gastos del mes) ÷ ingresos del mes × 100,
     * a un decimal. `null` (no se muestra) si no hay ingresos en el mes o el resultado es negativo.
     */
    fun ahorroDelMes(ingresosMes: Double, gastosMes: Double): Double? {
        if (ingresosMes <= 0.0) return null
        val ahorro = BigDecimal((ingresosMes - gastosMes) / ingresosMes * 100).setScale(1, RoundingMode.HALF_UP).toDouble()
        return ahorro.takeIf { it >= 0.0 }
    }

    /** Categoría con más gasto en [gastosDelMes], o `null` si no hubo gastos. */
    fun mayorGasto(gastosDelMes: List<MovimientoDashboard>): MayorGasto? {
        val gastos = gastosDelMes.filter { it.tipo == TipoMovimiento.GASTO }
        val totalGastos = gastos.sumOf { it.monto }
        if (gastos.isEmpty() || totalGastos <= 0.0) return null
        val (categoria, monto) = gastos
            .groupBy { it.nombreCategoria ?: "Sin categoría" }
            .mapValues { (_, lista) -> lista.sumOf { it.monto } }
            .maxBy { it.value }
        val porcentaje = BigDecimal(monto / totalGastos * 100).setScale(0, RoundingMode.HALF_UP).toInt()
        return MayorGasto(categoria, monto, porcentaje)
    }

    /**
     * Puntos de la línea de tendencia: el balance acumulado después de cada movimiento,
     * del más antiguo al más reciente. Con menos de dos puntos la línea no se dibuja.
     */
    fun puntosTendencia(datos: DatosDashboard): List<Double> {
        var acumulado = datos.cuentas.sumOf { it.saldoInicial }
        return datos.movimientos.sortedWith(compareBy({ it.fecha }, { it.id })).map {
            acumulado += if (it.tipo == TipoMovimiento.INGRESO) it.monto else -it.monto
            acumulado
        }
    }

    /** Los últimos [cuantos] movimientos, del más reciente al más antiguo. */
    fun recientes(movimientos: List<MovimientoDashboard>, cuantos: Int = 4): List<MovimientoDashboard> =
        movimientos.sortedWith(compareByDescending<MovimientoDashboard> { it.fecha }.thenByDescending { it.id }).take(cuantos)
}

/** Formatos de la spec 04: `$12,450.00`, `+$4,200.00`, `-$64.50`, "Hoy, 14:32", "12 Mar". */
object FormatosDashboard {

    private val simbolos = DecimalFormatSymbols(Locale.US)
    private val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    fun monto(valor: Double): String {
        val formato = DecimalFormat("#,##0.00", simbolos).apply { roundingMode = RoundingMode.HALF_UP }
        val texto = formato.format(kotlin.math.abs(valor))
        return if (valor < 0 && texto != "0.00") "-$$texto" else "$$texto"
    }

    /** Monto sin decimales, como en la tarjeta de meta ("Llevas $1,800 de $2,500"). */
    fun montoSinDecimales(valor: Double): String {
        val formato = DecimalFormat("#,##0", simbolos).apply { roundingMode = RoundingMode.HALF_UP }
        return "$" + formato.format(valor)
    }

    fun ingreso(valor: Double) = "+" + monto(valor)

    fun gasto(valor: Double) = "-" + monto(valor)

    fun conSigno(movimiento: MovimientoDashboard) =
        if (movimiento.tipo == TipoMovimiento.INGRESO) ingreso(movimiento.monto) else gasto(movimiento.monto)

    /** "4.2" (un decimal, con punto). */
    fun porcentaje(valor: Double): String = DecimalFormat("0.0", simbolos).format(valor)

    /** "Hoy, 14:32", "Ayer, 09:00" o "12 Mar". */
    fun fecha(instante: Instant, ahora: Instant, zona: ZoneId): String {
        val momento = instante.atZone(zona)
        val hoy = ahora.atZone(zona).toLocalDate()
        val hora = "%02d:%02d".format(momento.hour, momento.minute)
        return when (momento.toLocalDate()) {
            hoy -> "Hoy, $hora"
            hoy.minusDays(1) -> "Ayer, $hora"
            else -> "%02d %s".format(momento.dayOfMonth, meses[momento.monthValue - 1])
        }
    }

    /** Solo la primera palabra del nombre ("Mateo Marin" → "Mateo"). */
    fun primerNombre(nombre: String): String = nombre.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
}

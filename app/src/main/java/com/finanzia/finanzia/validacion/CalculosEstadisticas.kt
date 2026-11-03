package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.DatosDashboard
import com.finanzia.finanzia.datos.MetaDashboard
import com.finanzia.finanzia.datos.MovimientoDashboard
import com.finanzia.finanzia.datos.TipoMovimiento
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Opciones del selector de periodo de Estadísticas (spec 11). */
enum class TipoPeriodo { SEMANA, MES, ANIO, RANGO }

/** Un periodo de fechas con ambos extremos incluidos. */
data class Periodo(val tipo: TipoPeriodo, val inicio: LocalDate, val fin: LocalDate) {
    val dias: Long get() = ChronoUnit.DAYS.between(inicio, fin) + 1

    operator fun contains(fecha: LocalDate) = !fecha.isBefore(inicio) && !fecha.isAfter(fin)
}

/** Cómo se agrupan las barras de la gráfica. */
enum class Agrupacion { DIAS, SEMANAS, MESES }

/** Un grupo de la gráfica: dos barras (ingresos y gastos) con las sumas de ese tramo. */
data class GrupoGrafica(
    val etiqueta: String,
    val inicio: LocalDate,
    val fin: LocalDate,
    val ingresos: BigDecimal,
    val gastos: BigDecimal,
    /** El tramo todavía no ha llegado: etiqueta atenuada y sin barras. */
    val futuro: Boolean,
)

enum class SaludFinanciera { EXCELENTE, ESTABLE, NEGATIVA }

/** Categoría con más gasto del periodo y su porcentaje del total de gastos. */
data class CategoriaDestacada(val categoria: String, val porcentaje: Int)

/** Bloque "Recomendación sugerida": solo con meta activa y flujo neto positivo. */
data class Recomendacion(val neto: BigDecimal, val nombreMeta: String, val faltante: BigDecimal)

/** Todo lo que pinta Estadísticas, calculado con los movimientos reales del usuario. */
data class ResumenEstadisticas(
    val periodo: Periodo,
    val anterior: Periodo,
    val agrupacion: Agrupacion,
    val ingresos: BigDecimal,
    val gastos: BigDecimal,
    val neto: BigDecimal,
    /** Pastilla del flujo neto: null = no se muestra. */
    val variacionNeto: BigDecimal?,
    /** Texto verde o rojo bajo Ingresos: null = no se muestra. */
    val variacionIngresos: BigDecimal?,
    /** "[X]% de ingreso": null = no se muestra. */
    val porcentajeDeIngreso: Int?,
    /** Mensaje de salud financiera: null = no se muestra. */
    val salud: SaludFinanciera?,
    /** Pastilla "Prom. $X/día": null = no se muestra. */
    val promedioDiario: BigDecimal?,
    val hayMovimientos: Boolean,
    val grupos: List<GrupoGrafica>,
    /** Grupo resaltado al abrir el periodo. */
    val grupoPorDefecto: Int,
    /** Primera frase del diagnóstico: null = "Aún no hay suficiente información…". */
    val mayorGasto: CategoriaDestacada?,
    /** Segunda frase del diagnóstico: null = no se muestra. */
    val reduccion: CategoriaDestacada?,
    val recomendacion: Recomendacion?,
)

/** Cálculos de Estadísticas (spec 11). Todo el dinero con BigDecimal; nada es de muestra. */
object CalculosEstadisticas {

    private val CIEN = BigDecimal(100)
    private val VEINTE = BigDecimal(20)

    /** Periodo actual según la opción elegida. Las semanas van de lunes a domingo. */
    fun periodo(tipo: TipoPeriodo, hoy: LocalDate, rango: Pair<LocalDate, LocalDate>? = null): Periodo = when (tipo) {
        TipoPeriodo.SEMANA -> {
            val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            Periodo(tipo, lunes, lunes.plusDays(6))
        }
        TipoPeriodo.MES -> Periodo(tipo, hoy.withDayOfMonth(1), hoy.with(TemporalAdjusters.lastDayOfMonth()))
        TipoPeriodo.ANIO -> Periodo(tipo, hoy.withDayOfYear(1), hoy.with(TemporalAdjusters.lastDayOfYear()))
        TipoPeriodo.RANGO -> {
            val (desde, hasta) = requireNotNull(rango) { "El rango necesita sus fechas" }
            Periodo(tipo, minOf(desde, hasta), maxOf(desde, hasta))
        }
    }

    /** Periodo con el que se compara: el anterior, o uno inmediatamente anterior de la misma duración (Rango). */
    fun anterior(periodo: Periodo): Periodo = when (periodo.tipo) {
        TipoPeriodo.SEMANA -> Periodo(periodo.tipo, periodo.inicio.minusWeeks(1), periodo.fin.minusWeeks(1))
        TipoPeriodo.MES -> {
            val mes = YearMonth.from(periodo.inicio).minusMonths(1)
            Periodo(periodo.tipo, mes.atDay(1), mes.atEndOfMonth())
        }
        TipoPeriodo.ANIO -> {
            val anio = periodo.inicio.year - 1
            Periodo(periodo.tipo, LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31))
        }
        TipoPeriodo.RANGO -> Periodo(periodo.tipo, periodo.inicio.minusDays(periodo.dias), periodo.inicio.minusDays(1))
    }

    /** Movimientos cuyo campo Fecha (en la zona del teléfono) cae dentro del periodo. */
    fun delPeriodo(movimientos: List<MovimientoDashboard>, periodo: Periodo, zona: ZoneId): List<MovimientoDashboard> =
        movimientos.filter { it.fecha.atZone(zona).toLocalDate() in periodo }

    /** (actual − anterior) ÷ |anterior| × 100, a un decimal. `null` si el anterior es cero (no hay con qué comparar). */
    fun variacion(actual: BigDecimal, anterior: BigDecimal): BigDecimal? {
        if (anterior.signum() == 0) return null
        return (actual - anterior).multiply(CIEN).divide(anterior.abs(), 1, RoundingMode.HALF_UP)
    }

    /** Ahorro = neto ÷ ingresos × 100: 20% o más excelente; de 0% a menos de 20% estable; negativo. Sin ingresos, null. */
    fun salud(ingresos: BigDecimal, gastos: BigDecimal): SaludFinanciera? {
        if (ingresos.signum() <= 0) return null
        val neto = ingresos - gastos
        return when {
            neto.signum() < 0 -> SaludFinanciera.NEGATIVA
            neto.multiply(CIEN) >= ingresos.multiply(VEINTE) -> SaludFinanciera.EXCELENTE
            else -> SaludFinanciera.ESTABLE
        }
    }

    /** Gastos ÷ ingresos × 100, sin decimales. Sin ingresos, null. */
    fun porcentajeDeIngreso(gastos: BigDecimal, ingresos: BigDecimal): Int? {
        if (ingresos.signum() <= 0) return null
        return gastos.multiply(CIEN).divide(ingresos, 0, RoundingMode.HALF_UP).toInt()
    }

    /** Días que cuentan para el promedio: si el periodo está en curso, hasta hoy inclusive. */
    fun diasTranscurridos(periodo: Periodo, hoy: LocalDate): Long = when {
        hoy.isBefore(periodo.inicio) -> 0
        hoy.isAfter(periodo.fin) -> periodo.dias
        else -> ChronoUnit.DAYS.between(periodo.inicio, hoy) + 1
    }

    /** Gastos ÷ días del periodo (transcurridos si está en curso). null si aún no ha pasado ningún día. */
    fun promedioDiario(gastos: BigDecimal, periodo: Periodo, hoy: LocalDate): BigDecimal? {
        val dias = diasTranscurridos(periodo, hoy)
        if (dias <= 0) return null
        return gastos.divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP)
    }

    /** Semana y Rango corto: por días; Mes y Rango medio: por semanas; Año y Rango largo: por meses. */
    fun agrupacion(periodo: Periodo): Agrupacion = when (periodo.tipo) {
        TipoPeriodo.SEMANA -> Agrupacion.DIAS
        TipoPeriodo.MES -> Agrupacion.SEMANAS
        TipoPeriodo.ANIO -> Agrupacion.MESES
        TipoPeriodo.RANGO -> when {
            periodo.dias <= 14 -> Agrupacion.DIAS
            periodo.dias <= 120 -> Agrupacion.SEMANAS
            else -> Agrupacion.MESES
        }
    }

    /** Tramos de la gráfica (sin montos todavía): etiqueta, inicio y fin. */
    fun tramos(periodo: Periodo): List<Triple<String, LocalDate, LocalDate>> = when (periodo.tipo) {
        TipoPeriodo.SEMANA -> (0L..6L).map {
            val dia = periodo.inicio.plusDays(it)
            Triple(FormatosEstadisticas.diaCorto(dia.dayOfWeek), dia, dia)
        }
        // Sem 1 (1 a 7), Sem 2 (8 a 14), Sem 3 (15 a 21), Sem 4 (22 hasta fin de mes).
        TipoPeriodo.MES -> listOf(1 to 7, 8 to 14, 15 to 21, 22 to periodo.fin.dayOfMonth).mapIndexed { i, (desde, hasta) ->
            Triple("Sem ${i + 1}", periodo.inicio.withDayOfMonth(desde), periodo.inicio.withDayOfMonth(hasta))
        }
        TipoPeriodo.ANIO -> (1..12).map {
            val mes = YearMonth.of(periodo.inicio.year, it)
            Triple(FormatosEstadisticas.mesCorto(it), mes.atDay(1), mes.atEndOfMonth())
        }
        TipoPeriodo.RANGO -> when (agrupacion(periodo)) {
            Agrupacion.DIAS -> (0 until periodo.dias).map {
                val dia = periodo.inicio.plusDays(it)
                Triple(dia.dayOfMonth.toString(), dia, dia)
            }
            Agrupacion.SEMANAS -> generateSequence(periodo.inicio) { it.plusDays(7) }
                .takeWhile { !it.isAfter(periodo.fin) }
                .mapIndexed { i, desde -> Triple("Sem ${i + 1}", desde, minOf(desde.plusDays(6), periodo.fin)) }
                .toList()
            Agrupacion.MESES -> {
                val variosAnios = periodo.inicio.year != periodo.fin.year
                generateSequence(YearMonth.from(periodo.inicio)) { it.plusMonths(1) }
                    .takeWhile { !it.isAfter(YearMonth.from(periodo.fin)) }
                    .map { mes ->
                        val etiqueta = FormatosEstadisticas.mesCorto(mes.monthValue) +
                            if (variosAnios) " %02d".format(mes.year % 100) else ""
                        Triple(etiqueta, maxOf(mes.atDay(1), periodo.inicio), minOf(mes.atEndOfMonth(), periodo.fin))
                    }
                    .toList()
            }
        }
    }

    /** Grupos de la gráfica con las sumas de ingresos y gastos de cada tramo. */
    fun grupos(movimientos: List<MovimientoDashboard>, periodo: Periodo, hoy: LocalDate, zona: ZoneId): List<GrupoGrafica> {
        val conFecha = movimientos.map { it to it.fecha.atZone(zona).toLocalDate() }
        return tramos(periodo).map { (etiqueta, desde, hasta) ->
            val delTramo = conFecha.filter { (_, fecha) -> !fecha.isBefore(desde) && !fecha.isAfter(hasta) }.map { it.first }
            GrupoGrafica(
                etiqueta = etiqueta,
                inicio = desde,
                fin = hasta,
                ingresos = CalculosDashboard.total(delTramo, TipoMovimiento.INGRESO),
                gastos = CalculosDashboard.total(delTramo, TipoMovimiento.GASTO),
                futuro = desde.isAfter(hoy),
            )
        }
    }

    /** El grupo que contiene hoy; el último si el periodo ya terminó; el primero si aún no empieza. */
    fun grupoPorDefecto(grupos: List<GrupoGrafica>, hoy: LocalDate): Int {
        val conHoy = grupos.indexOfFirst { !hoy.isBefore(it.inicio) && !hoy.isAfter(it.fin) }
        return when {
            conHoy >= 0 -> conHoy
            grupos.isNotEmpty() && hoy.isAfter(grupos.last().fin) -> grupos.lastIndex
            else -> 0
        }
    }

    private fun gastosPorCategoria(movimientos: List<MovimientoDashboard>): Map<String, BigDecimal> =
        movimientos.filter { it.tipo == TipoMovimiento.GASTO }
            .groupBy { it.nombreCategoria ?: "Sin categoría" }
            .mapValues { (_, lista) -> CalculosDashboard.suma(lista.map { it.monto }) }

    /** Categoría con más gasto y su porcentaje del total (sin decimales). Empate: la primera en orden alfabético. */
    fun mayorGasto(movimientos: List<MovimientoDashboard>): CategoriaDestacada? {
        val porCategoria = gastosPorCategoria(movimientos)
        val total = CalculosDashboard.suma(porCategoria.values.toList())
        if (total.signum() <= 0) return null
        val (categoria, monto) = porCategoria.entries
            .sortedWith(compareByDescending<Map.Entry<String, BigDecimal>> { it.value }.thenBy { it.key })
            .first()
        return CategoriaDestacada(categoria, monto.multiply(CIEN).divide(total, 0, RoundingMode.HALF_UP).toInt())
    }

    /**
     * Categoría con gastos en ambos periodos que más bajó, si bajó al menos 1%.
     * El porcentaje se muestra sin decimales, como el "18%" del diseño.
     */
    fun mayorReduccion(actuales: List<MovimientoDashboard>, anteriores: List<MovimientoDashboard>): CategoriaDestacada? {
        val ahora = gastosPorCategoria(actuales)
        val antes = gastosPorCategoria(anteriores)
        return ahora.mapNotNull { (categoria, monto) ->
            val previo = antes[categoria]?.takeIf { it.signum() > 0 } ?: return@mapNotNull null
            if (monto.signum() <= 0) return@mapNotNull null
            val reduccion = (previo - monto).multiply(CIEN).divide(previo, 4, RoundingMode.HALF_UP)
            (categoria to reduccion).takeIf { reduccion >= BigDecimal.ONE }
        }
            .sortedWith(compareByDescending<Pair<String, BigDecimal>> { it.second }.thenBy { it.first })
            .firstOrNull()
            ?.let { (categoria, reduccion) -> CategoriaDestacada(categoria, reduccion.setScale(0, RoundingMode.HALF_UP).toInt()) }
    }

    /** Solo con meta activa, flujo neto positivo y algo por completar en la meta. */
    fun recomendacion(meta: MetaDashboard?, neto: BigDecimal): Recomendacion? {
        if (meta == null || neto.signum() <= 0) return null
        val faltante = meta.montoObjetivo - meta.aportado
        if (faltante.signum() <= 0) return null
        return Recomendacion(neto, meta.nombre, faltante)
    }

    /** Arma toda la pantalla para el periodo elegido. */
    fun resumir(
        datos: DatosDashboard,
        tipo: TipoPeriodo,
        hoy: LocalDate,
        zona: ZoneId,
        rango: Pair<LocalDate, LocalDate>? = null,
    ): ResumenEstadisticas {
        val periodo = periodo(tipo, hoy, rango)
        val anterior = anterior(periodo)
        val actuales = delPeriodo(datos.movimientos, periodo, zona)
        val previos = delPeriodo(datos.movimientos, anterior, zona)
        val ingresos = CalculosDashboard.total(actuales, TipoMovimiento.INGRESO)
        val gastos = CalculosDashboard.total(actuales, TipoMovimiento.GASTO)
        val neto = ingresos - gastos
        val ingresosAntes = CalculosDashboard.total(previos, TipoMovimiento.INGRESO)
        val netoAntes = ingresosAntes - CalculosDashboard.total(previos, TipoMovimiento.GASTO)
        val grupos = grupos(actuales, periodo, hoy, zona)
        return ResumenEstadisticas(
            periodo = periodo,
            anterior = anterior,
            agrupacion = agrupacion(periodo),
            ingresos = ingresos,
            gastos = gastos,
            neto = neto,
            variacionNeto = if (previos.isEmpty()) null else variacion(neto, netoAntes),
            variacionIngresos = variacion(ingresos, ingresosAntes),
            porcentajeDeIngreso = porcentajeDeIngreso(gastos, ingresos),
            salud = salud(ingresos, gastos),
            promedioDiario = if (actuales.isEmpty()) null else promedioDiario(gastos, periodo, hoy),
            hayMovimientos = actuales.isNotEmpty(),
            grupos = grupos,
            grupoPorDefecto = grupoPorDefecto(grupos, hoy),
            mayorGasto = mayorGasto(actuales),
            reduccion = mayorReduccion(actuales, previos),
            recomendacion = recomendacion(datos.meta, neto),
        )
    }
}

/**
 * Formatos de Estadísticas (spec 11), distintos a los del Dashboard:
 * `$4.200.000` (punto para miles, sin decimales), `+14.2%`, `44%`, "Nov", "Noviembre".
 */
object FormatosEstadisticas {

    private val simbolosMiles = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = '.'; decimalSeparator = ',' }
    private val simbolosPorcentaje = DecimalFormatSymbols(Locale.US)
    private val mesesCortos = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    private val mesesCompletos = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
    )
    private val diasCortos = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    /** "$4.200.000": sin signo, redondeado al peso más cercano. */
    fun monto(valor: BigDecimal): String {
        val pesos = valor.abs().setScale(0, RoundingMode.HALF_UP)
        return "$" + DecimalFormat("#,##0", simbolosMiles).format(pesos)
    }

    /** "+$2.350.000" o "-$120.000"; "$0" sin signo. */
    fun montoConSigno(valor: BigDecimal): String {
        val redondeado = valor.setScale(0, RoundingMode.HALF_UP)
        return when (redondeado.signum()) {
            0 -> monto(redondeado)
            1 -> "+" + monto(redondeado)
            else -> "-" + monto(redondeado)
        }
    }

    /** "+14.2%", "-3.5%" o "0.0%": un decimal, punto decimal y signo explícito. */
    fun variacion(valor: BigDecimal): String {
        val texto = DecimalFormat("0.0", simbolosPorcentaje).apply { roundingMode = RoundingMode.HALF_UP }
            .format(valor.abs().setScale(1, RoundingMode.HALF_UP))
        return when {
            valor.setScale(1, RoundingMode.HALF_UP).signum() > 0 -> "+$texto%"
            valor.setScale(1, RoundingMode.HALF_UP).signum() < 0 -> "-$texto%"
            else -> "$texto%"
        }
    }

    /** "44%": sin signo y sin decimales. */
    fun porcentaje(valor: Int): String = "$valor%"

    fun mesCorto(mes: Int): String = mesesCortos[mes - 1]

    fun mesCompleto(mes: Int): String = mesesCompletos[mes - 1]

    fun diaCorto(dia: DayOfWeek): String = diasCortos[dia.value - 1]

    /** "Mes (Nov 2026)" con el mes y año de hoy. */
    fun opcionMes(hoy: LocalDate): String = "Mes (${mesCorto(hoy.monthValue)} ${hoy.year})"

    /** "01 Oct" */
    private fun fechaCorta(fecha: LocalDate) = "%02d %s".format(fecha.dayOfMonth, mesCorto(fecha.monthValue))

    /** Texto bajo Ingresos: "vs Oct", "vs sem. anterior", "vs 2025" o "vs periodo anterior". */
    fun versus(anterior: Periodo): String = "vs " + when (anterior.tipo) {
        TipoPeriodo.SEMANA -> "sem. anterior"
        TipoPeriodo.MES -> mesCorto(anterior.inicio.monthValue)
        TipoPeriodo.ANIO -> anterior.inicio.year.toString()
        TipoPeriodo.RANGO -> "periodo anterior"
    }

    /** Final de la frase de reducción: "Octubre", "la semana anterior", "2025" o "el periodo anterior". */
    fun comparadoCon(anterior: Periodo): String = when (anterior.tipo) {
        TipoPeriodo.SEMANA -> "la semana anterior"
        TipoPeriodo.MES -> mesCompleto(anterior.inicio.monthValue)
        TipoPeriodo.ANIO -> anterior.inicio.year.toString()
        TipoPeriodo.RANGO -> "el periodo anterior"
    }

    /** "Evolución Diaria", "Evolución Semanal" o "Evolución Mensual". */
    fun tituloGrafica(agrupacion: Agrupacion): String = when (agrupacion) {
        Agrupacion.DIAS -> "Evolución Diaria"
        Agrupacion.SEMANAS -> "Evolución Semanal"
        Agrupacion.MESES -> "Evolución Mensual"
    }

    /** "Ingresos vs. Gastos (Esta semana)", "(Noviembre)", "(2026)" o "(01 Oct – 31 Oct)". */
    fun subtituloGrafica(periodo: Periodo): String = "Ingresos vs. Gastos (" + when (periodo.tipo) {
        TipoPeriodo.SEMANA -> "Esta semana"
        TipoPeriodo.MES -> mesCompleto(periodo.inicio.monthValue)
        TipoPeriodo.ANIO -> periodo.inicio.year.toString()
        TipoPeriodo.RANGO -> "${fechaCorta(periodo.inicio)} – ${fechaCorta(periodo.fin)}"
    } + ")"
}

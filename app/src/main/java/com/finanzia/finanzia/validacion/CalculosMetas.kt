package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.Meta
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Tarjeta "Ahorro Acumulado en Metas" (spec 07). */
data class ResumenMetas(
    val ahorrado: Double,
    val objetivo: Double,
    /** "[X]% completado": null si no hay metas (la pastilla no se muestra). */
    val porcentaje: Int?,
)

/** "A tu ritmo actual alcanzarás la meta "[nombre]" [dias] días antes/después de lo previsto." */
data class ConsejoMetas(val nombreMeta: String, val dias: Long, val antes: Boolean)

/** "Próximo aporte: $[monto] COP [frecuencia] ([fecha])". */
data class ProximoAporte(val monto: Double, val fecha: LocalDate, val frecuencia: FrecuenciaAporte)

/** Cálculos de las pantallas Metas y Nueva Meta (specs 07 y 08), con los datos reales. */
object CalculosMetas {

    /** Ahorrado ÷ objetivo × 100, redondeado a entero, entre 0 y 100. */
    fun porcentaje(ahorrado: Double, objetivo: Double): Int =
        if (objetivo <= 0.0) 0 else (ahorrado / objetivo * 100).roundToInt().coerceIn(0, 100)

    fun falta(meta: Meta): Double = (meta.montoObjetivo - meta.ahorrado).coerceAtLeast(0.0)

    fun resumen(metas: List<Meta>): ResumenMetas {
        val ahorrado = metas.sumOf { it.ahorrado }
        val objetivo = metas.sumOf { it.montoObjetivo }
        return ResumenMetas(ahorrado, objetivo, if (metas.isEmpty()) null else porcentaje(ahorrado, objetivo))
    }

    /**
     * Consejo sin IA (spec 07): entre las metas sin terminar, con fecha límite y al menos dos
     * aportes, se toma la de fecha límite más cercana. Ritmo = ahorrado ÷ días desde el primer
     * aporte; se proyecta cuándo se completa y se compara con la fecha límite.
     * null = "Aún no hay suficiente información…".
     */
    fun consejo(metas: List<Meta>, hoy: LocalDate): ConsejoMetas? {
        val meta = metas
            .filter { it.fechaLimite != null && it.aportes.size >= 2 && falta(it) > 0.0 }
            .minByOrNull { it.fechaLimite!! }
            ?: return null
        val primerAporte = meta.aportes.minOf { it.fecha }
        val dias = ChronoUnit.DAYS.between(primerAporte, hoy).coerceAtLeast(1)
        val ritmoDiario = meta.ahorrado / dias
        if (ritmoDiario <= 0.0) return null
        val diasNecesarios = ceil(falta(meta) / ritmoDiario).toLong()
        val proyectada = hoy.plusDays(diasNecesarios)
        val diferencia = ChronoUnit.DAYS.between(proyectada, meta.fechaLimite!!)
        return ConsejoMetas(meta.nombre, kotlin.math.abs(diferencia), antes = diferencia >= 0)
    }

    /**
     * Fechas de aporte desde hoy (incluido) hasta [hasta]: quincenal el 15 y el 30 de cada mes,
     * mensual el 30. En meses más cortos, el 30 pasa al último día del mes.
     */
    fun fechasDeAporte(frecuencia: FrecuenciaAporte, hoy: LocalDate, hasta: LocalDate): List<LocalDate> {
        val fechas = mutableListOf<LocalDate>()
        var mes = YearMonth.from(hoy)
        val ultimoMes = YearMonth.from(hasta)
        while (!mes.isAfter(ultimoMes)) {
            val dia30 = mes.atDay(minOf(30, mes.lengthOfMonth()))
            val delMes = when (frecuencia) {
                FrecuenciaAporte.QUINCENAL -> listOf(mes.atDay(15), dia30)
                FrecuenciaAporte.MENSUAL -> listOf(dia30)
            }
            fechas += delMes.filter { !it.isBefore(hoy) && !it.isAfter(hasta) }
            mes = mes.plusMonths(1)
        }
        return fechas
    }

    /**
     * Próximo aporte de una meta: lo que falta repartido en las fechas de aporte que quedan hasta
     * la fecha límite. Si la fecha límite ya pasó, todo lo que falta va en la próxima fecha.
     * null = "Próximo aporte: aún no hay datos" (meta sin frecuencia o sin fecha límite).
     */
    fun proximoAporte(meta: Meta, hoy: LocalDate): ProximoAporte? {
        val frecuencia = meta.frecuencia ?: return null
        val limite = meta.fechaLimite ?: return null
        val falta = falta(meta)
        if (falta <= 0.0) return null
        val fechas = fechasDeAporte(frecuencia, hoy, limite)
        val siguiente = fechas.firstOrNull()
            ?: fechasDeAporte(frecuencia, hoy, hoy.plusMonths(2)).first()
        val cuotas = fechas.size.coerceAtLeast(1)
        return ProximoAporte(falta / cuotas, siguiente, frecuencia)
    }

    /** Meses desde el mes actual hasta el elegido (Nueva Meta: "[N] meses restantes"). */
    fun mesesRestantes(hoy: LocalDate, mes: YearMonth): Long =
        ChronoUnit.MONTHS.between(YearMonth.from(hoy), mes).coerceAtLeast(0)

    /** Dos quincenas por mes (spec 08). */
    fun quincenasRestantes(hoy: LocalDate, mes: YearMonth): Long = mesesRestantes(hoy, mes) * 2

    private val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
    )

    /** "Junio 2025". */
    fun mesYAnio(mes: YearMonth): String = "${meses[mes.monthValue - 1]} ${mes.year}"

    /** "15 nov". */
    fun diaYMesCorto(fecha: LocalDate): String =
        "${fecha.dayOfMonth} ${meses[fecha.monthValue - 1].take(3).lowercase()}"
}

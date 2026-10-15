package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.Meta
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Consejo de Finanzia IA (spec 07): cuántos días antes o después se cumple la meta. */
data class ConsejoMeta(val nombreMeta: String, val dias: Long, val antes: Boolean)

/** Cálculos de la pantalla Metas (spec 07), siempre con los datos reales del usuario. */
object CalculosMetas {

    fun falta(meta: Meta): Double = (meta.montoObjetivo - meta.ahorrado).coerceAtLeast(0.0)

    /** Porcentaje de 0 a 100, redondeado a entero. */
    fun porcentaje(ahorrado: Double, objetivo: Double): Int =
        if (objetivo <= 0) 0 else (ahorrado / objetivo * 100).coerceIn(0.0, 100.0).roundToInt()

    fun porcentaje(meta: Meta): Int = porcentaje(meta.ahorrado, meta.montoObjetivo)

    fun totalAhorrado(metas: List<Meta>): Double = metas.sumOf { it.ahorrado }

    fun totalObjetivo(metas: List<Meta>): Double = metas.sumOf { it.montoObjetivo }

    /**
     * Ritmo real de la meta activa con fecha límite más cercana que tenga al menos dos aportes:
     * ahorrado ÷ días desde el primer aporte. Con ese ritmo se proyecta la fecha en que se
     * completa y se compara con la fecha límite. null = no hay datos suficientes.
     */
    fun consejo(metas: List<Meta>, ahora: Instant, zona: ZoneId): ConsejoMeta? {
        val meta = metas
            .filter { it.fechaLimite != null && it.aportes.size >= 2 && falta(it) > 0 }
            .minByOrNull { it.fechaLimite!! } ?: return null
        val primerAporte = meta.aportes.minOf { it.fecha }
        val diasTranscurridos = Duration.between(primerAporte, ahora).toDays()
        if (diasTranscurridos < 1) return null
        val porDia = meta.ahorrado / diasTranscurridos
        if (porDia <= 0) return null
        val hoy = ahora.atZone(zona).toLocalDate()
        val proyectada = hoy.plusDays((falta(meta) / porDia).roundToLong())
        val diferencia = ChronoUnit.DAYS.between(proyectada, meta.fechaLimite)
        return ConsejoMeta(meta.nombre, kotlin.math.abs(diferencia), antes = diferencia >= 0)
    }

    /** "Junio 2025" */
    fun mesYAnio(fecha: LocalDate): String = "${MESES[fecha.monthValue - 1]} ${fecha.year}"

    /** Meses completos desde el mes actual hasta el mes elegido (mínimo 0). */
    fun mesesRestantes(hoy: LocalDate, mes: YearMonth): Long =
        ChronoUnit.MONTHS.between(YearMonth.from(hoy), mes).coerceAtLeast(0)

    /** Dos quincenas por mes (spec 08). */
    fun quincenasRestantes(hoy: LocalDate, mes: YearMonth): Long = mesesRestantes(hoy, mes) * 2

    /** "8 meses restantes • 16 quincenas" o "1 mes restante • 2 quincenas". */
    fun textoRestante(hoy: LocalDate, mes: YearMonth): String {
        val meses = mesesRestantes(hoy, mes)
        val textoMeses = if (meses == 1L) "1 mes restante" else "$meses meses restantes"
        return "$textoMeses • ${quincenasRestantes(hoy, mes)} quincenas"
    }

    val MESES = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
    )
}

package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.CategoriaMeta
import java.time.LocalDate
import java.time.YearMonth

/** Errores de Nueva meta (spec 08). Cada uno se muestra en rojo dentro de su campo. */
object ValidacionNuevaMeta {

    const val MAX_NOMBRE = 60

    const val ERROR_NOMBRE = "Escribe el nombre de tu meta"
    const val ERROR_CATEGORIA = "Elige una categoría"
    const val ERROR_MONTO = "Escribe un monto mayor que 0"
    const val ERROR_FECHA = "Elige una fecha futura"

    fun nombre(texto: String): String? = if (texto.trim().isEmpty()) ERROR_NOMBRE else null

    fun categoria(categoria: CategoriaMeta?): String? = if (categoria == null) ERROR_CATEGORIA else null

    /** [digitos] son solo números, como los guarda [Montos.limpiar]. */
    fun monto(digitos: String): String? = if (Montos.validar(digitos) != null) ERROR_MONTO else null

    /** La fecha debe ser un mes posterior al actual. */
    fun fecha(mes: YearMonth?, hoy: LocalDate): String? =
        if (mes == null || !mes.isAfter(YearMonth.from(hoy))) ERROR_FECHA else null

    /** Montos rápidos de la spec 08 (se suman al monto escrito). */
    val MONTOS_RAPIDOS = listOf(20_000L, 50_000L, 100_000L, 150_000L, 200_000L)

    /** Suma un monto rápido a los dígitos escritos, sin pasar del máximo permitido. */
    fun sumar(digitos: String, valor: Long): String {
        val actual = digitos.toLongOrNull() ?: 0L
        return Montos.limpiar((actual + valor).toString())
    }
}

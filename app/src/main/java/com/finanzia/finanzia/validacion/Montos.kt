package com.finanzia.finanzia.validacion

/** Motivos por los que no se puede guardar un monto. */
enum class ErrorMonto(val mensaje: String) {
    VACIO("Ingresa un monto"),
    CERO("Ingresa un monto mayor a cero"),
}

/**
 * Montos en pesos colombianos, sin decimales. Internamente se guardan solo los dígitos
 * ("18500") y en pantalla se muestran con comas de miles ("18,500").
 */
object Montos {

    /** Máximo de dígitos permitidos (hasta 999,999,999,999). */
    const val MAX_DIGITOS = 12

    /** Deja solo los números, quita ceros a la izquierda y limita la longitud. */
    fun limpiar(entrada: String): String {
        val digitos = entrada.filter { it in '0'..'9' }
        if (digitos.isEmpty()) return ""
        val sinCeros = digitos.trimStart('0').ifEmpty { "0" }
        return sinCeros.take(MAX_DIGITOS)
    }

    /** "18500" → "18,500". Espera solo dígitos. */
    fun formatear(digitos: String): String =
        digitos.reversed().chunked(3).joinToString(",").reversed()

    fun validar(digitos: String): ErrorMonto? = when {
        digitos.isEmpty() -> ErrorMonto.VACIO
        digitos.all { it == '0' } -> ErrorMonto.CERO
        else -> null
    }

    fun aNumero(digitos: String): Long? = if (validar(digitos) == null) digitos.toLong() else null
}

package com.finanzia.finanzia.validacion

import java.math.BigDecimal

/**
 * Regla del saldo (spec 10): ningún movimiento nuevo o editado puede dejar el saldo disponible
 * de la cuenta por debajo de $0.00.
 *
 * Saldo disponible = saldo inicial + ingresos − gastos − aportes a metas (todo lo activo de la cuenta).
 * Todos los cálculos usan BigDecimal: nunca Float ni Double.
 */
object ReglaSaldo {

    sealed interface Resultado {
        /** Se puede guardar. [saldoResultante] es el saldo que queda después del cambio. */
        data class Permitido(val saldoResultante: BigDecimal) : Resultado

        /** El monto no es un número mayor que cero. */
        data object MontoInvalido : Resultado

        /**
         * No alcanza el saldo. [disponible] es lo que la persona tiene disponible para esta
         * operación (en una edición de gasto incluye lo que ese gasto ya había descontado).
         */
        data class SaldoInsuficiente(val disponible: BigDecimal) : Resultado
    }

    fun saldoDisponible(
        saldoInicial: BigDecimal,
        ingresos: BigDecimal,
        gastos: BigDecimal,
        aportes: BigDecimal,
    ): BigDecimal = saldoInicial + ingresos - gastos - aportes

    /** Regla general: el cambio se permite si el saldo queda en cero o más, o si no lo empeora. */
    fun validarCambio(saldoActual: BigDecimal, cambio: BigDecimal, disponible: BigDecimal = saldoActual): Resultado {
        val resultante = saldoActual + cambio
        // "No empeorar" permite corregir cuentas que ya estaban en negativo por pruebas anteriores.
        return if (resultante.signum() >= 0 || cambio.signum() >= 0) {
            Resultado.Permitido(resultante)
        } else {
            Resultado.SaldoInsuficiente(disponible)
        }
    }

    /** Un gasto nuevo o un aporte nuevo a una meta: resta [monto] del saldo. */
    fun validarSalida(saldoActual: BigDecimal, monto: BigDecimal): Resultado {
        if (monto.signum() <= 0) return Resultado.MontoInvalido
        return validarCambio(saldoActual, monto.negate())
    }

    /** Un ingreso nuevo nunca baja el saldo: solo se valida el monto. */
    fun validarEntrada(saldoActual: BigDecimal, monto: BigDecimal): Resultado {
        if (monto.signum() <= 0) return Resultado.MontoInvalido
        return Resultado.Permitido(saldoActual + monto)
    }

    /**
     * Editar un gasto (o un aporte) de [anterior] a [nuevo]. El saldo disponible para esta
     * operación incluye lo que el gasto ya había descontado: saldo actual + anterior.
     */
    fun validarEdicionSalida(saldoActual: BigDecimal, anterior: BigDecimal, nuevo: BigDecimal): Resultado {
        if (nuevo.signum() <= 0) return Resultado.MontoInvalido
        return validarCambio(saldoActual, anterior - nuevo, disponible = saldoActual + anterior)
    }

    /** Editar un ingreso de [anterior] a [nuevo]: bajarlo no puede dejar el saldo en negativo. */
    fun validarEdicionEntrada(saldoActual: BigDecimal, anterior: BigDecimal, nuevo: BigDecimal): Resultado {
        if (nuevo.signum() <= 0) return Resultado.MontoInvalido
        return validarCambio(saldoActual, nuevo - anterior)
    }

    /** Texto del error en rojo del campo del monto. */
    fun mensajeSaldoInsuficiente(disponible: BigDecimal): String =
        "No tienes saldo suficiente. Tu saldo disponible es ${FormatosDashboard.monto(disponible)}."

    /** En el onboarding, además, se pide registrar primero un ingreso (spec 10). */
    const val PISTA_REGISTRAR_INGRESO = "Registra primero un ingreso."
}

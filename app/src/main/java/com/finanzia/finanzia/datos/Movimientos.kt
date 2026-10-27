package com.finanzia.finanzia.datos

import com.finanzia.finanzia.validacion.ReglaSaldo
import java.math.BigDecimal

/** No se guardó porque el saldo disponible quedaría por debajo de $0.00 (spec 10). */
class SaldoInsuficienteException(val disponible: BigDecimal) :
    Exception(ReglaSaldo.mensajeSaldoInsuficiente(disponible))

class MontoInvalidoException : Exception("Ingresa un monto mayor a cero")

class MovimientoNoEncontradoException : Exception("No se encontró este movimiento. Vuelve atrás e inténtalo de nuevo.")

/** Convierte el resultado de la regla en éxito o en el error que muestra la pantalla. */
fun ReglaSaldo.Resultado.exigirPermitido() {
    when (this) {
        is ReglaSaldo.Resultado.Permitido -> Unit
        ReglaSaldo.Resultado.MontoInvalido -> throw MontoInvalidoException()
        is ReglaSaldo.Resultado.SaldoInsuficiente -> throw SaldoInsuficienteException(disponible)
    }
}

/** Editar movimientos (spec 10). El tipo (ingreso o gasto) no se puede cambiar. */
interface MovimientosRepository {
    /**
     * Cambia el monto y la categoría del movimiento [idMovimiento] (un UPDATE sobre el mismo
     * registro). [categoria] es el nombre visible: la frecuencia de un ingreso ("Mensual"…) o la
     * categoría de un gasto ("Comida"…). Falla con [SaldoInsuficienteException] si el saldo
     * quedaría en negativo.
     */
    suspend fun editar(idMovimiento: Long, monto: Long, categoria: String): Result<Unit>
}

/**
 * Versión simulada para tests y Previews: edita los movimientos del [FakeOnboardingRepository],
 * con los mismos ids que les da [DashboardSimulado] (primero los ingresos y luego los gastos).
 */
class MovimientosSimulados(private val onboarding: FakeOnboardingRepository) : MovimientosRepository {

    override suspend fun editar(idMovimiento: Long, monto: Long, categoria: String): Result<Unit> = runCatching {
        val nuevo = BigDecimal.valueOf(monto)
        val saldo = onboarding.saldoDisponible()
        val i = (idMovimiento - 1).toInt()
        if (i in onboarding.ingresos.indices) {
            val actual = onboarding.ingresos[i]
            ReglaSaldo.validarEdicionEntrada(saldo, BigDecimal.valueOf(actual.monto), nuevo).exigirPermitido()
            val frecuencia = FrecuenciaIngreso.entries.firstOrNull { it.nombreVisible == categoria } ?: actual.frecuencia
            onboarding.ingresos[i] = actual.copy(monto = monto, frecuencia = frecuencia)
            return@runCatching
        }
        val j = i - onboarding.ingresos.size
        if (j !in onboarding.gastos.indices) throw MovimientoNoEncontradoException()
        val actual = onboarding.gastos[j]
        ReglaSaldo.validarEdicionSalida(saldo, BigDecimal.valueOf(actual.monto), nuevo).exigirPermitido()
        val cat = CategoriaGasto.entries.firstOrNull { it.nombreVisible == categoria } ?: actual.categoria
        onboarding.gastos[j] = actual.copy(monto = monto, categoria = cat)
    }
}

package com.finanzia.finanzia.datos

/** Guarda el primer ingreso o el primer gasto del onboarding. */
interface OnboardingRepository {
    suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit>
    suspend fun guardarPrimerGasto(gasto: PrimerGasto): Result<Unit>

    /**
     * Ingreso registrado desde el botón "+" del Dashboard: guarda solo el movimiento, en la
     * categoría de su [frecuencia], y **no** cambia la frecuencia del usuario (esa se elige en el
     * onboarding). Por defecto se guarda como un ingreso del onboarding (para los repositorios simulados).
     */
    suspend fun guardarIngreso(monto: Long, descripcion: String, frecuencia: FrecuenciaIngreso): Result<Unit> =
        guardarPrimerIngreso(PrimerIngreso(monto, frecuencia, descripcion))
}

/** Versión simulada: guarda en memoria y se pierde al cerrar la app. */
// TODO(Supabase): guardar en la tabla Movimientos. Antes resolver las "Decisiones pendientes"
//  2 y 3 de la spec 03 (dónde va la frecuencia, qué cuenta y qué categoría usar).
class FakeOnboardingRepository : OnboardingRepository {

    val ingresos = mutableListOf<PrimerIngreso>()
    val gastos = mutableListOf<PrimerGasto>()

    /** Saldo disponible simulado: ingresos − gastos (la cuenta simulada empieza en $0.00). */
    fun saldoDisponible(): java.math.BigDecimal =
        com.finanzia.finanzia.validacion.ReglaSaldo.saldoDisponible(
            saldoInicial = java.math.BigDecimal.ZERO,
            ingresos = ingresos.fold(java.math.BigDecimal.ZERO) { suma, it -> suma + java.math.BigDecimal.valueOf(it.monto) },
            gastos = gastos.fold(java.math.BigDecimal.ZERO) { suma, it -> suma + java.math.BigDecimal.valueOf(it.monto) },
            aportes = java.math.BigDecimal.ZERO,
        )

    override suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit> {
        ingresos += ingreso
        return Result.success(Unit)
    }

    /** Aplica la regla del saldo (spec 10), como la versión real. */
    override suspend fun guardarPrimerGasto(gasto: PrimerGasto): Result<Unit> = runCatching {
        com.finanzia.finanzia.validacion.ReglaSaldo
            .validarSalida(saldoDisponible(), java.math.BigDecimal.valueOf(gasto.monto))
            .exigirPermitido()
        gastos += gasto
    }
}

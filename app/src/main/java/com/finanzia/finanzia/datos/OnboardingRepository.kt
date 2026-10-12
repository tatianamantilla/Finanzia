package com.finanzia.finanzia.datos

/** Guarda el primer ingreso o el primer gasto del onboarding. */
interface OnboardingRepository {
    suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit>
    suspend fun guardarPrimerGasto(gasto: PrimerGasto): Result<Unit>
}

/** Versión simulada: guarda en memoria y se pierde al cerrar la app. */
// TODO(Supabase): guardar en la tabla Movimientos. Antes resolver las "Decisiones pendientes"
//  2 y 3 de la spec 03 (dónde va la frecuencia, qué cuenta y qué categoría usar).
class FakeOnboardingRepository : OnboardingRepository {

    val ingresos = mutableListOf<PrimerIngreso>()
    val gastos = mutableListOf<PrimerGasto>()

    override suspend fun guardarPrimerIngreso(ingreso: PrimerIngreso): Result<Unit> {
        ingresos += ingreso
        return Result.success(Unit)
    }

    override suspend fun guardarPrimerGasto(gasto: PrimerGasto): Result<Unit> {
        gastos += gasto
        return Result.success(Unit)
    }
}

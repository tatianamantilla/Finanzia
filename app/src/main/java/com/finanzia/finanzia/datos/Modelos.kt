package com.finanzia.finanzia.datos

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
)

/** Opciones de la Pantalla 4 (Paso 2 • Ingresos). */
enum class FrecuenciaIngreso { MENSUAL, QUINCENAL, SEMANAL, DIARIO }

/** Opciones de la Pantalla 5 (Paso 2 • Gastos). */
enum class CategoriaGasto { GASTOS_FIJOS, COMIDA, DIVERSION, TRANSPORTE }

data class PrimerIngreso(val monto: Long, val frecuencia: FrecuenciaIngreso)

data class PrimerGasto(val monto: Long, val categoria: CategoriaGasto)

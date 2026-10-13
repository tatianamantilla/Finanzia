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

/** [descripcion] es el nombre del movimiento que se ve en el Dashboard. */
data class PrimerIngreso(
    val monto: Long,
    val frecuencia: FrecuenciaIngreso,
    val descripcion: String = "Primer ingreso",
)

data class PrimerGasto(
    val monto: Long,
    val categoria: CategoriaGasto,
    val descripcion: String = "Primer gasto",
)

/** Nombre de la categoría tal como se guarda en la tabla categorias (el mismo que muestra el diseño). */
val CategoriaGasto.nombreVisible: String
    get() = when (this) {
        CategoriaGasto.GASTOS_FIJOS -> "Gastos Fijos"
        CategoriaGasto.COMIDA -> "Comida"
        CategoriaGasto.DIVERSION -> "Diversión"
        CategoriaGasto.TRANSPORTE -> "Transporte"
    }

/** Categoría donde se guardan los ingresos. */
const val CATEGORIA_INGRESOS = "Ingresos"

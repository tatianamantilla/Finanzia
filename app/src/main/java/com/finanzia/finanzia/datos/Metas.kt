package com.finanzia.finanzia.datos

import java.time.Instant
import java.time.LocalDate

/** Un aporte a una meta (tabla `aportes`). */
data class Aporte(val monto: Double, val fecha: Instant)

/** Una meta activa del usuario con sus aportes (specs 07 y 08). */
data class Meta(
    val id: Long,
    val nombre: String,
    val montoObjetivo: Double,
    /** Campo `fecha_limite`; null si la meta no tiene fecha. */
    val fechaLimite: LocalDate?,
    val fechaCreacion: Instant,
    val aportes: List<Aporte>,
) {
    val ahorrado: Double get() = aportes.sumOf { it.monto }
}

/** Categorías de la pantalla Nueva meta, en el orden del diseño. */
enum class CategoriaMeta(val nombreVisible: String) {
    VIAJES("Viajes & Ocio"),
    EMERGENCIA("Emergencia"),
    TECNOLOGIA("Tecnología"),
    VEHICULO("Vehículo"),
    VIVIENDA("Vivienda"),
    EDUCACION("Educación"),
}

enum class FrecuenciaAporte(val nombreVisible: String) {
    QUINCENAL("Quincenal (15 y 30)"),
    MENSUAL("Mensual (Día 30)"),
}

/** Lo que el usuario llena en Nueva meta. */
data class NuevaMeta(
    val nombre: String,
    val montoObjetivo: Long,
    val fechaLimite: LocalDate,
    // TODO(Supabase): la categoría y la frecuencia no tienen columna en `metas` todavía
    // (spec 08, decisiones pendientes 1 y 2). Se piden en la pantalla pero no se guardan.
    val categoria: CategoriaMeta,
    val frecuencia: FrecuenciaAporte,
)

/** Lee y crea las metas del usuario que inició sesión. */
interface MetasRepository {
    /** Metas activas, de la más reciente a la más antigua. */
    suspend fun cargar(): Result<List<Meta>>

    suspend fun crear(meta: NuevaMeta): Result<Unit>
}

/** Versión simulada en memoria para tests y Previews. */
class MetasSimulado(
    metasIniciales: List<Meta> = emptyList(),
    private val ahora: () -> Instant = Instant::now,
) : MetasRepository {

    private val metas = metasIniciales.toMutableList()

    /** Lo último que se pidió crear (para que los tests lo revisen). */
    var ultimaCreada: NuevaMeta? = null
        private set

    /** Si no es null, [cargar] y [crear] fallan con este error. */
    var error: Throwable? = null

    override suspend fun cargar(): Result<List<Meta>> {
        error?.let { return Result.failure(it) }
        return Result.success(metas.sortedByDescending { it.fechaCreacion })
    }

    override suspend fun crear(meta: NuevaMeta): Result<Unit> {
        error?.let { return Result.failure(it) }
        ultimaCreada = meta
        metas += Meta(
            id = (metas.maxOfOrNull { it.id } ?: 0) + 1,
            nombre = meta.nombre,
            montoObjetivo = meta.montoObjetivo.toDouble(),
            fechaLimite = meta.fechaLimite,
            fechaCreacion = ahora(),
            aportes = emptyList(),
        )
        return Result.success(Unit)
    }
}

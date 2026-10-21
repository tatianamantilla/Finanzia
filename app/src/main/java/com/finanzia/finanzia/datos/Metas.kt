package com.finanzia.finanzia.datos

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * Las seis categorías fijas de la pantalla Nueva Meta (spec 08).
 * [valor] es lo que se guarda en metas.categoria (docs/supabase/07-metas-categoria-frecuencia.sql).
 */
enum class CategoriaMeta(val valor: String, val nombre: String) {
    VIAJES_OCIO("viajes_ocio", "Viajes & Ocio"),
    EMERGENCIA("emergencia", "Emergencia"),
    TECNOLOGIA("tecnologia", "Tecnología"),
    VEHICULO("vehiculo", "Vehículo"),
    VIVIENDA("vivienda", "Vivienda"),
    EDUCACION("educacion", "Educación"),
    ;

    companion object {
        fun desde(valor: String?): CategoriaMeta? = entries.firstOrNull { it.valor == valor }
    }
}

/** Cada cuánto se aporta a la meta. [valor] es lo que se guarda en metas.frecuencia_aporte. */
enum class FrecuenciaAporte(val valor: String, val opcion: String, val texto: String) {
    QUINCENAL("quincenal", "Quincenal (15 y 30)", "quincenal"),
    MENSUAL("mensual", "Mensual (Día 30)", "mensual"),
    ;

    companion object {
        fun desde(valor: String?): FrecuenciaAporte? = entries.firstOrNull { it.valor == valor }
    }
}

data class AporteMeta(val monto: Double, val fecha: LocalDate)

/** Una meta activa del usuario con sus aportes. */
data class Meta(
    val id: Long,
    val nombre: String,
    val montoObjetivo: Double,
    val fechaLimite: LocalDate?,
    val fechaCreacion: Instant,
    /** null en las metas creadas antes de existir la columna: "Sin categoría". */
    val categoria: CategoriaMeta?,
    /** null en las metas creadas antes de existir la columna: "Próximo aporte: aún no hay datos". */
    val frecuencia: FrecuenciaAporte?,
    val aportes: List<AporteMeta>,
) {
    val ahorrado: Double get() = aportes.sumOf { it.monto }
}

/** Lo que el usuario llena en la pantalla Nueva Meta. */
data class NuevaMeta(
    val nombre: String,
    val categoria: CategoriaMeta,
    val montoObjetivo: Long,
    /** Se guarda como el último día de este mes. */
    val mesLimite: YearMonth,
    val frecuencia: FrecuenciaAporte,
)

/** Metas del usuario que inició sesión. */
interface MetasRepository {
    /** Metas activas, de la más reciente a la más antigua. */
    suspend fun cargar(): Result<List<Meta>>

    suspend fun crear(meta: NuevaMeta): Result<Unit>
}

/** Versión simulada para tests y Previews: guarda en memoria. */
class MetasSimuladas(
    iniciales: List<Meta> = emptyList(),
    private val ahora: () -> Instant = Instant::now,
) : MetasRepository {
    private val metas = iniciales.toMutableList()

    /** Si no es null, [cargar] y [crear] fallan con este error (para probar los estados de error). */
    var error: Throwable? = null

    override suspend fun cargar(): Result<List<Meta>> {
        error?.let { return Result.failure(it) }
        return Result.success(metas.sortedByDescending { it.fechaCreacion })
    }

    override suspend fun crear(meta: NuevaMeta): Result<Unit> {
        error?.let { return Result.failure(it) }
        metas += Meta(
            id = (metas.maxOfOrNull { it.id } ?: 0) + 1,
            nombre = meta.nombre,
            montoObjetivo = meta.montoObjetivo.toDouble(),
            fechaLimite = meta.mesLimite.atEndOfMonth(),
            fechaCreacion = ahora(),
            categoria = meta.categoria,
            frecuencia = meta.frecuencia,
            aportes = emptyList(),
        )
        return Result.success(Unit)
    }
}

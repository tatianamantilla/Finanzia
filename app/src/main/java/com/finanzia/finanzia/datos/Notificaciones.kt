package com.finanzia.finanzia.datos

import java.time.Instant

/** Los filtros de la pantalla de Notificaciones (spec 05). "Todas" es `null`. */
enum class TipoNotificacion(val filtro: String) {
    PRESUPUESTO("Alertas de presupuesto"),
    PAGO("Recordatorios de pago"),
    SEGURIDAD_IA("Seguridad e IA"),
}

/**
 * Tipos de aviso que crea Supabase (columna `tipo` de la tabla notificaciones,
 * docs/supabase/05-notificaciones.sql) y el filtro donde aparece cada uno.
 */
enum class ClaseNotificacion(val valor: String, val filtro: TipoNotificacion) {
    GASTO_MAYOR("gasto_mayor", TipoNotificacion.PRESUPUESTO),
    GASTO_INUSUAL("gasto_inusual", TipoNotificacion.PRESUPUESTO),
    GASTOS_SUPERAN_INGRESOS("gastos_superan_ingresos", TipoNotificacion.PRESUPUESTO),
    SALDO_BAJO("saldo_bajo", TipoNotificacion.PRESUPUESTO),
    REGISTRAR_MOVIMIENTOS("registrar_movimientos", TipoNotificacion.PAGO),
    APORTE_META("aporte_meta", TipoNotificacion.PAGO),
    DIA_INGRESO("dia_ingreso", TipoNotificacion.PAGO),
    RESUMEN_MES("resumen_mes", TipoNotificacion.PAGO),
    AVANCE_META("avance_meta", TipoNotificacion.PAGO),
    META_CUMPLIDA("meta_cumplida", TipoNotificacion.PAGO),
    INICIO_SESION("inicio_sesion", TipoNotificacion.SEGURIDAD_IA),
    CONTRASENA_CAMBIADA("contrasena_cambiada", TipoNotificacion.SEGURIDAD_IA),
    CONSEJO_IA("consejo_ia", TipoNotificacion.SEGURIDAD_IA),
    ;

    companion object {
        /** null si Supabase manda un tipo que esta versión de la app no conoce. */
        fun desde(valor: String): ClaseNotificacion? = entries.firstOrNull { it.valor == valor }
    }
}

/** Una notificación del usuario, con los textos ya armados por Supabase. */
data class Notificacion(
    val id: Long,
    val clase: ClaseNotificacion,
    val titulo: String,
    val mensaje: String,
    val fecha: Instant,
    val leida: Boolean,
    /** Solo en avances de metas: de 0 a 1, para la barra de progreso. */
    val progreso: Double? = null,
) {
    val tipo: TipoNotificacion get() = clase.filtro
}

/** Acceso a las notificaciones del usuario que inició sesión. */
interface NotificacionesRepository {
    suspend fun cargar(): Result<List<Notificacion>>
}

/** Sin notificaciones (para tests): la pantalla muestra el estado vacío. */
class NotificacionesSinDatos : NotificacionesRepository {
    override suspend fun cargar(): Result<List<Notificacion>> = Result.success(emptyList())
}

/** Versión de prueba: devuelve lo que se le indique (para tests y Previews). */
class NotificacionesFijas(private val resultado: Result<List<Notificacion>>) : NotificacionesRepository {
    override suspend fun cargar(): Result<List<Notificacion>> = resultado
}

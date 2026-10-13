package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.BuildConfig

/**
 * Datos de conexión a Supabase. Vienen de local.properties a través de BuildConfig;
 * nunca se escriben en el código. La compilación falla si faltan (ver app/build.gradle.kts).
 */
object ConfigSupabase {
    val url: String = BuildConfig.SUPABASE_URL

    /** Clave publishable (sb_publishable_…): es pública y la protegen las políticas RLS. */
    val clavePublica: String = BuildConfig.SUPABASE_ANON_KEY
}

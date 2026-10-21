package com.finanzia.finanzia.datos

import com.finanzia.finanzia.datos.supabase.SupabaseAuthRepository
import com.finanzia.finanzia.datos.supabase.SupabaseDashboardRepository
import com.finanzia.finanzia.datos.supabase.SupabaseNotificacionesRepository
import com.finanzia.finanzia.datos.supabase.SupabaseOnboardingRepository

/**
 * Repositorios que usa la app al ejecutarse: los reales, conectados a Supabase.
 * Los tests crean los suyos con las versiones simuladas (FakeAuthRepository, FakeOnboardingRepository).
 */
object Repositorios {
    val auth: AuthRepository by lazy { SupabaseAuthRepository() }
    val onboarding: OnboardingRepository by lazy { SupabaseOnboardingRepository() }
    val dashboard: DashboardRepository by lazy { SupabaseDashboardRepository() }
    val notificaciones: NotificacionesRepository by lazy { SupabaseNotificacionesRepository() }
}

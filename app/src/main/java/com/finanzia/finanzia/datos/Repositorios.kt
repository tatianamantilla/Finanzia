package com.finanzia.finanzia.datos

/** Repositorios que usa la app al ejecutarse. Los tests crean los suyos. */
object Repositorios {
    // TODO(Supabase): usar las implementaciones reales.
    val auth: AuthRepository = FakeAuthRepository()
    val onboarding: OnboardingRepository = FakeOnboardingRepository()
}

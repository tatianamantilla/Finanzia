package com.finanzia.finanzia.datos

/** Repositorios que usa la app al ejecutarse. Los tests crean los suyos. */
object Repositorios {
    // TODO(Supabase): usar las implementaciones reales y quitar el usuario de prueba.
    val auth: AuthRepository = FakeAuthRepository().apply {
        // Usuario de prueba para entrar en el emulador (los datos simulados se borran al cerrar la app).
        agregarUsuario(nombre = "Paula Gómez", correo = "paula@ejemplo.com", contrasena = "Finanzia2026")
    }
    val onboarding: OnboardingRepository = FakeOnboardingRepository()
}

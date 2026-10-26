package com.finanzia.finanzia.validacion

import com.finanzia.finanzia.datos.DashboardSimulado
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.FrecuenciaIngreso
import com.finanzia.finanzia.datos.PrimerIngreso
import com.finanzia.finanzia.datos.nombreVisible
import com.finanzia.finanzia.ui.onboarding.PrimerIngresoViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.SQLiteMode

/** Cada ingreso se guarda en la categoría de su frecuencia, como los gastos en la suya. */
@RunWith(AndroidJUnit4::class)
// LEGACY: los modos NATIVE de Robolectric (gráficos y SQLite) no cargan en algunos computadores con Windows.
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class CategoriaIngresoTest {

    @Test
    fun cadaFrecuenciaTieneSuCategoria() {
        assertEquals(
            listOf("Mensual", "Quincenal", "Semanal", "Diario"),
            FrecuenciaIngreso.entries.map { it.nombreVisible },
        )
    }

    @Test
    fun elIngresoDelOnboardingQuedaConLaCategoriaDeSuFrecuencia() = runBlocking {
        val auth = FakeAuthRepository()
        auth.crearCuenta("Mateo Marin", "mateo@correo.com", "Clave123!").getOrThrow()
        val onboarding = FakeOnboardingRepository()
        onboarding.guardarPrimerIngreso(PrimerIngreso(2_500_000, FrecuenciaIngreso.QUINCENAL)).getOrThrow()

        val datos = DashboardSimulado(auth, onboarding).cargar().getOrThrow()

        assertEquals("Quincenal", datos.movimientos.single().nombreCategoria)
    }

    @Test
    fun elIngresoDelBotonMasQuedaConLaFrecuenciaElegida() {
        val onboarding = FakeOnboardingRepository()
        val viewModel = PrimerIngresoViewModel(onboarding, desdeDashboard = true)
        viewModel.cambiarMonto("80000")
        viewModel.seleccionar(FrecuenciaIngreso.SEMANAL)
        viewModel.guardar {}
        // guardar lanza una corrutina en el hilo principal: se deja que termine.
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(FrecuenciaIngreso.SEMANAL, onboarding.ingresos.single().frecuencia)
        assertEquals("Ingreso", onboarding.ingresos.single().descripcion)
    }
}

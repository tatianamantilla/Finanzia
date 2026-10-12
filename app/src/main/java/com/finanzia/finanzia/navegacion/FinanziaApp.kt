package com.finanzia.finanzia.navegacion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.ui.Etiquetas
import com.finanzia.finanzia.ui.PantallaProvisional
import com.finanzia.finanzia.ui.onboarding.PrimerGastoScreen
import com.finanzia.finanzia.ui.onboarding.PrimerGastoViewModel
import com.finanzia.finanzia.ui.onboarding.PrimerIngresoScreen
import com.finanzia.finanzia.ui.onboarding.PrimerIngresoViewModel
import com.finanzia.finanzia.ui.onboarding.PrimerosPasosScreen
import com.finanzia.finanzia.ui.registro.CrearCuentaScreen
import com.finanzia.finanzia.ui.registro.CrearCuentaViewModel
import com.finanzia.finanzia.ui.registro.CuentaCreadaScreen
import com.finanzia.finanzia.ui.theme.FinanziaTheme

object Rutas {
    const val CREAR_CUENTA = "crear_cuenta"
    const val CUENTA_CREADA = "cuenta_creada"
    const val PRIMEROS_PASOS = "primeros_pasos"
    const val PRIMER_INGRESO = "primer_ingreso"
    const val PRIMER_GASTO = "primer_gasto"
    const val DASHBOARD = "dashboard"
    const val INICIAR_SESION = "iniciar_sesion"
}

/** Punto de entrada de las pantallas. Recibe los repositorios para que los tests usen los suyos. */
@Composable
fun FinanziaApp(
    auth: AuthRepository,
    onboarding: OnboardingRepository,
    navController: NavHostController = rememberNavController(),
) {
    // Al llegar al Dashboard se borra el historial: "atrás" ya no vuelve al onboarding.
    val irAlDashboard: () -> Unit = {
        navController.navigate(Rutas.DASHBOARD) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    FinanziaTheme {
        NavHost(navController = navController, startDestination = Rutas.CREAR_CUENTA) {
            composable(Rutas.CREAR_CUENTA) {
                CrearCuentaScreen(
                    viewModel = viewModel { CrearCuentaViewModel(auth) },
                    onCuentaCreada = {
                        // La cuenta ya existe: "atrás" no debe volver al formulario.
                        navController.navigate(Rutas.CUENTA_CREADA) {
                            popUpTo(Rutas.CREAR_CUENTA) { inclusive = true }
                        }
                    },
                    onIniciarSesion = { navController.navigate(Rutas.INICIAR_SESION) },
                )
            }
            composable(Rutas.CUENTA_CREADA) {
                CuentaCreadaScreen(
                    correo = auth.usuarioActual?.correo.orEmpty(),
                    onComenzar = { navController.navigate(Rutas.PRIMEROS_PASOS) },
                )
            }
            composable(Rutas.PRIMEROS_PASOS) {
                PrimerosPasosScreen(
                    onPrimerIngreso = { navController.navigate(Rutas.PRIMER_INGRESO) },
                    onPrimerGasto = { navController.navigate(Rutas.PRIMER_GASTO) },
                    onIrAlDashboard = irAlDashboard,
                )
            }
            composable(Rutas.PRIMER_INGRESO) {
                PrimerIngresoScreen(
                    viewModel = viewModel { PrimerIngresoViewModel(onboarding) },
                    onAtras = { navController.popBackStack(Rutas.PRIMEROS_PASOS, inclusive = false) },
                    onTerminar = irAlDashboard,
                )
            }
            composable(Rutas.PRIMER_GASTO) {
                PrimerGastoScreen(
                    viewModel = viewModel { PrimerGastoViewModel(onboarding) },
                    onAtras = { navController.popBackStack(Rutas.PRIMEROS_PASOS, inclusive = false) },
                    onTerminar = irAlDashboard,
                )
            }
            // Pantallas provisionales: no son parte de este módulo.
            composable(Rutas.DASHBOARD) {
                PantallaProvisional("Dashboard", Etiquetas.PANTALLA_DASHBOARD)
            }
            composable(Rutas.INICIAR_SESION) {
                // TODO(Módulo 01): reemplazar por la pantalla de docs/specs/01-inicio-sesion.md.
                PantallaProvisional("Iniciar sesión", Etiquetas.PANTALLA_INICIAR_SESION)
            }
        }
    }
}

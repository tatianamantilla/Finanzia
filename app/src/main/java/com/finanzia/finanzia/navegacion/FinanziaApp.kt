package com.finanzia.finanzia.navegacion

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.DashboardRepository
import com.finanzia.finanzia.datos.DashboardSimulado
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.onboarding.PrimerGastoScreen
import com.finanzia.finanzia.ui.onboarding.PrimerGastoViewModel
import com.finanzia.finanzia.ui.onboarding.PrimerIngresoScreen
import com.finanzia.finanzia.ui.onboarding.PrimerIngresoViewModel
import com.finanzia.finanzia.ui.onboarding.PrimerosPasosScreen
import com.finanzia.finanzia.ui.recuperacion.NuevaContrasenaScreen
import com.finanzia.finanzia.ui.recuperacion.NuevaContrasenaViewModel
import com.finanzia.finanzia.ui.recuperacion.VerificarIdentidadScreen
import com.finanzia.finanzia.ui.recuperacion.VerificarIdentidadViewModel
import com.finanzia.finanzia.ui.registro.CrearCuentaScreen
import com.finanzia.finanzia.ui.registro.CrearCuentaViewModel
import com.finanzia.finanzia.ui.registro.CuentaCreadaScreen
import com.finanzia.finanzia.ui.sesion.AccesoAutorizadoScreen
import com.finanzia.finanzia.ui.sesion.IniciarSesionScreen
import com.finanzia.finanzia.ui.sesion.IniciarSesionViewModel
import com.finanzia.finanzia.ui.sesion.SALDO_SIN_DATO
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.validacion.CalculosDashboard
import com.finanzia.finanzia.validacion.FormatosDashboard

object Rutas {
    const val CREAR_CUENTA = "crear_cuenta"
    const val CUENTA_CREADA = "cuenta_creada"
    const val PRIMEROS_PASOS = "primeros_pasos"
    const val PRIMER_INGRESO = "primer_ingreso"
    const val PRIMER_GASTO = "primer_gasto"
    const val DASHBOARD = "dashboard"
    const val INICIAR_SESION = "iniciar_sesion"
    const val ACCESO_AUTORIZADO = "acceso_autorizado"
    const val OLVIDE_CONTRASENA = "olvide_contrasena"
    const val ARG_CORREO = "correo"
    const val NUEVA_CONTRASENA = "nueva_contrasena/{$ARG_CORREO}"

    /** Ruta del Paso 2 con el correo ya verificado en el Paso 1. */
    fun nuevaContrasena(correo: String) = "nueva_contrasena/${Uri.encode(correo)}"
}

/** Punto de entrada de las pantallas. Recibe los repositorios para que los tests usen los suyos. */
@Composable
fun FinanziaApp(
    auth: AuthRepository,
    onboarding: OnboardingRepository,
    dashboard: DashboardRepository = remember(auth, onboarding) { DashboardSimulado(auth, onboarding) },
    navController: NavHostController = rememberNavController(),
) {
    // Al llegar al Dashboard se borra el historial: "atrás" ya no vuelve al onboarding.
    val irAlDashboard: () -> Unit = {
        navController.navigate(Rutas.DASHBOARD) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    // Pantalla inicial: si el teléfono tiene una sesión guardada, "Acceso autorizado"; si no, Crear cuenta.
    var inicio by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(auth) {
        inicio = if (auth.restaurarSesion() != null) Rutas.ACCESO_AUTORIZADO else Rutas.CREAR_CUENTA
    }

    FinanziaTheme {
        val destinoInicial = inicio
        if (destinoInicial == null) {
            // Mientras se revisa la sesión guardada (un instante), solo el fondo de la app.
            Box(Modifier.fillMaxSize().background(FondoPantalla))
            return@FinanziaTheme
        }
        NavHost(navController = navController, startDestination = destinoInicial) {
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
            // Módulo 01 · Inicio de sesión
            composable(Rutas.INICIAR_SESION) {
                IniciarSesionScreen(
                    viewModel = viewModel { IniciarSesionViewModel(auth) },
                    onEntrar = {
                        // Con la sesión iniciada, "atrás" no debe volver al formulario.
                        navController.navigate(Rutas.ACCESO_AUTORIZADO) {
                            popUpTo(Rutas.INICIAR_SESION) { inclusive = true }
                        }
                    },
                    onOlvideContrasena = { navController.navigate(Rutas.OLVIDE_CONTRASENA) },
                    onCrearCuenta = {
                        // Normalmente se llega aquí desde Crear cuenta: se vuelve a esa pantalla en vez de
                        // apilar otra copia. Si no estaba en el historial, se abre y reemplaza a esta.
                        if (!navController.popBackStack(Rutas.CREAR_CUENTA, inclusive = false)) {
                            navController.navigate(Rutas.CREAR_CUENTA) {
                                popUpTo(Rutas.INICIAR_SESION) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable(Rutas.ACCESO_AUTORIZADO) {
                // Saldo real del usuario: el mismo cálculo que el balance del Dashboard
                // (saldo inicial de las cuentas + ingresos − gastos).
                var saldo by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(dashboard) {
                    saldo = dashboard.cargar().getOrNull()
                        ?.let { FormatosDashboard.monto(CalculosDashboard.balance(it)) }
                        ?: SALDO_SIN_DATO
                }
                AccesoAutorizadoScreen(
                    nombre = auth.usuarioActual?.nombre.orEmpty(),
                    saldo = saldo,
                    onContinuar = irAlDashboard,
                )
            }
            // Módulo 02 · Olvidar contraseña
            composable(Rutas.OLVIDE_CONTRASENA) {
                VerificarIdentidadScreen(
                    viewModel = viewModel { VerificarIdentidadViewModel(auth) },
                    onVerificado = { correo -> navController.navigate(Rutas.nuevaContrasena(correo)) },
                    onVolver = { navController.popBackStack(Rutas.INICIAR_SESION, inclusive = false) },
                )
            }
            composable(
                Rutas.NUEVA_CONTRASENA,
                arguments = listOf(navArgument(Rutas.ARG_CORREO) { type = NavType.StringType }),
            ) { entrada ->
                val correo = entrada.arguments?.getString(Rutas.ARG_CORREO).orEmpty()
                NuevaContrasenaScreen(
                    viewModel = viewModel { NuevaContrasenaViewModel(auth, correo) },
                    onAtras = { navController.popBackStack() },
                    onActualizada = {
                        // Con la contraseña cambiada, "atrás" no debe volver al restablecimiento.
                        navController.navigate(Rutas.ACCESO_AUTORIZADO) {
                            popUpTo(Rutas.INICIAR_SESION) { inclusive = true }
                        }
                    },
                )
            }
            // Spec 04 · Dashboard (reemplaza al Dashboard provisional).
            composable(Rutas.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel { DashboardViewModel(dashboard, auth) },
                    onSesionCerrada = {
                        // Sin sesión no se puede volver a ninguna pantalla anterior.
                        navController.navigate(Rutas.INICIAR_SESION) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

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
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.MovimientosRepository
import com.finanzia.finanzia.datos.MovimientosSimulados
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.datos.MetasSimuladas
import com.finanzia.finanzia.datos.NotificacionesRepository
import com.finanzia.finanzia.datos.NotificacionesSinDatos
import com.finanzia.finanzia.datos.OnboardingRepository
import com.finanzia.finanzia.ui.dashboard.DashboardScreen
import com.finanzia.finanzia.ui.dashboard.FilaMovimiento
import com.finanzia.finanzia.ui.dashboard.MovimientosScreen
import com.finanzia.finanzia.ui.dashboard.MovimientosViewModel
import com.finanzia.finanzia.ui.dashboard.DashboardViewModel
import com.finanzia.finanzia.ui.metas.DetalleMetaScreen
import com.finanzia.finanzia.ui.metas.DetalleMetaViewModel
import com.finanzia.finanzia.ui.metas.MetasScreen
import com.finanzia.finanzia.ui.metas.MetasViewModel
import com.finanzia.finanzia.ui.metas.NuevaMetaScreen
import com.finanzia.finanzia.ui.metas.NuevaMetaViewModel
import com.finanzia.finanzia.ui.notificaciones.NotificacionesScreen
import com.finanzia.finanzia.ui.notificaciones.NotificacionesViewModel
import com.finanzia.finanzia.ui.onboarding.EdicionMovimiento
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
import com.finanzia.finanzia.ui.theme.FinanziaTheme
import com.finanzia.finanzia.ui.theme.FondoPantalla
import com.finanzia.finanzia.validacion.CalculosAcceso
import com.finanzia.finanzia.validacion.ResumenAcceso
import java.time.Instant
import java.time.ZoneId

object Rutas {
    const val CREAR_CUENTA = "crear_cuenta"
    const val CUENTA_CREADA = "cuenta_creada"
    const val PRIMEROS_PASOS = "primeros_pasos"
    const val PRIMER_INGRESO = "primer_ingreso"
    const val PRIMER_GASTO = "primer_gasto"
    const val DASHBOARD = "dashboard"
    const val NUEVO_INGRESO = "nuevo_ingreso"
    const val NUEVO_GASTO = "nuevo_gasto"
    const val NOTIFICACIONES = "notificaciones"
    const val METAS = "metas"
    const val NUEVA_META = "nueva_meta"
    const val ARG_ID_META = "idMeta"
    const val DETALLE_META = "detalle_meta/{$ARG_ID_META}"
    const val INICIAR_SESION = "iniciar_sesion"
    const val ACCESO_AUTORIZADO = "acceso_autorizado"
    const val OLVIDE_CONTRASENA = "olvide_contrasena"
    const val ARG_CORREO = "correo"
    const val NUEVA_CONTRASENA = "nueva_contrasena/{$ARG_CORREO}"

    /** Ruta del Paso 2 con el correo ya verificado en el Paso 1. */
    fun nuevaContrasena(correo: String) = "nueva_contrasena/${Uri.encode(correo)}"

    /** Ruta del detalle de una meta (spec 09). */
    fun detalleMeta(idMeta: Long) = "detalle_meta/$idMeta"

    // Spec 10 · Lista de movimientos y edición.
    const val MOVIMIENTOS = "movimientos"
    const val ARG_ID_MOVIMIENTO = "idMovimiento"
    const val EDITAR_INGRESO = "editar_ingreso/{$ARG_ID_MOVIMIENTO}"
    const val EDITAR_GASTO = "editar_gasto/{$ARG_ID_MOVIMIENTO}"

    fun editarIngreso(id: Long) = "editar_ingreso/$id"
    fun editarGasto(id: Long) = "editar_gasto/$id"
}

/** Punto de entrada de las pantallas. Recibe los repositorios para que los tests usen los suyos. */
@Composable
fun FinanziaApp(
    auth: AuthRepository,
    onboarding: OnboardingRepository,
    dashboard: DashboardRepository = remember(auth, onboarding) { DashboardSimulado(auth, onboarding) },
    // La app real pasa Repositorios.notificaciones (Supabase); los tests usan los simulados.
    notificaciones: NotificacionesRepository = remember { NotificacionesSinDatos() },
    // La app real pasa Repositorios.metas (Supabase); los tests usan la versión en memoria.
    metas: MetasRepository = remember { MetasSimuladas() },
    // La app real pasa Repositorios.movimientos (Supabase); los tests editan el onboarding simulado.
    movimientos: MovimientosRepository = remember(onboarding) {
        (onboarding as? FakeOnboardingRepository)?.let(::MovimientosSimulados) ?: SinEdicionDeMovimientos
    },
    navController: NavHostController = rememberNavController(),
) {
    // Tocar un movimiento abre "Editar ingreso" o "Editar gasto" según su tipo (spec 10).
    val editarMovimiento: (FilaMovimiento) -> Unit = { fila ->
        navController.navigate(if (fila.esIngreso) Rutas.editarIngreso(fila.id) else Rutas.editarGasto(fila.id))
    }
    // Al llegar al Dashboard se borra el historial: "atrás" ya no vuelve al onboarding.
    val irAlDashboard: () -> Unit = {
        navController.navigate(Rutas.DASHBOARD) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    // Pestaña Metas: desde Inicio o Notificaciones. Sin copias repetidas en el historial.
    val irAMetas: () -> Unit = {
        navController.navigate(Rutas.METAS) { launchSingleTop = true }
    }
    // Flecha atrás o "Inicio" desde Notificaciones y Metas: vuelve al Dashboard que ya estaba abierto.
    val volverAlDashboard: () -> Unit = {
        if (!navController.popBackStack(Rutas.DASHBOARD, inclusive = false)) irAlDashboard()
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
                // Saldo, variación del mes e insight con los datos reales del usuario
                // (los mismos movimientos que lee el Dashboard; ver CalculosAcceso).
                var resumen by remember { mutableStateOf<ResumenAcceso?>(null) }
                LaunchedEffect(dashboard) {
                    resumen = dashboard.cargar().fold(
                        onSuccess = { CalculosAcceso.resumir(it, Instant.now(), ZoneId.systemDefault()) },
                        onFailure = { ResumenAcceso.SIN_DATOS },
                    )
                }
                AccesoAutorizadoScreen(
                    nombre = auth.usuarioActual?.nombre.orEmpty(),
                    resumen = resumen,
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
                    onIngreso = { navController.navigate(Rutas.NUEVO_INGRESO) },
                    onGasto = { navController.navigate(Rutas.NUEVO_GASTO) },
                    onNotificaciones = { navController.navigate(Rutas.NOTIFICACIONES) },
                    onMetas = irAMetas,
                    onVerTodos = { navController.navigate(Rutas.MOVIMIENTOS) },
                    onEditarMovimiento = editarMovimiento,
                    onSesionCerrada = {
                        // Sin sesión no se puede volver a ninguna pantalla anterior.
                        navController.navigate(Rutas.INICIAR_SESION) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                )
            }
            // Spec 05 · Notificaciones: se abre con la campana; la flecha (o "Inicio") vuelve al Dashboard.
            composable(Rutas.NOTIFICACIONES) {
                NotificacionesScreen(
                    viewModel = viewModel { NotificacionesViewModel(notificaciones) },
                    onAtras = volverAlDashboard,
                    onMetas = irAMetas,
                )
            }
            // Spec 07 · Metas: pestaña Metas de la barra inferior.
            composable(Rutas.METAS) {
                val metasViewModel = viewModel { MetasViewModel(metas) }
                // Al volver del detalle (donde se pueden editar aportes) se leen de nuevo los totales.
                LaunchedEffect(Unit) { metasViewModel.refrescar() }
                MetasScreen(
                    viewModel = metasViewModel,
                    onAtras = volverAlDashboard,
                    onNuevaMeta = { navController.navigate(Rutas.NUEVA_META) },
                    onDetalle = { navController.navigate(Rutas.detalleMeta(it)) },
                )
            }
            // Spec 09 · Detalle de meta: botón "Detalle de meta" de cada tarjeta.
            composable(
                Rutas.DETALLE_META,
                arguments = listOf(navArgument(Rutas.ARG_ID_META) { type = NavType.LongType }),
            ) { entrada ->
                val idMeta = entrada.arguments?.getLong(Rutas.ARG_ID_META) ?: 0L
                DetalleMetaScreen(
                    viewModel = viewModel { DetalleMetaViewModel(metas, idMeta) },
                    onAtras = { navController.popBackStack() },
                )
            }
            // Spec 08 · Nueva meta. Al guardar se abre una pantalla Metas nueva, que vuelve a leer
            // los datos: la meta recién creada aparece de primera.
            composable(Rutas.NUEVA_META) {
                NuevaMetaScreen(
                    viewModel = viewModel { NuevaMetaViewModel(metas) },
                    onAtras = { navController.popBackStack() },
                    onGuardada = {
                        navController.navigate(Rutas.METAS) {
                            popUpTo(Rutas.METAS) { inclusive = true }
                        }
                    },
                )
            }
            // Botón "+" del Dashboard: mismas pantallas del onboarding, en modo "Nuevo ingreso/gasto".
            // Al guardar se vuelve a un Dashboard nuevo, que lee otra vez los datos (saldo y movimientos al día).
            composable(Rutas.NUEVO_INGRESO) {
                PrimerIngresoScreen(
                    viewModel = viewModel { PrimerIngresoViewModel(onboarding, desdeDashboard = true) },
                    desdeDashboard = true,
                    onAtras = { navController.popBackStack() },
                    onTerminar = irAlDashboard,
                )
            }
            composable(Rutas.NUEVO_GASTO) {
                PrimerGastoScreen(
                    viewModel = viewModel { PrimerGastoViewModel(onboarding, desdeDashboard = true) },
                    desdeDashboard = true,
                    onAtras = { navController.popBackStack() },
                    onTerminar = irAlDashboard,
                )
            }
            // Spec 10 · "Ver todos" y edición de movimientos. Al guardar se vuelve a la pantalla
            // anterior, que lee otra vez los datos (balance, lista y consejo al día).
            composable(Rutas.MOVIMIENTOS) {
                MovimientosScreen(
                    viewModel = viewModel { MovimientosViewModel(dashboard) },
                    onAtras = { navController.popBackStack() },
                    onEditarMovimiento = editarMovimiento,
                )
            }
            composable(
                Rutas.EDITAR_INGRESO,
                arguments = listOf(navArgument(Rutas.ARG_ID_MOVIMIENTO) { type = NavType.LongType }),
            ) { entrada ->
                val id = entrada.arguments?.getLong(Rutas.ARG_ID_MOVIMIENTO) ?: 0L
                PrimerIngresoScreen(
                    viewModel = viewModel { PrimerIngresoViewModel(onboarding, edicion = EdicionMovimiento(id, movimientos, dashboard)) },
                    edicion = true,
                    onAtras = { navController.popBackStack() },
                    onTerminar = { navController.popBackStack() },
                )
            }
            composable(
                Rutas.EDITAR_GASTO,
                arguments = listOf(navArgument(Rutas.ARG_ID_MOVIMIENTO) { type = NavType.LongType }),
            ) { entrada ->
                val id = entrada.arguments?.getLong(Rutas.ARG_ID_MOVIMIENTO) ?: 0L
                PrimerGastoScreen(
                    viewModel = viewModel { PrimerGastoViewModel(onboarding, edicion = EdicionMovimiento(id, movimientos, dashboard)) },
                    edicion = true,
                    onAtras = { navController.popBackStack() },
                    onTerminar = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Para un onboarding que no es el simulado ni Supabase: editar no está disponible. */
private object SinEdicionDeMovimientos : MovimientosRepository {
    override suspend fun editar(idMovimiento: Long, monto: Long, categoria: String) =
        Result.failure<Unit>(IllegalStateException("No se pueden editar movimientos en este modo."))
}

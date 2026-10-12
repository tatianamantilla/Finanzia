package com.finanzia.finanzia.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.ui.componentes.AnchoMaximoContenido
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * En un teléfono pequeño (320 × 568 dp) se debe poder llegar, desplazando, al botón principal
 * y a los enlaces de cada pantalla, y ningún texto debe quedar cortado.
 * Se corre con letra normal ([PantallaPequenaTest]) y con letra al 200 % ([PantallaPequenaLetraGrandeTest]).
 */
abstract class PantallasAdaptablesBase(private val escalaLetra: Float) {

    @get:Rule
    val compose = createComposeRule()

    /** Textos que se recortan con "…" a propósito, como en el diseño. */
    private val recortesPermitidos = listOf(
        "Revisa tu bandeja en",
        "Correo verificado:",
        // Los placeholders del Paso 1 de olvidar contraseña ocupan una línea, como un campo de texto;
        // con letra muy grande se recortan con "…".
        "Ej. carlos_mendoza",
        "hola@ejemplo.com",
    )

    protected fun mostrar(contenido: @Composable () -> Unit) {
        compose.setContent {
            val densidad = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(densidad.density, escalaLetra)) {
                FinanziaTheme { contenido() }
            }
        }
    }

    /** Desplaza hasta el elemento y comprueba que queda completo dentro de la pantalla. */
    protected fun alcanzar(nodo: SemanticsNodeInteraction): SemanticsNodeInteraction {
        nodo.performScrollTo()
        val pantalla = compose.onRoot().fetchSemanticsNode().boundsInRoot
        val elemento = nodo.fetchSemanticsNode().boundsInRoot
        val margen = 1f
        assertTrue(
            "El elemento $elemento no queda completo en la pantalla $pantalla",
            elemento.top >= pantalla.top - margen &&
                elemento.bottom <= pantalla.bottom + margen &&
                elemento.left >= pantalla.left - margen &&
                elemento.right <= pantalla.right + margen,
        )
        return nodo
    }

    protected fun texto(valor: String) = compose.onNode(hasText(valor, substring = true))

    protected fun etiqueta(valor: String) = compose.onNode(hasTestTag(valor))

    protected fun sinTextosCortados() {
        compose.waitForIdle()
        val nodos = compose
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
            .fetchSemanticsNodes()
        assertTrue("No se encontraron textos", nodos.isNotEmpty())
        nodos.forEach { nodo ->
            val resultados = mutableListOf<TextLayoutResult>()
            nodo.config[SemanticsActions.GetTextLayoutResult].action?.invoke(resultados)
            val resultado = resultados.firstOrNull() ?: return@forEach
            val contenido = resultado.layoutInput.text.text
            if (recortesPermitidos.any { contenido.startsWith(it) }) return@forEach
            assertFalse("Texto cortado o con una palabra partida: \"$contenido\"", estaCortado(resultado))
        }
    }

    /**
     * Un texto está cortado si alguna línea es más ancha que su caja, si no caben todas sus
     * líneas a lo alto o si terminó en "…". (Se compara el ancho de cada línea y no su posición,
     * porque en los textos centrados la posición se mide respecto al espacio disponible.)
     */
    private fun estaCortado(resultado: TextLayoutResult): Boolean {
        val anchoCaja = resultado.size.width + 1f
        val lineaFuera = (0 until resultado.lineCount).any { linea ->
            resultado.getLineRight(linea) - resultado.getLineLeft(linea) > anchoCaja ||
                resultado.isLineEllipsized(linea)
        }
        return lineaFuera || resultado.didOverflowHeight || partePalabras(resultado)
    }

    /** Una palabra quedó partida si una línea termina en medio de ella (letra seguida de letra). */
    private fun partePalabras(resultado: TextLayoutResult): Boolean {
        val texto = resultado.layoutInput.text.text
        return (0 until resultado.lineCount - 1).any { linea ->
            val fin = resultado.getLineEnd(linea)
            fin in 1 until texto.length && texto[fin - 1].isLetterOrDigit() && texto[fin].isLetterOrDigit()
        }
    }

    /** Control: la comprobación sí detecta una palabra partida (para que no apruebe por error). */
    @Test
    fun laComprobacionDetectaPalabrasPartidas() {
        mostrar {
            Box(Modifier.width(60.dp)) { Text("Supercalifragilístico", fontSize = 16.sp) }
        }
        val fallo = runCatching { sinTextosCortados() }.exceptionOrNull()
        assertTrue("La comprobación no detectó la palabra partida", fallo is AssertionError)
    }

    // --- Pantalla 1 · Crear cuenta ---

    @Test
    fun crearCuenta_seLlegaAlBotonYALosEnlaces() {
        var creada = false
        var iniciarSesion = false
        mostrar {
            CrearCuentaScreen(
                CrearCuentaViewModel(FakeAuthRepository()),
                onCuentaCreada = { creada = true },
                onIniciarSesion = { iniciarSesion = true },
            )
        }
        sinTextosCortados()

        alcanzar(etiqueta(Etiquetas.CAMPO_NOMBRE)).performTextInput("Paula Gómez")
        alcanzar(etiqueta(Etiquetas.CAMPO_CORREO)).performTextInput("paula@ejemplo.com")
        alcanzar(etiqueta(Etiquetas.CAMPO_CONTRASENA)).performTextInput("Finanzia2026")
        alcanzar(texto("Términos de Servicio"))
        alcanzar(etiqueta(Etiquetas.CASILLA_TERMINOS)).performClick()
        alcanzar(texto("Crear cuenta")).performClick()
        alcanzar(texto("Iniciar sesión")).performClick()
        compose.waitForIdle()

        assertTrue("El botón Crear cuenta no respondió", creada)
        assertTrue("El enlace Iniciar sesión no respondió", iniciarSesion)
    }

    // --- Pantalla 2 · Cuenta creada ---

    @Test
    fun cuentaCreada_seLlegaAlBoton() {
        var comenzar = false
        mostrar { CuentaCreadaScreen(correo = "paula.gomez@ejemplo.com", onComenzar = { comenzar = true }) }
        sinTextosCortados()

        alcanzar(texto("Correo de confirmación enviado"))
        alcanzar(texto("Comenzar ahora")).performClick()

        assertTrue(comenzar)
    }

    // --- Pantalla 3 · Primeros pasos ---

    @Test
    fun primerosPasos_seLlegaALosBotonesYAlEnlace() {
        var ingreso = false
        var gasto = false
        var dashboard = false
        mostrar {
            PrimerosPasosScreen(
                onPrimerIngreso = { ingreso = true },
                onPrimerGasto = { gasto = true },
                onIrAlDashboard = { dashboard = true },
            )
        }
        sinTextosCortados()

        alcanzar(texto("Quiero registrar mi primer ingreso")).performClick()
        alcanzar(texto("Quiero registrar mi primer gasto")).performClick()
        alcanzar(texto("Explorar más tarde e ir al Dashboard")).performClick()
        alcanzar(texto("Tus datos financieros viajan cifrados"))

        assertTrue(ingreso)
        assertTrue(gasto)
        assertTrue(dashboard)
    }

    // --- Pantalla 4 · Primer ingreso ---

    @Test
    fun primerIngreso_seLlegaAlBotonYAlEnlace() {
        var atras = false
        var terminado = 0
        mostrar {
            PrimerIngresoScreen(
                PrimerIngresoViewModel(FakeOnboardingRepository()),
                onAtras = { atras = true },
                onTerminar = { terminado++ },
            )
        }
        sinTextosCortados()

        alcanzar(etiqueta(Etiquetas.BOTON_ATRAS)).performClick()
        alcanzar(etiqueta(Etiquetas.CAMPO_MONTO)).performTextInput("18500")
        alcanzar(texto("Diario"))
        alcanzar(texto("Guardar y continuar")).performClick()
        alcanzar(texto("Omitir este paso por ahora")).performClick()
        compose.waitForIdle()

        assertTrue(atras)
        assertEquals("Guardar y Omitir deben llevar al Dashboard", 2, terminado)
    }

    // --- Pantalla 5 · Primer gasto ---

    @Test
    fun primerGasto_seLlegaAlBotonYAlEnlace() {
        var atras = false
        var terminado = 0
        mostrar {
            PrimerGastoScreen(
                PrimerGastoViewModel(FakeOnboardingRepository()),
                onAtras = { atras = true },
                onTerminar = { terminado++ },
            )
        }
        sinTextosCortados()

        alcanzar(etiqueta(Etiquetas.BOTON_ATRAS)).performClick()
        alcanzar(texto("Administrar categorías"))
        alcanzar(etiqueta(Etiquetas.CAMPO_MONTO)).performTextInput("3200")
        alcanzar(texto("Transporte"))
        alcanzar(texto("Guardar gasto y finalizar")).performClick()
        alcanzar(texto("Omitir este paso por ahora")).performClick()
        compose.waitForIdle()

        assertTrue(atras)
        assertEquals("Guardar y Omitir deben llevar al Dashboard", 2, terminado)
    }

    // --- Módulo 01 · Inicio de sesión ---

    @Test
    fun iniciarSesion_seLlegaAlBotonYAlEnlace() {
        var entro = false
        var olvido = false
        val auth = FakeAuthRepository().apply { agregarUsuario("Paula Gómez", "paula@ejemplo.com", "Finanzia2026") }
        mostrar {
            IniciarSesionScreen(
                IniciarSesionViewModel(auth),
                onEntrar = { entro = true },
                onOlvideContrasena = { olvido = true },
            )
        }
        sinTextosCortados()

        alcanzar(etiqueta(Etiquetas.CAMPO_CORREO)).performTextInput("paula@ejemplo.com")
        alcanzar(etiqueta(Etiquetas.CAMPO_CONTRASENA)).performTextInput("Finanzia2026")
        alcanzar(texto("¿Olvidaste tu contraseña?")).performClick()
        alcanzar(texto("Entrar")).performClick()
        compose.waitForIdle()

        assertTrue("El botón Entrar no respondió", entro)
        assertTrue("El enlace ¿Olvidaste tu contraseña? no respondió", olvido)
    }

    @Test
    fun accesoAutorizado_seLlegaAlBotonYAlPie() {
        var continuar = false
        mostrar { AccesoAutorizadoScreen(nombre = "Paula Gómez", onContinuar = { continuar = true }) }
        sinTextosCortados()

        alcanzar(texto("Insight Inteligente de Hoy"))
        alcanzar(texto("Continuar al Dashboard")).performClick()
        alcanzar(texto("Finanzia Mobile App v3.4.1"))

        assertTrue(continuar)
    }

    // --- Módulo 02 · Olvidar contraseña ---

    @Test
    fun verificarIdentidad_seLlegaAlBotonYAlEnlace() {
        var verificado = false
        var volver = 0
        val auth = FakeAuthRepository().apply { agregarUsuario("Paula Gómez", "paula@ejemplo.com", "Finanzia2026") }
        mostrar {
            VerificarIdentidadScreen(
                VerificarIdentidadViewModel(auth),
                onVerificado = { verificado = true },
                onVolver = { volver++ },
            )
        }
        sinTextosCortados()

        alcanzar(etiqueta(Etiquetas.BOTON_ATRAS)).performClick()
        alcanzar(etiqueta(Etiquetas.CAMPO_NOMBRE)).performTextInput("Paula Gómez")
        alcanzar(etiqueta(Etiquetas.CAMPO_CORREO)).performTextInput("paula@ejemplo.com")
        alcanzar(texto("Contactar a soporte de Finanzia"))
        alcanzar(texto("Verificar y continuar")).performClick()
        alcanzar(texto("Volver a Iniciar sesión")).performClick()
        compose.waitForIdle()

        assertTrue("Verificar y continuar no respondió", verificado)
        assertEquals("La flecha y Volver deben regresar al inicio de sesión", 2, volver)
    }

    @Test
    fun nuevaContrasena_seLlegaAlBotonYALaCasilla() {
        var actualizada = false
        var atras = false
        val auth = FakeAuthRepository().apply { agregarUsuario("Paula Gómez", "paula.gomez@ejemplo.com", "Finanzia2026") }
        mostrar {
            NuevaContrasenaScreen(
                NuevaContrasenaViewModel(auth, correo = "paula.gomez@ejemplo.com"),
                onAtras = { atras = true },
                onActualizada = { actualizada = true },
            )
        }
        sinTextosCortados()

        alcanzar(etiqueta(Etiquetas.BOTON_ATRAS)).performClick()
        alcanzar(etiqueta(Etiquetas.CAMPO_CONTRASENA)).performTextInput("Finanzia2026!")
        alcanzar(texto("Al menos 1 símbolo especial"))
        alcanzar(etiqueta(Etiquetas.CAMPO_CONFIRMACION)).performTextInput("Finanzia2026!")
        alcanzar(etiqueta(Etiquetas.CASILLA_CERRAR_SESIONES))
        alcanzar(texto("Actualizar contraseña")).performClick()
        compose.waitForIdle()

        assertTrue(atras)
        assertTrue("Actualizar contraseña no respondió", actualizada)
    }

    @Test
    fun nuevaContrasenaConError_seLlegaATodo() {
        val viewModel = NuevaContrasenaViewModel(FakeAuthRepository(), correo = "paula@ejemplo.com")
        mostrar { NuevaContrasenaScreen(viewModel, onAtras = {}, onActualizada = {}) }

        alcanzar(etiqueta(Etiquetas.CAMPO_CONTRASENA)).performTextInput("Finanzia2026!")
        alcanzar(etiqueta(Etiquetas.CAMPO_CONFIRMACION)).performTextInput("Finanzia2026?")
        alcanzar(texto("Actualizar contraseña")).performClick()
        compose.waitForIdle()
        sinTextosCortados()

        alcanzar(texto("Las contraseñas no coinciden"))
        alcanzar(texto("Un símbolo especial"))
        alcanzar(texto("Ambas contraseñas deben ser idénticas."))
        alcanzar(texto("Mostrar ambas contraseñas")).performClick()
        alcanzar(texto("Actualizar contraseña"))

        assertTrue(viewModel.mostrarNueva && viewModel.mostrarConfirmacion)
    }
}

// GraphicsMode.NATIVE: mide el texto con el motor real de Android (sin esto las medidas son aproximadas).
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h568dp")
class PantallaPequenaTest : PantallasAdaptablesBase(escalaLetra = 1f)

// GraphicsMode.NATIVE: mide el texto con el motor real de Android (sin esto las medidas son aproximadas).
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h568dp")
class PantallaPequenaLetraGrandeTest : PantallasAdaptablesBase(escalaLetra = 2f)

/** En tabletas el contenido se centra con un ancho máximo en vez de estirarse. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w800dp-h1280dp")
class TabletaTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun elContenidoSeCentraConAnchoMaximo() {
        compose.setContent {
            FinanziaTheme { CrearCuentaScreen(CrearCuentaViewModel(FakeAuthRepository()), {}, {}) }
        }
        val pantalla = compose.onRoot().fetchSemanticsNode().boundsInRoot
        val boton = compose.onNode(hasText("Crear cuenta")).fetchSemanticsNode().boundsInRoot
        val densidad = compose.density.density
        val anchoMaximo = AnchoMaximoContenido.value * densidad

        assertTrue("El botón se estiró: ${boton.width}px", boton.width <= anchoMaximo)
        val margenIzquierdo = boton.left - pantalla.left
        val margenDerecho = pantalla.right - boton.right
        assertEquals("El contenido no está centrado", margenIzquierdo, margenDerecho, 2f)
    }
}

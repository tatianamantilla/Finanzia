package com.finanzia.finanzia.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.FakeAuthRepository
import com.finanzia.finanzia.datos.FakeOnboardingRepository
import com.finanzia.finanzia.datos.SinConexionException
import com.finanzia.finanzia.datos.Usuario
import com.finanzia.finanzia.navegacion.FinanziaApp
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Los errores van en rojo dentro de cada campo (sin Snackbar) y se borran al corregir. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class ErroresEnRojoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun abrir(auth: AuthRepository = FakeAuthRepository()) {
        compose.setContent { FinanziaApp(auth = auth, onboarding = FakeOnboardingRepository()) }
        compose.waitForIdle()
    }

    private fun clic(texto: String) {
        compose.onNodeWithText(texto, substring = true).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun escribir(etiqueta: String, texto: String) {
        compose.onNodeWithTag(etiqueta).performScrollTo().performTextInput(texto)
        compose.waitForIdle()
    }

    private fun seVe(texto: String) {
        compose.onNodeWithText(texto, substring = true).assertExists()
    }

    private fun noSeVe(texto: String) {
        compose.onAllNodes(hasText(texto, substring = true)).assertCountEquals(0)
    }

    /** Cuántos mensajes de error (con su ícono de alerta) hay en la pantalla. */
    private fun mensajesDeError(cuantos: Int) {
        compose.onAllNodes(hasContentDescription("Error"), useUnmergedTree = true).assertCountEquals(cuantos)
    }

    @Test
    fun crearCuentaVacioMarcaTodosLosCamposALaVez() {
        abrir()
        clic("Crear cuenta")
        seVe("Ingresa tu nombre completo")
        seVe("Ingresa tu correo electrónico")
        seVe("Ingresa una contraseña")
        seVe("Debes aceptar los Términos de Servicio y la Política de Privacidad")
        mensajesDeError(4)
    }

    @Test
    fun elErrorDesapareceAlCorregirElCampo() {
        abrir()
        clic("Crear cuenta")
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        noSeVe("Ingresa tu nombre completo")
        seVe("Ingresa tu correo electrónico")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        compose.waitForIdle()
        noSeVe("Debes aceptar")
        mensajesDeError(2)
    }

    @Test
    fun contrasenaQueNoCumpleYCorreoInvalido() {
        abrir()
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@")
        escribir(Etiquetas.CAMPO_CONTRASENA, "finanzia2026")
        clic("Crear cuenta")
        seVe("El correo no tiene un formato válido")
        seVe("La contraseña necesita al menos 1 mayúscula")
    }

    @Test
    fun correoYaRegistradoVaEnElCampoDeCorreo() {
        val auth = FakeAuthRepository().apply { agregarUsuario("Otra Persona", "mateo@ejemplo.com", "Finanzia2026") }
        abrir(auth)
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        clic("Crear cuenta")
        seVe("Este correo ya está registrado")
        mensajesDeError(1)
    }

    @Test
    fun sinConexionSeMuestraEnRojoSobreElBoton() {
        abrir(object : AuthRepository by FakeAuthRepository() {
            override suspend fun crearCuenta(nombre: String, correo: String, contrasena: String) =
                Result.failure<Usuario>(SinConexionException())
        })
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        clic("Crear cuenta")
        seVe("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo.")
        seVe("¡Empieza tu viaje!")
    }

    @Test
    fun elBotonMuestraElEstadoDeCargaMientrasEsperaASupabase() {
        val respuesta = CompletableDeferred<Result<Usuario>>()
        abrir(object : AuthRepository by FakeAuthRepository() {
            override suspend fun crearCuenta(nombre: String, correo: String, contrasena: String) = respuesta.await()
        })
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        clic("Crear cuenta")
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, Etiquetas.CARGANDO)).assertExists()
        respuesta.complete(Result.failure(SinConexionException()))
        compose.waitForIdle()
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, Etiquetas.CARGANDO)).assertCountEquals(0)
    }

    @Test
    fun credencialesIncorrectasEnInicioDeSesion() {
        val auth = FakeAuthRepository().apply { agregarUsuario("Mateo Marin", "mateo@ejemplo.com", "Finanzia2026") }
        abrir(auth)
        clic("Iniciar sesión")
        clic("Entrar")
        seVe("Ingresa tu correo electrónico")
        seVe("Ingresa tu contraseña")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Otra2026x")
        clic("Entrar")
        seVe("La contraseña es incorrecta")
        escribir(Etiquetas.CAMPO_CONTRASENA, "1")
        noSeVe("La contraseña es incorrecta")
    }

    @Test
    fun montoVacioEnElPrimerIngresoYSeBorraAlEscribir() {
        abrir()
        escribir(Etiquetas.CAMPO_NOMBRE, "Mateo Marin")
        escribir(Etiquetas.CAMPO_CORREO, "mateo@ejemplo.com")
        escribir(Etiquetas.CAMPO_CONTRASENA, "Finanzia2026")
        compose.onNodeWithTag(Etiquetas.CASILLA_TERMINOS).performScrollTo().performClick()
        clic("Crear cuenta")
        clic("Comenzar ahora")
        clic("Quiero registrar mi primer ingreso")
        clic("Guardar y continuar")
        seVe("Ingresa un monto")
        escribir(Etiquetas.CAMPO_MONTO, "0")
        noSeVe("Ingresa un monto")
        clic("Guardar y continuar")
        seVe("Ingresa un monto mayor a cero")
    }
}

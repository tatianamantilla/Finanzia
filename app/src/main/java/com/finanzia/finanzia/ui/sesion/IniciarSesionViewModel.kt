package com.finanzia.finanzia.ui.sesion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.ContrasenaIncorrectaException
import com.finanzia.finanzia.datos.CorreoNoRegistradoException
import com.finanzia.finanzia.datos.CredencialesIncorrectasException
import com.finanzia.finanzia.validacion.ErrorRegistro
import com.finanzia.finanzia.validacion.ValidacionRegistro
import kotlinx.coroutines.launch

/**
 * Estado y acciones de la Pantalla 1 · Inicio de sesión (spec 01).
 * Cada error se muestra en rojo dentro de su campo (sin Snackbar) y se borra al corregir ese campo.
 */
class IniciarSesionViewModel(private val auth: AuthRepository) : ViewModel() {

    var correo by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var mostrarContrasena by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set

    var errorCorreo by mutableStateOf<String?>(null)
        private set
    var errorContrasena by mutableStateOf<String?>(null)
        private set

    /** Errores que no son de un campo (sin conexión, confirmar el correo…), sobre el botón. */
    var errorGeneral by mutableStateOf<String?>(null)
        private set

    fun cambiarCorreo(valor: String) { correo = valor.trim(); errorCorreo = null; errorGeneral = null }
    fun cambiarContrasena(valor: String) { contrasena = valor; errorContrasena = null; errorGeneral = null }
    fun alternarMostrarContrasena() { mostrarContrasena = !mostrarContrasena }

    fun entrar(alEntrar: () -> Unit) {
        if (enviando) return
        errorCorreo = when {
            correo.isBlank() -> MENSAJE_CORREO_VACIO
            !ValidacionRegistro.esCorreoValido(correo) -> MENSAJE_CORREO_INVALIDO
            else -> null
        }
        errorContrasena = if (contrasena.isEmpty()) MENSAJE_CONTRASENA_VACIA else null
        errorGeneral = null
        if (errorCorreo != null || errorContrasena != null) return

        enviando = true
        viewModelScope.launch {
            auth.iniciarSesion(correo, contrasena)
                .onSuccess { alEntrar() }
                .onFailure { e ->
                    when (e) {
                        is CorreoNoRegistradoException -> errorCorreo = e.message
                        // Supabase no dice cuál de los dos falló: el mensaje va en la contraseña.
                        is ContrasenaIncorrectaException, is CredencialesIncorrectasException -> errorContrasena = e.message
                        else -> errorGeneral = e.message ?: MENSAJE_ERROR
                    }
                }
            enviando = false
        }
    }

    companion object {
        // Los mismos textos que usa Crear cuenta (ErrorRegistro).
        val MENSAJE_CORREO_VACIO = ErrorRegistro.CORREO_VACIO.mensaje
        val MENSAJE_CORREO_INVALIDO = ErrorRegistro.CORREO_INVALIDO.mensaje
        const val MENSAJE_CONTRASENA_VACIA = "Ingresa tu contraseña"
        const val MENSAJE_ERROR = "No se pudo iniciar sesión. Inténtalo de nuevo."
    }
}

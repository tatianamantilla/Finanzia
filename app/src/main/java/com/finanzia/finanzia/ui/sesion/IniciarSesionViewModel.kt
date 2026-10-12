package com.finanzia.finanzia.ui.sesion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.validacion.ErrorRegistro
import com.finanzia.finanzia.validacion.ValidacionRegistro
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Estado y acciones de la Pantalla 1 · Inicio de sesión (spec 01). */
class IniciarSesionViewModel(private val auth: AuthRepository) : ViewModel() {

    var correo by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var mostrarContrasena by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set

    private val canalMensajes = Channel<String>(Channel.BUFFERED)

    /** Mensajes para el Snackbar (campos vacíos o credenciales incorrectas). */
    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    fun cambiarCorreo(valor: String) { correo = valor.trim() }
    fun cambiarContrasena(valor: String) { contrasena = valor }
    fun alternarMostrarContrasena() { mostrarContrasena = !mostrarContrasena }

    fun entrar(alEntrar: () -> Unit) {
        if (enviando) return
        val error = when {
            correo.isBlank() -> MENSAJE_CORREO_VACIO
            !ValidacionRegistro.esCorreoValido(correo) -> MENSAJE_CORREO_INVALIDO
            contrasena.isEmpty() -> MENSAJE_CONTRASENA_VACIA
            else -> null
        }
        if (error != null) {
            canalMensajes.trySend(error)
            return
        }
        enviando = true
        viewModelScope.launch {
            auth.iniciarSesion(correo, contrasena)
                .onSuccess { alEntrar() }
                .onFailure { canalMensajes.send(it.message ?: MENSAJE_ERROR) }
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

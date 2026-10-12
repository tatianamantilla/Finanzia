package com.finanzia.finanzia.ui.registro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.validacion.ReglasContrasena
import com.finanzia.finanzia.validacion.ValidacionRegistro
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Estado y acciones de la Pantalla 1 · Crear cuenta. */
class CrearCuentaViewModel(private val auth: AuthRepository) : ViewModel() {

    var nombre by mutableStateOf("")
        private set
    var correo by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var mostrarContrasena by mutableStateOf(false)
        private set
    var aceptaTerminos by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set

    /** Los tres indicadores se recalculan cada vez que cambia la contraseña. */
    val reglas: ReglasContrasena get() = ValidacionRegistro.reglasContrasena(contrasena)

    private val canalMensajes = Channel<String>(Channel.BUFFERED)

    /** Mensajes para el Snackbar (qué falta o qué salió mal). */
    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    fun cambiarNombre(valor: String) { nombre = valor }
    fun cambiarCorreo(valor: String) { correo = valor.trim() }
    fun cambiarContrasena(valor: String) { contrasena = valor }
    fun alternarMostrarContrasena() { mostrarContrasena = !mostrarContrasena }
    fun cambiarAceptaTerminos(valor: Boolean) { aceptaTerminos = valor }

    fun crearCuenta(alTerminar: () -> Unit) {
        if (enviando) return
        val error = ValidacionRegistro.validar(nombre, correo, contrasena, aceptaTerminos)
        if (error != null) {
            canalMensajes.trySend(error.mensaje)
            return
        }
        enviando = true
        viewModelScope.launch {
            auth.crearCuenta(nombre, correo, contrasena)
                .onSuccess { alTerminar() }
                .onFailure { canalMensajes.send(it.message ?: "No se pudo crear la cuenta. Inténtalo de nuevo.") }
            enviando = false
        }
    }
}

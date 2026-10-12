package com.finanzia.finanzia.ui.recuperacion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.validacion.ErrorRecuperacion
import com.finanzia.finanzia.validacion.RequisitosNuevaContrasena
import com.finanzia.finanzia.validacion.ValidacionNuevaContrasena
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Estado y acciones de la Pantalla 1 · Paso 1 de 2 (spec 02). */
class VerificarIdentidadViewModel(private val auth: AuthRepository) : ViewModel() {

    var nombreUsuario by mutableStateOf("")
        private set
    var correo by mutableStateOf("")
        private set
    var enviando by mutableStateOf(false)
        private set

    private val canalMensajes = Channel<String>(Channel.BUFFERED)

    /** Mensajes para el Snackbar (qué falta o qué está mal). */
    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    fun cambiarNombreUsuario(valor: String) { nombreUsuario = valor }
    fun cambiarCorreo(valor: String) { correo = valor.trim() }

    /** Si los datos son de una cuenta, llama a [alVerificar] con el correo para pasar al Paso 2. */
    fun verificar(alVerificar: (correo: String) -> Unit) {
        if (enviando) return
        val error = ValidacionNuevaContrasena.validarPaso1(nombreUsuario, correo)
        if (error != null) {
            canalMensajes.trySend(error.mensaje)
            return
        }
        enviando = true
        viewModelScope.launch {
            auth.verificarIdentidad(nombreUsuario, correo)
                .onSuccess { alVerificar(correo) }
                .onFailure { canalMensajes.send(it.message ?: "No se pudo verificar tu identidad. Inténtalo de nuevo.") }
            enviando = false
        }
    }
}

/** Estado y acciones de la Pantalla 2 · Paso 2 de 2 y de su estado de error (spec 02). */
class NuevaContrasenaViewModel(
    private val auth: AuthRepository,
    val correo: String,
) : ViewModel() {

    var nuevaContrasena by mutableStateOf("")
        private set
    var confirmacion by mutableStateOf("")
        private set
    var mostrarNueva by mutableStateOf(false)
        private set
    var mostrarConfirmacion by mutableStateOf(false)
        private set

    /** En el diseño la casilla aparece marcada (recomendado). */
    var cerrarOtrasSesiones by mutableStateOf(true)
        private set

    /** `true` después de pulsar "Actualizar contraseña" con dos contraseñas distintas (pantalla de error). */
    var errorNoCoinciden by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set

    /** Los requisitos y la barra de fuerza se recalculan cada vez que cambia la contraseña. */
    val requisitos: RequisitosNuevaContrasena get() = ValidacionNuevaContrasena.requisitos(nuevaContrasena)

    /** En la pantalla de error, el campo de confirmación sigue en rojo mientras no coincida. */
    val coinciden: Boolean get() = nuevaContrasena == confirmacion

    private val canalMensajes = Channel<String>(Channel.BUFFERED)

    /** Mensajes para el Snackbar (contraseña vacía o que no cumple los requisitos). */
    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    fun cambiarNuevaContrasena(valor: String) { nuevaContrasena = valor }
    fun cambiarConfirmacion(valor: String) { confirmacion = valor }
    fun alternarMostrarNueva() { mostrarNueva = !mostrarNueva }
    fun alternarMostrarConfirmacion() { mostrarConfirmacion = !mostrarConfirmacion }
    fun cambiarCerrarOtrasSesiones(valor: Boolean) { cerrarOtrasSesiones = valor }

    /** Botón "Mostrar ambas contraseñas" de la pantalla de error. */
    fun mostrarAmbas() {
        mostrarNueva = true
        mostrarConfirmacion = true
    }

    fun actualizar(alTerminar: () -> Unit) {
        if (enviando) return
        when {
            nuevaContrasena.isEmpty() -> {
                canalMensajes.trySend(ErrorRecuperacion.CONTRASENA_VACIA.mensaje)
                return
            }
            !requisitos.cumpleTodos -> {
                canalMensajes.trySend(ErrorRecuperacion.NO_CUMPLE_REQUISITOS.mensaje)
                return
            }
            !coinciden -> {
                errorNoCoinciden = true
                return
            }
        }
        enviando = true
        viewModelScope.launch {
            auth.restablecerContrasena(correo, nuevaContrasena, cerrarOtrasSesiones)
                .onSuccess { alTerminar() }
                .onFailure { canalMensajes.send(it.message ?: "No se pudo actualizar la contraseña. Inténtalo de nuevo.") }
            enviando = false
        }
    }
}

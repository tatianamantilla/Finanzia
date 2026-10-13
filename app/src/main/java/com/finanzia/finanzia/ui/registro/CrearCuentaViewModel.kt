package com.finanzia.finanzia.ui.registro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AuthRepository
import com.finanzia.finanzia.datos.CorreoYaRegistradoException
import com.finanzia.finanzia.validacion.ErrorRegistro
import com.finanzia.finanzia.validacion.ReglasContrasena
import com.finanzia.finanzia.validacion.ValidacionRegistro
import kotlinx.coroutines.launch

/**
 * Estado y acciones de la Pantalla 1 · Crear cuenta.
 * Cada error se muestra en rojo dentro de su campo (sin Snackbar) y se borra al corregir ese campo.
 */
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

    var errorNombre by mutableStateOf<String?>(null)
        private set
    var errorCorreo by mutableStateOf<String?>(null)
        private set
    var errorContrasena by mutableStateOf<String?>(null)
        private set
    var errorTerminos by mutableStateOf<String?>(null)
        private set

    /** Errores que no son de un campo (sin conexión, confirmar el correo…), sobre el botón. */
    var errorGeneral by mutableStateOf<String?>(null)
        private set

    /** Los tres indicadores se recalculan cada vez que cambia la contraseña. */
    val reglas: ReglasContrasena get() = ValidacionRegistro.reglasContrasena(contrasena)

    fun cambiarNombre(valor: String) { nombre = valor; errorNombre = null; errorGeneral = null }
    fun cambiarCorreo(valor: String) { correo = valor.trim(); errorCorreo = null; errorGeneral = null }
    fun cambiarContrasena(valor: String) { contrasena = valor; errorContrasena = null; errorGeneral = null }
    fun alternarMostrarContrasena() { mostrarContrasena = !mostrarContrasena }
    fun cambiarAceptaTerminos(valor: Boolean) { aceptaTerminos = valor; errorTerminos = null; errorGeneral = null }

    fun crearCuenta(alTerminar: () -> Unit) {
        if (enviando) return
        // Se marcan todos los campos con problema a la vez.
        val errores = ValidacionRegistro.validarCampos(nombre, correo, contrasena, aceptaTerminos)
        errorNombre = errores.firstOrNull { it.campo == CampoRegistro.NOMBRE }?.mensaje
        errorCorreo = errores.firstOrNull { it.campo == CampoRegistro.CORREO }?.mensaje
        errorContrasena = errores.firstOrNull { it.campo == CampoRegistro.CONTRASENA }?.mensaje
        errorTerminos = errores.firstOrNull { it.campo == CampoRegistro.TERMINOS }?.mensaje
        errorGeneral = null
        if (errores.isNotEmpty()) return

        enviando = true
        viewModelScope.launch {
            auth.crearCuenta(nombre, correo, contrasena)
                .onSuccess { alTerminar() }
                .onFailure { e ->
                    when (e) {
                        is CorreoYaRegistradoException -> errorCorreo = e.message
                        else -> errorGeneral = e.message ?: "No se pudo crear la cuenta. Inténtalo de nuevo."
                    }
                }
            enviando = false
        }
    }
}

enum class CampoRegistro { NOMBRE, CORREO, CONTRASENA, TERMINOS }

/** A qué campo pertenece cada error de validación. */
val ErrorRegistro.campo: CampoRegistro
    get() = when (this) {
        ErrorRegistro.NOMBRE_VACIO -> CampoRegistro.NOMBRE
        ErrorRegistro.CORREO_VACIO, ErrorRegistro.CORREO_INVALIDO -> CampoRegistro.CORREO
        ErrorRegistro.CONTRASENA_VACIA, ErrorRegistro.CONTRASENA_CORTA,
        ErrorRegistro.CONTRASENA_SIN_NUMERO, ErrorRegistro.CONTRASENA_SIN_MAYUSCULA -> CampoRegistro.CONTRASENA
        ErrorRegistro.TERMINOS_NO_ACEPTADOS -> CampoRegistro.TERMINOS
    }

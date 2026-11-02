package com.finanzia.finanzia.ui.asistente

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.AsistenteRepository
import com.finanzia.finanzia.datos.MensajeChat
import com.finanzia.finanzia.datos.RolMensaje
import com.finanzia.finanzia.validacion.FormatosDashboard
import kotlinx.coroutines.launch

sealed interface EstadoAsistente {
    data object Cargando : EstadoAsistente
    data class Error(val mensaje: String) : EstadoAsistente
    data object Listo : EstadoAsistente
}

/** Asistente Finanzia IA (spec 11): historial, pregunta y respuesta. */
class AsistenteViewModel(
    private val repositorio: AsistenteRepository,
    nombreUsuario: String,
) : ViewModel() {

    /** Primer nombre para el saludo ("¡Hola, Mateo!"). */
    val primerNombre: String = FormatosDashboard.primerNombre(nombreUsuario)

    var estado by mutableStateOf<EstadoAsistente>(EstadoAsistente.Cargando)
        private set

    /** Conversación en pantalla, del más antiguo al más reciente. */
    val mensajes = mutableStateListOf<MensajeChat>()

    var texto by mutableStateOf("")
        private set

    /** true mientras Finanzia IA responde: se ve "está escribiendo…" y no se puede enviar otra. */
    var respondiendo by mutableStateOf(false)
        private set

    /** Error al enviar, en rojo encima de la barra para escribir. */
    var errorEnvio by mutableStateOf<String?>(null)
        private set

    /** La última pregunta que falló, para "Reintentar". */
    private var preguntaFallida: String? = null

    val puedeEnviar: Boolean get() = texto.isNotBlank() && !respondiendo

    init {
        cargar()
    }

    fun cargar() {
        estado = EstadoAsistente.Cargando
        viewModelScope.launch {
            repositorio.cargarHistorial()
                .onSuccess {
                    mensajes.clear()
                    mensajes.addAll(it)
                    estado = EstadoAsistente.Listo
                }
                .onFailure { estado = EstadoAsistente.Error(it.message ?: "No se pudo cargar la conversación. Inténtalo de nuevo.") }
        }
    }

    fun cambiarTexto(nuevo: String) {
        texto = nuevo.take(MAX_CARACTERES)
        errorEnvio = null
    }

    fun enviar() = enviarPregunta(texto)

    /** Las sugerencias de la tarjeta de bienvenida se envían como pregunta. */
    fun usarSugerencia(sugerencia: String) = enviarPregunta(sugerencia)

    fun reintentar() {
        preguntaFallida?.let(::enviarPregunta)
    }

    private fun enviarPregunta(entrada: String) {
        val pregunta = entrada.trim()
        if (pregunta.isEmpty() || respondiendo) return
        val enviado = MensajeChat(RolMensaje.USUARIO, pregunta)
        mensajes += enviado
        texto = ""
        errorEnvio = null
        preguntaFallida = null
        respondiendo = true
        viewModelScope.launch {
            repositorio.preguntar(pregunta)
                .onSuccess { mensajes += it }
                .onFailure {
                    // La pregunta no se pierde: vuelve al campo y se puede reintentar.
                    mensajes.remove(enviado)
                    texto = pregunta
                    preguntaFallida = pregunta
                    errorEnvio = it.message ?: "No se pudo enviar tu pregunta. Inténtalo de nuevo."
                }
            respondiendo = false
        }
    }

    companion object {
        const val MAX_CARACTERES = 500

        val SUGERENCIAS = listOf(
            "¿En qué gasté más este mes?",
            "¿Cómo voy con mis metas?",
            "¿Cuánto puedo ahorrar este mes?",
        )
    }
}

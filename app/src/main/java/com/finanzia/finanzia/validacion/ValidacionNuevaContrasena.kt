package com.finanzia.finanzia.validacion

/** Los cuatro "Requisitos de robustez" del Paso 2 de olvidar contraseña (spec 02). */
data class RequisitosNuevaContrasena(
    val tieneLongitud: Boolean,
    val tieneNumero: Boolean,
    val tieneMayuscula: Boolean,
    val tieneSimbolo: Boolean,
) {
    /** Cuántos requisitos se cumplen: es el número de segmentos verdes de la barra de fuerza. */
    val cumplidos: Int get() = listOf(tieneLongitud, tieneNumero, tieneMayuscula, tieneSimbolo).count { it }

    val cumpleTodos: Boolean get() = cumplidos == 4
}

/** Motivos por los que no se puede restablecer la contraseña. Cada uno tiene el texto del Snackbar. */
enum class ErrorRecuperacion(val mensaje: String) {
    NOMBRE_VACIO("Ingresa tu nombre de usuario"),
    CORREO_VACIO(ErrorRegistro.CORREO_VACIO.mensaje),
    CORREO_INVALIDO(ErrorRegistro.CORREO_INVALIDO.mensaje),
    CONTRASENA_VACIA("Ingresa tu nueva contraseña"),
    NO_CUMPLE_REQUISITOS("La nueva contraseña no cumple todos los requisitos"),
}

object ValidacionNuevaContrasena {

    fun requisitos(contrasena: String): RequisitosNuevaContrasena {
        val reglas = ValidacionRegistro.reglasContrasena(contrasena)
        return RequisitosNuevaContrasena(
            tieneLongitud = reglas.tieneLongitud,
            tieneNumero = reglas.tieneNumero,
            tieneMayuscula = reglas.tieneMayuscula,
            // Símbolo especial: cualquier carácter que no sea letra, número ni espacio (por ejemplo !@#$).
            tieneSimbolo = contrasena.any { !it.isLetterOrDigit() && !it.isWhitespace() },
        )
    }

    /**
     * Texto verde junto a "Nueva contraseña". El diseño solo define "Seguridad media" (3 de 4) y
     * "Cumple requisitos" (4 de 4); "Seguridad baja" es provisional ("Decisiones pendientes" 4).
     */
    fun textoSeguridad(cumplidos: Int): String? = when (cumplidos) {
        0 -> null
        1, 2 -> "Seguridad baja"
        3 -> "Seguridad media"
        else -> "Cumple requisitos"
    }

    /** Paso 1: devuelve el primer problema del formulario, o `null` si está completo. */
    fun validarPaso1(nombreUsuario: String, correo: String): ErrorRecuperacion? = when {
        nombreUsuario.isBlank() -> ErrorRecuperacion.NOMBRE_VACIO
        correo.isBlank() -> ErrorRecuperacion.CORREO_VACIO
        !ValidacionRegistro.esCorreoValido(correo) -> ErrorRecuperacion.CORREO_INVALIDO
        else -> null
    }

    /** "paula@ejemplo.com" → "p***a@ejemplo.com" (la pantalla lo recorta con "…" si no cabe). */
    fun enmascararCorreo(correo: String): String {
        val partes = correo.trim().split("@", limit = 2)
        if (partes.size < 2) return correo
        val usuario = partes[0]
        val oculto = if (usuario.length <= 2) usuario.take(1) + "***" else "${usuario.first()}***${usuario.last()}"
        return "$oculto@${partes[1]}"
    }
}

package com.finanzia.finanzia.validacion

/** Estado de los tres indicadores de la contraseña ("8+ carac.", "1 número", "1 mayúscula"). */
data class ReglasContrasena(
    val tieneLongitud: Boolean,
    val tieneNumero: Boolean,
    val tieneMayuscula: Boolean,
) {
    val cumpleTodas: Boolean get() = tieneLongitud && tieneNumero && tieneMayuscula
}

/** Motivos por los que no se puede crear la cuenta. Cada uno tiene el texto del Snackbar. */
enum class ErrorRegistro(val mensaje: String) {
    NOMBRE_VACIO("Ingresa tu nombre completo"),
    CORREO_VACIO("Ingresa tu correo electrónico"),
    CORREO_INVALIDO("El correo no tiene un formato válido"),
    CONTRASENA_VACIA("Ingresa una contraseña"),
    CONTRASENA_CORTA("La contraseña necesita mínimo 8 caracteres"),
    CONTRASENA_SIN_NUMERO("La contraseña necesita al menos 1 número"),
    CONTRASENA_SIN_MAYUSCULA("La contraseña necesita al menos 1 mayúscula"),
    TERMINOS_NO_ACEPTADOS("Debes aceptar los Términos de Servicio y la Política de Privacidad"),
}

object ValidacionRegistro {

    const val LONGITUD_MINIMA_CONTRASENA = 8

    private val formatoCorreo = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$""")

    fun esCorreoValido(correo: String): Boolean = formatoCorreo.matches(correo.trim())

    fun reglasContrasena(contrasena: String) = ReglasContrasena(
        tieneLongitud = contrasena.length >= LONGITUD_MINIMA_CONTRASENA,
        tieneNumero = contrasena.any { it.isDigit() },
        tieneMayuscula = contrasena.any { it.isUpperCase() },
    )

    /** Devuelve el primer problema del formulario, o `null` si todo está correcto. */
    fun validar(
        nombre: String,
        correo: String,
        contrasena: String,
        aceptaTerminos: Boolean,
    ): ErrorRegistro? {
        val reglas = reglasContrasena(contrasena)
        return when {
            nombre.isBlank() -> ErrorRegistro.NOMBRE_VACIO
            correo.isBlank() -> ErrorRegistro.CORREO_VACIO
            !esCorreoValido(correo) -> ErrorRegistro.CORREO_INVALIDO
            contrasena.isEmpty() -> ErrorRegistro.CONTRASENA_VACIA
            !reglas.tieneLongitud -> ErrorRegistro.CONTRASENA_CORTA
            !reglas.tieneNumero -> ErrorRegistro.CONTRASENA_SIN_NUMERO
            !reglas.tieneMayuscula -> ErrorRegistro.CONTRASENA_SIN_MAYUSCULA
            !aceptaTerminos -> ErrorRegistro.TERMINOS_NO_ACEPTADOS
            else -> null
        }
    }
}

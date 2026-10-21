package com.finanzia.finanzia.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzia.finanzia.datos.CategoriaMeta
import com.finanzia.finanzia.datos.FrecuenciaAporte
import com.finanzia.finanzia.datos.MetasRepository
import com.finanzia.finanzia.datos.NuevaMeta
import com.finanzia.finanzia.validacion.Montos
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/** Pantalla Nueva Meta (spec 08): formulario, validación y guardado. */
class NuevaMetaViewModel(
    private val repositorio: MetasRepository,
    private val hoy: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    // Al abrir: nombre y monto vacíos, sin categoría ni fecha, frecuencia quincenal (spec 08).
    var nombre by mutableStateOf("")
        private set
    var categoria by mutableStateOf<CategoriaMeta?>(null)
        private set
    /** Solo los dígitos del monto ("3500000"); la pantalla lo muestra con comas de miles. */
    var monto by mutableStateOf("")
        private set
    /** Último monto rápido tocado (se ve en verde). */
    var montoRapido by mutableStateOf<Long?>(null)
        private set
    var mes by mutableStateOf<YearMonth?>(null)
        private set
    var frecuencia by mutableStateOf(FrecuenciaAporte.QUINCENAL)
        private set

    var errorNombre by mutableStateOf<String?>(null)
        private set
    var errorCategoria by mutableStateOf<String?>(null)
        private set
    var errorMonto by mutableStateOf<String?>(null)
        private set
    var errorFecha by mutableStateOf<String?>(null)
        private set
    var errorGeneral by mutableStateOf<String?>(null)
        private set
    var guardando by mutableStateOf(false)
        private set

    val mesActual: YearMonth get() = YearMonth.from(hoy())

    fun cambiarNombre(texto: String) {
        if (texto.trim().length > MAX_NOMBRE) return
        nombre = texto
        errorNombre = null
    }

    fun borrarNombre() = cambiarNombre("")

    fun elegirCategoria(nueva: CategoriaMeta) {
        categoria = nueva
        errorCategoria = null
    }

    fun cambiarMonto(texto: String) {
        monto = Montos.limpiar(texto)
        montoRapido = null
        errorMonto = null
    }

    /** Los montos rápidos se SUMAN al monto escrito (spec 08). */
    fun sumarMonto(valor: Long) {
        val actual = monto.toLongOrNull() ?: 0L
        monto = Montos.limpiar((actual + valor).toString())
        montoRapido = valor
        errorMonto = null
    }

    fun elegirMes(nuevo: YearMonth) {
        mes = nuevo
        errorFecha = null
    }

    fun elegirFrecuencia(nueva: FrecuenciaAporte) {
        frecuencia = nueva
    }

    fun guardar(alGuardar: () -> Unit) {
        if (guardando) return
        val nombreLimpio = nombre.trim()
        errorNombre = if (nombreLimpio.isEmpty()) "Escribe el nombre de tu meta" else null
        errorCategoria = if (categoria == null) "Elige una categoría" else null
        errorMonto = if (Montos.validar(monto) != null) "Escribe un monto mayor que 0" else null
        val mesElegido = mes
        errorFecha = if (mesElegido == null || !mesElegido.isAfter(mesActual)) "Elige una fecha futura" else null
        if (errorNombre != null || errorCategoria != null || errorMonto != null || errorFecha != null) return

        val nueva = NuevaMeta(
            nombre = nombreLimpio,
            categoria = categoria!!,
            montoObjetivo = Montos.aNumero(monto)!!,
            mesLimite = mesElegido!!,
            frecuencia = frecuencia,
        )
        guardando = true
        errorGeneral = null
        viewModelScope.launch {
            repositorio.crear(nueva)
                .onSuccess { alGuardar() }
                .onFailure { errorGeneral = it.message ?: "No se pudo guardar tu meta. Inténtalo de nuevo." }
            guardando = false
        }
    }

    companion object {
        const val MAX_NOMBRE = 60

        /** Montos rápidos (Simplificación aprobada, spec 08). */
        val MONTOS_RAPIDOS = listOf(20_000L, 50_000L, 100_000L, 150_000L, 200_000L)
    }
}

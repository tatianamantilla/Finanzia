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
import com.finanzia.finanzia.validacion.ValidacionNuevaMeta
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/** Nueva meta (spec 08): un solo paso que termina en "Guardar meta". */
class NuevaMetaViewModel(
    private val repositorio: MetasRepository,
    private val reloj: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    var nombre by mutableStateOf("")
        private set
    var categoria by mutableStateOf<CategoriaMeta?>(null)
        private set
    /** Solo dígitos ("3500000"); en pantalla se ve con separadores de miles. */
    var monto by mutableStateOf("")
        private set
    /** El último monto rápido tocado (se ve en verde). */
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

    fun hoy(): LocalDate = reloj()

    fun cambiarNombre(texto: String) {
        nombre = texto.take(ValidacionNuevaMeta.MAX_NOMBRE)
        errorNombre = null
    }

    fun borrarNombre() = cambiarNombre("")

    fun elegirCategoria(nueva: CategoriaMeta) {
        categoria = nueva
        errorCategoria = null
    }

    fun cambiarMonto(entrada: String) {
        monto = Montos.limpiar(entrada)
        montoRapido = null
        errorMonto = null
    }

    fun sumarMonto(valor: Long) {
        monto = ValidacionNuevaMeta.sumar(monto, valor)
        montoRapido = valor
        errorMonto = null
    }

    fun elegirMes(nuevo: YearMonth) {
        mes = nuevo
        errorFecha = null
    }

    fun elegirFrecuencia(nueva: FrecuenciaAporte) { frecuencia = nueva }

    /** Valida todo; si está bien, guarda y llama a [alGuardar]. */
    fun guardar(alGuardar: () -> Unit) {
        if (guardando) return
        errorNombre = ValidacionNuevaMeta.nombre(nombre)
        errorCategoria = ValidacionNuevaMeta.categoria(categoria)
        errorMonto = ValidacionNuevaMeta.monto(monto)
        errorFecha = ValidacionNuevaMeta.fecha(mes, hoy())
        errorGeneral = null
        if (listOf(errorNombre, errorCategoria, errorMonto, errorFecha).any { it != null }) return

        val meta = NuevaMeta(
            nombre = nombre.trim(),
            montoObjetivo = monto.toLong(),
            fechaLimite = mes!!.atEndOfMonth(),
            categoria = categoria!!,
            frecuencia = frecuencia,
        )
        guardando = true
        viewModelScope.launch {
            repositorio.crear(meta)
                .onSuccess { alGuardar() }
                .onFailure { errorGeneral = it.message ?: "No se pudo guardar tu meta. Inténtalo de nuevo." }
            guardando = false
        }
    }
}

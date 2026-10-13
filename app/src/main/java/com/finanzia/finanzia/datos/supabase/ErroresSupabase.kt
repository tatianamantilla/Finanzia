package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.SinConexionException
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/**
 * Ejecuta una operación con Supabase y la convierte en [Result]:
 * - sin internet o tiempo agotado → [SinConexionException] (mensaje claro en español);
 * - si se cancela (por ejemplo, el usuario sale de la pantalla), se deja cancelar de verdad;
 * - cualquier otro error se devuelve tal cual para que la pantalla lo muestre.
 */
internal suspend fun <T> conErroresClaros(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestException) {
        Result.failure(SinConexionException())
    } catch (e: IOException) {
        // Incluye el tiempo agotado de Ktor (HttpRequestTimeoutException).
        Result.failure(SinConexionException())
    } catch (e: Exception) {
        Result.failure(e)
    }

package com.finanzia.finanzia.integracion

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finanzia.finanzia.datos.supabase.ConfigSupabase
import com.finanzia.finanzia.datos.supabase.crearClienteSupabase
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL

/**
 * Paso 1 de la fase 2: la app se conecta a Supabase real.
 * Solo corre con: ./gradlew testDebugUnitTest -Pintegracion (necesita internet y local.properties).
 * No crea ni modifica nada en Supabase.
 */
@RunWith(AndroidJUnit4::class)
class ConexionSupabaseIntegracionTest {

    private fun leerAjustesDeAuth(): String {
        val conexion = URL("${ConfigSupabase.url}/auth/v1/settings").openConnection() as HttpURLConnection
        conexion.setRequestProperty("apikey", ConfigSupabase.clavePublica)
        return try {
            assertEquals("Supabase no respondió bien: revisa SUPABASE_URL y SUPABASE_ANON_KEY", 200, conexion.responseCode)
            conexion.inputStream.bufferedReader().readText()
        } finally {
            conexion.disconnect()
        }
    }

    @Test
    fun laUrlYLaClavePublicaConectanConSupabase() {
        val ajustes = Json.parseToJsonElement(leerAjustesDeAuth()).jsonObject
        assertTrue("El registro con correo está desactivado en Supabase", ajustes["external"]!!.jsonObject["email"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun laConfirmacionPorCorreoEstaDesactivada() {
        // Decisión de la fase 2: sin confirmación, el onboarding puede guardar justo después de registrarse.
        val ajustes = Json.parseToJsonElement(leerAjustesDeAuth()).jsonObject
        assertTrue(
            "La confirmación por correo está activada: desactívala en Authentication → Sign In / Providers → Confirm email",
            ajustes["mailer_autoconfirm"]!!.jsonPrimitive.boolean,
        )
    }

    @Test
    fun sinSesionNoSePuedenLeerLosUsuarios() = runBlocking {
        try {
            crearClienteSupabase(guardarSesionEnElTelefono = false).postgrest["usuarios"].select()
            fail("Un visitante sin sesión pudo leer la tabla usuarios: revisa los permisos y RLS")
        } catch (e: RestException) {
            // Esperado: el rol anónimo no tiene ningún permiso sobre la tabla ("permission denied", código 42501).
            val detalle = "${e.error} ${e.message}"
            assertTrue("Error inesperado: $detalle", "42501" in detalle || "permission denied" in detalle)
        }
    }
}

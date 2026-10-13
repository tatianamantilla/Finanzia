package com.finanzia.finanzia.datos.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Cliente único de Supabase para toda la app.
 *
 * - Auth: crear cuenta, iniciar sesión, recuperar contraseña. La sesión queda guardada en el
 *   teléfono (la maneja el propio SDK), así la app puede abrir directo en "Acceso autorizado".
 * - Postgrest: leer y guardar en las tablas (usuarios, movimientos…). Lo que cada usuario puede
 *   ver o cambiar lo deciden las políticas RLS de Supabase (docs/supabase/02-permisos-y-rls.sql).
 *
 * Se crea la primera vez que se usa: los tests y las Previews que trabajan con los repositorios
 * simulados nunca lo crean ni se conectan a internet.
 */
object ClienteSupabase {

    val cliente: SupabaseClient by lazy { crearClienteSupabase(guardarSesionEnElTelefono = true) }
}

/**
 * Crea un cliente de Supabase. Los tests de integración usan [guardarSesionEnElTelefono] = false:
 * la sesión (y el código temporal de la recuperación de contraseña) viven solo en memoria y no
 * queda nada guardado al terminar.
 */
fun crearClienteSupabase(guardarSesionEnElTelefono: Boolean): SupabaseClient =
    createSupabaseClient(
        supabaseUrl = ConfigSupabase.url,
        supabaseKey = ConfigSupabase.clavePublica,
    ) {
        install(Auth) {
            if (!guardarSesionEnElTelefono) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
        }
        install(Postgrest)
    }

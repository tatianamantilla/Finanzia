package com.finanzia.finanzia.datos.supabase

import com.finanzia.finanzia.datos.CATEGORIA_INGRESOS
import com.finanzia.finanzia.datos.CUENTA_PRINCIPAL
import com.finanzia.finanzia.datos.CategoriaGasto
import com.finanzia.finanzia.datos.SesionTerminadaException
import com.finanzia.finanzia.datos.nombreVisible
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ids que necesita el onboarding para guardar movimientos. */
internal data class IdsUsuario(
    val idUsuario: Long,
    val idCuentaPrincipal: Long,
    /** Nombre de la categoría → id_categoria. */
    val categorias: Map<String, Long>,
)

/**
 * Datos que todo usuario necesita para registrar movimientos: la cuenta "Principal" y las
 * categorías Gastos Fijos, Comida, Diversión, Transporte e Ingresos, cada una con un color de la
 * tabla `colores`.
 *
 * La fila de `usuarios`, la cuenta y las 4 categorías de gasto ya las crea el trigger del registro
 * (docs/supabase/01-usuarios-y-auth.sql). Aquí la app agrega solo lo que falte (la categoría
 * Ingresos y los colores): se puede llamar muchas veces sin duplicar nada.
 */
internal object DatosInicialesSupabase {

    /** Categorías por defecto, en el orden en que reciben los colores de la tabla colores. */
    val categoriasPorDefecto: List<Pair<String, String>> =
        CategoriaGasto.entries.map { it.nombreVisible to "gasto" } + (CATEGORIA_INGRESOS to "ingreso")

    suspend fun asegurar(cliente: SupabaseClient): IdsUsuario {
        val idUsuario = idUsuarioActual(cliente)
        val idCuenta = asegurarCuentaPrincipal(cliente, idUsuario)
        val categorias = asegurarCategorias(cliente, idUsuario)
        return IdsUsuario(idUsuario, idCuenta, categorias)
    }

    suspend fun idUsuarioActual(cliente: SupabaseClient): Long {
        val idAuth = cliente.auth.currentUserOrNull()?.id ?: throw SesionTerminadaException()
        return cliente.from("usuarios").select { filter { eq("id_auth", idAuth) } }
            .decodeSingleOrNull<FilaUsuarioId>()?.idUsuario
            ?: error("Tu usuario todavía no está listo en Finanzia. Inténtalo de nuevo en unos segundos.")
    }

    private suspend fun asegurarCuentaPrincipal(cliente: SupabaseClient, idUsuario: Long): Long {
        val existente = cliente.from("cuentas").select {
            filter {
                eq("id_usuario", idUsuario)
                eq("nombre", CUENTA_PRINCIPAL)
            }
            order("id_cuenta", Order.ASCENDING)
        }.decodeList<FilaId>().firstOrNull()
        if (existente != null) return existente.id

        return cliente.from("cuentas").insert(NuevaCuenta(idUsuario, CUENTA_PRINCIPAL, "principal")) {
            select()
        }.decodeSingle<FilaId>().id
    }

    private suspend fun asegurarCategorias(cliente: SupabaseClient, idUsuario: Long): Map<String, Long> {
        val colores = cliente.from("colores").select { order("id_color", Order.ASCENDING) }.decodeList<FilaColor>()
        val existentes = cliente.from("categorias").select { filter { eq("id_usuario", idUsuario) } }
            .decodeList<FilaCategoria>()
            .associateBy { it.nombre }
            .toMutableMap()

        categoriasPorDefecto.forEachIndexed { indice, (nombre, tipo) ->
            val color = colores.getOrNull(indice % colores.size.coerceAtLeast(1))?.idColor
            val categoria = existentes[nombre]
            when {
                categoria == null -> {
                    val nueva = cliente.from("categorias").insert(NuevaCategoria(idUsuario, color, nombre, tipo)) {
                        select()
                    }.decodeSingle<FilaCategoria>()
                    existentes[nombre] = nueva
                }
                categoria.idColor == null && color != null -> {
                    cliente.from("categorias").update(CambioColor(color)) {
                        filter { eq("id_categoria", categoria.idCategoria) }
                    }
                }
            }
        }
        return existentes.mapValues { it.value.idCategoria }
    }

    @Serializable
    private data class FilaUsuarioId(@SerialName("id_usuario") val idUsuario: Long)

    @Serializable
    private data class FilaId(@SerialName("id_cuenta") val id: Long)

    @Serializable
    private data class FilaColor(@SerialName("id_color") val idColor: Long)

    @Serializable
    private data class FilaCategoria(
        @SerialName("id_categoria") val idCategoria: Long,
        val nombre: String,
        @SerialName("id_color") val idColor: Long? = null,
    )

    @Serializable
    private data class NuevaCuenta(
        @SerialName("id_usuario") val idUsuario: Long,
        val nombre: String,
        val tipo: String,
    )

    @Serializable
    private data class NuevaCategoria(
        @SerialName("id_usuario") val idUsuario: Long,
        @SerialName("id_color") val idColor: Long?,
        val nombre: String,
        val tipo: String,
    )

    @Serializable
    private data class CambioColor(@SerialName("id_color") val idColor: Long)
}

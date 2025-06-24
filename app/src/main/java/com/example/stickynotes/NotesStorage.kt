package com.example.stickynotes

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Objeto singleton responsável por guardar e carregar as notas na memória persistente (SharedPreferences).
 * Usa JSON para serializar/deserializar a lista de notas.
 */
object NotesStorage {
    // Nome do ficheiro de preferências e chave interna para as notas
    private const val PREFS_NAME = "sticky_notes_prefs"
    private const val NOTES_KEY = "notes_json"

    /**
     * Guarda uma lista de notas nas SharedPreferences.
     *
     * @param context Contexto Android para aceder às prefs.
     * @param notes Lista de notas a guardar.
     */
    fun saveNotes(context: Context, notes: List<Notes>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(notes) // Serializa a lista de notas em JSON
        prefs.edit {
            putString(NOTES_KEY, json)   // Guarda o JSON nas prefs
            // apply() é chamado automaticamente (função extension do AndroidX)
        }
    }

    /**
     * Lê a lista de notas das SharedPreferences.
     * Se não houver nada guardado, devolve lista vazia.
     * Se alguma nota não tiver "createdAt", atribui o tempo atual (correção de compatibilidade).
     *
     * @param context Contexto Android.
     * @return Lista de notas guardadas.
     */
    fun loadNotes(context: Context): List<Notes> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(NOTES_KEY, null)
        return if (json.isNullOrEmpty()) {
            // Se não houver notas guardadas, devolve lista vazia
            emptyList()
        } else {
            // Deserializa o JSON para lista de Notes
            val type = object : TypeToken<List<Notes>>() {}.type
            try {
                val notes = Gson().fromJson<List<Notes>>(json, type) ?: emptyList()
                // Patch: corrige notas sem createdAt (migração/compatibilidade)
                notes.map { note ->
                    if (note.createdAt == 0L) note.copy(createdAt = System.currentTimeMillis())
                    else note
                }
            } catch (e: Exception) {
                // Em caso de erro na deserialização, devolve lista vazia
                emptyList()
            }
        }
    }
}

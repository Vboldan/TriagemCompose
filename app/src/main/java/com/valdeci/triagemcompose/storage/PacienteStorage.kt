package com.valdeci.triagemcompose.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.valdeci.triagemcompose.Paciente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensão para acessar o DataStore
val Context.dataStore by preferencesDataStore(name = "pacientes_store")

class PacienteStorage(private val context: Context) {
    private val gson = Gson()
    private val key = stringPreferencesKey("pacientes_json")

    // Função suspend para garantir salvamento seguro
    suspend fun salvar(lista: List<Paciente>) {
        val json = gson.toJson(lista)
        context.dataStore.edit { prefs ->
            prefs[key] = json
        }
    }

    // Recupera os dados como Flow
    fun carregar(): Flow<List<Paciente>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[key] ?: "[]"
            val tipo = object : TypeToken<List<Paciente>>() {}.type
            gson.fromJson(json, tipo)
        }
    }
}
package com.valdeci.triagemcompose

data class Paciente(
    val nome: String,
    val idade: Int,
    val sintomas: String,
    var classificacao: String,
    val timestamp: Long = System.currentTimeMillis()
)
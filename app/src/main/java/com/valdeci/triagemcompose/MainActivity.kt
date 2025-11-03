package com.valdeci.triagemcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.valdeci.triagemcompose.storage.PacienteStorage
import com.valdeci.triagemcompose.ui.theme.TriagemComposeTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = PacienteStorage(applicationContext)

        setContent {
            TriagemComposeTheme {
                TriagemScreen(storage)
            }
        }
    }
}

@Composable
fun TriagemScreen(storage: PacienteStorage) {
    var nome by remember { mutableStateOf("") }
    var idade by remember { mutableStateOf("") }
    var sintomas by remember { mutableStateOf("") }
    var corSelecionada by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val listaPacientes = remember { mutableStateListOf<Paciente>() }

    // Carrega os dados ao iniciar
    LaunchedEffect(Unit) {
        storage.carregar().collectLatest { pacientes ->
            listaPacientes.clear()
            listaPacientes.addAll(pacientes)
        }
    }

    // Verifica a cada minuto se algum paciente VERDE passou de 1 hora
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val agora = System.currentTimeMillis()
            val atualizada = listaPacientes.map { paciente ->
                if (paciente.classificacao == "VERDE" &&
                    agora - paciente.timestamp > 60 * 60 * 1000
                ) {
                    paciente.copy(classificacao = "PRIORITÁRIO")
                } else {
                    paciente
                }
            }
            listaPacientes.clear()
            listaPacientes.addAll(atualizada)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Triagem Compose", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black)
            )

            OutlinedTextField(
                value = idade,
                onValueChange = { idade = it },
                label = { Text("Idade") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black)
            )

            OutlinedTextField(
                value = sintomas,
                onValueChange = { sintomas = it },
                label = { Text("Sintomas") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black)
            )

            Text(text = "Classifique o grau de emergência:")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { corSelecionada = "VERDE" }, modifier = Modifier.weight(1f)) {
                    Text("Verde")
                }
                Button(onClick = { corSelecionada = "AMARELO" }, modifier = Modifier.weight(1f)) {
                    Text("Amarelo")
                }
                Button(onClick = { corSelecionada = "VERMELHO" }, modifier = Modifier.weight(1f)) {
                    Text("Vermelho")
                }
            }

            Button(
                onClick = {
                    val idadeInt = idade.toIntOrNull()
                    if (nome.isNotBlank() && idadeInt != null && sintomas.isNotBlank() && corSelecionada.isNotBlank()) {
                        val paciente = Paciente(nome, idadeInt, sintomas, corSelecionada)
                        listaPacientes.add(paciente)
                        resultado = "Paciente ${paciente.nome}, ${paciente.idade} anos, classificado como ${paciente.classificacao}"
                        nome = ""
                        idade = ""
                        sintomas = ""
                        corSelecionada = ""

                        scope.launch {
                            storage.salvar(listaPacientes.toList())
                        }
                    } else {
                        resultado = "Preencha todos os campos e selecione uma cor"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Triagem")
            }

            Button(
                onClick = {
                    listaPacientes.clear()
                    resultado = "Lista de pacientes apagada"

                    scope.launch {
                        storage.salvar(emptyList())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpar Lista")
            }

            if (resultado.isNotEmpty()) {
                Text(text = resultado, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Pacientes triados:", style = MaterialTheme.typography.titleMedium)
        }

        items(listaPacientes) { paciente ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(text = "• ${paciente.nome}, ${paciente.idade} anos — ${paciente.classificacao}")
                Text(text = "Sintomas: ${paciente.sintomas}")
                if (paciente.classificacao == "VERDE") {
                    Text(
                        text = "⚠️ Deve ser atendido em até 1 hora!",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (paciente.classificacao == "PRIORITÁRIO") {
                    Text(
                        text = "⏱️ Tempo excedido! Reclassificado como PRIORITÁRIO.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
package com.example.agent_phone.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OllamaSettingsDialog(
    initialBaseUrl: String,
    initialModel: String,
    onDismiss: () -> Unit,
    onSave: (baseUrl: String, model: String) -> Unit
) {
    var baseUrl by remember { mutableStateOf(initialBaseUrl) }
    var model by remember { mutableStateOf(initialModel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Ollama Settings") },
        text = {
            Column {
                Text(
                    text = "Configure your local Ollama connection.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Ollama Base URL") },
                    placeholder = { Text("http://10.0.2.2:11434") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("LLM Model Name") },
                    placeholder = { Text("qwen3.8:27b") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tip: For Emulator use http://10.0.2.2:11434. Ensure 'OLLAMA_HOST=0.0.0.0' is set when starting Ollama on host machine.",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(baseUrl, model) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

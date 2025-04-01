package com.example.momentum.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddHabitForm(
    onSave: (name: String, frequency: Int, reminderTime: String?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(1) }
    var reminderTime by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add Habit", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Habit Type") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = frequency.toString(),
            onValueChange = { it.toIntOrNull()?.let { freq -> frequency = freq } },
            label = { Text("Frequency per day") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = reminderTime,
            onValueChange = { reminderTime = it },
            label = { Text("Reminder Time (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSave(name, frequency, reminderTime) }) {
                Text("Save")
            }
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}


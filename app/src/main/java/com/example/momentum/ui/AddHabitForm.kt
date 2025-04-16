package com.example.momentum.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
@Composable
fun AddHabitForm(
    onSave: (name: String, frequency: Int, reminderTime: String?) -> Unit,
    onCancel: () -> Unit,
    isLandscape: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(1) }
    var reminderTime by remember { mutableStateOf("") }

    if (isLandscape) {
        // Landscape layout
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
        ) {
            // Left panel - Form title and instructions
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .padding(end = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Create a new habit to track your daily progress. Set how often you want to complete this habit each day.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tips: Consistent habits lead to long-term success. Start small and build gradually.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Right panel - Form fields
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onSave(name, frequency, if (reminderTime.isBlank()) null else reminderTime)
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    } else {
        // Portrait layout
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

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
                Button(
                    onClick = {
                        onSave(name, frequency, if (reminderTime.isBlank()) null else reminderTime)
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }

                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}
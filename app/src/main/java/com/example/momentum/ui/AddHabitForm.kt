package com.example.momentum.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.momentum.ui.components.DaySelector
import coil.compose.rememberAsyncImagePainter



@Composable
fun AddHabitForm(
    onSave: (name: String, frequency: Int, reminderTime: String?, activeDays: Set<Int>, iconImageUri: String?) -> Unit,
    onCancel: () -> Unit,
    isLandscape: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var frequencyInput by remember { mutableStateOf("") }
    val frequency = frequencyInput.toIntOrNull()?.coerceIn(1, 5)
    var reminderTime by remember { mutableStateOf("") }
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri.value = it
    }

    val iconPickerSection = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Choose Icon Image")
            }
            imageUri.value?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Habit Icon Preview",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(top = 4.dp)
                )
            }
        }
    }

    // Initialize with all days selected (Monday = 0, Sunday = 6)
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5, 6)) }

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
                    text = "Create a new habit to track your daily progress. Set how often you want to complete this habit each day and which days of the week it should appear.",
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
                    onValueChange = {
                        name = it.trimStart().take(50)  // limit to 50 characters
                    },
                    label = { Text("Habit Type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = frequencyInput,
                    onValueChange = { input ->
                        frequencyInput = input.filter { it.isDigit() }.take(1)
                    },
                    label = { Text("Frequency per day (1–5)") },
                    modifier = Modifier.fillMaxWidth()
                )

//                OutlinedTextField(
//                    value = reminderTime,
//                    onValueChange = { reminderTime = it },
//                    label = { Text("Reminder Time (optional)") },
//                    modifier = Modifier.fillMaxWidth()
//                )

                iconPickerSection()

                // Day selector
                DaySelector(
                    selectedDays = selectedDays,
                    onDayToggle = { dayIndex ->
                        selectedDays = if (dayIndex in selectedDays) {
                            // Don't allow removing the last day
                            if (selectedDays.size > 1) {
                                selectedDays - dayIndex
                            } else {
                                selectedDays
                            }
                        } else {
                            selectedDays + dayIndex
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (frequency != null) {
                                val iconPath = imageUri.value?.let { uri ->
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val fileName = "habit_icon_${System.currentTimeMillis()}.png"
                                        val file = context.filesDir.resolve(fileName)
                                        inputStream?.use { input ->
                                            file.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        file.absolutePath
                                    } catch (e: Exception) {
                                        Log.e("AddHabitForm", "Failed to save image", e)
                                        null
                                    }
                                }
                                onSave(
                                    name,
                                    frequency,
                                    null,
                                    selectedDays,
                                    iconPath
                                )
                            }
                        },
                        enabled = name.isNotBlank() && frequency != null,
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
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.trimStart().take(50)  // field safeguarding
                },
                label = { Text("Habit Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = frequencyInput,
                onValueChange = { input ->
                    frequencyInput = input.filter { it.isDigit() }.take(1)
                },
                label = { Text("Frequency per day") },
                modifier = Modifier.fillMaxWidth()
            )

//            OutlinedTextField(
//                value = reminderTime,
//                onValueChange = { reminderTime = it },
//                label = { Text("Reminder Time (optional)") },
//                modifier = Modifier.fillMaxWidth()
//            )

            iconPickerSection()

            // Day selector
            DaySelector(
                selectedDays = selectedDays,
                onDayToggle = { dayIndex ->
                    selectedDays = if (dayIndex in selectedDays) {
                        // Don't allow removing the last day
                        if (selectedDays.size > 1) {
                            selectedDays - dayIndex
                        } else {
                            selectedDays
                        }
                    } else {
                        selectedDays + dayIndex
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (frequency != null) {
                            val iconPath = imageUri.value?.let { uri ->
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val fileName = "habit_icon_${System.currentTimeMillis()}.png"
                                    val file = context.filesDir.resolve(fileName)
                                    inputStream?.use { input ->
                                        file.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    file.absolutePath
                                } catch (e: Exception) {
                                    Log.e("AddHabitForm", "Failed to save image", e)
                                    null
                                }
                            }
                            onSave(
                                name,
                                frequency,
                                null,
                                selectedDays,
                                iconPath
                            )
                        }
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

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (frequency != null) {
                            val iconPath = imageUri.value?.let { uri ->
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val fileName = "habit_icon_${System.currentTimeMillis()}.png"
                                    val file = context.filesDir.resolve(fileName)
                                    inputStream?.use { input ->
                                        file.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    file.absolutePath
                                } catch (e: Exception) {
                                    Log.e("AddHabitForm", "Failed to save image", e)
                                    null
                                }
                            }
                            onSave(
                                name,
                                frequency,
                                null,
                                selectedDays,
                                iconPath
                            )
                        }
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
}
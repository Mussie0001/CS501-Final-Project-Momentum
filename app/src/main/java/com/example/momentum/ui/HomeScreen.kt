package com.example.momentum.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentum.R
import com.example.momentum.model.Habit
import com.example.momentum.ui.components.HabitItem
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habits: List<Habit>,
    onHabitToggle: (Int, Int) -> Unit, // Updated to pass both habit index and completion index
    onHabitDelete: (Int) -> Unit,
    onAddHabitClick: () -> Unit,
    quote: String,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit,
    isLandscape: Boolean = false
) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d").format(Date())
    }

    // State list for habit recomposition (checkmarks)
    val habitsState = remember(habits) { habits.toMutableStateList() }

    // Count total and completed sub-habits
    val totalCompletions = habitsState.sumOf { it.frequency }
    val completedCount = habitsState.sumOf { it.completions.size }

    val calculateTotalCompletions = {
        habitsState.sumOf { it.completions.size }
    }

    val calculateTotalRequired = {
        habitsState.sumOf { it.frequency }
    }

    // Delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Int?>(null) }

    // Function to handle toggling habit completion
    val handleToggle: (Int, Int) -> Unit = { habitIndex, completionIndex ->
        Log.d("HomeScreen", "Toggle habit at index $habitIndex, completion $completionIndex")

        onHabitToggle(habitIndex, completionIndex)

    }

    // Confirmation dialog
    if (showDeleteDialog && habitToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                habitToDelete = null
            },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete this habit? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        habitToDelete?.let { onHabitDelete(it) }
                        showDeleteDialog = false
                        habitToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        habitToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (!isLandscape) {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { onTabSelected("home") },
                        icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onTabSelected("add") },
                        icon = { Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add") },
                        label = { Text("New Habit") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onTabSelected("history") },
                        icon = { Icon(painterResource(id = R.drawable.ic_history), contentDescription = "History") },
                        label = { Text("History") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onTabSelected("settings") },
                        icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { padding ->
        if (isLandscape) {
            // Landscape layout
            Row(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Left panel: quote and date
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // app title
                    Text(
                        text = "Momentum",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )

                    Text(
                        text = "Hi, today is $currentDate",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // quote box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = quote,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Summary at the bottom
                    Text(
                        text = "$completedCount of $totalCompletions completions done today",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Right panel: habits list
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(16.dp)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "Today's Habits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Habits list with LazyColumn for scrolling
                    if (habitsState.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No habits yet! Tap 'New Habit' to add one.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = habitsState,
                                key = { _, habit -> habit.id }
                            ) { index, habit ->
                                key(habit.id, habit.completions.size) {
                                    HabitItem(
                                        habit = habit,
                                        onToggle = { completionIndex ->
                                            handleToggle(index, completionIndex)
                                        },
                                        onDelete = {
                                            habitToDelete = index
                                            showDeleteDialog = true
                                        }
                                    )
                                }

                                if (index < habitsState.size - 1) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // app title
                Text(
                    text = "Momentum",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )

                Text(
                    text = "Hi, today is $currentDate",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // quote box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = quote,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Habits section title
                Text(
                    text = "Today's Habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Habits list with LazyColumn for scrolling
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (habitsState.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No habits yet! Tap 'New Habit' to add one.",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(
                                    items = habitsState,
                                    key = { _, habit -> habit.id }
                                ) { index, habit ->
                                    key(habit.id, habit.completions.size) {
                                        HabitItem(
                                            habit = habit,
                                            onToggle = { completionIndex ->
                                                handleToggle(index, completionIndex)
                                            },
                                            onDelete = {
                                                habitToDelete = index
                                                showDeleteDialog = true
                                            }
                                        )
                                    }

                                    if (index < habitsState.size - 1) {
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }

                        // Summary section at the bottom
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "${calculateTotalCompletions()} of ${calculateTotalRequired()} completions done today",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
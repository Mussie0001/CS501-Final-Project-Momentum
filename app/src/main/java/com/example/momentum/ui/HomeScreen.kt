package com.example.momentum.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentum.R
import com.example.momentum.model.Habit
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habits: List<Habit>,
    onHabitToggle: (Int) -> Unit,
    onHabitDelete: (Int) -> Unit,
    onAddHabitClick: () -> Unit,
    quote: String,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit
) {
    // state list for habit recomposition (checkmarks)
    val habitsState = remember(habits) { habits.toMutableStateList() }
    val completedCount = habitsState.count { it.isCompleted }

    // delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Int?>(null) }

    // Function to handle toggling habit completion
    val handleToggle: (Int) -> Unit = { index ->
        Log.d("HomeScreen", "Toggle habit at index $index")

        val habit = habitsState[index]
        habitsState[index] = habit.copy(isCompleted = !habit.isCompleted)
        onHabitToggle(index)
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
    ) { padding ->
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

            // Habits section with summary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
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
                                key(habit.id, habit.isCompleted) {
                                    HabitItem(
                                        habit = habit,
                                        onToggle = {
                                            handleToggle(index)
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
                        text = "$completedCount of ${habitsState.size} habits completed today",
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

@Composable
fun HabitItem(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(habit.id, habit.isCompleted) {
        Log.d("HabitItem", "Rendering habit: ${habit.name}, completed: ${habit.isCompleted}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onToggle()
                Log.d("HabitItem", "Clicked habit: ${habit.name}")
            },
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE0E0E0),
                modifier = Modifier.size(48.dp)
            ) {
                Image(
                    painter = painterResource(id = habit.iconRes),
                    contentDescription = habit.name,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = habit.name,
                modifier = Modifier.weight(1f)
            )

            // Visual cues for habit completion
            if (habit.isCompleted) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Context menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Habit")
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
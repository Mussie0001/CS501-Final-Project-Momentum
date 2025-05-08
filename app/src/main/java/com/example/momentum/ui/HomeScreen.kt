package com.example.momentum.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.momentum.R
import com.example.momentum.model.Habit
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habits: List<Habit>,
    onHabitToggle: (Int, Int) -> Unit,
    onHabitDelete: (Int) -> Unit,
    onAddHabitClick: () -> Unit,
    quote: String,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit,
    isLandscape: Boolean = false
) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    val totalCompletions = habits.sumOf { it.frequency }
    val completedCount = habits.sumOf { it.completions.size }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Int?>(null) }

    val handleToggle: (Int, Int) -> Unit = { habitIndex, completionIndex ->
        onHabitToggle(habitIndex, completionIndex)
    }

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
        Column(
            modifier = modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            Text(
                text = "Today's Habits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Habit list section
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No habits yet! Tap 'New Habit' to add one.", textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(habits, key = { _, habit -> habit.id }) { index, habit ->
                        HabitItem(
                            habit = habit,
                            onToggle = { completionIndex -> handleToggle(index, completionIndex) },
                            onDelete = {
                                habitToDelete = index
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Completion summary at the bottom
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "$completedCount of $totalCompletions completions done today",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggle: (completionIndex: Int) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                if (habit.iconImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = habit.iconImageUri),
                        contentDescription = habit.name,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = habit.iconRes),
                        contentDescription = habit.name,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium // larger text for habit name
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🔥 Streak: ${habit.streak} day${if (habit.streak == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )


                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(habit.frequency.coerceAtMost(5)) { i ->
                        val isChecked = i < habit.completions.size
                        val shape = RoundedCornerShape(4.dp)

                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onToggle(i) },
                            shape = shape,
                            color = if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.Transparent,
                            border = if (!isChecked) {
                                ButtonDefaults.outlinedButtonBorder
                            } else null
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
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


package com.example.momentum.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.example.momentum.model.Habit

@Composable
fun HabitItem(
    habit: Habit,
    onToggle: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val frequency = habit.frequency

    val completedCount = habit.completions.size
    val isFullyCompleted = completedCount >= frequency

    var localCompletedCount by remember(habit.id, completedCount) {
        mutableStateOf(completedCount)
    }
    var localIsFullyCompleted by remember(habit.id, isFullyCompleted) {
        mutableStateOf(isFullyCompleted)
    }

    // clicking entire habit row
    val handleRowClick = {
        if (localCompletedCount < frequency) {
            localCompletedCount++
            localIsFullyCompleted = localCompletedCount >= frequency
            onToggle(localCompletedCount - 1)
        } else {
            localCompletedCount = 0
            localIsFullyCompleted = false
            // Remove all completions one by one
            for (i in frequency - 1 downTo 0) {
                onToggle(i)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { handleRowClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (localIsFullyCompleted)
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

            // Frequency checkboxes row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Generate checkbox
                for (i in 0 until frequency) {
                    val isCompleted = i < localCompletedCount
                    CompletionCheckbox(
                        isCompleted = isCompleted,
                        onClick = {
                            if (isCompleted) {
                                for (j in frequency - 1 downTo i) {
                                    if (j < localCompletedCount) {
                                        onToggle(j)
                                    }
                                }
                                localCompletedCount = i
                                localIsFullyCompleted = false
                            } else {
                                // Fill empty checkboxes this and all previous ones
                                for (j in 0..i) {
                                    if (j >= localCompletedCount) {
                                        onToggle(j)
                                    }
                                }
                                localCompletedCount = i + 1
                                localIsFullyCompleted = localCompletedCount >= frequency
                            }
                        }
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

@Composable
fun CompletionCheckbox(
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else
                Color.LightGray.copy(alpha = 0.2f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
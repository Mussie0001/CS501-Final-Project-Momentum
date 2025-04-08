package com.example.momentum.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * History screen composable function that can be called directly from your Activity
 */
@Composable
fun HistoryScreen() {
    // State to track which view is currently active
    var currentView by remember { mutableStateOf("weekly") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        // Header
        Text(
            text = "Habit History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // View toggle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = currentView == "weekly",
                onClick = { currentView = "weekly" },
                label = { Text("Weekly View") },
                modifier = Modifier.padding(end = 8.dp)
            )

            FilterChip(
                selected = currentView == "monthly",
                onClick = { currentView = "monthly" },
                label = { Text("Monthly View") }
            )
        }

        // Content based on selected view
        when (currentView) {
            "weekly" -> WeeklyView()
            "monthly" -> MonthlyHistoryView()
        }
    }
}

@Composable
private fun WeeklyView() {
    // Days row
    val currentDate = LocalDate.of(2025, 4, 1)
    val days = listOf(
        LocalDate.of(2025, 3, 29),
        LocalDate.of(2025, 3, 30),
        LocalDate.of(2025, 3, 31),
        LocalDate.of(2025, 4, 1),
        LocalDate.of(2025, 4, 2),
        LocalDate.of(2025, 4, 3)
    )

    // Sample habits
    val habits = listOf("Morning Walk", "Read 30 mins", "Drink Water", "Meditate")

    // Completed habits map (random for past days)
    val completedHabits = mapOf(
        LocalDate.of(2025, 3, 29) to listOf("Morning Walk", "Meditate"),
        LocalDate.of(2025, 3, 30) to listOf("Read 30 mins", "Drink Water"),
        LocalDate.of(2025, 3, 31) to listOf("Morning Walk", "Drink Water"),
        LocalDate.of(2025, 4, 1) to emptyList(),
        LocalDate.of(2025, 4, 2) to emptyList(),
        LocalDate.of(2025, 4, 3) to emptyList()
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Days header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { date ->
                    DayHeader(
                        date = date,
                        isCurrentDay = date.isEqual(currentDate),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.LightGray
            )

            // Habits and their completion status
            habits.forEach { habit ->
                HabitRow(
                    habitName = habit,
                    days = days,
                    completedHabits = completedHabits,
                    currentDate = currentDate
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DayHeader(
    date: LocalDate,
    isCurrentDay: Boolean,
    modifier: Modifier = Modifier
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    val dateFormatter = DateTimeFormatter.ofPattern("d")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayFormatter.format(date),
            fontSize = 12.sp,
            color = if (isCurrentDay) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isCurrentDay) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .border(
                    width = if (isCurrentDay) 2.dp else 0.dp,
                    color = if (isCurrentDay) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateFormatter.format(date),
                fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HabitRow(
    habitName: String,
    days: List<LocalDate>,
    completedHabits: Map<LocalDate, List<String>>,
    currentDate: LocalDate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Habit name
        Text(
            text = habitName,
            modifier = Modifier.width(120.dp),
            fontWeight = FontWeight.Medium
        )

        // Status for each day
        days.forEach { date ->
            val isCompleted = completedHabits[date]?.contains(habitName) ?: false
            val isPastDay = date.isBefore(currentDate)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && isPastDay) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isPastDay) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}
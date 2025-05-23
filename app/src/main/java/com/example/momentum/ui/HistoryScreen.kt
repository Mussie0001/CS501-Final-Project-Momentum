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
fun HistoryScreen(isLandscape: Boolean = false) {
    // State to track which view is currently active
    var currentView by remember { mutableStateOf("weekly") }

    if (isLandscape) {
        // Landscape layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Left panel - Controls and view selection
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .padding(end = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Habit History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentView == "weekly",
                        onClick = { currentView = "weekly" },
                        label = { Text("Weekly View") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterChip(
                        selected = currentView == "monthly",
                        onClick = { currentView = "monthly" },
                        label = { Text("Monthly View") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Right panel - Content view
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                // Content based on selected view
                when (currentView) {
                    "weekly" -> WeeklyView()
                    "monthly" -> MonthlyHistoryView()
                }
            }
        }
    } else {
        // Portrait layout
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
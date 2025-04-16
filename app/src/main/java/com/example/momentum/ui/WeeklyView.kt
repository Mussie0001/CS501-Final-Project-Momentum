package com.example.momentum.ui

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momentum.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun WeeklyView(
    habitViewModel: HabitViewModel = viewModel()
) {
    // 1) detect orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 2) week selection state
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val selectedLocalDate = remember(selectedDate) {
        Instant.ofEpochMilli(selectedDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    // 3) compute week bounds
    val startOfWeek = remember(selectedLocalDate) {
        selectedLocalDate.with(java.time.DayOfWeek.MONDAY)
    }
    val endOfWeek = remember(selectedLocalDate) {
        selectedLocalDate.with(java.time.DayOfWeek.SUNDAY)
    }

    // 4) load data
    val habits = habitViewModel.habits.collectAsState().value
    val completionsRange = habitViewModel
        .getHabitCompletionsForRange(startOfWeek, endOfWeek)
        .collectAsState(initial = emptyMap()).value
    val completionDates = remember(completionsRange) {
        completionsRange.values.flatten().toSet()
    }
    val habitsForSelectedDate = remember(completionsRange, selectedLocalDate, habits) {
        completionsRange.filter { it.value.contains(selectedLocalDate) }
            .keys
            .mapNotNull { id -> habits.find { it.id == id }?.name }
    }

    // 5) column with dynamic padding
    val pad = if (isLandscape) 8.dp else 16.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(pad)
    ) {
        // ── always show calendar ────────────────────────────────────
        WeekViewWithNavigation(
            selectedDate    = selectedDate,
            completionDates = completionDates,
            onDateSelected  = { selectedDate = it }
        )

        // ── only in PORTRAIT show the Completed‑Habits section ────
        if (!isLandscape) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            Text(
                text = "Completed Habits for ${
                    selectedLocalDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
                }",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (habitsForSelectedDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No habits completed on this day",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(habitsForSelectedDate) { habitName ->
                        HabitCompletionItem(habitName = habitName)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekViewWithNavigation(
    selectedDate: Long,
    completionDates: Set<LocalDate>,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate


    val weekFormatter = SimpleDateFormat("MMM d", Locale.getDefault())

    // Calculate start and end of week
    val weekCalendar = Calendar.getInstance().apply {
        timeInMillis = selectedDate
        firstDayOfWeek = Calendar.MONDAY
    }
    weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val startOfWeek = weekCalendar.timeInMillis
    weekCalendar.add(Calendar.DAY_OF_YEAR, 6)
    val endOfWeek = weekCalendar.timeInMillis

    Column(modifier = Modifier.padding(16.dp)) {
        // Week header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                onDateSelected(calendar.timeInMillis)
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Week")
            }

            Text(
                text = "${weekFormatter.format(Date(startOfWeek))} - ${weekFormatter.format(Date(endOfWeek))}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                onDateSelected(calendar.timeInMillis)
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Week")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) {
                Text(
                    text = dayOfWeek,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Week grid
        Row(modifier = Modifier.fillMaxWidth()) {
            // Generate days for the week
            val tempCalendar = Calendar.getInstance().apply {
                timeInMillis = startOfWeek
            }

            repeat(7) {
                val dayCalendar = Calendar.getInstance().apply {
                    timeInMillis = tempCalendar.timeInMillis
                }
                tempCalendar.add(Calendar.DAY_OF_YEAR, 1)

                val isSelected = dayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        dayCalendar.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR) &&
                        dayCalendar.get(Calendar.DAY_OF_WEEK) == calendar.get(Calendar.DAY_OF_WEEK)

                val dayLocalDate = Instant.ofEpochMilli(dayCalendar.timeInMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val hasCompletions = completionDates.contains(dayLocalDate)

                DayCell(
                    day = dayCalendar.get(Calendar.DAY_OF_MONTH),
                    isCurrentDay = dayLocalDate == LocalDate.now(),
                    isSelected = isSelected,
                    hasCompletions = hasCompletions,
                    onClick = {
                        onDateSelected(dayCalendar.timeInMillis)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isCurrentDay: Boolean,
    isSelected: Boolean,
    hasCompletions: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
    ) {
        if (isSelected) {
            // Selected day
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Non-selected day
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                shape = MaterialTheme.shapes.small,
                color = when {
                    isCurrentDay -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    hasCompletions -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else -> Color.Transparent
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day.toString(),
                        color = when {
                            isCurrentDay -> MaterialTheme.colorScheme.primary
                            hasCompletions -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (isCurrentDay || hasCompletions) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun String.HabitCompletionItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = this@HabitCompletionItem,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
package com.example.momentum.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun MonthlyHistoryView(
    habitViewModel: HabitViewModel = viewModel()
) {
    // State to track the selected date
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    // Convert selected timestamp to LocalDate
    val selectedLocalDate = remember(selectedDate) {
        Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    // Get the first and last day of the month for querying
    val firstDayOfMonth = remember(selectedLocalDate) {
        selectedLocalDate.withDayOfMonth(1)
    }

    val lastDayOfMonth = remember(selectedLocalDate) {
        val lastDay = selectedLocalDate.withDayOfMonth(selectedLocalDate.lengthOfMonth())
        lastDay
    }

    // Collect all habits from the database
    val habits = habitViewModel.habits.collectAsState().value

    // Collect habit completions for the current month
    val completionsRange = habitViewModel.getHabitCompletionsForRange(
        firstDayOfMonth,
        lastDayOfMonth
    ).collectAsState(initial = emptyMap()).value

    // Generate a map of completion dates for highlighting the calendar
    val completionDates = remember(completionsRange) {
        completionsRange.values.flatten().toSet()
    }

    // Get habits completed for selected date
    val habitsForSelectedDate = remember(completionsRange, selectedLocalDate, habits) {
        val habitIds = completionsRange.filter { (_, dates) ->
            dates.contains(selectedLocalDate)
        }.keys

        // Map habitIds to actual habit names
        habitIds.mapNotNull { habitId ->
            habits.find { it.id == habitId }?.name
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Calendar view
        CalendarViewWithNavigation(
            selectedDate = selectedDate,
            completionDates = completionDates,
            onDateSelected = {
                selectedDate = it
            }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        // Habits for selected date
        Text(
            text = "Completed Habits for ${selectedLocalDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (habitsForSelectedDate.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
            // List of completed habits
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

@Composable
fun HabitCompletionItem(habitName: String) {
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
                text = habitName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarViewWithNavigation(
    selectedDate: Long,
    completionDates: Set<LocalDate>,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate

    // Set to first day of current month
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val monthCalendar = Calendar.getInstance()
    monthCalendar.set(currentYear, currentMonth, 1)

    val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Column(modifier = Modifier.padding(16.dp)) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar.add(Calendar.MONTH, -1)
                onDateSelected(calendar.timeInMillis)
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Text(
                text = dateFormatter.format(Date(selectedDate)),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = {
                calendar.add(Calendar.MONTH, 1)
                onDateSelected(calendar.timeInMillis)
            }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")) {
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

        // Calendar grid
        val days = ArrayList<Date>()

        // Get the first day of month
        val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1

        // Add empty days for the beginning of the month
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        // Generate 42 days (6 weeks)
        repeat(42) {
            days.add(Date(monthCalendar.timeInMillis))
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Display days in a grid
        val rows = days.chunked(7)

        for (week in rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in week) {
                    val dayCalendar = Calendar.getInstance()
                    dayCalendar.time = day

                    val isCurrentMonth = dayCalendar.get(Calendar.MONTH) == currentMonth
                    val isSelected = dayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            dayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                            dayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)

                    // Check if this day has actual habit completions from the database
                    val dayLocalDate = Instant.ofEpochMilli(day.time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val hasCompletions = completionDates.contains(dayLocalDate) && isCurrentMonth

                    DayCell(
                        day = dayCalendar.get(Calendar.DAY_OF_MONTH),
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isSelected,
                        hasCompletions = hasCompletions,
                        onClick = {
                            onDateSelected(dayCalendar.timeInMillis)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasCompletions: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
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
                color = if (hasCompletions)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else
                    Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day.toString(),
                        color = if (isCurrentMonth)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = if (hasCompletions) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
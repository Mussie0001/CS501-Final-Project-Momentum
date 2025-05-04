package com.example.momentum.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@Composable
fun MonthlyHistoryView(
    habitViewModel: HabitViewModel = viewModel()
) {
    // 1) Detect orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 2) State to track the selected date
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    // Convert selected timestamp to LocalDate
    val selectedLocalDate = remember(selectedDate) {
        Instant.ofEpochMilli(selectedDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    // Bounds for API query
    val firstDayOfMonth = remember(selectedLocalDate) {
        selectedLocalDate.withDayOfMonth(1)
    }
    val lastDayOfMonth = remember(selectedLocalDate) {
        selectedLocalDate.withDayOfMonth(selectedLocalDate.lengthOfMonth())
    }

    // Data from VM
    val habits = habitViewModel.habits.collectAsState().value
    val completionsRange =
        habitViewModel.getHabitCompletionsForRange(firstDayOfMonth, lastDayOfMonth)
            .collectAsState(initial = emptyMap()).value
    val completionDates = remember(completionsRange) {
        completionsRange.values.flatten().toSet()
    }
    val habitsForSelectedDate = remember(completionsRange, selectedLocalDate, habits) {
        // Map of habit names to completion counts
        val habitCompletions = mutableMapOf<String, Int>()

        // Count completions per habit
        completionsRange.forEach { (habitId, dates) ->
            if (dates.contains(selectedLocalDate)) {
                val habit = habits.find { it.id == habitId }
                if (habit != null) {
                    // Count how many times this habit was completed on selected date
                    val countOnDate = dates.count { it == selectedLocalDate }
                    habitCompletions[habit.name] = (habitCompletions[habit.name] ?: 0) + countOnDate
                }
            }
        }

        habitCompletions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()   // keep your inset handling
    ) {
        // Calendar view (always shown)
        CalendarViewWithNavigation(
            selectedDate    = selectedDate,
            completionDates = completionDates,
            onDateSelected  = { selectedDate = it }
        )

        // ── Only in PORTRAIT do we render the bottom Completed‑Habits section ──
        if (!isLandscape) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            Text(
                text = "Completed Habits for ${
                    selectedLocalDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                }",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (habitsForSelectedDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    items(habitsForSelectedDate.toList()) { (habitName, completionCount) ->
                        HabitCompletionItem(
                            habitName = habitName,
                            completionCount = completionCount
                        )
                    }
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
    // Detect orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Set up our “cursor” date
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    // Build the 42-day list for a 6-week grid
    val firstOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
    }
    val offset = (firstOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
    val startCal = (firstOfMonth.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, -offset)
    }
    val days: List<Date> = List(42) {
        startCal.time.also { startCal.add(Calendar.DAY_OF_MONTH, 1) }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // ─── Month navigation row ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton({
                calendar.add(Calendar.MONTH, -1)
                onDateSelected(calendar.timeInMillis)
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(Date(selectedDate)),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton({
                calendar.add(Calendar.MONTH, 1)
                onDateSelected(calendar.timeInMillis)
            }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }

        Spacer(Modifier.height(8.dp))

        // ─── Weekday headers ───────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ─── Grid: landscape uses LazyVerticalGrid (scrollable);
        //           portrait uses a static 6×7 grid of square cells ───
        if (isLandscape) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),      // take available vertical space
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Top
            ) {
                items(days) { date ->
                    // For each date, recompute flags:
                    val dc = Calendar.getInstance().apply { time = date }
                    val isCurrentMonth = dc.get(Calendar.MONTH) == currentMonth
                    val isSelected =
                        dc.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                dc.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                dc.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)

                    val local = Instant.ofEpochMilli(date.time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val hasCompletions = isCurrentMonth && completionDates.contains(local)

                    MonthDayCell(
                        day = dc.get(Calendar.DAY_OF_MONTH),
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isSelected,
                        hasCompletions = hasCompletions,
                        onClick = { onDateSelected(dc.timeInMillis) },
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                    )
                }
            }
        } else {
            Column {
                days.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            val dc = Calendar.getInstance().apply { time = date }
                            val isCurrentMonth = dc.get(Calendar.MONTH) == currentMonth
                            val isSelected =
                                dc.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                        dc.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                        dc.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)

                            val local = Instant.ofEpochMilli(date.time)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val hasCompletions = isCurrentMonth && completionDates.contains(local)

                            MonthDayCell(
                                day = dc.get(Calendar.DAY_OF_MONTH),
                                isCurrentMonth = isCurrentMonth,
                                isSelected = isSelected,
                                hasCompletions = hasCompletions,
                                onClick = { onDateSelected(dc.timeInMillis) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MonthDayCell(
    day: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasCompletions: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clickable(onClick = onClick)
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
                modifier = Modifier.fillMaxSize(),
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
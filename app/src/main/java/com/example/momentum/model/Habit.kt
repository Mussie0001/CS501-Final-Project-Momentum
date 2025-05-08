package com.example.momentum.model

import androidx.annotation.DrawableRes
import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    @DrawableRes val iconRes: Int,
    val frequency: Int = 1, // How many times the habit should be completed daily
    val reminderTime: String? = null,
    val activeDays: Set<Int> = setOf(0, 1, 2, 3, 4, 5, 6), // Default: all days (Mon=0, Sun=6)
    val completions: List<LocalDate> = emptyList(), // Completions for today
    val isCompleted: Boolean = false, // Deprecated: For backward compatibility only
    val iconImageUri: String? = null
) {
    fun isFullyCompleted(): Boolean = completions.size >= frequency

    // Check if this habit is active for the given day of week (0 = Monday, 6 = Sunday)
    fun isActiveOn(dayOfWeek: Int): Boolean = dayOfWeek in activeDays

    // Check if this habit is active today
    fun isActiveToday(): Boolean {
        val today = LocalDate.now()
        // Convert Java's DayOfWeek (1-7, Mon-Sun) to our 0-6 format
        val todayIndex = today.dayOfWeek.value - 1
        return isActiveOn(todayIndex)
    }

    // Add a completion for today
    fun addCompletion(date: LocalDate): Habit {
        // Only add if we haven't met the frequency yet
        if (completions.size < frequency) {
            val newCompletions = completions.toMutableList()
            newCompletions.add(date)
            return copy(completions = newCompletions)
        }
        return this
    }

    // Remove a completion
    fun removeCompletion(): Habit {
        if (completions.isNotEmpty()) {
            val newCompletions = completions.toMutableList()
            newCompletions.removeAt(newCompletions.size - 1)
            return copy(completions = newCompletions)
        }
        return this
    }
}
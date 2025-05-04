package com.example.momentum.model

import androidx.annotation.DrawableRes
import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    @DrawableRes val iconRes: Int,
    val frequency: Int = 1,
    val reminderTime: String? = null,
    val completions: List<LocalDate> = emptyList(),
    val isCompleted: Boolean = false
) {
    fun isFullyCompleted(): Boolean = completions.size >= frequency

    fun addCompletion(date: LocalDate): Habit {
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
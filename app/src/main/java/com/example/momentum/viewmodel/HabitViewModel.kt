package com.example.momentum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.momentum.data.repository.HabitRepository
import com.example.momentum.model.Habit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    // Add a refresh trigger to force reloading habits
    private val _refreshTrigger = MutableStateFlow(0L)

    // Flow of habits with today's completion status
    val habits: StateFlow<List<Habit>> = _refreshTrigger
        .flatMapLatest { repository.getHabitsWithTodayCompletion() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Initialize with a refresh
    init {
        refreshHabits()
    }

    // Method to force refresh of habits from the database
    fun refreshHabits() {
        viewModelScope.launch {
            _refreshTrigger.value = System.currentTimeMillis()
        }
    }

    // Toggle a habit completion status with completion index
    fun toggleHabitCompletion(habitIndex: Int, completionIndex: Int) {
        val habit = habits.value[habitIndex]
        viewModelScope.launch {
            repository.toggleHabitCompletion(habit, completionIndex)
            // The Flow will automatically update due to flatMapLatest
        }
    }

    // Add a new habit with frequency support
    fun addHabit(name: String, iconRes: Int, frequency: Int = 1, reminderTime: String? = null) {
        viewModelScope.launch {
            repository.addHabit(name, iconRes, frequency, reminderTime)
            // Refresh to ensure the UI updates
            refreshHabits()
        }
    }

    // Clean up duplicate habits
    fun removeDuplicateHabits() {
        viewModelScope.launch {
            repository.removeDuplicateHabits()
            refreshHabits()
        }
    }

    // Get count of habits
    suspend fun getHabitCount(): Int {
        return repository.getHabitCount()
    }

    // Delete a habit
    fun deleteHabit(index: Int) {
        val habit = habits.value[index]
        viewModelScope.launch {
            repository.deleteHabit(habit)
            refreshHabits()
        }
    }

    // Get completion data for history screen (for a date range)
    fun getHabitCompletionsForRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<Long, List<LocalDate>>> {
        return repository.getCompletionsForDateRange(startDate, endDate)
    }

    // Factory for creating the ViewModel with dependencies
    class Factory(private val repository: HabitRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
                return HabitViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
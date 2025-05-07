package com.example.momentum.data.repository

import android.util.Log
import com.example.momentum.data.dao.HabitDao
import com.example.momentum.data.entity.HabitCompletionEntity
import com.example.momentum.data.entity.HabitEntity
import com.example.momentum.data.util.StringListConverter
import com.example.momentum.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class HabitRepository(private val habitDao: HabitDao) {

    private val stringListConverter = StringListConverter()

    // Convert between domain model and entity
    private fun HabitEntity.toModel(completions: List<LocalDate> = emptyList()): Habit {
        return Habit(
            id = this.id,
            name = this.name,
            iconRes = this.iconRes,
            frequency = this.frequency,
            reminderTime = this.reminderTime,
            activeDays = stringListConverter.fromString(this.activeDays),
            completions = completions,
            isCompleted = completions.isNotEmpty() // For backward compatibility
        )
    }

    private fun Habit.toEntity(): HabitEntity {
        return HabitEntity(
            id = this.id,
            name = this.name,
            iconRes = this.iconRes,
            frequency = this.frequency,
            reminderTime = this.reminderTime,
            activeDays = stringListConverter.toString(this.activeDays),
            createdAt = System.currentTimeMillis()
        )
    }

    // Convert LocalDate to timestamp
    private fun LocalDate.toTimestamp(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    // Convert timestamp to LocalDate
    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    // Function to get habits with completion status for today
    fun getHabitsWithTodayCompletion(): Flow<List<Habit>> {
        val today = LocalDate.now().toTimestamp()
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        // Get the current day of week (0-6, Monday to Sunday)
        val todayDayOfWeek = LocalDate.now().dayOfWeek.value - 1

        return habitDao.getAllHabits().map { habitEntities ->
            habitEntities
                .filter { entity ->
                    // Only include habits active today
                    val activeDays = stringListConverter.fromString(entity.activeDays)
                    todayDayOfWeek in activeDays
                }
                .map { habitEntity ->
                    // Get all completions for this habit today
                    val todayCompletions = habitDao.getHabitCompletionsInTimeRange(
                        habitId = habitEntity.id,
                        startTime = startOfDay,
                        endTime = endOfDay
                    )

                    // Convert to LocalDate objects for the model
                    val completionDates = todayCompletions.map { it.completedDate.toLocalDate() }

                    // Create the model with today's completions
                    habitEntity.toModel(completionDates)
                }
        }
    }

    // Function to toggle habit completion status with index
    suspend fun toggleHabitCompletion(habit: Habit, completionIndex: Int) {
        val today = LocalDate.now()
        val todayTimestamp = today.toTimestamp()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        // Get existing completions for today
        val existingCompletions = habitDao.getHabitCompletionsInTimeRange(
            habitId = habit.id,
            startTime = startOfDay,
            endTime = endOfDay
        )

        if (existingCompletions.size <= completionIndex) {
            // We need to add a completion if we don't have enough yet and haven't reached frequency
            if (existingCompletions.size < habit.frequency) {
                // Add a new completion
                val completion = HabitCompletionEntity(
                    habitId = habit.id,
                    completedDate = todayTimestamp,
                    completedTime = System.currentTimeMillis()
                )
                habitDao.insertHabitCompletion(completion)
            }
        } else {
            // Remove the completion at this index
            val completionToRemove = existingCompletions[completionIndex]
            habitDao.deleteHabitCompletion(completionToRemove)
        }
    }

    // Add a new habit
    suspend fun addHabit(
        name: String,
        iconRes: Int,
        frequency: Int = 1,
        reminderTime: String? = null,
        activeDays: Set<Int> = setOf(0, 1, 2, 3, 4, 5, 6) // Default: all days
    ): Long {
        val habitEntity = HabitEntity(
            name = name,
            iconRes = iconRes,
            frequency = frequency,
            reminderTime = reminderTime,
            activeDays = stringListConverter.toString(activeDays)
        )
        return habitDao.insertHabit(habitEntity)
    }

    // Update an existing habit
    suspend fun updateHabit(habit: Habit) {
        val habitEntity = habit.toEntity()
        habitDao.updateHabit(habitEntity)
    }

    // Delete a habit
    suspend fun deleteHabit(habit: Habit) {
        val habitEntity = habit.toEntity()
        habitDao.deleteHabit(habitEntity)
    }

    // Delete all habits
    suspend fun deleteAllHabits() {
        Log.d("HabitRepository", "Deleting all habits")
        habitDao.deleteAllHabits()
    }

    // Get completion data for history screen (for a date range)
    fun getCompletionsForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<Long, List<LocalDate>>> {
        val startTimestamp = startDate.toTimestamp()
        val endTimestamp = endDate.toTimestamp()

        return habitDao.getHabitCompletionsInRange(startTimestamp, endTimestamp)
            .map { completions ->
                completions.groupBy(
                    { it.habitId },
                    {
                        // Convert the timestamp back to LocalDate properly
                        it.completedDate.toLocalDate()
                    }
                )
            }
    }

    // Clean up duplicate habits
    suspend fun removeDuplicateHabits() {
        habitDao.removeDuplicateHabits()
    }

    // Get count of habits
    suspend fun getHabitCount(): Int {
        return habitDao.getHabitCount()
    }

    // Get a habit by ID
    suspend fun getHabitById(habitId: Long): HabitEntity? {
        return habitDao.getHabitById(habitId)
    }
}
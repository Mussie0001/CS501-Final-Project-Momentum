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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow


class HabitRepository(private val habitDao: HabitDao) {

    private val stringListConverter = StringListConverter()

    // Convert between domain model and entity
    private fun HabitEntity.toModel(completions: List<LocalDate> = emptyList(), streak: Int = 0): Habit {
        return Habit(
            id = this.id,
            name = this.name,
            iconRes = this.iconRes,
            frequency = this.frequency,
            reminderTime = this.reminderTime,
            activeDays = stringListConverter.fromString(this.activeDays),
            completions = completions,
            isCompleted = completions.isNotEmpty(), // For backward compatibility
            iconImageUri = iconImageUri,
            streak = streak
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
            createdAt = System.currentTimeMillis(),
            iconImageUri = iconImageUri
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

    internal fun calculateStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.distinct().sortedDescending()
        var streak = 0
        var expectedDate = LocalDate.now()

        for (date in sortedDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break // found a gap
            }
        }

        return streak
    }



    // Function to get habits with completion status for today
    fun getHabitsWithTodayCompletion(): Flow<List<Habit>> = flow {
        val today = LocalDate.now()
        val todayDayOfWeek = today.dayOfWeek.value - 1
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val habitEntities = habitDao.getAllHabits().first()

        val habits = habitEntities.filter { entity ->
            val activeDays = stringListConverter.fromString(entity.activeDays)
            todayDayOfWeek in activeDays
        }.map { entity ->
            val todayCompletions = habitDao.getHabitCompletionsInTimeRange(
                habitId = entity.id,
                startTime = startOfDay,
                endTime = endOfDay
            )

            val allCompletions = habitDao.getHabitCompletionHistory(entity.id).first()
            val completionDates = allCompletions
                .map { it.completedDate.toLocalDate() }
                .distinct()
                .sortedDescending()

            val streak = calculateStreak(completionDates)

            entity.toModel(
                completions = todayCompletions.map { it.completedDate.toLocalDate() },
                streak = streak
            )
        }

        emit(habits)
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
        activeDays: Set<Int> = setOf(0, 1, 2, 3, 4, 5, 6), // Default: all days
        iconImageUri: String? = null
    ): Long {
        val habitEntity = HabitEntity(
            name = name,
            iconRes = iconRes,
            frequency = frequency,
            reminderTime = reminderTime,
            activeDays = stringListConverter.toString(activeDays),
            iconImageUri = iconImageUri
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
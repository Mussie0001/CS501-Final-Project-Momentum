package com.example.momentum.data.repository

import com.example.momentum.data.dao.HabitDao
import com.example.momentum.data.entity.HabitCompletionEntity
import com.example.momentum.data.entity.HabitEntity
import com.example.momentum.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repository class to handle habit and habit completion data operations
 */
class HabitRepository(private val habitDao: HabitDao) {

    private fun HabitEntity.toModel(isCompleted: Boolean = false): Habit {
        return Habit(
            id = this.id,
            name = this.name,
            iconRes = this.iconRes,
            isCompleted = isCompleted
        )
    }

    private fun Habit.toEntity(frequency: Int = 1, reminderTime: String? = null): HabitEntity {
        return HabitEntity(
            id = this.id,
            name = this.name,
            iconRes = this.iconRes,
            frequency = frequency,
            reminderTime = reminderTime
        )
    }

    // Convert LocalDate to timestamp
    private fun LocalDate.toTimestamp(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getHabitsWithTodayCompletion(): Flow<List<Habit>> {
        val today = LocalDate.now().toTimestamp()
        return habitDao.getAllHabits().map { habitEntities ->
            val completionsToday = habitDao.getCompletionsForDate(today)
            habitEntities.map { habitEntity ->
                val isCompletedToday = completionsToday.any { it.habitId == habitEntity.id }
                habitEntity.toModel(isCompletedToday)
            }
        }
    }

    suspend fun toggleHabitCompletion(habit: Habit) {
        val today = LocalDate.now().toTimestamp()
        val existingCompletion = habitDao.getHabitCompletionByDate(habit.id, today)

        if (existingCompletion != null) {
            habitDao.deleteHabitCompletion(existingCompletion)
        } else {
            val completion = HabitCompletionEntity(
                habitId = habit.id,
                completedDate = today
            )
            habitDao.insertHabitCompletion(completion)
        }
    }

    suspend fun removeDuplicateHabits() {
        habitDao.removeDuplicateHabits()
    }

    // Get count
    suspend fun getHabitCount(): Int {
        return habitDao.getHabitCount()
    }

    // Add habit
    suspend fun addHabit(name: String, iconRes: Int, frequency: Int = 1, reminderTime: String? = null): Long {
        val habitEntity = HabitEntity(
            name = name,
            iconRes = iconRes,
            frequency = frequency,
            reminderTime = reminderTime
        )
        return habitDao.insertHabit(habitEntity)
    }

    // Update habit
    suspend fun updateHabit(habit: Habit, frequency: Int = 1, reminderTime: String? = null) {
        val habitEntity = habit.toEntity(frequency, reminderTime)
        habitDao.updateHabit(habitEntity)
    }

    // Delete habit
    suspend fun deleteHabit(habit: Habit) {
        val habitEntity = habit.toEntity()
        habitDao.deleteHabit(habitEntity)
    }

    // Get completions (History date range)
    fun getCompletionsForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Map<Long, List<LocalDate>>> {
        val startTimestamp = startDate.toTimestamp()
        val endTimestamp = endDate.toTimestamp()

        return habitDao.getHabitCompletionsInRange(startTimestamp, endTimestamp)
            .map { completions ->
                completions.groupBy(
                    { it.habitId },
                    {
                        LocalDate.ofEpochDay(it.completedDate / (24 * 60 * 60 * 1000))
                    }
                )
            }
    }
}
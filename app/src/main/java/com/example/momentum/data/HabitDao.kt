package com.example.momentum.data.dao

import androidx.room.*
import com.example.momentum.data.entity.HabitCompletionEntity
import com.example.momentum.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Habit-related database operations
 * Enhanced to support multiple completions per habit per day
 */
@Dao
interface HabitDao {
    // Habit operations
    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits ORDER BY createdAt")
    fun getAllHabits(): Flow<List<HabitEntity>>

    // Count habits
    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    // Clean up duplicates - keep only the oldest entry for each habit name
    @Query("DELETE FROM habits WHERE id NOT IN (SELECT MIN(id) FROM habits GROUP BY name)")
    suspend fun removeDuplicateHabits()

    // Delete all habits
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    // Habit completion operations
    @Insert
    suspend fun insertHabitCompletion(completion: HabitCompletionEntity): Long

    @Delete
    suspend fun deleteHabitCompletion(completion: HabitCompletionEntity)

    // Get a specific completion by habit and date
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedDate = :date")
    suspend fun getHabitCompletionByDate(habitId: Long, date: Long): HabitCompletionEntity?

    // Get all completions for a habit on a specific date
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedDate = :date ORDER BY completedTime ASC")
    suspend fun getHabitCompletionsForDate(habitId: Long, date: Long): List<HabitCompletionEntity>

    // Get all habit completions for a time range (useful for getting today's completions)
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedTime BETWEEN :startTime AND :endTime ORDER BY completedTime ASC")
    suspend fun getHabitCompletionsInTimeRange(habitId: Long, startTime: Long, endTime: Long): List<HabitCompletionEntity>

    // Get completion history for a habit
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedDate DESC")
    fun getHabitCompletionHistory(habitId: Long): Flow<List<HabitCompletionEntity>>

    // Get completions for a date range (for the history view)
    @Query("SELECT * FROM habit_completions WHERE completedDate BETWEEN :startDate AND :endDate ORDER BY completedDate DESC")
    fun getHabitCompletionsInRange(startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>>

    // Get all completions for a specific date (all habits)
    @Query("SELECT hc.* FROM habit_completions hc JOIN habits h ON hc.habitId = h.id WHERE completedDate = :date")
    suspend fun getCompletionsForDate(date: Long): List<HabitCompletionEntity>

    // Count completions by habit for a date range
    @Transaction
    @Query("SELECT h.*, COUNT(hc.id) as completionCount FROM habits h " +
            "LEFT JOIN habit_completions hc ON h.id = hc.habitId " +
            "AND hc.completedDate BETWEEN :startDate AND :endDate " +
            "GROUP BY h.id")
    fun getHabitsWithCompletionCount(startDate: Long, endDate: Long): Flow<List<HabitWithCompletionCount>>

    // Get completion count for a specific habit and date
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND completedDate = :date")
    suspend fun getCompletionCountForHabitAndDate(habitId: Long, date: Long): Int

    // Custom data class to represent habit completion counts
    data class HabitWithCompletionCount(
        val id: Long,
        val name: String,
        val iconRes: Int,
        val completionCount: Int
    )
}
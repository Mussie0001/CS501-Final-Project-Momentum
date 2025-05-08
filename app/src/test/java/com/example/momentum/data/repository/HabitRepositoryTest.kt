package com.example.momentum.data.repository

import com.example.momentum.data.dao.HabitDao
import com.example.momentum.data.entity.HabitCompletionEntity
import com.example.momentum.data.entity.HabitEntity
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HabitRepositoryTest {

    private val repo = HabitRepository(dummyDao())

    @Test
    fun streakIs3For3ConsecutiveDays() {
        val today = LocalDate.now()
        val streakDates = listOf(today, today.minusDays(1), today.minusDays(2))
        assertEquals(3, repo.calculateStreak(streakDates))
    }

    @Test
    fun streakIs1IfYesterdayIsMissed() {
        val today = LocalDate.now()
        val gapDates = listOf(today, today.minusDays(2))
        assertEquals(1, repo.calculateStreak(gapDates))
    }
}

// Dummy DAO to satisfy the HabitRepository constructor for unit testing
private fun dummyDao() = object : HabitDao {
    override suspend fun insertHabit(habit: HabitEntity) = 0L
    override suspend fun updateHabit(habit: HabitEntity) {}
    override suspend fun deleteHabit(habit: HabitEntity) {}
    override fun getAllHabits() = flowOf<List<HabitEntity>>(emptyList())
    override suspend fun getHabitCount() = 0
    override suspend fun removeDuplicateHabits() {}
    override suspend fun deleteAllHabits() {}
    override suspend fun getHabitById(habitId: Long) = null
    override suspend fun insertHabitCompletion(completion: HabitCompletionEntity) = 0L
    override suspend fun deleteHabitCompletion(completion: HabitCompletionEntity) {}
    override suspend fun getHabitCompletionByDate(habitId: Long, date: Long) = null
    override suspend fun getHabitCompletionsForDate(habitId: Long, date: Long) = emptyList<HabitCompletionEntity>()
    override suspend fun getHabitCompletionsInTimeRange(habitId: Long, startTime: Long, endTime: Long) = emptyList<HabitCompletionEntity>()
    override fun getHabitCompletionHistory(habitId: Long) = flowOf<List<HabitCompletionEntity>>(emptyList())
    override fun getHabitCompletionsInRange(startDate: Long, endDate: Long) = flowOf<List<HabitCompletionEntity>>(emptyList())
    override suspend fun getCompletionsForDate(date: Long) = emptyList<HabitCompletionEntity>()
    override fun getHabitsWithCompletionCount(startDate: Long, endDate: Long) =
        flowOf<List<HabitDao.HabitWithCompletionCount>>(emptyList())

    override suspend fun getCompletionCountForHabitAndDate(habitId: Long, date: Long) = 0
}

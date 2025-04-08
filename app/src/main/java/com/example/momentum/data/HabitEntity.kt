package com.example.momentum.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a habit in the database
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconRes: Int,
    val frequency: Int = 1,
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
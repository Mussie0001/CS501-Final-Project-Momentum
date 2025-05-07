package com.example.momentum.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.momentum.data.util.StringListConverter

/**
 * Entity representing a habit in the database
 * Updated to support frequency feature and active days
 */
@Entity(tableName = "habits")
@TypeConverters(StringListConverter::class)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconRes: Int,
    val frequency: Int = 1, // How many times per day this habit should be completed
    val reminderTime: String? = null,
    val activeDays: String = "0,1,2,3,4,5,6", // Default: all days active (stored as comma-separated day indices)
    val createdAt: Long = System.currentTimeMillis()
)
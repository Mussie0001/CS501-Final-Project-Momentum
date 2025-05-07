package com.example.momentum.data.util

import androidx.room.TypeConverter

/**
 * Converter for Room to handle conversion between a list of integers (day indices)
 * and a comma-separated string for storage in SQLite
 */
class StringListConverter {
    @TypeConverter
    fun fromString(value: String): Set<Int> {
        return if (value.isEmpty()) {
            emptySet()
        } else {
            value.split(",").map { it.trim().toInt() }.toSet()
        }
    }

    @TypeConverter
    fun toString(value: Set<Int>): String {
        return value.joinToString(",")
    }
}
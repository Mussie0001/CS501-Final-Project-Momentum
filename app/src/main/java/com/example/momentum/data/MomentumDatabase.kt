package com.example.momentum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.momentum.data.dao.HabitDao
import com.example.momentum.data.entity.HabitCompletionEntity
import com.example.momentum.data.entity.HabitEntity
import com.example.momentum.data.util.DateConverter
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.momentum.data.util.StringListConverter

/**
 * Main database class for the Momentum app
 */
@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 2, // new version
    exportSchema = false
)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class MomentumDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: MomentumDatabase? = null

        fun getDatabase(context: Context): MomentumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MomentumDatabase::class.java,
                    "momentum_database"
                )
                    .fallbackToDestructiveMigration() // recreate the database
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.example.momentum.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.momentum.data.repository.HabitRepository
import com.example.momentum.viewmodel.HabitViewModel

/**
 * Handles initial data setup for the app
 */
class DataInitializer(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("momentum_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_FIRST_RUN = "first_run_completed"
        private const val KEY_DB_INITIALIZED = "database_initialized"
    }

    fun isFirstRun(): Boolean {
        return !prefs.getBoolean(KEY_FIRST_RUN, false)
    }

    fun markFirstRunComplete() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
    }

    fun isDatabaseInitialized(): Boolean {
        return prefs.getBoolean(KEY_DB_INITIALIZED, false)
    }

    fun markDatabaseInitialized() {
        prefs.edit().putBoolean(KEY_DB_INITIALIZED, true).apply()
    }

    fun resetInitializationState() {
        prefs.edit()
            .putBoolean(KEY_FIRST_RUN, false)
            .putBoolean(KEY_DB_INITIALIZED, false)
            .apply()
    }
}
package com.example.momentum

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.momentum.data.DataInitializer
import com.example.momentum.data.MomentumDatabase
import com.example.momentum.data.repository.HabitRepository
import com.example.momentum.ui.AddHabitForm
import com.example.momentum.ui.HistoryScreen
import com.example.momentum.ui.HomeScreen
import com.example.momentum.ui.SettingsScreen
import com.example.momentum.ui.ThemePreference
import com.example.momentum.ui.theme.MomentumTheme
import com.example.momentum.viewmodel.HabitViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : ComponentActivity() {

    private lateinit var themePreference: ThemePreference
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var database: MomentumDatabase
    private lateinit var repository: HabitRepository

    // Add this property to track the previous tab
    private var lastSelectedTab = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize theme preference
        themePreference = ThemePreference(this)

        // Initialize database and repository
        database = MomentumDatabase.getDatabase(this)
        repository = HabitRepository(database.habitDao())

        // Initialize ViewModel
        habitViewModel = ViewModelProvider(
            this,
            HabitViewModel.Factory(repository)
        )[HabitViewModel::class.java]

        // Store a DB initialization flag in SharedPreferences
        val prefs = getSharedPreferences("momentum_prefs", MODE_PRIVATE)
        val isDbInitialized = prefs.getBoolean("db_initialized", false)

        // Clean up duplicates and populate sample data if needed
        lifecycleScope.launch {
            // First, check if we need to clean up duplicates
            if (!isDbInitialized) {
                // First-time run - delete any existing data and populate from scratch
                Log.d("MainActivity", "First time run detected. Initializing database with sample data.")

                // Clean the database manually if deleteAllHabits is not available
                try {
                    val habitCount = repository.getHabitCount()
                    if (habitCount > 0) {
                        Log.d("MainActivity", "Clearing existing data. Found $habitCount habits.")
                        repository.removeDuplicateHabits()

                        // Get all habits and delete them individually if needed
                        val habits = habitViewModel.habits.value
                        for (habit in habits) {
                            repository.deleteHabit(habit)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error clearing existing data", e)
                }

                // Populate with sample data
                populateSampleData()

                // Mark as initialized
                prefs.edit().putBoolean("db_initialized", true).apply()
                Log.d("MainActivity", "Database marked as initialized.")
            } else {
                // App has run before, just clean up any duplicates
                Log.d("MainActivity", "App has run before. Checking for duplicates.")
                repository.removeDuplicateHabits()
            }
        }

        setContent {
            val userDarkMode = themePreference.isDarkMode.collectAsStateWithLifecycle(
                initialValue = false
            ).value

            val isDarkTheme = userDarkMode
            MomentumTheme(darkTheme = isDarkTheme) {
                var selectedTab by remember { mutableStateOf("home") }
                var quote by remember { mutableStateOf("") }

                // Collect habits from ViewModel as a state
                val habits = habitViewModel.habits.collectAsStateWithLifecycle().value

                LaunchedEffect(true) {
                    quote = fetchQuote()
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == "home",
                                onClick = {
                                    // If navigating back to home from another tab, refresh habits
                                    if (selectedTab != "home") {
                                        Log.d("MainActivity", "Returning to home tab, refreshing habits")
                                        habitViewModel.refreshHabits()
                                    }
                                    lastSelectedTab = selectedTab
                                    selectedTab = "home"
                                },
                                icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "add",
                                onClick = {
                                    lastSelectedTab = selectedTab
                                    selectedTab = "add"
                                },
                                icon = { Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add") },
                                label = { Text("New Habit") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "history",
                                onClick = {
                                    lastSelectedTab = selectedTab
                                    selectedTab = "history"
                                },
                                icon = { Icon(painterResource(id = R.drawable.ic_history), contentDescription = "History") },
                                label = { Text("History") }
                            )

                            NavigationBarItem(
                                selected = selectedTab == "settings",
                                onClick = {
                                    lastSelectedTab = selectedTab
                                    selectedTab = "settings"
                                },
                                icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                                label = { Text("Settings") }
                            )
                        }
                    }

                ) { padding ->
                    when (selectedTab) {
                        // navigate to home screen
                        "home" -> HomeScreen(
                            habits = habits,
                            onHabitToggle = { index ->
                                habitViewModel.toggleHabitCompletion(index)
                            },
                            onHabitDelete = { index ->
                                habitViewModel.deleteHabit(index)
                            },
                            quote = quote,
                            onAddHabitClick = { selectedTab = "add" },
                            modifier = Modifier.padding(padding),
                            onTabSelected = {
                                lastSelectedTab = selectedTab
                                selectedTab = it
                            },
                        )
                        // navigate to add new habit page
                        "add" -> AddHabitForm(
                            onSave = { name, freq, reminder ->
                                habitViewModel.addHabit(
                                    name = name,
                                    iconRes = R.drawable.ic_exercise,
                                    frequency = freq,
                                    reminderTime = reminder
                                )
                                lastSelectedTab = selectedTab
                                selectedTab = "home"
                            },
                            onCancel = {
                                lastSelectedTab = selectedTab
                                selectedTab = "home"
                            }
                        )

                        "history" -> HistoryScreen()

                        // Settings screen implementation
                        "settings" -> SettingsScreen(
                            isDarkMode = userDarkMode,
                            onThemeToggle = { newMode ->
                                lifecycleScope.launch {
                                    themePreference.updateTheme(newMode)
                                }
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchQuote(): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = URL("https://zenquotes.io/api/random")
            val conn = url.openConnection() as HttpsURLConnection
            try {
                val response = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)
                val quote = jsonArray.getJSONObject(0).getString("q")
                "\"$quote\"" // looks better with quotes around it
            } catch (e: Exception) {
                Log.e("ZenQuotes", "Error fetching quote", e)
                "You don't have to be extreme, just consistent." // fallback quote from wireframe
            } finally {
                conn.disconnect()
            }
        }
    }

    /**
     * Populates the database with sample habits if the database is empty
     */
    private suspend fun populateSampleData() {
        // Check if habits already exist by checking the count
        val currentHabits = habitViewModel.habits.value

        // Only populate if there are no habits in the database
        if (currentHabits.isEmpty()) {
            Log.d("MainActivity", "Populating sample data - database is empty")

            // Sample habits from original app
            val sampleHabits = listOf(
                Triple("Exercise", R.drawable.ic_exercise, 1),
                Triple("Reading", R.drawable.ic_reading, 1),
                Triple("Drink Water", R.drawable.ic_water, 1),
                Triple("Study", R.drawable.ic_study, 1)
            )

            // Add each sample habit
            sampleHabits.forEach { (name, icon, frequency) ->
                try {
                    repository.addHabit(
                        name = name,
                        iconRes = icon,
                        frequency = frequency
                    )
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error adding habit $name", e)
                }
            }

            // Complete some habits for today to match the initial state
            // Wait for habits to be loaded from database
            kotlinx.coroutines.delay(500)

            try {
                // Get updated habits and complete the selected ones (Drink Water and Study)
                val updatedHabits = habitViewModel.habits.value

                // Find the indices of "Drink Water" and "Study"
                val waterIndex = updatedHabits.indexOfFirst { it.name == "Drink Water" }
                val studyIndex = updatedHabits.indexOfFirst { it.name == "Study" }

                // Toggle completion if found
                if (waterIndex >= 0) {
                    habitViewModel.toggleHabitCompletion(waterIndex)
                }

                if (studyIndex >= 0) {
                    habitViewModel.toggleHabitCompletion(studyIndex)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error completing habits", e)
            }
        } else {
            Log.d("MainActivity", "Sample data already exists - skipping population")
        }
    }
}
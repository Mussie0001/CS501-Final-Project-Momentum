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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themePreference = ThemePreference(this)
        database = MomentumDatabase.getDatabase(this)
        repository = HabitRepository(database.habitDao())

        habitViewModel = ViewModelProvider(
            this,
            HabitViewModel.Factory(repository)
        )[HabitViewModel::class.java]

        val prefs = getSharedPreferences("momentum_prefs", MODE_PRIVATE)
        val isDbInitialized = prefs.getBoolean("db_initialized", false)

        lifecycleScope.launch {
            if (!isDbInitialized) {
                Log.d("MainActivity", "First time run detected. Initializing database with sample data.")
                repository.deleteAllHabits()
                populateSampleData()
                prefs.edit().putBoolean("db_initialized", true).apply()
                Log.d("MainActivity", "Database marked as initialized.")
            } else {
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
                val habits = habitViewModel.habits.collectAsStateWithLifecycle().value

                LaunchedEffect(true) {
                    quote = fetchQuote()
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == "home",
                                onClick = { selectedTab = "home" },
                                icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "add",
                                onClick = { selectedTab = "add"},
                                icon = { Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add") },
                                label = { Text("New Habit") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "history",
                                onClick = { selectedTab = "history" },
                                icon = { Icon(painterResource(id = R.drawable.ic_history), contentDescription = "History") },
                                label = { Text("History") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "settings",
                                onClick = { selectedTab = "settings" },
                                icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                                label = { Text("Settings") }
                            )
                        }
                    }
                ) { padding ->
                    when (selectedTab) {
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
                            onTabSelected = { selectedTab = it },
                        )
                        "add" -> AddHabitForm(
                            onSave = { name, freq, reminder ->
                                habitViewModel.addHabit(
                                    name = name,
                                    iconRes = R.drawable.ic_exercise,
                                    frequency = freq,
                                    reminderTime = reminder
                                )
                                selectedTab = "home"
                            },
                            onCancel = { selectedTab = "home" }
                        )
                        "history" -> HistoryScreen()
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
                "\"$quote\""
            } catch (e: Exception) {
                Log.e("ZenQuotes", "Error fetching quote", e)
                "You don't have to be extreme, just consistent."
            } finally {
                conn.disconnect()
            }
        }
    }

    private suspend fun populateSampleData() {
        val currentHabits = habitViewModel.habits.value

        if (currentHabits.isEmpty()) {
            Log.d("MainActivity", "Populating sample data - database is empty")

            val sampleHabits = listOf(
                Triple("Exercise", R.drawable.ic_exercise, 1),
                Triple("Reading", R.drawable.ic_reading, 1),
                Triple("Drink Water", R.drawable.ic_water, 1),
                Triple("Study", R.drawable.ic_study, 1)
            )

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

            kotlinx.coroutines.delay(500)

            try {
                val updatedHabits = habitViewModel.habits.value
                val waterIndex = updatedHabits.indexOfFirst { it.name == "Drink Water" }
                val studyIndex = updatedHabits.indexOfFirst { it.name == "Study" }

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

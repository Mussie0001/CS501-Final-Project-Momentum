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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.text.SimpleDateFormat
import java.util.*
import android.content.res.Configuration
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {

    private lateinit var themePreference: ThemePreference
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var database: MomentumDatabase
    private lateinit var repository: HabitRepository

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

        // Clean up duplicates and populate sample data if needed
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = getSharedPreferences("momentum_prefs", MODE_PRIVATE)
            val isDbInitialized = prefs.getBoolean("db_initialized", false)

            if (!isDbInitialized) {
                Log.d("MainActivity", "First run: Initializing DB")
                try {
                    // Clear existing data
                    repository.removeDuplicateHabits()
                    habitViewModel.habits.value.forEach { repository.deleteHabit(it) }

                    // Populate sample data
                    populateSampleData()

                    // Mark as initialized
                    prefs.edit().putBoolean("db_initialized", true).apply()
                } catch (e: Exception) {
                    Log.e("MainActivity", "DB init failed", e)
                }
            } else {
                repository.removeDuplicateHabits() // Regular cleanup
            }
        }

        setContent {
            val userDarkMode = themePreference.isDarkMode.collectAsStateWithLifecycle(
                initialValue = false
            ).value

            val isDarkTheme = userDarkMode

            // Get current orientation
            val configuration = LocalConfiguration.current
            val isLandscape = remember {
                derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            }.value

            MomentumTheme(darkTheme = isDarkTheme) {
                var selectedTab by rememberSaveable { mutableStateOf("home") }
                var quote by rememberSaveable { mutableStateOf("") }

                // Collect habits from ViewModel as a state
                val habits by habitViewModel.habits.collectAsStateWithLifecycle()

                LaunchedEffect(true) {
                    if (quote.isEmpty()) {
                        quote = fetchQuote()
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (!isLandscape) {
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
                    }
                ) { padding ->
                    if (isLandscape) {
                        // Landscape layout with side navigation
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Side navigation bar
                            NavigationRail {
                                NavigationRailItem(
                                    selected = selectedTab == "home",
                                    onClick = {
                                        if (selectedTab != "home") {
                                            habitViewModel.refreshHabits()
                                        }
                                        lastSelectedTab = selectedTab
                                        selectedTab = "home"
                                    },
                                    icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
                                    label = { Text("Home") }
                                )

                                NavigationRailItem(
                                    selected = selectedTab == "add",
                                    onClick = {
                                        lastSelectedTab = selectedTab
                                        selectedTab = "add"
                                    },
                                    icon = { Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add") },
                                    label = { Text("New") }
                                )

                                NavigationRailItem(
                                    selected = selectedTab == "history",
                                    onClick = {
                                        lastSelectedTab = selectedTab
                                        selectedTab = "history"
                                    },
                                    icon = { Icon(painterResource(id = R.drawable.ic_history), contentDescription = "History") },
                                    label = { Text("History") }
                                )

                                NavigationRailItem(
                                    selected = selectedTab == "settings",
                                    onClick = {
                                        lastSelectedTab = selectedTab
                                        selectedTab = "settings"
                                    },
                                    icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }

                            // Content area
                            Box(modifier = Modifier.weight(1f)) {
                                when (selectedTab) {
                                    "home" -> HomeScreen(
                                        habits = habits,
                                        onHabitToggle = { habitIndex, completionIndex -> habitViewModel.toggleHabitCompletion(habitIndex, completionIndex) },
                                        onHabitDelete = { index -> habitViewModel.deleteHabit(index) },
                                        quote = quote,
                                        onAddHabitClick = { selectedTab = "add" },
                                        modifier = Modifier.padding(padding),
                                        onTabSelected = { lastSelectedTab = selectedTab; selectedTab = it },
                                        isLandscape = true
                                    )
                                    "add" -> AddHabitForm(
                                        onSave = { name, freq, reminder, activeDays, iconImageUri ->
                                            habitViewModel.addHabit(
                                                name = name,
                                                iconRes = R.drawable.ic_exercise,
                                                frequency = freq,
                                                reminderTime = reminder,
                                                activeDays = activeDays,
                                                iconImageUri = iconImageUri
                                            )
                                            lastSelectedTab = selectedTab
                                            selectedTab = "home"
                                        },
                                        onCancel = {
                                            lastSelectedTab = selectedTab
                                            selectedTab = "home"
                                        },
                                        isLandscape = true
                                    )
                                    "history" -> HistoryScreen(isLandscape = true)
                                    "settings" -> SettingsScreen(
                                        isDarkMode = userDarkMode,
                                        onThemeToggle = { newMode ->
                                            lifecycleScope.launch {
                                                themePreference.updateTheme(newMode)
                                            }
                                        },
                                        modifier = Modifier.padding(padding),
                                        isLandscape = true
                                    )
                                }
                            }
                        }
                    } else {
                        // Portrait mode content
                        when (selectedTab) {
                            "home" -> HomeScreen(
                                habits = habits,
                                onHabitToggle = { habitIndex, completionIndex -> habitViewModel.toggleHabitCompletion(habitIndex, completionIndex) },
                                onHabitDelete = { index -> habitViewModel.deleteHabit(index) },
                                quote = quote,
                                onAddHabitClick = { selectedTab = "add" },
                                modifier = Modifier.padding(padding),
                                onTabSelected = { lastSelectedTab = selectedTab; selectedTab = it },
                                isLandscape = false
                            )
                            "add" -> AddHabitForm(
                                onSave = { name, freq, reminder, activeDays, iconImageUri ->
                                    habitViewModel.addHabit(
                                        name = name,
                                        iconRes = R.drawable.ic_exercise,
                                        frequency = freq,
                                        reminderTime = reminder,
                                        activeDays = activeDays,
                                        iconImageUri = iconImageUri
                                    )
                                    lastSelectedTab = selectedTab
                                    selectedTab = "home"
                                },
                                onCancel = {
                                    lastSelectedTab = selectedTab
                                    selectedTab = "home"
                                },
                                isLandscape = false
                            )
                            "history" -> HistoryScreen(isLandscape = false)
                            "settings" -> SettingsScreen(
                                isDarkMode = userDarkMode,
                                onThemeToggle = { newMode ->
                                    lifecycleScope.launch {
                                        themePreference.updateTheme(newMode)
                                    }
                                },
                                modifier = Modifier.padding(padding),
                                isLandscape = false
                            )
                        }
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
    private suspend fun populateSampleData() = withContext(Dispatchers.IO) {
        val currentHabits = habitViewModel.habits.value
        if (currentHabits.isEmpty()) {
            Log.d("MainActivity", "Populating sample data")
            val sampleHabits = listOf(
                Triple("Exercise", R.drawable.ic_exercise, 1),
                Triple("Reading", R.drawable.ic_reading, 1),
                Triple("Drink Water", R.drawable.ic_water, 1),
                Triple("Study", R.drawable.ic_study, 1)
            )

            sampleHabits.forEach { (name, icon, frequency) ->
                repository.addHabit(
                    name = name,
                    iconRes = icon,
                    frequency = frequency
                )
            }

            // Complete "Drink Water" and "Study" for demo
            val updatedHabits = habitViewModel.habits.value
            updatedHabits.indexOfFirst { it.name == "Drink Water" }.takeIf { it >= 0 }?.let {
                habitViewModel.toggleHabitCompletion(it, 0)
            }
            updatedHabits.indexOfFirst { it.name == "Study" }.takeIf { it >= 0 }?.let {
                habitViewModel.toggleHabitCompletion(it, 0)
            }
        }
    }
}
package com.example.momentum

import android.os.Bundle
import android.util.Log
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.momentum.data.MomentumDatabase
import com.example.momentum.data.repository.HabitRepository
import com.example.momentum.ui.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {

    private lateinit var themePreference: ThemePreference
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var database: MomentumDatabase
    private lateinit var repository: HabitRepository

    private var lastSelectedTab = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themePreference = ThemePreference(this)
        database = MomentumDatabase.getDatabase(this)
        repository = HabitRepository(database.habitDao())

        habitViewModel = ViewModelProvider(
            this,
            HabitViewModel.Factory(repository)
        )[HabitViewModel::class.java]

        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = getSharedPreferences("momentum_prefs", MODE_PRIVATE)
            val isDbInitialized = prefs.getBoolean("db_initialized", false)

            if (!isDbInitialized) {
                Log.d("MainActivity", "First run: Initializing DB")
                try {
                    repository.removeDuplicateHabits()
                    habitViewModel.habits.value.forEach { repository.deleteHabit(it) }
                    populateSampleData()
                    prefs.edit().putBoolean("db_initialized", true).apply()
                } catch (e: Exception) {
                    Log.e("MainActivity", "DB init failed", e)
                }
            } else {
                repository.removeDuplicateHabits()
            }
        }

        setContent {
            val userDarkMode = themePreference.isDarkMode.collectAsStateWithLifecycle(false).value
            val isDarkTheme = userDarkMode

            val configuration = LocalConfiguration.current
            val isLandscape = remember {
                derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            }.value

            // Step counter state
            val context = this
            val sensorManager = remember { context.getSystemService(SENSOR_SERVICE) as SensorManager }
            val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
            var totalSteps by remember { mutableStateOf(0f) }

            DisposableEffect(Unit) {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                            totalSteps = event.values.firstOrNull() ?: 0f
                        }
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                stepSensor?.let {
                    sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
                }

                onDispose {
                    sensorManager.unregisterListener(listener)
                }
            }

            MomentumTheme(darkTheme = isDarkTheme) {
                var selectedTab by rememberSaveable { mutableStateOf("home") }
                var quote by rememberSaveable { mutableStateOf("") }
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
                                        if (selectedTab != "home") habitViewModel.refreshHabits()
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
                        Row(modifier = Modifier.fillMaxSize()) {
                            NavigationRail {
                                NavigationRailItem(
                                    selected = selectedTab == "home",
                                    onClick = {
                                        if (selectedTab != "home") habitViewModel.refreshHabits()
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
                                        isLandscape = true,
                                        fetchQuote = { fetchQuote() },
                                        steps = totalSteps.toInt()
                                    )
                                    "add" -> AddHabitForm(
                                        onSave = { name, freq, reminder, activeDays, iconImageUri ->
                                            habitViewModel.addHabit(name, R.drawable.ic_exercise, freq, reminder, activeDays, iconImageUri)
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
                        when (selectedTab) {
                            "home" -> HomeScreen(
                                habits = habits,
                                onHabitToggle = { habitIndex, completionIndex -> habitViewModel.toggleHabitCompletion(habitIndex, completionIndex) },
                                onHabitDelete = { index -> habitViewModel.deleteHabit(index) },
                                quote = quote,
                                onAddHabitClick = { selectedTab = "add" },
                                modifier = Modifier.padding(padding),
                                onTabSelected = { lastSelectedTab = selectedTab; selectedTab = it },
                                isLandscape = false,
                                fetchQuote = { fetchQuote() },
                                steps = totalSteps.toInt()
                            )
                            "add" -> AddHabitForm(
                                onSave = { name, freq, reminder, activeDays, iconImageUri ->
                                    habitViewModel.addHabit(name, R.drawable.ic_exercise, freq, reminder, activeDays, iconImageUri)
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
        return withContext(Dispatchers.IO) {
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
                repository.addHabit(name = name, iconRes = icon, frequency = frequency)
            }
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
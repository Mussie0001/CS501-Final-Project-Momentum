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
import com.example.momentum.model.Habit
import com.example.momentum.ui.HomeScreen
import com.example.momentum.ui.theme.MomentumTheme
import org.json.JSONArray
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


val sampleHabits = listOf(
    Habit("Exercise", R.drawable.ic_exercise, false),
    Habit("Reading", R.drawable.ic_reading, false),
    Habit("Drink Water", R.drawable.ic_water, true),
    Habit("Study", R.drawable.ic_study, true)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MomentumTheme {
                var selectedTab by remember { mutableStateOf("home") }
                var quote by remember { mutableStateOf("") }
                var habits by remember { mutableStateOf(sampleHabits.toMutableList()) }

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
                                habits = habits.toMutableList().also {
                                    it[index] = it[index].copy(isCompleted = !it[index].isCompleted)
                                }
                            },
                            quote = quote,
                            onAddHabitClick = { /* WORK IN PROGRESS -- SOON */ },
                            modifier = Modifier.padding(padding),
                            onTabSelected = { selectedTab = it }
                        )
                        // "history" -> HistoryScreen()
                        // "settings" -> SettingsScreen()
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
                "\"$quote\"" // looks better with quotes around it
            } catch (e: Exception) {
                Log.e("ZenQuotes", "Error fetching quote", e)
                "You don’t have to be extreme, just consistent." // fallback quote from wireframe
            } finally {
                conn.disconnect()
            }
        }
    }


}





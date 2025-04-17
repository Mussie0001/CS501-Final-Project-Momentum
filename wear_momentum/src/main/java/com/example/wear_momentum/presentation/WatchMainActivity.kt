package com.example.wear_momentum.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wear_momentum.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class WatchMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchHomeScreen()
        }
    }
}

@Composable
fun WatchHomeScreen() {
    var quote by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        quote = fetchQuote()
    }

    val habits = remember {
        mutableStateListOf(
            Habit("Exercise", R.drawable.ic_exercise_watch, true),
            Habit("Reading", R.drawable.ic_reading_watch, false),
            Habit("Drink Water", R.drawable.ic_water_watch, true),
            Habit("Study", R.drawable.ic_study_watch, false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Momentum",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = quote,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }

        habits.forEachIndexed { index, habit ->
            HabitRow(habit = habit) {
                habits[index] = habit.copy(isCompleted = !habit.isCompleted)
            }
        }
    }
}

@Composable
fun HabitRow(habit: Habit, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = habit.iconRes),
                    contentDescription = habit.name,
                    modifier = Modifier.size(24.dp)
                )
                if (habit.isCompleted) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

data class Habit(val name: String, val iconRes: Int, val isCompleted: Boolean)

suspend fun fetchQuote(): String {
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
            // fallback quote
            "You don't have to be extreme, just consistent."
        } finally {
            conn.disconnect()
        }
    }
}
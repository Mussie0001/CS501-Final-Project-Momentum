package com.example.wear_momentum.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import com.example.wear_momentum.presentation.theme.MomentumTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wear_momentum.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class WatchMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MomentumTheme {
                WatchHomeScreen()
            }
        }
    }
}

@Composable
fun WatchHomeScreen() {
    var quote by remember { mutableStateOf("Loading...") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // debounce refresh mechanism
    var lastRefreshTime by remember { mutableStateOf(0L) }

    // step counter state (sensor-driven)
    var totalSteps by remember { mutableStateOf(0f) }

    // hardcoded value for demo
    // val stepsToDisplay = 12345
    val stepsToDisplay = totalSteps.toInt()

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    totalSteps = event.values.firstOrNull() ?: 0f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        stepSensor?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(stepListener)
        }
    }

    // Fetch initial quote
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clickable {
                    val now = System.currentTimeMillis()
                    if (now - lastRefreshTime > 1500) {
                        lastRefreshTime = now
                        quote = "Refreshing..."
                        coroutineScope.launch {
                            quote = fetchQuote()
                        }
                    }
                },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = quote,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 180.dp)
        ) {
            items(habits) { habit ->
                HabitGridItem(habit = habit) {
                    val index = habits.indexOf(habit)
                    if (index >= 0) {
                        habits[index] = habit.copy(isCompleted = !habit.isCompleted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val stepColor = if (stepsToDisplay >= 10_000) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .defaultMinSize(minHeight = 60.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "Steps Today",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepsToDisplay.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = stepColor
                )
            }
        }
    }
}

@Composable
fun HabitGridItem(habit: Habit, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = habit.iconRes),
                    contentDescription = habit.name,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            alpha = if (habit.isCompleted) 0.4f else 1.0f
                        }
                )
                if (habit.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
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
            "You don't have to be extreme, just consistent."
        } finally {
            conn.disconnect()
        }
    }
}



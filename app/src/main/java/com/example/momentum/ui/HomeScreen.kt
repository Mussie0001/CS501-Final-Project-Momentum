package com.example.momentum.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentum.R
import com.example.momentum.model.Habit

@Composable
fun HomeScreen(
    habits: List<Habit>,
    onHabitToggle: (Int) -> Unit,
    onAddHabitClick: () -> Unit,
    quote: String,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit
) {
    val completedCount = habits.count { it.isCompleted }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { onTabSelected("home") },
                    icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onTabSelected("add") },
                    icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = "Add") },
                    label = { Text("New Habit") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onTabSelected("history") },
                    icon = { Icon(painterResource(R.drawable.ic_history), contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onTabSelected("settings") },
                    icon = { Icon(painterResource(R.drawable.ic_settings), contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // app title
            Text(
                text = "Momentum",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // quote box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = quote,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // habit list and summary grouped together
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                habits.forEachIndexed { index, habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHabitToggle(index) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0E0E0),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Image(
                                painter = painterResource(id = habit.iconRes),
                                contentDescription = habit.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(habit.name, modifier = Modifier.weight(1f))
                        if (habit.isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = "Completed")
                        }
                    }
                }

                Divider()

                Text(
                    text = "$completedCount of ${habits.size} habits completed today",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}






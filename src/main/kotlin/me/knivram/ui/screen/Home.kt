package me.knivram.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import me.knivram.repository.Workout
import me.knivram.repository.WorkoutRepository
import java.time.format.DateTimeFormatter

class Home : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        var workouts by remember { mutableStateOf(listOf<Workout>()) }
        LaunchedEffect(Unit) {
            workouts = WorkoutRepository.getAll()
        }
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TopAppBar(title = { Text("AthliTrack") })
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { navigator.push(ExercisesScreen()) }) { Text("Exercises") }
                Button(onClick = { navigator.push(TemplatesScreen()) }) { Text("Templates") }
                Button(onClick = { navigator.push(AddEditWorkoutScreen()) }) { Text("Add Workout") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(workouts) { workout ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Name: ${workout.name}")
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            Text("Start: ${workout.startDatetime.format(formatter)}")
                            Text("End: ${workout.endDatetime.format(formatter)}")
                            val duration = workout.duration()
                            val hours = duration.toHours()
                            val minutes = duration.toMinutes() % 60
                            Text("Duration: ${hours}h ${minutes}m")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { navigator.push(ViewWorkoutScreen(workout)) }) { Text("View") }
                                Button(onClick = { navigator.push(AddEditWorkoutScreen(workout)) }) { Text("Edit") }
                                Button(onClick = {
                                    coroutineScope.launch {
                                        WorkoutRepository.delete(workout.id)
                                        workouts = WorkoutRepository.getAll()
                                    }
                                }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

package me.knivram.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.knivram.repository.Exercise
import me.knivram.repository.WorkoutRepository
import java.time.format.DateTimeFormatter

data class ProgressRecord(val date: String, val volume: Double)

class ProgressScreen(private val exercise: Exercise) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var records by remember { mutableStateOf(listOf<ProgressRecord>()) }
        var nextExpected by remember { mutableStateOf<Double?>(null) }
        LaunchedEffect(exercise) {
            val workouts = WorkoutRepository.getAll().filter { workout ->
                workout.exercises.any { it.exerciseId == exercise.id }
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val recs = workouts.map { workout ->
                val volume = workout.exercises.filter { it.exerciseId == exercise.id }
                    .flatMap { it.sets }
                    .sumOf { it.weight * it.reps }
                ProgressRecord(workout.startDatetime.format(formatter), volume)
            }.sortedBy { it.date }
            records = recs
            if (recs.size >= 2) {
                val increases = recs.zipWithNext().map { (a, b) -> b.volume - a.volume }
                val avgIncrease = increases.average()
                nextExpected = recs.last().volume + avgIncrease
            }
        }
        Scaffold(topBar = {
            TopAppBar(title = { Text("Progress of ${exercise.name}") }, navigationIcon = {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            })
        }) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (records.isEmpty()) {
                    Text("No progress records available.")
                } else {
                    LazyColumn {
                        items(records) { record ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(record.date)
                                Text("Volume: ${record.volume}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    nextExpected?.let {
                        Text("Next Expected Volume: $it")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chart Placeholder")
                }
            }
        }
    }
}

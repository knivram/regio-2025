package me.knivram.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.knivram.repository.Workout
import me.knivram.repository.Exercise
import me.knivram.repository.ExerciseRepository

class ViewWorkoutScreen(private val workout: Workout) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var exerciseMap by remember { mutableStateOf<Map<Int, Exercise>>(emptyMap()) }
        LaunchedEffect(Unit) {
            val exercises = ExerciseRepository.getAll()
            exerciseMap = exercises.associateBy { it.id }
        }
        var totalVolume by remember { mutableStateOf(0.0) }
        var totalSets by remember { mutableStateOf(0) }
        var totalReps by remember { mutableStateOf(0) }
        LaunchedEffect(workout) {
            var volume = 0.0
            var sets = 0
            var reps = 0
            workout.exercises.forEach { ex ->
                ex.sets.forEach { set ->
                    volume += set.weight * set.reps
                    sets++
                    reps += set.reps
                }
            }
            totalVolume = volume
            totalSets = sets
            totalReps = reps
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workout \"${workout.name}\"") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text("Total Volume: $totalVolume")
                    Text("Total Sets: $totalSets")
                    Text("Total Reps: $totalReps")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(workout.exercises) { we ->
                    val exercise = exerciseMap[we.exerciseId]
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Exercise: ${exercise?.name ?: "Unknown"}")
                            Column {
                                we.sets.forEach { set ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Set ${set.setNumber}: ${set.weight}kg x ${set.reps} reps")
                                    }
                                }
                            }
                            Button(onClick = { navigator.push(ProgressScreen(exercise ?: return@Button)) }) {
                                Text("My Progress")
                            }
                        }
                    }
                }
            }
        }
    }
}

package me.knivram.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import me.knivram.repository.ExerciseRepository
import me.knivram.repository.Workout
import me.knivram.repository.WorkoutExercise
import me.knivram.repository.WorkoutSet
import me.knivram.repository.WorkoutRepository
import me.knivram.repository.TemplateRepository
import me.knivram.repository.Template
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddEditWorkoutScreen(private val workout: Workout? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        var name by remember { mutableStateOf(workout?.name ?: "") }
        var startText by remember { mutableStateOf(workout?.startDatetime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: "") }
        var endText by remember { mutableStateOf(workout?.endDatetime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: "") }
        var workoutExercises by remember { mutableStateOf(workout?.exercises?.toMutableList() ?: mutableListOf<WorkoutExercise>()) }
        val availableExercises = ExerciseRepository.getAll().associate { it.id to it.name }
        var showTemplateDialog by remember { mutableStateOf(false) }
        var selectedTemplate by remember { mutableStateOf<Template?>(null) }
        if (showTemplateDialog) {
            AlertDialog(
                onDismissRequest = { showTemplateDialog = false },
                title = { Text("Select Template") },
                text = {
                    Column {
                        TemplateRepository.getAll().forEach { tmpl ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                                selectedTemplate = tmpl
                            }) {
                                Text(tmpl.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        selectedTemplate?.let { tmpl ->
                            workoutExercises.clear()
                            tmpl.exercises.forEach { te ->
                                val sets = (1..te.sets).map { idx -> WorkoutSet(0, 0, idx, 0.0, te.reps) }
                                workoutExercises.add(WorkoutExercise(0, 0, te.exerciseId, te.position, sets))
                            }
                        }
                        showTemplateDialog = false
                    }) { Text("Load") }
                },
                dismissButton = {
                    Button(onClick = { showTemplateDialog = false }) { Text("Cancel") }
                }
            )
        }
        Scaffold(topBar = {
            TopAppBar(title = { Text(if (workout == null) "Add Workout" else "Edit Workout \"${workout.name}\"") }, navigationIcon = {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            })
        }) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Workout Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Start (yyyy-MM-dd HH:mm)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("End (yyyy-MM-dd HH:mm)") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { showTemplateDialog = true }) { Text("Load Template") }
                    Button(onClick = {
                        availableExercises.keys.firstOrNull()?.let { exId ->
                            val newSets = listOf(WorkoutSet(0, 0, 1, 0.0, 0))
                            workoutExercises.add(WorkoutExercise(0, 0, exId, workoutExercises.size + 1, newSets))
                        }
                    }) { Text("Add Exercise") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Exercises:")
                LazyColumn {
                    itemsIndexed(workoutExercises) { index, we ->
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Exercise: ${availableExercises[we.exerciseId] ?: "Unknown"}")
                                we.sets.forEachIndexed { setIndex, set ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        OutlinedTextField(
                                            value = set.weight.toString(),
                                            onValueChange = {
                                                val newWeight = it.toDoubleOrNull() ?: 0.0
                                                we.sets = we.sets.toMutableList().also { list ->
                                                    list[setIndex] = set.copy(weight = newWeight)
                                                }
                                            },
                                            label = { Text("Weight") },
                                            modifier = Modifier.width(100.dp)
                                        )
                                        OutlinedTextField(
                                            value = set.reps.toString(),
                                            onValueChange = {
                                                val newReps = it.toIntOrNull() ?: 0
                                                we.sets = we.sets.toMutableList().also { list ->
                                                    list[setIndex] = set.copy(reps = newReps)
                                                }
                                            },
                                            label = { Text("Reps") },
                                            modifier = Modifier.width(100.dp)
                                        )
                                    }
                                }
                                Button(onClick = {
                                    val newSetNumber = we.sets.size + 1
                                    we.sets = we.sets + WorkoutSet(0, 0, newSetNumber, 0.0, 0)
                                }) { Text("Add Set") }
                                Button(onClick = {
                                    if (we.sets.isNotEmpty()) {
                                        we.sets = we.sets.dropLast(1)
                                    }
                                }) { Text("Remove Last Set") }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { navigator.pop() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        val startDate = LocalDateTime.parse(startText, formatter)
                        val endDate = LocalDateTime.parse(endText, formatter)
                        val newWorkout = Workout(workout?.id ?: 0, name, startDate, endDate, workoutExercises)
                        coroutineScope.launch {
                            if (workout == null) {
                                WorkoutRepository.insert(newWorkout)
                            } else {
                                WorkoutRepository.update(newWorkout)
                            }
                            navigator.pop()
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

package me.knivram.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        // Use default values if workout is null
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        var name by remember { mutableStateOf(workout?.name ?: "") }
        var startText by remember {
            mutableStateOf(
                workout?.startDatetime?.format(formatter)
                    ?: LocalDateTime.now().minusHours(1).format(formatter)
            )
        }
        var endText by remember {
            mutableStateOf(
                workout?.endDatetime?.format(formatter)
                    ?: LocalDateTime.now().format(formatter)
            )
        }
        var workoutExercises by remember { mutableStateOf(workout?.exercises?.toMutableList() ?: mutableListOf<WorkoutExercise>()) }
        var availableExercises by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
        var showTemplateDialog by remember { mutableStateOf(false) }
        var selectedTemplate by remember { mutableStateOf<Template?>(null) }
        var templates by remember { mutableStateOf<List<Template>>(emptyList()) }
        // State for the add exercise dropdown
        var addExerciseExpanded by remember { mutableStateOf(false) }
        // For displaying error messages
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            availableExercises = ExerciseRepository.getAll().associate { it.id to it.name }
        }

        if (showTemplateDialog) {
            LaunchedEffect(showTemplateDialog) {
                templates = TemplateRepository.getAll()
            }
            AlertDialog(
                onDismissRequest = { showTemplateDialog = false },
                title = { Text("Select Template") },
                text = {
                    Column {
                        templates.forEach { tmpl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { selectedTemplate = tmpl },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tmpl.name)
                                if (selectedTemplate?.id == tmpl.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected"
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        selectedTemplate?.let { tmpl ->
                            val newExercises = tmpl.exercises.mapIndexed { index, te ->
                                val sets = (1..te.sets).map { idx -> WorkoutSet(0, 0, idx, 0.0, te.reps) }
                                WorkoutExercise(0, 0, te.exerciseId, index + 1, sets)
                            }
                            workoutExercises = newExercises.toMutableList()
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
            TopAppBar(
                title = { Text(if (workout == null) "Add Workout" else "Edit Workout \"${workout.name}\"") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workout Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startText,
                    onValueChange = { startText = it },
                    label = { Text("Start (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = { endText = it },
                    label = { Text("End (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Display error message if any
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { showTemplateDialog = true }) { Text("Load Template") }
                    // Single dropdown button for adding exercise.
                    Box {
                        Button(onClick = { addExerciseExpanded = true }) {
                            Text("Add Exercise")
                        }
                        DropdownMenu(expanded = addExerciseExpanded, onDismissRequest = { addExerciseExpanded = false }) {
                            availableExercises.forEach { (id, exerciseName) ->
                                DropdownMenuItem(onClick = {
                                    workoutExercises = workoutExercises.toMutableList().apply {
                                        add(WorkoutExercise(0, 0, id, size + 1, listOf(WorkoutSet(0, 0, 1, 0.0, 0))))
                                    }
                                    addExerciseExpanded = false
                                }) {
                                    Text(exerciseName)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Exercises:")
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn {
                        itemsIndexed(workoutExercises) { index, we ->
                            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Exercise: ${availableExercises[we.exerciseId] ?: "Unknown"}")
                                        // Remove exercise button:
                                        Button(onClick = {
                                            workoutExercises = workoutExercises.toMutableList().apply { removeAt(index) }
                                        }) { Text("Remove Exercise") }
                                    }
                                    we.sets.forEachIndexed { setIndex, set ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            OutlinedTextField(
                                                value = set.weight.toString(),
                                                onValueChange = { newWeightStr ->
                                                    val newWeight = newWeightStr.toDoubleOrNull() ?: 0.0
                                                    val updatedSets = we.sets.toMutableList().apply {
                                                        set(setIndex, set.copy(weight = newWeight))
                                                    }
                                                    val updatedExercise = we.copy(sets = updatedSets)
                                                    workoutExercises = workoutExercises.toMutableList().apply {
                                                        set(index, updatedExercise)
                                                    }
                                                },
                                                label = { Text("Weight") },
                                                modifier = Modifier.width(100.dp)
                                            )
                                            OutlinedTextField(
                                                value = set.reps.toString(),
                                                onValueChange = { newRepsStr ->
                                                    val newReps = newRepsStr.toIntOrNull() ?: 0
                                                    val updatedSets = we.sets.toMutableList().apply {
                                                        set(setIndex, set.copy(reps = newReps))
                                                    }
                                                    val updatedExercise = we.copy(sets = updatedSets)
                                                    workoutExercises = workoutExercises.toMutableList().apply {
                                                        set(index, updatedExercise)
                                                    }
                                                },
                                                label = { Text("Reps") },
                                                modifier = Modifier.width(100.dp)
                                            )
                                        }
                                    }
                                    Button(onClick = {
                                        val newSetNumber = we.sets.size + 1
                                        val updatedSets = we.sets.toMutableList().apply {
                                            add(WorkoutSet(0, 0, newSetNumber, 0.0, 0))
                                        }
                                        val updatedExercise = we.copy(sets = updatedSets)
                                        workoutExercises = workoutExercises.toMutableList().apply {
                                            set(index, updatedExercise)
                                        }
                                    }) { Text("Add Set") }
                                    Button(onClick = {
                                        if (we.sets.isNotEmpty()) {
                                            val updatedSets = we.sets.dropLast(1)
                                            val updatedExercise = we.copy(sets = updatedSets)
                                            workoutExercises = workoutExercises.toMutableList().apply {
                                                set(index, updatedExercise)
                                            }
                                        }
                                    }) { Text("Remove Last Set") }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { navigator.pop() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        try {
                            val startDate = LocalDateTime.parse(startText.trim(), formatter)
                            val endDate = LocalDateTime.parse(endText.trim(), formatter)
                            errorMessage = null
                            val newWorkout = Workout(workout?.id ?: 0, name, startDate, endDate, workoutExercises)
                            coroutineScope.launch {
                                if (workout == null) {
                                    WorkoutRepository.insert(newWorkout)
                                } else {
                                    WorkoutRepository.update(newWorkout)
                                }
                                navigator.pop()
                            }
                        } catch (e: Exception) {
                            errorMessage = "Invalid date input. Please enter date in format yyyy-MM-dd HH:mm"
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

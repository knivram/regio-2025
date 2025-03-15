package me.knivram.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import me.knivram.repository.ExerciseRepository
import me.knivram.repository.Template
import me.knivram.repository.TemplateExercise
import me.knivram.repository.TemplateRepository

class AddEditTemplateScreen(private val template: Template?) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var name by remember { mutableStateOf(template?.name ?: "") }
        var templateExercises by remember { mutableStateOf(template?.exercises?.toMutableList() ?: mutableListOf<TemplateExercise>()) }
        var selectedExerciseId by remember { mutableStateOf<Int?>(null) }
        var selectedExerciseName by remember { mutableStateOf<String?>(null) }
        var setsText by remember { mutableStateOf("") }
        var repsText by remember { mutableStateOf("") }
        var availableExercises by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            availableExercises = ExerciseRepository.getAll().associate { it.id to it.name }
        }

        Scaffold(topBar = {
            TopAppBar(
                title = { Text(if (template == null) "Add Template" else "Edit Template \"${template.name}\"") },
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
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Template Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Exercises:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { expanded = true }) { Text(selectedExerciseName ?: "Select Exercise") }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            availableExercises.forEach { (id, exerciseName) ->
                                DropdownMenuItem(onClick = {
                                    selectedExerciseId = id
                                    selectedExerciseName = exerciseName
                                    expanded = false
                                }) {
                                    Text(exerciseName)
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = setsText, onValueChange = { setsText = it }, label = { Text("Sets") }, modifier = Modifier.width(80.dp))
                    OutlinedTextField(value = repsText, onValueChange = { repsText = it }, label = { Text("Reps") }, modifier = Modifier.width(80.dp))
                    Button(onClick = {
                        if (selectedExerciseId != null && setsText.toIntOrNull() != null && repsText.toIntOrNull() != null) {
                            val newExercise = TemplateExercise(
                                id = 0,
                                templateId = 0,
                                exerciseId = selectedExerciseId!!,
                                sets = setsText.toInt(),
                                reps = repsText.toInt(),
                                position = templateExercises.size + 1
                            )
                            if (templateExercises.none { it.exerciseId == selectedExerciseId }) {
                                templateExercises = templateExercises.toMutableList().apply { add(newExercise) }
                            }
                        }
                    }) { Text("Add") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn {
                        items(templateExercises) { te ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val exerciseName = availableExercises[te.exerciseId] ?: "Unknown"
                                Text("$exerciseName - ${te.sets} sets x ${te.reps} reps")
                                Button(onClick = {
                                    templateExercises = templateExercises.toMutableList().apply { remove(te) }
                                }) { Text("Remove") }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { navigator.pop() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (name.isNotBlank() && templateExercises.isNotEmpty()) {
                            coroutineScope.launch {
                                val newTemplate = Template(template?.id ?: 0, name, templateExercises)
                                if (template == null) {
                                    TemplateRepository.insert(newTemplate)
                                } else {
                                    TemplateRepository.update(newTemplate)
                                }
                                navigator.pop()
                            }
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

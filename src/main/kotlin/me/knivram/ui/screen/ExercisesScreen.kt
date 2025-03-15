package me.knivram.ui.screen

import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import me.knivram.repository.Exercise
import me.knivram.repository.ExerciseRepository

class ExercisesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
        var newName by remember { mutableStateOf("") }
        var newDescription by remember { mutableStateOf("") }
        var newType by remember { mutableStateOf("") }
        var editExercise by remember { mutableStateOf<Exercise?>(null) }
        var editName by remember { mutableStateOf("") }
        var editDescription by remember { mutableStateOf("") }
        var editType by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            exercises = ExerciseRepository.getAll()
        }

        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Exercises") },
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
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Description") })
                OutlinedTextField(value = newType, onValueChange = { newType = it }, label = { Text("Type") })
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        coroutineScope.launch {
                            ExerciseRepository.insert(Exercise(0, newName, newDescription, newType))
                            exercises = ExerciseRepository.getAll()
                            newName = ""
                            newDescription = ""
                            newType = ""
                        }
                    }
                }) { Text("Add Exercise") }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn {
                        items(exercises) { exercise ->
                            Card(modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    editExercise = exercise
                                    editName = exercise.name
                                    editDescription = exercise.description
                                    editType = exercise.type
                                }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Name: ${exercise.name}")
                                        Text("Description: ${exercise.description}")
                                        Text("Type: ${exercise.type}")
                                    }
                                    Row {
                                        Button(onClick = { navigator.push(ProgressScreen(exercise)) }) { Text("My Progress") }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            coroutineScope.launch {
                                                ExerciseRepository.delete(exercise.id)
                                                exercises = ExerciseRepository.getAll()
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
        if (editExercise != null) {
            AlertDialog(
                onDismissRequest = { editExercise = null },
                title = { Text("Edit Exercise") },
                text = {
                    Column {
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") })
                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Description") })
                        OutlinedTextField(value = editType, onValueChange = { editType = it }, label = { Text("Type") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (editExercise != null) {
                            coroutineScope.launch {
                                ExerciseRepository.update(Exercise(editExercise!!.id, editName, editDescription, editType))
                                exercises = ExerciseRepository.getAll()
                                editExercise = null
                            }
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    Button(onClick = { editExercise = null }) { Text("Cancel") }
                }
            )
        }
    }
}

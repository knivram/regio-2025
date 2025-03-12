package me.knivram.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.knivram.repository.Todo
import me.knivram.repository.TodoRepository
import me.knivram.ui.components.AddInput
import me.knivram.ui.components.TopBar
import kotlinx.coroutines.launch

class Home: Screen{

    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        var todos by remember { mutableStateOf(listOf<Todo>()) }

        LaunchedEffect(Unit) {
            todos = TodoRepository.getAll()
        }

        fun addTodo(newTodo: String) {
            coroutineScope.launch {
                TodoRepository.insert(Todo.new(title = newTodo))
                todos = TodoRepository.getAll()
            }
        }

        fun updateDone(todo: Todo, done: Boolean) {
            coroutineScope.launch {
                TodoRepository.update(todo.copy(done = done))
                todos = TodoRepository.getAll()
            }
        }

        Scaffold(
            topBar = {
                TopBar(title = "Home")
            }
        ) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
            ) {
                AddInput(placeHolder = "Add todo", onAdd = {addTodo(it)})
                LazyColumn {
                    items(todos) { todo ->
                        TodoItem(
                            todo = todo,
                            onDoneChanged = { done -> updateDone(todo, done) }
                        )
                    }
                }
            }
        }
    }
}

@Composable fun TodoItem(todo: Todo, onDoneChanged: (Boolean) -> Unit) {
    val navigator = LocalNavigator.currentOrThrow
    Row(
        modifier = Modifier.clickable {  navigator.push(Detail(todo)) }
    ) {
        Checkbox(checked = todo.done, onCheckedChange = onDoneChanged)
        Spacer(Modifier.width(8.dp))
        Text(todo.title)
    }
}

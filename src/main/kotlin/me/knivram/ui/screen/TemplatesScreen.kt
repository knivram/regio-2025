package me.knivram.ui.screen

import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import me.knivram.repository.TemplateRepository

class TemplatesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var templates by remember { mutableStateOf(TemplateRepository.getAll()) }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(topBar = {
            TopAppBar(title = { Text("Templates") }, navigationIcon = {
                IconButton(onClick = { navigator.pop() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            })
        }) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Button(onClick = { navigator.push(AddEditTemplateScreen(null)) }) { Text("Add Template") }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(templates) { template ->
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                            navigator.push(AddEditTemplateScreen(template))
                        }) {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(template.name)
                                Button(onClick = {
                                    coroutineScope.launch {
                                        TemplateRepository.delete(template.id)
                                        templates = TemplateRepository.getAll()
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

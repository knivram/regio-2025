package me.knivram.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.knivram.repository.Todo
import me.knivram.ui.components.TopBar

class Detail(val todo: Todo): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopBar(title = todo.title, navigateUp = { navigator.pop() })
            }
        ) { innerPadding ->
            Column (
                modifier = Modifier.padding(innerPadding).padding(top = 12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            }
        }
    }
}
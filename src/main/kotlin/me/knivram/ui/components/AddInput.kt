package me.knivram.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

@Composable
fun AddInput(
    placeHolder: String,
    onAdd: (input: String) -> Unit,
) {
    var newEventName by remember { mutableStateOf("") }

    OutlinedTextField(
        placeholder = {
            Text(placeHolder)
        },
        value = newEventName,
        onValueChange = {
            newEventName = it
        },
        maxLines = 1,
        trailingIcon = {
            IconButton(
                onClick = {
                    onAdd(newEventName)
                    newEventName = ""
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Button"
                )
            }
        },
    )
}
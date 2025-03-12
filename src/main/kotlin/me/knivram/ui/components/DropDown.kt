package me.knivram.ui.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import java.util.UUID

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropDown(
    items: Map<UUID, String>,
    displayValue: String,
    onSelect: (id: UUID) -> Unit,
    label: @Composable (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { (id, value) ->
                DropdownMenuItem(onClick = {
                    onSelect(id)
                    expanded = false
                }) {
                    Text(text = value)
                }
            }
        }
    }

}

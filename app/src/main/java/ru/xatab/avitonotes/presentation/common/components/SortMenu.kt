package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.SortOrder

@Composable
fun SortMenu(order: SortOrder, onSort: (SortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = {
            expanded = true
        }) { Icon(Icons.Outlined.SwapVert, stringResource(R.string.action_sort)) }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            SortOrder.entries.forEach { value ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (value ==
                                SortOrder.NEWEST
                            ) {
                                stringResource(R.string.sort_newest)
                            } else {
                                stringResource(R.string.sort_oldest)
                            },
                        )
                    },
                    onClick = {
                        onSort(value)
                        expanded = false
                    },
                    trailingIcon = { if (value == order) Icon(Icons.Outlined.Check, null) },
                )
            }
        }
    }
}

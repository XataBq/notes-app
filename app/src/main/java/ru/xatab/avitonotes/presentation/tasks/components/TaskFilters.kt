package ru.xatab.avitonotes.presentation.tasks.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.TaskFilter

@Composable
fun TaskFilters(selected: TaskFilter, onSelect: (TaskFilter) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            TaskFilter.ALL -> stringResource(R.string.filter_all)
                            TaskFilter.ACTIVE -> stringResource(R.string.filter_active)
                            TaskFilter.COMPLETED -> stringResource(R.string.filter_completed)
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}

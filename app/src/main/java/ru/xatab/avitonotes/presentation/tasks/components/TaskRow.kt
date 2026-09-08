package ru.xatab.avitonotes.presentation.tasks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.Task

@Composable
fun TaskRow(task: Task, enabled: Boolean, onCompleted: (Boolean) -> Unit, onDelete: () -> Unit) {
    val resources = androidx.compose.ui.platform.LocalContext.current.resources
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        color = if (task.completed) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Row(Modifier.fillMaxWidth().padding(6.dp, 8.dp, 12.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onCompleted,
                enabled = enabled,
                modifier = Modifier.semantics {
                    contentDescription =
                        resources.getString(R.string.task_status_description, task.title)
                },
            )
            Column(Modifier.weight(1f).padding(start = 6.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    if (task.completed) {
                        stringResource(
                            R.string.task_completed,
                        )
                    } else {
                        stringResource(R.string.task_active)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    stringResource(R.string.task_delete, task.title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

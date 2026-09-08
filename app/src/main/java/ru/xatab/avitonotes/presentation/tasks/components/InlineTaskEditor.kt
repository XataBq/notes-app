package ru.xatab.avitonotes.presentation.tasks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.presentation.common.input.keyboardDismissTarget
import ru.xatab.avitonotes.presentation.tasks.state.TaskInput

@Composable
fun InlineTaskEditor(input: TaskInput.Text, onChange: (String) -> Unit, onSave: () -> Unit, onCancel: () -> Unit) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val save = {
        onSave()
        if (input.title.isNotBlank()) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
    Surface(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
        Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = input.title,
                onValueChange = onChange,
                modifier = Modifier.weight(1f).focusRequester(focus).keyboardDismissTarget(),
                singleLine = true,
                enabled = !input.saving,
                placeholder = {
                    Text(stringResource(R.string.task_title_hint), style = MaterialTheme.typography.bodyMedium)
                },
                isError = input.error,
                supportingText = if (input.error) {
                    { Text(stringResource(R.string.error_empty_title)) }
                } else {
                    null
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
            )
            IconButton(onClick = save, enabled = !input.saving) {
                if (input.saving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.Outlined.Check,
                        stringResource(R.string.task_save),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onCancel, enabled = !input.saving) {
                Icon(Icons.Outlined.Close, stringResource(R.string.task_cancel))
            }
        }
    }
}

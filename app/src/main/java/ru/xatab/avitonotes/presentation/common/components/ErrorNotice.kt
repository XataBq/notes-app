package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.presentation.common.text.UiText

@Composable
fun ErrorNotice(message: String, retry: (() -> Unit)? = null) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (retry != null) TextButton(onClick = retry) { Text(stringResource(R.string.action_retry)) }
        }
    }
}

@Composable
fun ErrorNotice(message: UiText, retry: (() -> Unit)? = null) {
    ErrorNotice(message.asString(), retry)
}

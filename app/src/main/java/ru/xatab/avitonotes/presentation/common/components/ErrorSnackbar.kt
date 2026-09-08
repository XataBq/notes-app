package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import ru.xatab.avitonotes.presentation.common.text.UiText

@Composable
fun ErrorSnackbar(message: UiText?, host: SnackbarHostState, dismiss: () -> Unit) {
    val resolved = message?.asString()
    LaunchedEffect(resolved) {
        if (resolved != null) {
            host.showSnackbar(resolved, withDismissAction = true)
            dismiss()
        }
    }
}

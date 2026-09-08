package ru.xatab.avitonotes.presentation.common.media

import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import ru.xatab.avitonotes.R

@Composable
fun PermissionDialog(title: String, message: String, dismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                dismiss()
                context.startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:${context.packageName}".toUri(),
                    ),
                )
            }) { Text(stringResource(R.string.tab_settings)) }
        },
        dismissButton = {
            TextButton(
                onClick = dismiss,
            ) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

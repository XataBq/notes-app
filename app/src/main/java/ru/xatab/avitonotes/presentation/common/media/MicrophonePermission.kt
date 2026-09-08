package ru.xatab.avitonotes.presentation.common.media

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import ru.xatab.avitonotes.R

@Composable
fun rememberMicrophoneRequest(onAllowed: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val latestAllowed by rememberUpdatedState(onAllowed)
    var denied by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) latestAllowed() else denied = true
    }
    if (denied) {
        PermissionDialog(
            stringResource(R.string.microphone_unavailable),
            stringResource(R.string.microphone_permission_help),
        ) {
            denied = false
        }
    }
    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            latestAllowed()
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

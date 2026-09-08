package ru.xatab.avitonotes.presentation.editor.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.presentation.common.media.GalleryImagePicker
import ru.xatab.avitonotes.presentation.common.media.PermissionDialog
import ru.xatab.avitonotes.presentation.common.text.UiText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteImagePicker(showMenu: Boolean, onDismiss: () -> Unit, onImport: (String) -> Job?, onError: (UiText) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cameraDenied by rememberSaveable { mutableStateOf(false) }
    var cameraPath by rememberSaveable { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = cameraPath
        cameraPath = null
        if (path != null) {
            scope.launch {
                val file = File(path)
                if (success) onImport(Uri.fromFile(file).toString())?.join()
                withContext(Dispatchers.IO) { file.delete() }
            }
        }
    }

    val takePhoto: () -> Unit = {
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
                    File.createTempFile("capture-", ".jpg", directory)
                }
                cameraPath = file.absolutePath
                camera.launch(FileProvider.getUriForFile(context, "${context.packageName}.files", file))
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                cameraPath?.let { path -> withContext(Dispatchers.IO) { File(path).delete() } }
                cameraPath = null
                onError(UiText(R.string.camera_open_error))
            }
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) takePhoto() else cameraDenied = true
    }

    val picker = rememberLauncherForActivityResult(GalleryImagePicker()) { uri ->
        if (uri != null) onImport(uri.toString())
    }

    if (cameraDenied) {
        PermissionDialog(
            stringResource(R.string.camera_permission_title),
            stringResource(R.string.camera_permission_help),
        ) { cameraDenied = false }
    }
    if (showMenu) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(R.string.image_add), style = MaterialTheme.typography.titleLarge)
                TextButton(
                    onClick = {
                        onDismiss()
                        try {
                            picker.launch(Unit)
                        } catch (_: ActivityNotFoundException) {
                            onError(UiText(R.string.gallery_missing))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Image, null)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.gallery_select))
                }
                TextButton(
                    onClick = {
                        onDismiss()
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) takePhoto() else permission.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.PhotoCamera, null)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.camera_take_photo))
                }
            }
        }
    }
}

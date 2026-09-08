package ru.xatab.avitonotes.presentation.notes.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import java.io.File
import ru.xatab.avitonotes.R

@Composable
fun NoteImage(path: String?, modifier: Modifier = Modifier) {
    val placeholder: @Composable () -> Unit = {
        Surface(modifier, shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Image,
                    stringResource(R.string.image_missing),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
    if (path == null) {
        placeholder()
    } else {
        SubcomposeAsyncImage(
            model = File(path),
            contentDescription = stringResource(R.string.image_description),
            modifier = modifier.clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            error = { placeholder() },
        )
    }
}

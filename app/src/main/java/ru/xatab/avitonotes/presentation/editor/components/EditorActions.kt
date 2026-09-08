package ru.xatab.avitonotes.presentation.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R

@Composable
fun EditorActions(enabled: Boolean, voiceEnabled: Boolean, onImage: () -> Unit, onVoice: () -> Unit) {
    Row(Modifier.fillMaxWidth().testTag("editor-actions"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(
            onClick = onImage,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .testTag("image-action"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Outlined.AddPhotoAlternate, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.editor_photo), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        FilledTonalButton(
            onClick = onVoice,
            enabled = voiceEnabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .testTag("voice-action"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Outlined.Mic, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.editor_dictate), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

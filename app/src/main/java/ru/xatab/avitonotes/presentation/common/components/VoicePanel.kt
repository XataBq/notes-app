package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.presentation.common.state.VoiceState
import ru.xatab.avitonotes.presentation.common.voice.VoiceInput

@Composable
fun VoicePanel(state: VoiceState, voice: VoiceInput) {
    if (state == VoiceState.Idle) return
    Surface(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state == VoiceState.Processing) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        if (state is VoiceState.Error) Icons.Outlined.ErrorOutline else Icons.Outlined.Mic,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    when (state) {
                        is VoiceState.Recording -> stringResource(R.string.voice_recording, state.seconds)
                        is VoiceState.Error -> stringResource(R.string.voice_error)
                        else -> stringResource(R.string.voice_processing)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                when (state) {
                    is VoiceState.Recording -> stringResource(R.string.voice_recording_hint)
                    is VoiceState.Error -> state.message.asString()
                    else -> stringResource(R.string.voice_processing_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state is VoiceState.Recording) {
                Button(onClick = voice::finish, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Stop, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.voice_finish))
                }
            }
            if (state is VoiceState.Error) Button(onClick = voice::start) { Text(stringResource(R.string.voice_retry)) }
            TextButton(onClick = voice::cancel) { Text(stringResource(R.string.action_cancel)) }
        }
    }
}

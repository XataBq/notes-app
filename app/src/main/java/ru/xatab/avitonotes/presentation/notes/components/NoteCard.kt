package ru.xatab.avitonotes.presentation.notes.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.Note

@Composable
fun NoteCard(
    note: Note,
    grid: Boolean,
    compact: Boolean,
    deleting: Boolean,
    busy: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val openLabel = stringResource(R.string.note_open)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).then(
            if (deleting) Modifier else Modifier.clickable(onClickLabel = openLabel, onClick = onOpen),
        ),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f)),
        shadowElevation = 1.dp,
    ) {
        if (grid) {
            Column {
                Box {
                    NoteCover(note, Modifier.fillMaxWidth().height(if (compact) 48.dp else 92.dp), grid = true)
                    if (deleting) DeleteNoteButton(note, busy, onDelete, Modifier.align(Alignment.TopEnd).padding(4.dp))
                }
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        note.title,
                        style = MaterialTheme.typography.titleMedium,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        note.text.ifBlank { stringResource(R.string.note_description_placeholder) },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    NoteDate(note, Modifier.padding(top = 5.dp))
                }
            }
        } else {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                NoteCover(note, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)), grid = false)
                Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        note.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (note.text.isNotBlank()) {
                        Text(
                            note.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    NoteDate(note)
                }
                if (deleting) DeleteNoteButton(note, busy, onDelete)
            }
        }
    }
}

@Composable
private fun NoteCover(note: Note, modifier: Modifier, grid: Boolean) {
    if (note.imagePath != null) {
        NoteImage(note.imagePath, modifier)
    } else {
        val accent = when ((note.id % 4).toInt()) {
            1 -> Color(0xFF965EEB)
            2 -> Color(0xFF67A344)
            3 -> Color(0xFFDE6978)
            else -> MaterialTheme.colorScheme.primary
        }
        Box(
            modifier.background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ),
            ),
            contentAlignment = Alignment.Center,
        ) {
            if (grid) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(14.dp).size(30.dp).background(
                        accent.copy(alpha = 0.08f),
                        RoundedCornerShape(10.dp),
                    ),
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.Notes,
                stringResource(R.string.image_missing),
                tint = accent,
                modifier = Modifier.size(29.dp),
            )
        }
    }
}

@Composable
private fun NoteDate(note: Note, modifier: Modifier = Modifier) {
    Text(
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(note.createdAt)),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun DeleteNoteButton(note: Note, busy: Boolean, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onDelete, enabled = !busy, modifier = modifier) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                Icons.Outlined.DeleteOutline,
                stringResource(R.string.note_delete, note.title),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

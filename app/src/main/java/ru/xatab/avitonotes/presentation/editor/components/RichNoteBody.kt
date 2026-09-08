package ru.xatab.avitonotes.presentation.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle
import ru.xatab.avitonotes.domain.usecase.TextFormatting
import ru.xatab.avitonotes.presentation.common.input.keyboardDismissTarget
import ru.xatab.avitonotes.presentation.editor.text.NoteTextTransformation

@Composable
fun RichNoteBody(
    text: String,
    marks: List<TextMark>,
    typingStyles: Set<TextStyle>?,
    enabled: Boolean,
    onText: (String) -> Unit,
    onSelection: () -> Unit,
    onFormat: (TextStyle, Int, Int) -> Unit,
) {
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text)) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val description = stringResource(R.string.note_body_description)

    LaunchedEffect(text) {
        if (value.text != text) value = TextFieldValue(text, TextRange(text.length))
    }

    val from = value.selection.min
    val to = value.selection.max
    val selected = if (from == to) {
        typingStyles ?: marks.filter { it.start < from && it.end >= from }.map { it.style }.toSet()
    } else {
        TextStyle.entries.filter { TextFormatting.covers(marks, from, to, it) }.toSet()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormattingToolbar(selected, enabled) { style ->
            onFormat(style, from, to)
            focus.requestFocus()
            keyboard?.show()
        }
        BasicTextField(
            value = value,
            onValueChange = { next ->
                if (next.text == value.text && next.selection != value.selection) onSelection()
                value = next
                onText(next.text)
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .focusRequester(focus)
                .keyboardDismissTarget()
                .semantics { contentDescription = description },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = remember(marks) { NoteTextTransformation(marks) },
            decorationBox = { field ->
                Box {
                    if (value.text.isEmpty()) {
                        Text(
                            stringResource(R.string.note_body_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    field()
                }
            },
        )
    }
}

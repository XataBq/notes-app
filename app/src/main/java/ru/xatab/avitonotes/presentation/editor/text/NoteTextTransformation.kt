package ru.xatab.avitonotes.presentation.editor.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle

class NoteTextTransformation(private val marks: List<TextMark>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = AnnotatedString.Builder(text).apply {
            marks.forEach { mark ->
                val start = mark.start.coerceIn(0, text.length)
                val end = mark.end.coerceIn(0, text.length)
                if (start < end) {
                    addStyle(
                        when (mark.style) {
                            TextStyle.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                            TextStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                            TextStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                        },
                        start,
                        end,
                    )
                }
            }
        }.toAnnotatedString()
        return TransformedText(formatted, OffsetMapping.Identity)
    }
}

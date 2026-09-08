package ru.xatab.avitonotes.presentation.editor.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import java.io.File
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle

class NoteShareIntentFactory(private val context: Context) {
    fun create(title: String, body: String, marks: List<TextMark>, imagePath: String?): Intent {
        val heading = title.trim()
        val offset = if (heading.isEmpty()) 0 else heading.length + 2
        val text = SpannableString(if (heading.isEmpty()) body else "$heading\n\n$body")
        if (heading.isNotEmpty()) {
            text.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                heading.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        marks.forEach { mark ->
            val start = mark.start.coerceIn(0, body.length)
            val end = mark.end.coerceIn(0, body.length)
            if (start < end) {
                text.setSpan(
                    when (mark.style) {
                        TextStyle.BOLD -> StyleSpan(Typeface.BOLD)
                        TextStyle.ITALIC -> StyleSpan(Typeface.ITALIC)
                        TextStyle.UNDERLINE -> UnderlineSpan()
                    },
                    start + offset,
                    end + offset,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, heading)
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_HTML_TEXT, HtmlCompat.toHtml(
                text,
                HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
            ))

            imagePath?.let { path ->
                val directory = File(context.filesDir, "note-images").canonicalFile
                val file = File(path).canonicalFile
                if (file.parentFile == directory && file.isFile) {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                    type = context.contentResolver.getType(uri)?.takeIf { it.startsWith("image/") } ?: "image/*"

                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newUri(context.contentResolver, heading, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
        }
    }
}

package ru.xatab.avitonotes.data.local.media

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider

class NoteFileProvider : FileProvider() {
    override fun getType(uri: Uri): String? {
        val guessed = super.getType(uri)
        if (guessed != "application/octet-stream") return guessed
        return runCatching {
            openFile(uri, "r")?.use { descriptor ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
                options.outMimeType
            }
        }.getOrNull() ?: guessed
    }
}

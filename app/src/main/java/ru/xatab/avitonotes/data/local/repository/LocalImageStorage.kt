package ru.xatab.avitonotes.data.local.repository

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.repository.ImageStorage

class LocalImageStorage(private val context: Context) : ImageStorage {
    private val directory = File(context.filesDir, "note-images")
    override suspend fun import(source: String): String = withContext(Dispatchers.IO) {
        directory.mkdirs()
        val target = File(directory, "${UUID.randomUUID()}.image")
        try {
            context.contentResolver.openInputStream(source.toUri())?.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > 20 * 1024 * 1024) throw AppException(FailureKind.IMAGE)
                        output.write(buffer, 0, count)
                    }
                }
            } ?: throw AppException(FailureKind.IMAGE)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(target.path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw AppException(FailureKind.IMAGE)
            target.absolutePath
        } catch (error: Exception) {
            target.delete()
            if (error is kotlinx.coroutines.CancellationException) throw error
            throw AppException(FailureKind.IMAGE, error)
        }
    }

    override suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        if (path != null) {
            val file = File(path)

            try {
                if (file.canonicalFile.parentFile == directory.canonicalFile) file.delete()
            } catch (_: IOException) {
            } catch (_: SecurityException) {
            }
        }
        Unit
    }
}

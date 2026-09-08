package ru.xatab.avitonotes

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.data.local.repository.LocalImageStorage
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind

@RunWith(AndroidJUnit4::class)
class ImageStorageTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun copiedImageSurvivesSourceRemovalAndOnlyOwnedImagesCanBeDeleted() = runBlocking {
        val source = File.createTempFile("image-test", ".png", context.cacheDir)
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val storage = LocalImageStorage(context)
        var copy: File? = null
        try {
            copy = File(storage.import(Uri.fromFile(source).toString()))
            assertNotEquals(source.canonicalPath, copy.canonicalPath)
            assertArrayEquals(source.readBytes(), copy.readBytes())
            storage.delete(source.path)
            assertTrue(source.exists())
            source.delete()
            assertTrue(copy.exists())
            storage.delete(copy.path)
            assertFalse(copy.exists())
        } finally {
            source.delete()
            copy?.delete()
        }
    }

    @Test fun invalidImageLeavesNoImportedFile() = runBlocking {
        val source = File.createTempFile("bad-image", ".png", context.cacheDir).apply { writeText("not an image") }
        val directory = File(context.filesDir, "note-images")
        val before = directory.list()?.toSet().orEmpty()
        try {
            try {
                LocalImageStorage(context).import(Uri.fromFile(source).toString())
                fail("Expected image error")
            } catch (
                error: AppException,
            ) {
                assertEquals(FailureKind.IMAGE, error.kind)
            }
            assertEquals(before, directory.list()?.toSet().orEmpty())
        } finally {
            source.delete()
        }
    }
}

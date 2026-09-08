package ru.xatab.avitonotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.presentation.common.media.GalleryImagePicker

@RunWith(AndroidJUnit4::class)
class GalleryPickerTest {
    @Test fun pickerAcceptsOnlyImagesAndDoesNotOpenDocumentBrowser() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val picker = GalleryImagePicker()
        val intent = picker.createIntent(context, Unit)
        assertEquals("image/*", intent.type)
        assertNotEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
        assertNotEquals(Intent.ACTION_GET_CONTENT, intent.action)
        val result = Intent().setData(Uri.parse("content://media/external/images/media/1"))
        assertEquals(result.data, picker.parseResult(Activity.RESULT_OK, result))
        assertNull(picker.parseResult(Activity.RESULT_CANCELED, result))
    }
}

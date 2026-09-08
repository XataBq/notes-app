package ru.xatab.avitonotes

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.data.audio.PcmVoiceRecorder

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 28)
class RecordingTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun pcmRecordingCanStopCancelAndRestart() = runBlocking {
        ui.waitForIdle()
        val context = ui.activity.applicationContext
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, Manifest.permission.RECORD_AUDIO)
        val recorder = PcmVoiceRecorder(context)
        try {
            recorder.start()
            delay(750)
            val first = recorder.stop()
            assertTrue(first.size in 3200..1_760_000)
            assertTrue(first.size % 2 == 0)
            recorder.start()
            delay(150)
            recorder.cancel()
            recorder.start()
            delay(750)
            assertTrue(recorder.stop().size >= 3200)
        } finally {
            recorder.cancel()
        }
    }
}

package ru.xatab.avitonotes

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.data.local.database.NotesDatabase
import ru.xatab.avitonotes.data.local.entity.NoteEntity
import ru.xatab.avitonotes.data.local.entity.TextMarksCodec
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle

@RunWith(AndroidJUnit4::class)
class PremiumLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun gridAdaptsToRotationAndOpensFormattedNote() {
        val db = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java,
            "avito-notes.db",
        ).build()
        val titles =
            listOf(
                "Идеи для дома",
                "Выходные в городе",
                "Новая привычка",
                "Книги на осень",
                "Любимые места",
                "План на неделю",
            )
        val bodies = listOf(
            "Больше света, живые растения и уютное место для чтения.",
            "Зайти на выставку, выпить кофе и погулять без маршрута.",
            "Двадцать минут в день для того, что давно хотелось начать.",
            "Истории, к которым хочется возвращаться. Читать по главе перед сном.",
            "Маленькая кофейня у парка. Столик у окна и самый вкусный капучино.",
            "Самое важное\nЗавершить проект и оставить время для себя.\n\nНе забыть\nЗабрать посылку и спланировать выходные.",
        )
        val ids = runBlocking {
            titles.mapIndexed { index, title ->
                db.notes().insert(
                    NoteEntity(
                        title = title,
                        text = bodies[index],
                        imagePath = null,
                        createdAt = System.currentTimeMillis() + index * 1000,
                        marks = if (index == titles.lastIndex) {
                            TextMarksCodec.encode(listOf(TextMark(0, 13, TextStyle.BOLD)), bodies[index].length)
                        } else {
                            "[]"
                        },
                    ),
                )
            }
        }
        try {
            sort("От новых к старым")
            ui.onNodeWithTag("search-field").performTextReplacement(titles.last())
            ui.onNodeWithContentDescription("Найти").performClick()
            ui.waitUntil(10_000) { ui.onAllNodesWithText(titles.last()).fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithContentDescription("Очистить поиск").performClick()
            ui.waitUntil(10_000) { ui.onAllNodesWithText(titles[4]).fetchSemanticsNodes().isNotEmpty() }
            screenshot("notes-list-premium")
            ui.onNodeWithContentDescription("Показать сеткой").performClick()
            val first = ui.onNodeWithText(titles.last()).fetchSemanticsNode().boundsInRoot
            val second = ui.onNodeWithText(titles[4]).fetchSemanticsNode().boundsInRoot
            assertTrue("Two grid columns should fit a regular portrait phone", second.left > first.right)
            screenshot("notes-grid")
            ui.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            ui.waitUntil(10_000) {
                ui.activity.resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
            }
            ui.onNodeWithText(titles.last()).assertIsDisplayed()
            screenshot("notes-grid-landscape")
            portrait()
            ui.onNodeWithText(titles.last()).performClick()
            ui.onNodeWithContentDescription("Текст заметки").assertTextContains(bodies.last())
            screenshot("editor-premium")
            ui.onNodeWithContentDescription("Поделиться заметкой").performClick()
            android.os.SystemClock.sleep(800)
            screenshot("share")
            InstrumentationRegistry.getInstrumentation().uiAutomation.performGlobalAction(
                android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK,
            )
            ui.waitForIdle()
            ui.onNodeWithContentDescription("Назад").performClick()
            ui.onNodeWithContentDescription("Показать списком").performClick()
        } finally {
            portrait()
            runBlocking { ids.forEach { id -> db.notes().get(id)?.let { db.notes().delete(it) } } }
            db.close()
        }
    }

    private fun sort(label: String) {
        ui.onNodeWithContentDescription("Сортировать").performClick()
        ui.onNodeWithText(label).performClick()
    }

    private fun portrait() {
        ui.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        ui.waitUntil(10_000) { ui.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        ui.waitForIdle()
    }

    private fun screenshot(name: String) {
        ui.waitForIdle()
        android.os.SystemClock.sleep(500)
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/avitonotes-$name.png")
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }
}

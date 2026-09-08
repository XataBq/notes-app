package ru.xatab.avitonotes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextRange
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.data.local.database.NotesDatabase

@RunWith(AndroidJUnit4::class)
class ExtraFeaturesFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun formattingSurvivesSaveRecreationAndGridChoicePersists() {
        val title = "Стили ${System.currentTimeMillis()}"
        ui.onNodeWithContentDescription("Создать заметку").performClick()
        ui.onNodeWithContentDescription("Заголовок заметки").performTextInput(title)
        val body = ui.onNodeWithContentDescription("Текст заметки")
        body.performTextInput("Важная мысль")
        body.performTextInputSelection(TextRange(0, 6))
        assertEquals(
            TextRange(0, 6),
            body.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TextSelectionRange],
        )
        listOf("Полужирный", "Курсив", "Подчёркивание").forEach {
            ui.onNodeWithContentDescription(it).performClick().assertIsOn()
        }
        ui.onNodeWithContentDescription("Поделиться заметкой").assertIsEnabled()
        ui.onNodeWithText("Сохранить").performClick()
        ui.waitUntil(10_000) { ui.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        val db = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java,
            "avito-notes.db",
        ).build()
        try {
            val saved = runBlocking { db.notes().observe().first().single { it.title == title }.toDomain() }
            assertEquals("Saved marks: ${saved.marks}", 3, saved.marks.size)
        } finally {
            db.close()
        }
        ui.onNodeWithText(title).performClick()
        ui.onNodeWithContentDescription("Текст заметки").performTextInputSelection(TextRange(0, 6))
        listOf("Полужирный", "Курсив", "Подчёркивание").forEach { ui.onNodeWithContentDescription(it).assertIsOn() }
        ui.activityRule.scenario.recreate()
        ui.onNodeWithContentDescription("Текст заметки").performTextInputSelection(TextRange(0, 6))
        ui.onNodeWithContentDescription("Полужирный").assertIsOn()
        ui.onNodeWithContentDescription("Назад").performClick()
        ui.onNodeWithContentDescription("Показать сеткой").performClick()
        ui.onNodeWithContentDescription("Показать списком").assertExists()
        ui.activityRule.scenario.recreate()
        ui.waitUntil(10_000) {
            ui.onAllNodesWithContentDescription("Показать списком").fetchSemanticsNodes().isNotEmpty()
        }
        ui.onNodeWithText(title).assertIsDisplayed()
        ui.onNodeWithContentDescription("Показать списком").performClick()
        ui.onNodeWithContentDescription("Режим удаления").performClick()
        ui.onNodeWithContentDescription("Удалить $title").performClick()
        ui.onNodeWithText("Удалить").performClick()
        ui.waitUntil(10_000) { ui.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithContentDescription("Завершить удаление").performClick()
    }
}

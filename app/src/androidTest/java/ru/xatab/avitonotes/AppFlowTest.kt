package ru.xatab.avitonotes

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun tab(title: String) = ui.onNode(hasText(title) and hasClickAction()).performClick()
    private fun shown(text: String) {
        ui.waitUntil(10_000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText(text).assertIsDisplayed()
    }
    private fun screenshot(name: String) {
        ui.waitForIdle()

        android.os.SystemClock.sleep(500)
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/avitonotes-$name.png")
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }
    private fun selected(text: String) {
        ui.waitUntil(10_000) { ui.onAllNodes(hasText(text) and isSelected()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText(text).assertIsSelected()
    }
    private fun accent(description: String) {
        ui.waitUntil(10_000) { ui.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun notesTasksAndAppearance() {
        val suffix = System.currentTimeMillis().toString().takeLast(6)
        val noteTitle = "План на неделю $suffix"
        ui.onNodeWithContentDescription("Создать заметку").performClick()
        ui.onNodeWithText("Сохранить").performClick()
        shown("Введите заголовок заметки")
        ui.onNodeWithContentDescription("Заголовок заметки").performTextInput(noteTitle)
        ui.onNodeWithContentDescription("Текст заметки").performTextInput("Закончить проект и подготовиться к встрече.")
        ui.onNodeWithText("Сохранить").performClick()
        shown(noteTitle)
        screenshot("notes")
        ui.onNodeWithText(noteTitle).performClick()
        ui.onNodeWithContentDescription(
            "Текст заметки",
        ).assertTextContains("Закончить проект и подготовиться к встрече.")
        ui.onNodeWithContentDescription("Текст заметки").performTextReplacement("Обновлённый план")
        ui.onNodeWithContentDescription("Заголовок заметки").performClick()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        screenshot("editor")
        ui.onNodeWithText("Сохранить").performClick()
        shown(noteTitle)
        ui.onNode(hasSetTextAction()).performTextInput("Нет такой заметки $suffix")
        ui.onNodeWithText(noteTitle).assertExists()
        ui.onNodeWithContentDescription("Найти").performClick()
        shown("Ничего не нашлось")
        ui.onNodeWithContentDescription("Очистить поиск").performClick()
        shown(noteTitle)
        ui.onNodeWithContentDescription("Сортировать").performClick()
        ui.onNodeWithText("От старых к новым").performClick()
        ui.onNodeWithContentDescription("Режим удаления").performClick()
        ui.onNodeWithContentDescription("Создать заметку").assertDoesNotExist()
        ui.onNodeWithText(noteTitle).assertHasNoClickAction()
        ui.onNodeWithText("Сохранить").assertDoesNotExist()
        ui.onNodeWithContentDescription("Удалить $noteTitle").performClick()
        shown("Удалить заметку?")
        ui.onNodeWithText("Отмена").performClick()
        shown(noteTitle)
        ui.onNodeWithContentDescription("Удалить $noteTitle").performClick()
        ui.onNodeWithText("Удалить").performClick()
        ui.waitUntil(10_000) { ui.onAllNodesWithText(noteTitle).fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithContentDescription("Завершить удаление").performClick()

        tab("Задачи")
        ui.onNodeWithContentDescription("Создать задачу").performClick()
        ui.onNodeWithText("Написать текстом").performClick()
        ui.onNodeWithContentDescription("Сохранить задачу").performClick()
        shown("Введите название")
        val task = "Подготовить проект $suffix"
        ui.onNode(hasSetTextAction() and hasText("Что нужно сделать?")).performTextInput(task)
        ui.onNodeWithContentDescription("Сохранить задачу").performClick()
        shown(task)
        ui.onNodeWithContentDescription("Статус задачи: $task").performClick().assertIsOn()
        ui.onNodeWithText("Активные").performClick()
        ui.onNodeWithText(task).assertDoesNotExist()
        ui.onNodeWithText("Выполненные").performClick()
        shown(task)
        ui.activityRule.scenario.recreate()
        shown(task)
        ui.onNodeWithContentDescription("Статус задачи: $task").assertIsOn()
        screenshot("tasks")

        tab("Настройки")
        ui.onNodeWithText("Тёмная").performClick()
        selected("Тёмная")
        ui.onNodeWithContentDescription("Фиолетовый").performScrollTo().performClick()
        accent("Фиолетовый, выбран")
        screenshot("settings-dark")
        ui.activityRule.scenario.recreate()
        selected("Тёмная")
        accent("Фиолетовый, выбран")
        ui.onNodeWithText("Сбросить настройки").performScrollTo().performClick()
        selected("Системная")
        accent("Голубой, выбран")
    }
}

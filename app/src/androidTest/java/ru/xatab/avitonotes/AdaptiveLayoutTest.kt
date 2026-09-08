package ru.xatab.avitonotes

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdaptiveLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun landscapeKeepsListUsableAndOutsideTapClosesSearchKeyboard() {
        try {
            orient(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE)
            tab("Задачи")
            val height = ui.onNodeWithTag("tasks-list").fetchSemanticsNode().boundsInRoot.height / ui.density.density
            assertTrue("List viewport is only $height dp", height >= 180f)
            val search = ui.onNodeWithTag("search-field").fetchSemanticsNode().boundsInRoot
            val header = ui.onNodeWithTag("screen-header").fetchSemanticsNode().boundsInRoot
            assertTrue("Wide layout should place search inside the title row", search.bottom <= header.bottom)
            screenshot("tasks-landscape")

            ui.onNodeWithTag("search-field").performClick().performTextInput("Проверка")
            ui.waitUntil(10_000) { keyboardVisible() }
            ui.onNode(hasText("Задачи") and !hasClickAction()).performTouchInput { click() }
            ui.waitUntil(10_000) { !keyboardVisible() }
            ui.onNodeWithTag("search-field").assertIsNotFocused()
            ui.onNodeWithTag("bottom-navigation").assertIsDisplayed()
            ui.onNodeWithContentDescription("Очистить поиск").performClick()
        } finally {
            orient(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        }
    }

    @Test fun editorActionsStayOnOneRowImmediatelyAfterText() {
        orient(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        ui.onNodeWithContentDescription("Создать заметку").performClick()
        val image = ui.onNodeWithTag("image-action").fetchSemanticsNode().boundsInRoot
        val voice = ui.onNodeWithTag("voice-action").fetchSemanticsNode().boundsInRoot
        val body = ui.onNodeWithContentDescription("Текст заметки").fetchSemanticsNode().boundsInRoot
        assertTrue("Editor actions must share one row", abs(image.top - voice.top) <= ui.density.density)
        assertTrue("Unexpected blank space below body", (image.top - body.bottom) / ui.density.density <= 40f)
        ui.onNodeWithTag("voice-action").assertIsDisplayed()
        screenshot("editor-compact")
        ui.onNodeWithTag("image-action").performClick()
        ui.onNodeWithText("Из галереи").assertIsDisplayed()
        ui.onNodeWithText("Из файлов устройства").assertDoesNotExist()
    }

    @Test fun repeatedTabSwitchesKeepSingleDestinationAndRestoreSearch() {
        ui.onNodeWithTag("search-field").performTextInput("Сохранённый запрос")
        ui.onNodeWithContentDescription("Найти").performClick()
        repeat(5) {
            tab("Задачи")
            ui.onAllNodesWithTag("screen-header").assertCountEquals(1)
            ui.onNodeWithTag("tasks-list").assertExists()
            tab("Настройки")
            ui.onAllNodesWithTag("screen-header").assertCountEquals(1)
            tab("Заметки")
            ui.onNodeWithTag("search-field").assertTextContains("Сохранённый запрос")
        }
        ui.onNodeWithContentDescription("Очистить поиск").performClick()
    }

    private fun tab(text: String) = ui.onNode(hasText(text) and hasClickAction()).performClick()

    private fun orient(request: Int, expected: Int) {
        ui.activityRule.scenario.onActivity { it.requestedOrientation = request }
        ui.waitUntil(10_000) { ui.activity.resources.configuration.orientation == expected }
        ui.waitForIdle()
    }

    private fun keyboardVisible(): Boolean =
        ViewCompat.getRootWindowInsets(ui.activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) == true

    private fun screenshot(name: String) {
        ui.waitForIdle()

        android.os.SystemClock.sleep(500)
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/avitonotes-$name.png")
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }
}

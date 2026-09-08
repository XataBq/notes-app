package ru.xatab.avitonotes.presentation.common.input

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

private class FocusedField {
    var owner: Any? = null
    var bounds: Rect? = null
    val exclusions = mutableMapOf<Any, Rect>()
    fun clear(token: Any) {
        if (owner == token) {
            owner = null
            bounds = null
        }
    }
}

private val LocalFocusedField = staticCompositionLocalOf<FocusedField?> { null }

@Composable
fun KeyboardDismissArea(content: @Composable BoxScope.() -> Unit) {
    val field = remember { FocusedField() }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    CompositionLocalProvider(LocalFocusedField provides field) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(focus, keyboard) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        if (field.bounds?.contains(down.position) == false &&
                            field.exclusions.values.none { it.contains(down.position) }
                        ) {
                            focus.clearFocus()
                            keyboard?.hide()
                        }
                    }
                },
            content = content,
        )
    }
}

@Composable
fun Modifier.keepKeyboardFocus(): Modifier {
    val field = LocalFocusedField.current ?: return this
    val token = remember { Any() }
    DisposableEffect(field, token) { onDispose { field.exclusions.remove(token) } }
    return onGloballyPositioned { field.exclusions[token] = it.boundsInRoot() }
}

@Composable
fun Modifier.keyboardDismissTarget(): Modifier {
    val field = LocalFocusedField.current ?: return this
    val token = remember { Any() }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    var focused by remember { mutableStateOf(false) }
    DisposableEffect(field, token) { onDispose { field.clear(token) } }
    return onGloballyPositioned {
        bounds = it.boundsInRoot()
        if (focused) {
            field.owner = token
            field.bounds = bounds
        }
    }.onFocusChanged {
        focused = it.isFocused
        if (focused) {
            field.owner = token
            field.bounds = bounds
        } else {
            field.clear(token)
        }
    }
}

package ru.xatab.avitonotes.presentation.common.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiText(@param:StringRes val resource: Int, val args: List<Any> = emptyList()) {
    @Composable
    fun asString(): String = stringResource(resource, *args.toTypedArray())

    fun resolve(context: Context): String = context.getString(resource, *args.toTypedArray())
}

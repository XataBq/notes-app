package ru.xatab.avitonotes.presentation.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.TextStyle
import ru.xatab.avitonotes.presentation.common.input.keepKeyboardFocus

@Composable
fun FormattingToolbar(selected: Set<TextStyle>, enabled: Boolean, onToggle: (TextStyle) -> Unit) {
    Surface(
        modifier = Modifier.keepKeyboardFocus().testTag("format-toolbar"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            TextStyle.entries.forEach { style ->
                val label = stringResource(
                    when (style) {
                        TextStyle.BOLD -> R.string.format_bold
                        TextStyle.ITALIC -> R.string.format_italic
                        TextStyle.UNDERLINE -> R.string.format_underline
                    },
                )
                IconToggleButton(
                    checked = style in selected,
                    onCheckedChange = { onToggle(style) },
                    enabled = enabled,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        when (style) {
                            TextStyle.BOLD -> Icons.Outlined.FormatBold
                            TextStyle.ITALIC -> Icons.Outlined.FormatItalic
                            TextStyle.UNDERLINE -> Icons.Outlined.FormatUnderlined
                        },
                        label,
                    )
                }
            }
        }
    }
}

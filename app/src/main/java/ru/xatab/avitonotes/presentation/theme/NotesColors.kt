package ru.xatab.avitonotes.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import ru.xatab.avitonotes.domain.model.AccentColor

fun AccentColor.color(dark: Boolean = false): Color = when (this) {
    AccentColor.BLUE -> if (dark) Color(0xFF66CAFF) else Color(0xFF007CB9)
    AccentColor.PURPLE -> if (dark) Color(0xFFC7A7FF) else Color(0xFF7646C0)
    AccentColor.GREEN -> if (dark) Color(0xFFA7D680) else Color(0xFF467B2B)
    AccentColor.CORAL -> if (dark) Color(0xFFFFA4B0) else Color(0xFFBD3E56)
}

fun notesColorScheme(accent: AccentColor, dark: Boolean): ColorScheme {
    val primary = accent.color(dark)
    return if (dark) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF15212A),
            primaryContainer = primary.copy(alpha = .15f).compositeOver(Color(0xFF202326)),
            onPrimaryContainer = primary,
            secondary = primary,
            secondaryContainer = Color(0xFF333A40),
            onSecondaryContainer = Color(0xFFF0F2F4),
            background = Color(0xFF151719),
            onBackground = Color(0xFFF1F3F5),
            surface = Color(0xFF202326),
            onSurface = Color(0xFFF1F3F5),
            surfaceVariant = Color(0xFF2C3035),
            onSurfaceVariant = Color(0xFFB2B8BF),
            outlineVariant = Color(0xFF363A40),
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = Color(0xFF151719),
            surfaceContainerLow = Color(0xFF202326),
            surfaceContainer = Color(0xFF25282C),
            surfaceContainerHigh = Color(0xFF2C3035),
            surfaceContainerHighest = Color(0xFF33383D),
            surfaceDim = Color(0xFF151719),
            surfaceBright = Color(0xFF33383D),
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = .10f).compositeOver(Color.White),
            onPrimaryContainer = primary,
            secondary = primary,
            secondaryContainer = Color(0xFFE8F1F7),
            onSecondaryContainer = Color(0xFF202126),
            background = Color(0xFFF6F7F8),
            onBackground = Color(0xFF202126),
            surface = Color.White,
            onSurface = Color(0xFF202126),
            surfaceVariant = Color(0xFFF0F2F4),
            onSurfaceVariant = Color(0xFF69717A),
            outlineVariant = Color(0xFFE6E9ED),
            surfaceTint = Color.Transparent,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8F9FA),
            surfaceContainer = Color(0xFFF3F4F5),
            surfaceContainerHigh = Color(0xFFEFF1F3),
            surfaceContainerHighest = Color(0xFFE8EBEE),
            surfaceDim = Color(0xFFE8EBEE),
            surfaceBright = Color.White,
        )
    }
}

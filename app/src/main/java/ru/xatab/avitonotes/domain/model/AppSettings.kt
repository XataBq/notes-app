package ru.xatab.avitonotes.domain.model

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val accent: AccentColor = AccentColor.BLUE,
    val noteLayout: NoteLayout = NoteLayout.LIST,
)

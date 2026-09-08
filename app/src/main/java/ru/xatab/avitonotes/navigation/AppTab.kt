package ru.xatab.avitonotes.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import ru.xatab.avitonotes.R

enum class AppTab(val route: String, val label: Int, val icon: ImageVector) {
    NOTES("notes", R.string.tab_notes, Icons.Outlined.NoteAlt),
    TASKS("tasks", R.string.tab_tasks, Icons.Outlined.CheckCircleOutline),
    SETTINGS("settings", R.string.tab_settings, Icons.Outlined.Tune),
}

package ru.xatab.avitonotes.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import ru.xatab.avitonotes.presentation.common.input.KeyboardDismissArea
import ru.xatab.avitonotes.presentation.editor.screen.NoteEditorScreen
import ru.xatab.avitonotes.presentation.notes.screen.NotesScreen
import ru.xatab.avitonotes.presentation.settings.screen.SettingsScreen
import ru.xatab.avitonotes.presentation.tasks.screen.TasksScreen
import ru.xatab.avitonotes.presentation.theme.NotesTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesApp(vm: AppViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    NotesTheme(settings) {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val route = entry?.destination?.route
        val keyboardVisible = WindowInsets.isImeVisible
        KeyboardDismissArea {
            Scaffold(
                modifier = Modifier.imePadding(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (AppTab.entries.any { it.route == route } && !keyboardVisible) {
                        AppBottomBar(route) { tab ->
                            if (nav.currentDestination?.route != tab.route) {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                },
            ) { padding ->
                NavHost(
                    navController = nav,
                    startDestination = AppTab.NOTES.route,
                    modifier = Modifier.padding(padding).consumeWindowInsets(padding)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                    sizeTransform = { null },
                ) {
                    composable(AppTab.NOTES.route) {
                        NotesScreen(hiltViewModel()) { id -> nav.navigate("note/$id") }
                    }
                    composable("note/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                        NoteEditorScreen(hiltViewModel()) { nav.popBackStack() }
                    }
                    composable(AppTab.TASKS.route) { TasksScreen(hiltViewModel()) }
                    composable(AppTab.SETTINGS.route) { SettingsScreen(hiltViewModel()) }
                }
            }
        }
    }
}

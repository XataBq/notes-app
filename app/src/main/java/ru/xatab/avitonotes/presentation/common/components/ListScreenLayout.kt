package ru.xatab.avitonotes.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ListScreenLayout(
    title: String,
    query: String,
    onQuery: (String) -> Unit,
    onSearch: () -> Unit,
    searchLabel: String,
    snackbar: SnackbarHostState,
    actions: @Composable RowScope.() -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val inlineSearch = maxWidth >= 600.dp && LocalDensity.current.fontScale <= 1.4f
        val search: @Composable () -> Unit = { SearchField(query, onQuery, onSearch, searchLabel) }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = { ScreenHeader(title, actions, if (inlineSearch) search else null) },
            snackbarHost = { SnackbarHost(snackbar) },
            floatingActionButton = floatingActionButton,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
            ) {
                if (!inlineSearch) Box(Modifier.padding(horizontal = 16.dp)) { search() }
                content()
            }
        }
    }
}

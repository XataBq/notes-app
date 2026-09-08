package ru.xatab.avitonotes.presentation.notes.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.NoteLayout
import ru.xatab.avitonotes.presentation.common.components.*
import ru.xatab.avitonotes.presentation.common.text.userMessage
import ru.xatab.avitonotes.presentation.notes.components.DeleteNoteDialog
import ru.xatab.avitonotes.presentation.notes.components.NoteCard
import ru.xatab.avitonotes.presentation.notes.viewmodel.NotesViewModel

@Composable
fun NotesScreen(vm: NotesViewModel, open: (Long) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val notes = vm.notes.collectAsLazyPagingItems()
    val snack = remember { SnackbarHostState() }
    val scroll = rememberLazyGridState()
    val grid = state.layout == NoteLayout.GRID
    val compact = LocalConfiguration.current.screenHeightDp < 480

    ErrorSnackbar(state.error, snack, vm::dismissError)
    LaunchedEffect(state.appliedQuery, state.order) {
        scroll.requestScrollToItem(0)
    }
    state.pendingDeletion?.let { DeleteNoteDialog(it, vm::confirmDeletion, vm::dismissDeletion) }

    ListScreenLayout(
        title = stringResource(R.string.tab_notes),
        query = state.query,
        onQuery = vm::query,
        onSearch = vm::search,
        searchLabel = stringResource(R.string.notes_search),
        snackbar = snack,
        actions = {
            IconButton(onClick = vm::toggleLayout) {
                Icon(
                    if (grid) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                    stringResource(if (grid) R.string.layout_list else R.string.layout_grid),
                )
            }
            SortMenu(state.order, vm::sort)
            IconButton(onClick = vm::toggleDeletion) {
                Icon(
                    if (state.deleting) Icons.Outlined.Done else Icons.Outlined.DeleteOutline,
                    stringResource(if (state.deleting) R.string.deletion_finish else R.string.deletion_start),
                    tint = if (state.deleting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        floatingActionButton = {
            if (!state.deleting) {
                FloatingActionButton(
                    onClick = { open(0) },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Icon(Icons.Outlined.Add, stringResource(R.string.note_create)) }
            }
        },
    ) {
        LazyVerticalGrid(
            columns = if (grid) GridCells.Adaptive(156.dp) else GridCells.Fixed(1),
            state = scroll,
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("notes-list"),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 88.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.deleting) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(R.string.deletion_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            when (val refresh = notes.loadState.refresh) {
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorNotice(refresh.error.userMessage(), notes::retry)
                }
                LoadState.Loading -> if (notes.itemCount == 0) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.NotLoading -> if (notes.itemCount == 0) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            Icons.Outlined.NoteAlt,
                            stringResource(
                                if (state.appliedQuery.isEmpty()) R.string.notes_empty else R.string.search_empty,
                            ),
                            stringResource(
                                if (state.appliedQuery.isEmpty()) {
                                    R.string.notes_empty_hint
                                } else {
                                    R.string.notes_search_empty_hint
                                },
                            ),
                        )
                    }
                }
            }
            items(notes.itemCount, key = notes.itemKey { it.id }, contentType = { "note" }) { index ->
                notes[index]?.let { note ->
                    NoteCard(
                        note,
                        grid,
                        compact,
                        state.deleting,
                        note.id in state.deletingIds,
                        onOpen = { if (!state.deleting) open(note.id) },
                        onDelete = { vm.requestDelete(note) },
                    )
                }
            }
            when (val append = notes.loadState.append) {
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorNotice(append.error.userMessage(), notes::retry)
                }
                LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}

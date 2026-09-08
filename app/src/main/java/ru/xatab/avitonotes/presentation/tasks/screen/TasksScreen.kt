package ru.xatab.avitonotes.presentation.tasks.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.Task
import ru.xatab.avitonotes.domain.model.TaskFilter
import ru.xatab.avitonotes.presentation.common.components.EmptyState
import ru.xatab.avitonotes.presentation.common.components.ErrorNotice
import ru.xatab.avitonotes.presentation.common.components.ErrorSnackbar
import ru.xatab.avitonotes.presentation.common.components.ListScreenLayout
import ru.xatab.avitonotes.presentation.common.components.SortMenu
import ru.xatab.avitonotes.presentation.common.components.VoicePanel
import ru.xatab.avitonotes.presentation.common.media.rememberMicrophoneRequest
import ru.xatab.avitonotes.presentation.common.voice.StopRecordingInBackground
import ru.xatab.avitonotes.presentation.tasks.components.DeleteTaskDialog
import ru.xatab.avitonotes.presentation.tasks.components.InlineTaskEditor
import ru.xatab.avitonotes.presentation.tasks.components.TaskFilters
import ru.xatab.avitonotes.presentation.tasks.components.TaskRow
import ru.xatab.avitonotes.presentation.tasks.state.TaskInput
import ru.xatab.avitonotes.presentation.tasks.viewmodel.TasksViewModel

@Composable
fun TasksScreen(vm: TasksViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    var menu by remember { mutableStateOf(false) }
    val microphone = rememberMicrophoneRequest(vm.voice::start)
    StopRecordingInBackground(vm.voice)
    ErrorSnackbar(state.error, snack, vm::dismissError)
    state.pendingDeletion?.let { DeleteTaskDialog(it, vm::confirmDeletion, vm::dismissDeletion) }

    ListScreenLayout(
        title = stringResource(R.string.tab_tasks),
        query = state.query,
        onQuery = vm::query,
        onSearch = vm::search,
        searchLabel = stringResource(R.string.tasks_search),
        snackbar = snack,
        actions = { SortMenu(state.order, vm::sort) },
        floatingActionButton = {
            if (state.input == TaskInput.Idle) {
                Box {
                    FloatingActionButton(
                        onClick = { menu = true },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) { Icon(Icons.Outlined.Add, stringResource(R.string.task_create)) }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.task_type)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = {
                                menu = false
                                vm.beginText()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.task_dictate)) },
                            leadingIcon = { Icon(Icons.Outlined.Mic, null) },
                            onClick = {
                                menu = false
                                microphone()
                            },
                        )
                    }
                }
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("tasks-list"),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "filters") { TaskFilters(state.filter, vm::filter) }
            when (val input = state.input) {
                is TaskInput.Text -> item(key = "input") {
                    InlineTaskEditor(input, vm::editText, vm::add, vm::cancelText)
                }
                is TaskInput.Voice -> item(key = "voice") { VoicePanel(input.state, vm.voice) }
                TaskInput.Idle -> Unit
            }
            when {
                state.loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.loadError != null -> item { ErrorNotice(state.loadError!!, vm::reload) }
                state.tasks.isEmpty() && state.input == TaskInput.Idle -> item {
                    EmptyState(
                        icon = Icons.Outlined.Checklist,
                        title = when {
                            state.appliedQuery.isNotEmpty() -> stringResource(R.string.search_empty)
                            state.filter == TaskFilter.ACTIVE -> stringResource(R.string.tasks_all_done)
                            state.filter == TaskFilter.COMPLETED -> stringResource(R.string.tasks_no_completed)
                            else -> stringResource(R.string.tasks_empty)
                        },
                        subtitle = if (state.appliedQuery.isNotEmpty()) {
                            stringResource(R.string.tasks_search_empty_hint)
                        } else {
                            stringResource(R.string.tasks_empty_hint)
                        },
                    )
                }
            }
            items(state.tasks, key = Task::id) { task ->
                TaskRow(
                    task = task,
                    enabled = task.id !in state.updating,
                    onCompleted = { vm.toggle(task, it) },
                    onDelete = { vm.requestDelete(task) },
                )
            }
        }
    }
}

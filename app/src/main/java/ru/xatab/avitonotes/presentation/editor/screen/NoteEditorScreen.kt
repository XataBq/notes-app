package ru.xatab.avitonotes.presentation.editor.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.presentation.common.components.*
import ru.xatab.avitonotes.presentation.common.input.keyboardDismissTarget
import ru.xatab.avitonotes.presentation.common.media.rememberMicrophoneRequest
import ru.xatab.avitonotes.presentation.common.state.busy
import ru.xatab.avitonotes.presentation.common.text.UiText
import ru.xatab.avitonotes.presentation.common.voice.StopRecordingInBackground
import ru.xatab.avitonotes.presentation.editor.components.*
import ru.xatab.avitonotes.presentation.editor.share.NoteShareIntentFactory
import ru.xatab.avitonotes.presentation.editor.viewmodel.NoteEditorViewModel
import ru.xatab.avitonotes.presentation.notes.components.NoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(vm: NoteEditorViewModel, back: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var imageMenu by remember { mutableStateOf(false) }
    val busy = state.saving || state.importing
    val editable = !state.loading && state.loadError == null && !state.missing && !state.saved
    val canSave = !busy && editable && !state.voice.busy
    val titleDescription = stringResource(R.string.note_title_description)
    val microphone = rememberMicrophoneRequest(vm.voice::start)

    StopRecordingInBackground(vm.voice)
    ErrorSnackbar(state.error, snack, vm::dismissError)
    LaunchedEffect(state.saved) { if (state.saved) back() }
    BackHandler(enabled = busy) { }
    NoteImagePicker(imageMenu, { imageMenu = false }, vm::importImage, vm::error)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = back, enabled = !busy) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(
                        enabled =
                        canSave && (state.title.isNotBlank() || state.text.isNotBlank() || state.imagePath != null),
                        onClick = {
                            scope.launch {
                                try {
                                    val intent = withContext(Dispatchers.IO) {
                                        NoteShareIntentFactory(
                                            context,
                                        ).create(state.title, state.text, state.marks, state.imagePath)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, context.getString(R.string.share_chooser)),
                                    )
                                } catch (error: Exception) {
                                    if (error is kotlinx.coroutines.CancellationException) throw error
                                    vm.error(UiText(R.string.share_error))
                                }
                            }
                        },
                    ) { Icon(Icons.Outlined.IosShare, stringResource(R.string.note_share)) }
                    Button(
                        onClick = vm::save,
                        enabled = canSave,
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        if (state.saving) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.missing -> Box(Modifier.padding(padding).padding(20.dp)) {
                ErrorNotice(stringResource(R.string.note_missing))
            }
            state.loadError != null -> Box(Modifier.padding(padding).padding(20.dp)) {
                ErrorNotice(state.loadError!!, vm::load)
            }
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                    .imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BasicTextField(
                    value = state.title,
                    onValueChange = vm::title,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().keyboardDismissTarget().semantics {
                        contentDescription = titleDescription
                    },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { field ->
                        Box {
                            if (state.title.isEmpty()) {
                                Text(
                                    stringResource(R.string.note_title_hint),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            field()
                        }
                    },
                )
                if (state.titleError) {
                    Text(
                        stringResource(R.string.note_title_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                RichNoteBody(
                    state.text,
                    state.marks,
                    state.typingStyles,
                    !busy,
                    vm::text,
                    vm::selectionChanged,
                    vm::format,
                )
                if (state.importing) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state.imagePath != null) {
                    Box {
                        NoteImage(state.imagePath, Modifier.fillMaxWidth().height(220.dp))
                        FilledIconButton(
                            onClick = vm::removeImage,
                            enabled = !busy,
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        ) { Icon(Icons.Outlined.Close, stringResource(R.string.image_delete)) }
                    }
                }
                VoicePanel(state.voice, vm.voice)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                EditorActions(!busy, !busy && !state.voice.busy, { imageMenu = true }, microphone)
            }
        }
    }
}

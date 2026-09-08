package ru.xatab.avitonotes.presentation.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.xatab.avitonotes.di.ApplicationScope
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle
import ru.xatab.avitonotes.domain.repository.ImageStorage
import ru.xatab.avitonotes.domain.repository.NoteRepository
import ru.xatab.avitonotes.domain.repository.SpeechRepository
import ru.xatab.avitonotes.domain.repository.VoiceRecorder
import ru.xatab.avitonotes.domain.usecase.SaveNote
import ru.xatab.avitonotes.domain.usecase.TextFormatting
import ru.xatab.avitonotes.presentation.common.state.busy
import ru.xatab.avitonotes.presentation.common.text.UiText
import ru.xatab.avitonotes.presentation.common.text.userMessage
import ru.xatab.avitonotes.presentation.common.voice.VoiceInput
import ru.xatab.avitonotes.presentation.editor.state.EditorUiState

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val images: ImageStorage,
    private val savedState: SavedStateHandle,
    @param:ApplicationScope private val cleanupScope: CoroutineScope,
    recorder: VoiceRecorder,
    speech: SpeechRepository,
) : ViewModel() {
    private val id: Long = savedState["id"] ?: 0L
    private val _state = MutableStateFlow(EditorUiState())
    val state = _state.asStateFlow()
    private var createdAt = System.currentTimeMillis()
    private var originalImage: String? = null
    val voice = VoiceInput(viewModelScope, cleanupScope, recorder, { value ->
        _state.update { it.copy(voice = value) }
    }) { audio ->
        val result = speech.recognize(audio).trim()
        if (result.isEmpty()) throw AppException(FailureKind.INVALID_RESPONSE)
        text(state.value.text + (if (state.value.text.isBlank()) "" else "\n\n") + result)
    }
    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, loadError = null, missing = false) }
        viewModelScope.launch {
            try {
                val note = if (id == 0L) null else repository.get(id) ?: throw AppException(FailureKind.NOT_FOUND)
                originalImage = note?.imagePath
                createdAt = note?.createdAt ?: createdAt
                val restored = savedState.get<Boolean>("draft") == true
                _state.update {
                    it.copy(
                        title = if (restored) savedState["title"] ?: "" else note?.title ?: "",
                        text = if (restored) savedState["text"] ?: "" else note?.text ?: "",
                        marks = if (restored) {
                            runCatching { Json.decodeFromString<List<TextMark>>(savedState["marks"] ?: "[]") }
                                .getOrDefault(emptyList())
                        } else {
                            note?.marks.orEmpty()
                        },
                        imagePath = if (restored) savedState["image"] else note?.imagePath,
                        loading = false,
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        missing = (error as? AppException)?.kind == FailureKind.NOT_FOUND,
                        loadError = error.userMessage(),
                    )
                }
            }
        }
    }

    private fun rememberDraft() {
        savedState["draft"] = true
        savedState["title"] = state.value.title
        savedState["text"] = state.value.text
        savedState["image"] = state.value.imagePath
        savedState["marks"] = Json.encodeToString(state.value.marks)
    }

    fun title(value: String) {
        _state.update { it.copy(title = value, titleError = false) }
        rememberDraft()
    }

    fun text(value: String) {
        _state.update {
            it.copy(text = value, marks = TextFormatting.edit(it.text, value, it.marks, it.typingStyles))
        }
        rememberDraft()
    }

    fun selectionChanged() {
        _state.update { it.copy(typingStyles = null) }
    }

    fun format(style: TextStyle, start: Int, end: Int) {
        _state.update { current ->
            if (start == end) {
                val active = current.typingStyles ?: current.marks
                    .filter { it.start < start && it.end >= start }.map { it.style }.toSet()
                current.copy(typingStyles = if (style in active) active - style else active + style)
            } else {
                current.copy(marks = TextFormatting.toggle(current.marks, start, end, style, current.text.length))
            }
        }
        rememberDraft()
    }

    fun importImage(uri: String): kotlinx.coroutines.Job? {
        if (state.value.importing || state.value.saving) return null
        _state.update { it.copy(importing = true, error = null) }
        return viewModelScope.launch {
            try {
                val path = images.import(uri)
                val previous = state.value.imagePath
                _state.update { it.copy(imagePath = path) }
                rememberDraft()
                if (previous != originalImage) images.delete(previous)
            } catch (
                error: Exception,
            ) {
                _state.update { it.copy(error = error.userMessage()) }
            } finally {
                _state.update { it.copy(importing = false) }
            }
        }
    }

    fun removeImage() {
        val previous = state.value.imagePath
        _state.update { it.copy(imagePath = null) }
        rememberDraft()
        if (previous != originalImage) cleanupScope.launch { images.delete(previous) }
    }

    fun save() {
        val current = state.value
        if (current.loading || current.loadError != null || current.saving || current.saved || current.importing ||
            current.missing ||
            current.voice.busy
        ) {
            return
        }
        if (current.title.isBlank()) {
            _state.update { it.copy(titleError = true) }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                SaveNote(repository)(Note(id, current.title, current.text, current.imagePath, createdAt, current.marks))
                originalImage = current.imagePath
                _state.update { it.copy(saved = true) }
            } catch (
                error: Exception,
            ) {
                _state.update { it.copy(error = error.userMessage()) }
            } finally {
                _state.update { it.copy(saving = false) }
            }
        }
    }

    fun error(message: UiText) {
        _state.update { it.copy(error = message) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        voice.close()
        val path = state.value.imagePath
        if (path != originalImage && !state.value.saved) cleanupScope.launch { images.delete(path) }
    }
}

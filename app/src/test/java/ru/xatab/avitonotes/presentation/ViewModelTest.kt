package ru.xatab.avitonotes.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.xatab.avitonotes.domain.model.AccentColor
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.AppSettings
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.NoteLayout
import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.model.Task
import ru.xatab.avitonotes.domain.model.ThemeMode
import ru.xatab.avitonotes.domain.model.TokenBalance
import ru.xatab.avitonotes.domain.repository.GigaChatRepository
import ru.xatab.avitonotes.domain.repository.ImageStorage
import ru.xatab.avitonotes.domain.repository.NoteRepository
import ru.xatab.avitonotes.domain.repository.SettingsRepository
import ru.xatab.avitonotes.domain.repository.SpeechRepository
import ru.xatab.avitonotes.domain.repository.TaskRepository
import ru.xatab.avitonotes.domain.repository.VoiceRecorder
import ru.xatab.avitonotes.domain.usecase.selectNotes
import ru.xatab.avitonotes.presentation.common.state.VoiceState
import ru.xatab.avitonotes.presentation.editor.viewmodel.NoteEditorViewModel
import ru.xatab.avitonotes.presentation.notes.viewmodel.NotesViewModel
import ru.xatab.avitonotes.presentation.tasks.state.TaskInput
import ru.xatab.avitonotes.presentation.tasks.viewmodel.TasksViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before fun setup() = Dispatchers.setMain(dispatcher)

    @After fun teardown() {
        store.clear()
        dispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }
    private fun <T : ViewModel> keep(vm: T): T = vm.also { store.put("vm", it) }

    @Test fun `notes search waits for submit and clears immediately`() = runTest(dispatcher) {
        val repo = Notes().apply {
            rows.value =
                listOf(Note(1, "Москва", "", null, 1), Note(2, "Казань", "Москва", null, 2))
        }
        val vm = keep(NotesViewModel(repo, Settings(), SavedStateHandle()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.notes.collect() }
        runCurrent()
        vm.query("моск")
        runCurrent()
        assertEquals("", repo.lastQuery)
        vm.search()
        runCurrent()
        assertEquals("моск", repo.lastQuery)
        vm.query("")
        runCurrent()
        assertEquals("", repo.lastQuery)
        vm.requestDelete(repo.rows.value.first())
        vm.confirmDeletion()
        runCurrent()
        assertEquals(2, repo.rows.value.size)
        vm.toggleDeletion()
        vm.requestDelete(repo.rows.value.first())
        assertEquals(2, repo.rows.value.size)
        vm.dismissDeletion()
        vm.confirmDeletion()
        assertEquals(2, repo.rows.value.size)
        vm.requestDelete(repo.rows.value.first())
        vm.confirmDeletion()
        runCurrent()
        assertEquals(1, repo.rows.value.size)
    }

    @Test fun `inline task validates blank title and prevents duplicate submission`() = runTest(dispatcher) {
        val repo = Tasks()
        val vm = keep(TasksViewModel(repo, SavedStateHandle(), Recorder(), Speech(), Giga(), backgroundScope))
        runCurrent()
        vm.beginText()
        vm.editText("  ")
        vm.add()
        assertTrue((vm.state.value.input as TaskInput.Text).error)
        vm.editText("  Купить молоко  ")
        vm.add()
        vm.add()
        runCurrent()
        assertEquals(listOf("Купить молоко"), repo.rows.value.map { it.title })
        assertEquals(TaskInput.Idle, vm.state.value.input)
        vm.toggle(repo.rows.value.first(), true)
        runCurrent()
        assertTrue(vm.state.value.tasks.first().completed)
    }

    @Test fun `voice appends to edited note and save retains original creation date`() = runTest(dispatcher) {
        val repo = Notes().apply { rows.value = listOf(Note(7, "План", "Первый абзац", "original", 42)) }
        val vm =
            keep(
                NoteEditorViewModel(
                    repo,
                    Images(),
                    SavedStateHandle(mapOf("id" to 7L)),
                    backgroundScope,
                    Recorder(),
                    Speech(),
                ),
            )
        runCurrent()
        vm.voice.start()
        runCurrent()
        vm.voice.finish()
        runCurrent()
        assertEquals("Первый абзац\n\nРаспознанный текст", vm.state.value.text)
        vm.title("Новый план")
        vm.save()
        runCurrent()
        assertTrue(vm.state.value.saved)
        assertEquals(42L, repo.rows.value.single().createdAt)
        assertEquals("original", repo.rows.value.single().imagePath)
    }

    @Test fun `failed editor load cannot overwrite existing data`() = runTest(dispatcher) {
        val repo = Notes().apply { loadFailure = true }
        val vm =
            keep(
                NoteEditorViewModel(
                    repo,
                    Images(),
                    SavedStateHandle(mapOf("id" to 7L)),
                    backgroundScope,
                    Recorder(),
                    Speech(),
                ),
            )
        runCurrent()
        assertNotNull(vm.state.value.loadError)
        vm.title("Нельзя сохранить")
        vm.save()
        runCurrent()
        assertEquals(0, repo.saves)
    }

    @Test fun `failed voice task processing leaves database unchanged`() = runTest(dispatcher) {
        val repo = Tasks()
        val vm =
            keep(TasksViewModel(repo, SavedStateHandle(), Recorder(), Speech(), Giga(fail = true), backgroundScope))
        runCurrent()
        vm.voice.start()
        runCurrent()
        vm.voice.finish()
        runCurrent()
        assertTrue(repo.rows.value.isEmpty())
        assertTrue(
            (vm.state.value.input as TaskInput.Voice).state is VoiceState.Error,
        )
    }

    private class Notes : NoteRepository {
        var lastQuery = ""
        override fun pages(query: String, order: SortOrder): Flow<PagingData<Note>> {
            lastQuery = query
            return rows.map { PagingData.from(selectNotes(it, query, order)) }
        }
        val rows = MutableStateFlow(emptyList<Note>())
        var loadFailure = false
        var saves = 0
        override fun observe() = rows
        override suspend fun get(id: Long): Note? {
            if (loadFailure) throw AppException(FailureKind.STORAGE)
            return rows.value.find {
                it.id ==
                    id
            }
        }
        override suspend fun save(note: Note): Long {
            saves++
            val id =
                note.id.takeIf { it != 0L } ?: 1
            rows.value = rows.value.filterNot { it.id == id } + note.copy(id = id)
            return id
        }
        override suspend fun delete(note: Note) {
            rows.value = rows.value.filterNot { it.id == note.id }
        }
    }
    private class Tasks : TaskRepository {
        val rows = MutableStateFlow(emptyList<Task>())
        override fun observe() = rows
        override suspend fun add(title: String): Long {
            yield()
            val id =
                rows.value.size + 1L
            rows.value += Task(id, title, false, id)
            return id
        }
        override suspend fun setCompleted(id: Long, completed: Boolean) {
            rows.value =
                rows.value.map { if (it.id == id) it.copy(completed = completed) else it }
        }
        override suspend fun delete(id: Long) {
            rows.value = rows.value.filterNot { it.id == id }
        }
    }
    private class Settings : SettingsRepository {
        override val settings = MutableStateFlow(AppSettings())
        override suspend fun setTheme(theme: ThemeMode) {
            settings.value = settings.value.copy(theme = theme)
        }
        override suspend fun setAccent(accent: AccentColor) {
            settings.value = settings.value.copy(accent = accent)
        }
        override suspend fun setNoteLayout(layout: NoteLayout) {
            settings.value =
                settings.value.copy(noteLayout = layout)
        }
        override suspend fun reset() {
            settings.value = AppSettings()
        }
    }

    private class Images : ImageStorage {
        override suspend fun import(source: String) = source
        override suspend fun delete(path: String?) = Unit
    }
    private class Recorder : VoiceRecorder {
        override suspend fun start() = Unit
        override suspend fun stop() = ByteArray(6400)
        override suspend fun cancel() = Unit
    }
    private class Speech : SpeechRepository {
        override suspend fun recognize(audio: ByteArray) = "Распознанный текст"
    }
    private class Giga(private val fail: Boolean = false) : GigaChatRepository {
        override suspend fun balance() = emptyList<TokenBalance>()
        override suspend fun makeTask(text: String): String {
            if (fail) throw AppException(FailureKind.NETWORK)
            return text
        }
    }
}

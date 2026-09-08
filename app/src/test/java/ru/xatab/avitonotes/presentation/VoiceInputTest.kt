package ru.xatab.avitonotes.presentation

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import ru.xatab.avitonotes.domain.repository.VoiceRecorder
import ru.xatab.avitonotes.presentation.common.state.VoiceState
import ru.xatab.avitonotes.presentation.common.voice.VoiceInput

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceInputTest {
    private class Recorder : VoiceRecorder {
        var starts = 0
        var stops = 0
        var cancels = 0
        var cleanupDelay = 0L
        override suspend fun start() {
            starts++
        }
        override suspend fun stop(): ByteArray {
            stops++
            return ByteArray(6400)
        }
        override suspend fun cancel() {
            delay(cleanupDelay)
            cancels++
        }
    }

    @Test fun `finish is idempotent and releases microphone`() = runTest {
        val recorder = Recorder()
        val states = mutableListOf<VoiceState>()
        var consumed = 0
        val voice = VoiceInput(this, this, recorder, states::add) { consumed++ }
        voice.start()
        voice.start()
        runCurrent()
        assertTrue(states.last() is VoiceState.Recording)
        voice.finish()
        voice.finish()
        advanceUntilIdle()
        assertEquals(1, recorder.starts)
        assertEquals(1, recorder.stops)
        assertEquals(1, consumed)
        assertEquals(1, recorder.cancels)
        assertEquals(VoiceState.Idle, states.last())
    }

    @Test fun `background cancellation blocks restart until cleanup and never submits audio`() = runTest {
        val recorder = Recorder().apply { cleanupDelay = 1000 }
        var consumed = 0
        val states = mutableListOf<VoiceState>()
        val voice = VoiceInput(this, this, recorder, states::add) { consumed++ }
        voice.start()
        runCurrent()
        voice.background()
        voice.start()
        runCurrent()
        assertEquals(1, recorder.starts)
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(0, consumed)
        assertEquals(VoiceState.Idle, states.last())
        voice.start()
        runCurrent()
        assertEquals(2, recorder.starts)
        voice.cancel()
        advanceUntilIdle()
    }

    @Test fun `recording automatically finishes at 55 seconds`() = runTest {
        val recorder = Recorder()
        var consumed = 0
        val voice = VoiceInput(this, this, recorder, {}) { consumed++ }
        voice.start()
        advanceTimeBy(55_000)
        runCurrent()
        assertEquals(1, consumed)
        assertEquals(1, recorder.stops)
    }

    @Test fun `processing cancellation does not become error or save result`() = runTest {
        val states = mutableListOf<VoiceState>()
        var consumed = 0
        val voice = VoiceInput(this, this, Recorder(), states::add) {
            delay(5000)
            consumed++
        }
        voice.start()
        runCurrent()
        voice.finish()
        runCurrent()
        assertEquals(VoiceState.Processing, states.last())
        voice.cancel()
        advanceUntilIdle()
        assertEquals(0, consumed)
        assertFalse(states.any { it is VoiceState.Error })
    }
}

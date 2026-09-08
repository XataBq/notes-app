package ru.xatab.avitonotes.presentation.common.voice

import kotlinx.coroutines.*
import ru.xatab.avitonotes.domain.repository.VoiceRecorder
import ru.xatab.avitonotes.presentation.common.state.VoiceState
import ru.xatab.avitonotes.presentation.common.text.userMessage

class VoiceInput(
    private val scope: CoroutineScope,
    private val cleanupScope: CoroutineScope,
    private val recorder: VoiceRecorder,
    private val onState: (VoiceState) -> Unit,
    private val consume: suspend (ByteArray) -> Unit,
) {
    private var job: Job? = null
    private var recording = false
    private var stop = CompletableDeferred<Unit>()

    fun start() {
        if (job?.isCompleted == false) return
        stop = CompletableDeferred()
        recording = true
        job = scope.launch {
            try {
                recorder.start()
                coroutineScope {
                    val timer = launch {
                        for (seconds in 0 until 55) {
                            onState(VoiceState.Recording(seconds))
                            delay(1000)
                        }
                    }
                    withTimeoutOrNull(55_000) { stop.await() }
                    timer.cancelAndJoin()
                }
                recording = false
                onState(VoiceState.Processing)
                consume(recorder.stop())
                onState(VoiceState.Idle)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                onState(VoiceState.Error(error.userMessage()))
            } finally {
                recording = false
                withContext(NonCancellable) { recorder.cancel() }
            }
        }
    }

    fun finish() {
        if (recording) stop.complete(Unit)
    }

    fun cancel() {
        val previous = job
        previous?.cancel()
        recording = false
        onState(VoiceState.Idle)
        cleanupScope.launch { previous?.join() }
    }

    fun background() {
        if (recording) cancel()
    }

    fun close() {
        cancel()
    }
}

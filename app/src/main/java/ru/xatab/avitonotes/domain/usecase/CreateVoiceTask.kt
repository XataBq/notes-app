package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.repository.GigaChatRepository
import ru.xatab.avitonotes.domain.repository.SpeechRepository
import ru.xatab.avitonotes.domain.repository.TaskRepository

class CreateVoiceTask(
    private val speech: SpeechRepository,
    private val giga: GigaChatRepository,
    private val tasks: TaskRepository,
) {
    suspend operator fun invoke(audio: ByteArray): Long {
        val transcript = speech.recognize(audio).trim()
        if (transcript.isEmpty()) throw AppException(FailureKind.INVALID_RESPONSE)
        val title = giga.makeTask(transcript).trim()
        if (title.isEmpty()) throw AppException(FailureKind.INVALID_RESPONSE)
        return AddTask(tasks)(title)
    }
}

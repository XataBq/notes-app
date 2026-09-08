package ru.xatab.avitonotes.data.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.repository.VoiceRecorder

class PcmVoiceRecorder(private val context: Context) : VoiceRecorder {
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recorder: AudioRecord? = null
    private var capture: Deferred<ByteArray>? = null

    @Volatile private var running = false

    @SuppressLint("MissingPermission")
    override suspend fun start() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (recorder != null) return@withLock
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                throw AppException(FailureKind.RECORDING)
            }
            val minimum = AudioRecord.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minimum <= 0) throw AppException(FailureKind.RECORDING)
            val instance = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minimum * 2, 8192),
            )
            try {
                if (instance.state != AudioRecord.STATE_INITIALIZED) throw AppException(FailureKind.RECORDING)
                instance.startRecording()
                if (instance.recordingState !=
                    AudioRecord.RECORDSTATE_RECORDING
                ) {
                    throw AppException(FailureKind.RECORDING)
                }
                recorder = instance
                running = true
                capture = scope.async {
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(4096)
                    while (running && output.size() < 1_760_000) {
                        val read = instance.read(buffer, 0, minOf(buffer.size, 1_760_000 - output.size()))
                        if (read < 0 && running) throw AppException(FailureKind.RECORDING)
                        if (read > 0) output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                }
            } catch (error: Exception) {
                instance.release()
                recorder = null
                running = false
                throw AppException(FailureKind.RECORDING, error)
            }
        }
    }

    override suspend fun stop(): ByteArray = withContext(Dispatchers.IO) {
        mutex.withLock {
            running = false
            try {
                recorder?.stop()
                val result = capture?.await() ?: throw AppException(FailureKind.EMPTY_AUDIO)
                if (result.size < 3200) throw AppException(FailureKind.EMPTY_AUDIO)
                result
            } finally {
                recorder?.release()
                recorder = null
                capture = null
            }
        }
    }

    override suspend fun cancel() = withContext(Dispatchers.IO + NonCancellable) {
        mutex.withLock {
            running = false
            try {
                recorder?.stop()
            } catch (_: IllegalStateException) { }
            try {
                capture?.cancelAndJoin()
            } finally {
                recorder?.release()
                recorder = null
                capture = null
            }
            Unit
        }
    }
}

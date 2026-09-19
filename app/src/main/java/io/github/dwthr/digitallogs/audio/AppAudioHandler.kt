package io.github.dwthr.digitallogs.audio

import android.net.Uri
import androidx.media3.common.Player
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface AppAudioHandler {
	suspend fun recordAudioAsRef(amplitudeFlow: (Flow<Float>) -> Unit)
	fun stopRecordingAndSave(): Uuid? //TODO: Combine playback/record and integrate state as flow?
	fun providePlayer(): Player
	suspend fun getAudioUriFromRefUuid(audioRefUuid: String): Uri?
	fun deleteRecording(mediaRefId: String): Boolean
	fun cancelRecording()
}
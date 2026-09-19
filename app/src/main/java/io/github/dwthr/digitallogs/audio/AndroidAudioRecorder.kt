package io.github.dwthr.digitallogs.audio

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AndroidAudioRecorder {
	suspend fun startRecord(outputAudioFile: File): Flow<Float>
	fun stopRecord()
}
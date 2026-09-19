package io.github.dwthr.digitallogs.audio.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.github.dwthr.digitallogs.audio.AndroidAudioRecorder
import io.github.dwthr.digitallogs.audio.AppAudioHandler
import io.github.dwthr.digitallogs.audio.data.AudioException.Record.CacheFileRecordFailure
import io.github.dwthr.digitallogs.audio.data.AudioException.Record.MissingCacheFile
import io.github.dwthr.digitallogs.audio.data.AudioException.Record.NonNullRecorderStopBeforeStart
import io.github.dwthr.digitallogs.audio.data.AudioException.Record.ReferenceFileSaveFailure
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.uuid.Uuid

class AppAudioHandlerImpl(
	private val appContext: Context,
	private val audioRecorder: AndroidAudioRecorder,
	private val localRepository: LocalRepository
): AppAudioHandler {
	private val appMediaFileDirectory = File("${appContext.dataDir}/Media")
	private var cachedFileRecording: File? = null

	override suspend fun recordAudioAsRef(amplitudeFlow: (Flow<Float>) -> Unit) {
		val audioCacheFile = withContext(Dispatchers.IO) {
			try {
				File.createTempFile("audioRecording_", ".ogg")
			} catch (e: Exception) {
				if (e !is IOException && e !is SecurityException) {
					throw e
				} else {
					Log.e(this::class.simpleName, "Record audio: Failed to create audioCacheFile", e)
					throw CacheFileRecordFailure()
				}
			}
		}
		cachedFileRecording = audioCacheFile.also {
			amplitudeFlow(audioRecorder.startRecord(it))
		}
	}

	@Throws(RecordInvalidData::class)
	override fun stopRecordingAndSave(): Uuid? {
		try {
			try {
				audioRecorder.stopRecord()
			} catch (e: NonNullRecorderStopBeforeStart) {
				Log.e(this::class.simpleName, "Cannot save recording: recording does not exist")
				clearCache()
				throw e
			} catch (e: RecordInvalidData) {
				Log.e(this::class.simpleName, "Record data invalid, unable to save")
				clearCache()
				return null
			}

			cachedFileRecording.let { cachedRecording ->
				if (cachedRecording == null || !cachedRecording.exists()) {
					Log.e(this::class.simpleName, "Unable to save recording: cache file missing")
					throw MissingCacheFile()
				}

				val mediaReferenceId = try {
					Uuid.random()
				} catch (e: Exception) {
					Log.e(this::class.simpleName, "Uuid generation failed", e)
					throw e
				}

				val mediaReferenceFolder = File(appMediaFileDirectory, mediaReferenceId.toString())
				val mediaReferenceFile = File(mediaReferenceFolder, "audioRecording.ogg") //TODO: Have helper class handle present and future audio suffixes
				try {
					mediaReferenceFolder.mkdirs()
					mediaReferenceFile.createNewFile()

					cachedRecording.inputStream().use { inputStream ->
						mediaReferenceFile.outputStream().use { outputStream ->
							inputStream.copyTo(outputStream, bufferSize = 32 * 1024)
						}
					}
				} catch (e: Exception) {
					Log.e(this::class.simpleName, "Failed to save cache file as reference", e)
					throw ReferenceFileSaveFailure()
				} finally {
					mediaReferenceFolder.deleteOnExit()
					Log.v(this::class.simpleName, "Deleting recording files")
				}

				return mediaReferenceId
			}
		} finally {
			Log.v(this::class.simpleName, "Cleaning up record file cache")
			clearCache()
		}
	}

	override fun cancelRecording() {
		try {
			audioRecorder.stopRecord()
		} catch (e: RuntimeException) {
			Log.i(this::class.simpleName, "Cancel recording", e)
		}
		finally { clearCache() }
	}

	override fun providePlayer(): Player {
		return ExoPlayer.Builder(appContext)
			.build()
	}

	override suspend fun getAudioUriFromRefUuid(audioRefUuid: String): Uri? {
		try {
			audioRecorder.stopRecord()
		} catch (e: NonNullRecorderStopBeforeStart) {
			clearCache()
		} catch (e: RecordInvalidData) {
			clearCache()
		}
		val appMediaFile = File(appMediaFileDirectory, audioRefUuid)

		return if (appMediaFile.exists()) appMediaFile.toUri() else null
	}

	override fun deleteRecording(mediaRefId: String): Boolean {
		val recording = File(appMediaFileDirectory, mediaRefId)

		if (!recording.exists()) return false
		try {
			return recording.deleteRecursively()
		} catch (e: Exception) {
			Log.e(this::class.simpleName, "Failed to delete recording: $mediaRefId", e)
			return false
		}
	}

	private fun clearCache() {
		Log.i(this::class.simpleName, "Clearing cache")
		cachedFileRecording?.delete()
		cachedFileRecording = null
	}
}
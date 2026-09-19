package io.github.dwthr.digitallogs.audio.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import io.github.dwthr.digitallogs.audio.AndroidAudioRecorder
import io.github.dwthr.digitallogs.audio.data.AudioException.Record.NonNullRecorderStopBeforeStart
import io.github.dwthr.digitallogs.audio.data.AudioException.Record.RecorderAlreadyInUse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

class AndroidAudioRecorderImpl( //TODO: https://developer.android.com/develop/ui/compose/layouts/adaptive/support-multi-window-mode#exclusive_resource_access
	private val appContext: Context
): AndroidAudioRecorder {
	private var recorder: MediaRecorder? = null
	private val amplitudeFlowPercent: Flow<Float> = flow {
		while (recorder != null) {
			try {
				recorder?.let {
					val amplitude = it.maxAmplitude.toFloat()
					emit(amplitude / 32767)
					Log.v(this::class.simpleName, "Recording amplitude: $amplitude")
				}
			} catch (e: Exception) {
				Log.w(this::class.simpleName, "Non-null recorder maxAmplitude errored", e)
				emit(0f)
			}
			delay(25.milliseconds)
		}
	}


	private fun createRecorder(): MediaRecorder = if(Build.VERSION.SDK_INT >= 31) {
		MediaRecorder(appContext)
	} else {
		MediaRecorder()
	} //TODO API31: Remove deprecated version

	/**
	 * @throws [RecorderAlreadyInUse]
	 */
	override suspend fun startRecord(outputAudioFile: File): Flow<Float> {
		if (recorder != null) {
			Log.w(this::class.simpleName, "Called startRecord when recorder not null")
			throw RecorderAlreadyInUse()
		}
		createRecorder().apply {
			setAudioSource(MediaRecorder.AudioSource.MIC)
			setOutputFormat(MediaRecorder.OutputFormat.OGG)
			setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
			setOutputFile(FileOutputStream(outputAudioFile).fd)

			try {
				prepare()
				start()
			} catch (e: IllegalStateException) {
				Log.e(this::class.simpleName, "Recording initialization when already in use", e)
				throw RecorderAlreadyInUse()
			}

			recorder = this
		}

		return amplitudeFlowPercent
	}


	/**
	 * @throws [NonNullRecorderStopBeforeStart]
	 */
	override fun stopRecord() {
		recorder?.apply {
			try {
				stop() //TODO: Have this as sharedflow/channel?
			} catch (e: RuntimeException) {
				Log.v(this::class.simpleName, "Stop called immediately after start and RuntimeException was thrown", e)
				throw e
			}
			catch (e: IllegalStateException) {
				Log.w(this::class.simpleName, "Non-null recorder stopped before start", e)
				throw NonNullRecorderStopBeforeStart()
			} finally {
				release()
			}
		}
		recorder = null
	}
}
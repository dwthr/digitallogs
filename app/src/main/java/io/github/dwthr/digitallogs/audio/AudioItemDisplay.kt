package io.github.dwthr.digitallogs.audio

import android.net.Uri
import androidx.media3.common.MediaItem

data class AudioItemDisplay(
	val refId: Long,
	val contentUri: Uri,
	val mediaItem: MediaItem,
	val durationSeconds: Int,
)

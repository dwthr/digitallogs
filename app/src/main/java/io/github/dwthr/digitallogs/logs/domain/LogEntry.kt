package io.github.dwthr.digitallogs.logs.domain

import androidx.compose.runtime.Immutable
import io.github.dwthr.digitallogs.common.domain.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Immutable
@Serializable
data class LogEntry(
	@Serializable(with = InstantSerializer::class)
	val timestamp: Instant,
	val data: EntryData //TODO: Implement support for audio/video/image/etc.
)

@Immutable
sealed interface EntryData {
	data class Media(
		val fileReferenceUuid: String
	): EntryData
	data class Text(
		val text: String
	): EntryData
}

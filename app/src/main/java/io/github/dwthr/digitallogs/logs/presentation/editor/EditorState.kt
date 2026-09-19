package io.github.dwthr.digitallogs.logs.presentation.editor

import io.github.dwthr.digitallogs.logs.domain.LogEntry
import kotlin.time.Instant

internal data class LogEditorState(
	val visibleDialogType: DialogType? = null,
	val recordAmplitudeCount: Int = 50,
	val loadState: LogEditorLoadState = LogEditorLoadState.IsLoading
)

internal sealed interface LogEditorLoadState {
	data object IsLoading: LogEditorLoadState
	data object IsError: LogEditorLoadState
	data class Loaded(
		val logTitle: String,
		val logEntries: List<LogEntry>,
		val expandedEntry: LogEntry? = null,
		val selectedEntries: Set<LogEntry> = emptySet(),
		val logEntryTextValue: String = "",
		val audioState: AudioState? = null,
		val loadingRefId: String? = null,
		val recordMediaTimestamp: Instant? = null,
		val recordAmplitude: List<Float> = emptyList()
	): LogEditorLoadState
}

sealed interface AudioState {
	object MicRecording: AudioState
	data class AudioPlaying(val refUuid: String): AudioState
}

internal sealed interface DialogType {
	data object MicPermissionRejected: DialogType
	data object MicPermissionDisabled: DialogType
	data object SelectDeleteConfirmation: DialogType
}
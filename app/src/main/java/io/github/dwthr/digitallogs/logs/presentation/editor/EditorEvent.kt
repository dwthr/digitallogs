package io.github.dwthr.digitallogs.logs.presentation.editor

import io.github.dwthr.digitallogs.logs.domain.LogEntry

sealed interface EditorEvent {
    data object OnBack: EditorEvent
    data object OnTextEntryAdd: EditorEvent
    data class OnLogTextFieldChanged(val text: String): EditorEvent
    data class OnMicRecordToggle(val isRecording: Boolean): EditorEvent
    data object OnMicRecordPermissionDisabled: EditorEvent
    data object OnNavigatePermissions: EditorEvent
    data object OnMicRecordPermissionRejected: EditorEvent
    data object OnMicRecordPermissionGranted: EditorEvent
    data object OnDialogConfirm: EditorEvent
    data object OnDialogDismiss: EditorEvent
    data object OnMicRecordCancel: EditorEvent
    data class OnAudioEntryPressed(val audioRefUuid: String): EditorEvent
    data class OnEntrySelectionToggle(val entry: LogEntry): EditorEvent
    data object OnEntrySelectionClear: EditorEvent
    data class OnEntryExpandedChanged(val entry: LogEntry?): EditorEvent
    data object OnSelectedEntriesDelete: EditorEvent
	data object OnShareSelectedEntries: EditorEvent
}
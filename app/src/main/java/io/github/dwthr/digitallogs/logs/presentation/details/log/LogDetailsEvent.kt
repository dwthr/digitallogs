package io.github.dwthr.digitallogs.logs.presentation.details.log

import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.logs.domain.Tag

sealed interface LogDetailsEvent {
	data class OnTagDeleteClicked(val tag: Tag): LogDetailsEvent

	data object OnDialogConfirm: LogDetailsEvent
	data object OnDialogCancel: LogDetailsEvent

	data object OnTagAddClicked: LogDetailsEvent
	data class AddTagDialogTextChanged(val textFieldValue: TextFieldValue): LogDetailsEvent

	data object OnTitleEditClicked: LogDetailsEvent
	data class EditTitleDialogTextChanged(val text: String): LogDetailsEvent

	data object OnBack: LogDetailsEvent
}
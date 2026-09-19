package io.github.dwthr.digitallogs.logs.presentation.details.category

import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.logs.domain.Tag

sealed interface CategoryDetailsEvent {
	data object OnBack: CategoryDetailsEvent
	data class OnTagDeleteClicked(val tag: Tag): CategoryDetailsEvent

	data object OnDialogConfirm: CategoryDetailsEvent
	data object OnDialogCancel: CategoryDetailsEvent

	data object OnTagAddClicked: CategoryDetailsEvent
	data class AddTagDialogTextChanged(val textFieldValue: TextFieldValue): CategoryDetailsEvent

	data object OnTitleEditClicked: CategoryDetailsEvent
	data class EditTitleDialogTextChanged(val text: String): CategoryDetailsEvent
}
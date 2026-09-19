package io.github.dwthr.digitallogs.logs.presentation.itemcreator.category

sealed interface CategoryCreatorEvent {
    object OnConfirm : CategoryCreatorEvent
    object OnCancel: CategoryCreatorEvent
    object ToggleAuth: CategoryCreatorEvent
    data class OnTitleTextFieldChanged(val text: String): CategoryCreatorEvent
    data class SetSelectedAuthenticationIndex(val index: Int?): CategoryCreatorEvent
}
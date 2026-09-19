package io.github.dwthr.digitallogs.logs.presentation.itemcreator.log

sealed interface LogCreatorEvent {
    object OnConfirm: LogCreatorEvent
    object OnCancel: LogCreatorEvent
    data class OnTitleTextFieldChanged(val text: String): LogCreatorEvent
    //task
}
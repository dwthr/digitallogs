package io.github.dwthr.digitallogs.logs.presentation.loglist

import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.Tag

sealed interface LogListEvent {
    data object OnSheetDismiss: LogListEvent
    data object OnSortButtonClicked: LogListEvent
    data class OnBackButtonClicked(val isSinglePane: Boolean): LogListEvent
    data object OnLogAddClick: LogListEvent
    data object OnSelectedLogsDelete: LogListEvent
    data object OnLogSelectionClear: LogListEvent
    data object OnDialogConfirm: LogListEvent
    data object OnDialogDismiss: LogListEvent
    data class OnTagButtonClick(val log: LogNote): LogListEvent
    data object OnToolbarDetailsClicked: LogListEvent
    data class OnListItemSelectedChanged(val index: Int): LogListEvent
    data class OnTagAdd(val tag: Tag, val log: LogNote): LogListEvent
    data class OnSearchQueryChange(val query: String): LogListEvent
    data class OnLogClick(val log: LogNote): LogListEvent
    data class OnSortOptionClicked(val optionIndex: Int): LogListEvent
}
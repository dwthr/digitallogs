package io.github.dwthr.digitallogs.logs.presentation.home

import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputType

sealed interface HomeEvent {
    data object OnClickCreateCategory: HomeEvent
    data object OnClickSettings: HomeEvent
    data object OnRecentShowAllClick: HomeEvent
    data object OnCategoriesSortButtonClicked: HomeEvent
    data class OnCategoryClick(val category: Category) : HomeEvent
    data class OnLogClick(val log: LogNote) : HomeEvent
    data object OnToolbarDetailsClicked: HomeEvent
    data object OnSelectedCategoriesDelete: HomeEvent
    data object OnLogSelectionClear: HomeEvent
    data object OnDialogConfirm: HomeEvent
    data object OnDialogDismiss: HomeEvent
    data class OnListItemSelectedChanged(val index: Int): HomeEvent
    data object OnSheetDismiss: HomeEvent
    data class OnSortOptionClicked(val optionIndex: Int): HomeEvent
    //    data class OnListItemSelectedRange(val startIndex: Int, val endIndex: Int): HomeEvent
    //inputChipTextField Search
    data object OnSearchEnter: HomeEvent
    data object OnSearchBackspace: HomeEvent

    data class OnSelectedSearchChipChanged(val index: Int?): HomeEvent
    data class OnAutocompleteOptionClick(val index: Int): HomeEvent
    data class OnSearchQueryTextChange(val textFieldValue: TextFieldValue): HomeEvent
    data class OnSearchChipClick(val index: Int): HomeEvent
    data class OnTypeSuggestionChipClick(val chip: QueryInputType): HomeEvent
}
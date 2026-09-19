package io.github.dwthr.digitallogs.logs.presentation.home

import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.common.presentation.UiText
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.CategorySortType
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputType

internal data class HomeState(
	val categories: List<Category> = emptyList(),
	val selectedCategories: Set<Category> = emptySet(),
	val recentLogs: List<LogAndCategory> = emptyList(),
	val sortType: CategorySortType = CategorySortType.TITLE_DESC,
	val dialogTypeVisible: CategoryListDialog? = null,
	val sheetTypeVisible: HomeSheetType? = null,
	val selectedSortTypeIndex: Int = 1,
	//search state
	val previousSearchQueryText: List<UiText> = emptyList(), //TODO: Should be null to differentiate blank vs non-existent search
	val logSearchResult: List<LogNote> = emptyList(),
	val autocompleteOptions: List<String> = emptyList(),
	val searchFieldTextValue: TextFieldValue = TextFieldValue(),
	val searchQueryChips: List<UiText> = emptyList(),
	val searchChipSelectedIndex: Int? = null,
	val searchFieldChips: List<UiText> = emptyList(),
	val nextTypeSuggestionChips: List<QueryInputType> = emptyList(),
)

internal sealed interface CategoryListDialog {
	data object ConfirmDeleteSelected: CategoryListDialog
}

internal sealed interface HomeSheetType {
	data object Sort: HomeSheetType
}

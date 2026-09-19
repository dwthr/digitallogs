@file:OptIn(FlowPreview::class)

package io.github.dwthr.digitallogs.logs.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.domain.combine
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.common.presentation.UiText
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.CategoryItem
import io.github.dwthr.digitallogs.logs.domain.CategorySortType
import io.github.dwthr.digitallogs.logs.domain.LogItem
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.LogText
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.domain.TagItem
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.presentation.search.QueryDataToUIConverter.toQueryDataChipLabel
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputState
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputType
import io.github.dwthr.digitallogs.logs.presentation.home.HomeEvent.OnAutocompleteOptionClick
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel( // TODO: pagination
    private val repository: LocalRepository,
): ViewModel() {

    private val _sortType = MutableStateFlow(CategorySortType.TITLE_DESC)
    private val _recentLogs = repository.getRecentlyModifiedLogsWithCategory(12)

    private val _queryInputState = MutableStateFlow(QueryInputState())
    private val _queryResults = MutableStateFlow(emptyList<LogNote>())
    private val queryResults = _queryResults.asStateFlow()

    private val _queryDataChips = _queryInputState.mapLatest { inputState ->
        listOf(UiText.Basic("Search logs")) + inputState.queryData.queries.flatMap { queryItem -> queryItem.toQueryDataChipLabel() }
    }
    private val _searchQueryBuilderChips: StateFlow<List<UiText>> = _queryInputState.mapLatest { inputState ->
        inputState.toUIChipBuilderState()
    }.stateInWhileSubscribed(emptyList())

    private val _autocompleteCategories: StateFlow<List<Category>> = _queryInputState //TODO: Usecase
        .repositorySearchQueryFilter()
        .searchFlowTypeCast(
            QueryInputType.Queryable.CATEGORY,
            { state -> repository.searchCategoriesByTitle(state.searchFieldTFV.text) }
        )
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList<Category>())
    private val _autocompleteLogs: StateFlow<List<LogNote>> = _queryInputState
        .repositorySearchQueryFilter()
        .searchFlowTypeCast(QueryInputType.Queryable.LOG,
	        onCastSuccess = { state -> repository.searchLogsByTitle(state.searchFieldTFV.text) },
        )
        .stateInWhileSubscribed(emptyList())
    private val _autocompleteTags: StateFlow<List<Tag>> = _queryInputState
        .repositorySearchQueryFilter()
        .searchFlowTypeCast(
            QueryInputType.Queryable.TAG,
            { state -> repository.searchTagsByLabel(state.searchFieldTFV.text) }
        )
        .stateInWhileSubscribed(emptyList())

    private val _autocompleteSuggestions = combine(_autocompleteLogs, //Searches for queryable
        _autocompleteCategories, _autocompleteTags, _queryInputState) {
        notes, notebooks, tags, builderInput ->
            when(builderInput.queryItem) {
	            QueryInputType.Queryable.LOG -> notes.map { it.title }
	            QueryInputType.Queryable.CATEGORY -> notebooks.map { it.title }
	            QueryInputType.Queryable.TAG -> tags.map { it.label }
                else -> emptyList()
            }
    }.stateInWhileSubscribed(emptyList())

    private val _queryNextTypeSuggestionChips: Flow<List<QueryInputType>> = _queryInputState.mapLatest {
        it.getQueryInputSuggestions() //TODO: Delete?
    }.stateInWhileSubscribed(emptyList() )

    private val _notebooks = _sortType
        .flatMapLatest { sortType ->
            repository.getAllCategories(sortType)
        }
        .stateInWhileSubscribed(emptyList())

    private val _state = MutableStateFlow(HomeState())
	internal val state = combine(_state, _sortType, _recentLogs, _notebooks,
        _autocompleteSuggestions, _queryDataChips, _queryInputState,
        _queryNextTypeSuggestionChips, _searchQueryBuilderChips, queryResults
    ) { state, sortType, recentLogs, notebooks, dropdownSuggestions, searchQueryChips,
        queryState, nextTypeSuggestionChips, queryBuilderChips, queryResults ->
        state.copy(
            categories = notebooks,
            sortType = sortType,
            recentLogs = recentLogs,
            searchFieldTextValue = queryState.searchFieldTFV,
            searchQueryChips = searchQueryChips,
            autocompleteOptions = dropdownSuggestions,
            nextTypeSuggestionChips = nextTypeSuggestionChips,
            searchFieldChips = queryBuilderChips,
            logSearchResult = queryResults
        )
    }.stateInWhileSubscribed(HomeState())


    fun onEvent(event: HomeEvent) {
        when(event) {
            is HomeEvent.OnClickCreateCategory -> Unit
            is HomeEvent.OnCategoryClick -> {
                _state.value.selectedCategories.let { selectedCategories ->
                    if (selectedCategories.isEmpty()) return

                    _state.update { state ->
                        state.copy(
                            selectedCategories = if (event.category in selectedCategories) {
                                selectedCategories.minusElement(event.category)
                            } else {
                                selectedCategories + event.category
                            }
                        )
                    }
                }
            }
            is HomeEvent.OnLogClick -> {
                _queryInputState.update { it.clear() } //TODO: Better implementation that automatically handles clear
                _queryResults.update { emptyList() }
                _state.update {
                    it.copy(
                        previousSearchQueryText = emptyList()
                    )
                }
            }
            is HomeEvent.OnSearchQueryTextChange -> { //TODO: Add highlighting/dropdown? Have enter keyboardAction.
                _queryInputState.update { it.textFieldValueChanged(event.textFieldValue) }
            }
	        HomeEvent.OnRecentShowAllClick -> Unit

	        is HomeEvent.OnSearchEnter -> { //TODO: class that handles notedata and unpacks query
                if ((_queryInputState.value.queryItem == QueryInputType.Queryable.TEXT) &&
                    _queryInputState.value.searchFieldTFV.text.isNotEmpty()
                ) { //Adds chip instead of searching //TODO: Make code more clear
                    _queryInputState.update { queryState -> queryState.updateQueryDataAndClear {
                        it.add(
                            andJoinWithLast = _queryInputState.value.logicalOperator == QueryInputType.LogicalOperators.AND,
                            newQuery = LogText(
                                data = _queryInputState.value.searchFieldTFV.text,
                                isInverted = _queryInputState.value.inputIsInverted
                            ),
                        ) }
                    }
                } else if (_autocompleteSuggestions.value.firstOrNull() != null) {
                    onEvent(OnAutocompleteOptionClick(0))
                } else { //Execute Search
                    viewModelScope.launch {
                        _state.update { it.copy( //TODO: Have this handled by a better search class
                            previousSearchQueryText = _queryInputState.value.queryData.queries
                                .flatMap { logicalQuery ->
                                    logicalQuery
                                    .toQueryDataChipLabel()
                                }
                        ) }
                        _queryResults.update { repository.searchQueryLog(_queryInputState.value.queryData) }
                    }
                }
            }
	        is HomeEvent.OnSearchChipClick -> {

            }
	        is HomeEvent.OnSearchBackspace -> {
                if (_queryInputState.value.searchFieldTFV.text.isEmpty()) {
                    _queryInputState.update { it.deleteLast() }
                }
            }
	        is HomeEvent.OnSelectedSearchChipChanged -> {
                _state.update { it.copy( searchChipSelectedIndex = event.index ) }
            }

	        is OnAutocompleteOptionClick -> {
                val builderData = _queryInputState.value
                val isAndType: Boolean = builderData.logicalOperator == QueryInputType.LogicalOperators.AND
                val isInverted: Boolean = builderData.inputIsInverted

                val queryableWithMetadata = when(builderData.queryItem) { //For type casting //TODO: Find better solution?
	                QueryInputType.Queryable.LOG -> {
                        val item = _autocompleteLogs.value.getOrNull(event.index)
                            ?: throw NotImplementedError("Autocomplete option value out of bounds")

                        LogItem(
	                        log = item,
	                        isInverted = isInverted
                        )
                    }
                    QueryInputType.Queryable.CATEGORY -> {
                        val item = _autocompleteCategories.value.getOrNull(event.index)
                            ?: throw NotImplementedError("Autocomplete option value out of bounds")

                        CategoryItem(
	                        category = item,
	                        isInverted = isInverted
                        )
                    }
                    QueryInputType.Queryable.TAG -> {
                        val item = _autocompleteTags.value.getOrNull(event.index)
                            ?: throw NotImplementedError("Autocomplete option value out of bounds")

                        TagItem(
                            tag = item,
                            isInverted = isInverted
                        )
                    }
                    QueryInputType.Queryable.TEXT -> {
                        LogText(
	                        data = _queryInputState.value.searchFieldTFV.text,
	                        isInverted = isInverted
                        )
                    }
                }

//                _queryData.update {
//                    it.add(
//                        andJoinWithLast = isAndType,
//                        newQuery = queryableWithMetadata
//                    )
//                }
                _queryInputState.update { it.updateQueryDataAndClear { previousQuery ->
                    previousQuery.add(
                        andJoinWithLast = isAndType,
                        newQuery = queryableWithMetadata
                    )
                } }
//                _queryInputState.update { it.clear() } //TODO: Try to move this whole block into the state class
            }

	        is HomeEvent.OnTypeSuggestionChipClick -> { //TODO: Change to index-based
                _queryInputState.update { it.queryInputTypeClicked(event.chip) }
            }

	        HomeEvent.OnDialogConfirm -> {
                _state.update {
                    when(state.value.dialogTypeVisible) {
                        CategoryListDialog.ConfirmDeleteSelected -> {
                            viewModelScope.launch {
                                repository.deleteCategories(*state.value.selectedCategories.toTypedArray())
                            }
                        }
                        null -> return
                    }
                    it.copy(
                        dialogTypeVisible = null,
                        selectedCategories = emptySet()
                    )
                }
            }
	        HomeEvent.OnDialogDismiss -> _state.update { it.copy(dialogTypeVisible = null) }
	        is HomeEvent.OnListItemSelectedChanged -> {
                state.value.selectedCategories.let { selectedCategories ->
                    val categoryEventIndex = state.value.categories.elementAtOrNull(event.index)

                    Log.d(this::class.simpleName, "Selection item at index [${event.index}]: ${categoryEventIndex?.title}")
                    if (categoryEventIndex == null) {
                        Log.w(this::class.simpleName, "No category was found at selected index (${event.index})")
                        return
                    }


                    _state.update { it.copy(
                        selectedCategories = if (categoryEventIndex in selectedCategories) {
                            selectedCategories.minusElement(categoryEventIndex)
                        } else {
                            selectedCategories + categoryEventIndex
                        }
                    ) }
                }
            }
	        HomeEvent.OnLogSelectionClear -> {
                _state.update { it.copy(selectedCategories = emptySet()) }
            }
	        HomeEvent.OnSelectedCategoriesDelete -> {
                _state.update { it.copy(dialogTypeVisible = CategoryListDialog.ConfirmDeleteSelected) }
            }
	        HomeEvent.OnToolbarDetailsClicked -> {
                _state.update { it.copy(selectedCategories = emptySet()) }
            }

	        HomeEvent.OnClickSettings -> Unit
	        HomeEvent.OnSheetDismiss -> {
                _state.update { it.copy(
                    sheetTypeVisible = null
                ) }
            }
	        is HomeEvent.OnSortOptionClicked -> { //TODO: single source of truth
                _state.update { it.copy(selectedSortTypeIndex = event.optionIndex) }
                _sortType.value = CategorySortType.entries[event.optionIndex]
            }

	        HomeEvent.OnCategoriesSortButtonClicked -> {
                _state.update { it.copy(sheetTypeVisible = HomeSheetType.Sort) }
            }
        }
    }

    private fun <T> Flow<T>.repositorySearchQueryFilter(): Flow<T> {
        return this.debounce(150)
            .distinctUntilChanged()
    }
    private fun <R> Flow<QueryInputState>.searchFlowTypeCast(castType: QueryInputType, onCastSuccess: suspend (QueryInputState) -> Flow<R>): Flow<R> {//onCastFail: suspend () -> Flow<R>): Flow<R> {
        return this.flatMapLatest { state ->
            if (state.queryItem == castType) onCastSuccess(state) else emptyFlow()
        }
    }
}
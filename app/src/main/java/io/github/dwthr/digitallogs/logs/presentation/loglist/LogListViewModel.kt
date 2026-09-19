package io.github.dwthr.digitallogs.logs.presentation.loglist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.LogSortType
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ListViewType {
    data object FromAllRecent: ListViewType
    data class FromCategory(val categoryId: Long): ListViewType
    data object ShowAll: ListViewType
}

class LogListViewModel(
    private val repository: LocalRepository,
    private val listViewType: ListViewType
): ViewModel() {
    private val _sortType = MutableStateFlow(LogSortType.DATE_CREATED_DESC)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _logs = _sortType.flatMapLatest { sortType ->
        when(listViewType) {
            ListViewType.FromAllRecent -> {
                repository.getAllLogs(
                    sortedBy = LogSortType.LAST_MODIFIED_DESC
                )
            }
            is ListViewType.FromCategory -> {
                repository.getLogsFromCategory(
                    notebookId = listViewType.categoryId,
                    sortedBy = sortType
                )
            }
            ListViewType.ShowAll -> {
                repository.getAllLogs(
                    sortedBy = sortType
                )
            }
        }
    }.stateInWhileSubscribed(emptyList())

    private val _category = (listViewType as? ListViewType.FromCategory)?.let {
        repository.getCategoryById(it.categoryId)
    } ?: flowOf(null)

    private val _state = MutableStateFlow(LogListState())
    internal val state = combine(_state, _logs, _category) { state, notes, parentCategory ->
        state.copy(
            loadState = when(listViewType) {
	            ListViewType.FromAllRecent -> {
                    LogListLoadState.LoadedRecents(
	                    logs = notes
                    )
                }
	            is ListViewType.FromCategory -> {
                    parentCategory?.let {
                        LogListLoadState.LoadedWithCategory(
                            logs = notes,
                            parentCategoryTitle = parentCategory.title
                        )
                    } ?: LogListLoadState.Loading
                }
	            ListViewType.ShowAll -> {
                    LogListLoadState.LoadedAll(
                        logs = notes
                    )
                }
            }
        )
    }.stateInWhileSubscribed(LogListState())

    fun onEvent(event: LogListEvent) {
        when(event) {
            is LogListEvent.OnLogClick -> {
                state.value.selectedLogs.let { selectedLogs ->
                    if (selectedLogs.isEmpty()) return

                    _state.update { it.copy(
                        selectedLogs = if (event.log in selectedLogs) {
                            selectedLogs.minusElement(event.log)
                        } else {
                            selectedLogs + event.log
                        }
                    ) }
                }
            }
            is LogListEvent.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            LogListEvent.OnLogAddClick -> Unit
            is LogListEvent.OnTagButtonClick -> {
//                _state.update { it.copy(
//                    tagSheetLog = event.log,
//                    sheetTypeVisible = LogListSheetType.TagSheet
//                ) }
            }

            is LogListEvent.OnSheetDismiss -> {
                _state.update { it.copy(sheetTypeVisible = null) }
            }

            is LogListEvent.OnTagAdd -> {
                viewModelScope.launch{ //TODO: Finish
                }
            }

	        is LogListEvent.OnSortOptionClicked -> { //TODO: single source of truth for sort behavior
                _state.update { it.copy(selectedSortTypeIndex = event.optionIndex) }
                _sortType.value = LogSortType.entries[event.optionIndex]
            }

	        LogListEvent.OnSortButtonClicked -> {
                _state.update { it.copy(sheetTypeVisible = LogListSheetType.SortType) }
            }

	        is LogListEvent.OnBackButtonClicked -> Unit
	        is LogListEvent.OnListItemSelectedChanged -> { //TODO: Multi selection via dragging
                (state.value.loadState as? LogListLoadState.Loaded)?.let { loadedState ->
                    state.value.selectedLogs.let { selectedLogs ->
                        val logEventIndex = loadedState.logs.elementAtOrNull(event.index)

                        Log.d(this::class.simpleName, "Selection item at index [${event.index}]: ${logEventIndex?.title}")
                        if (logEventIndex == null) {
                            Log.w(this::class.simpleName, "No log was found at selected index (${event.index})")
                            return
                        }

                        _state.update { it.copy(
                            selectedLogs = if (logEventIndex in selectedLogs) {
                                selectedLogs.minusElement(logEventIndex)
                            } else {
                                selectedLogs + logEventIndex
                            }
                        ) }
                    }
                }
            }

	        is LogListEvent.OnToolbarDetailsClicked -> {
                _state.update { it.copy(selectedLogs = emptySet()) }
            }

	        is LogListEvent.OnSelectedLogsDelete -> {
                _state.update { it.copy(dialogTypeVisible = LogListDialog.DeleteSelectedConfirmation) }
            }

	        LogListEvent.OnLogSelectionClear -> {
                _state.update { it.copy(selectedLogs = emptySet()) }
            }

	        LogListEvent.OnDialogConfirm -> {
                _state.update {
                    when(state.value.dialogTypeVisible) {
	                    LogListDialog.DeleteSelectedConfirmation -> {
                            viewModelScope.launch {
                                repository.deleteLogs(*state.value.selectedLogs.toTypedArray())
                                _state.update { it.copy(
                                    selectedLogs = emptySet()
                                ) }
                            }
                        }
	                    null -> return
                    }
                    it.copy(dialogTypeVisible = null)
                }
            }
	        LogListEvent.OnDialogDismiss -> _state.update { it.copy(dialogTypeVisible = null) }
        }
    }
}
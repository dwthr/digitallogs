package io.github.dwthr.digitallogs.logs.presentation.itemcreator.log

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.NewLogNote
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.DataError
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.log.LogCreatorLoadState.IsLoading
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.log.LogCreatorLoadState.Loaded
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogCreatorViewModel(
    private val repository: LocalRepository,
    private val parentCategoryId: Long
) : ViewModel() {

    private val _parentNotebook = repository.getCategoryById(parentCategoryId)

    private val _state = MutableStateFlow(LogCreatorState())
    internal val state = combine(_state, _parentNotebook) { state, parentCategory ->
        state.copy(
            loadState = if (parentCategory == null) {
	            IsLoading
            } else {
                var newLoadState = state.loadState

                if (state.loadState !is Loaded) {
                    newLoadState = Loaded(
                        parentCategory = parentCategory,
                        titleValue = parentCategory.generateNewLogTitle() //TODO: implement default with user override
                    )
                }

                newLoadState
            }
        )
    }.stateInWhileSubscribed(
        LogCreatorState()
    )

    fun onEvent(event: LogCreatorEvent) {
        val loadedState = state.value.loadState as? Loaded
        if (loadedState == null) {
            Log.v(this::class.simpleName, "onEvent called from unloaded state")
            return
        }

        when(event) {
            LogCreatorEvent.OnCancel -> Unit
            is LogCreatorEvent.OnConfirm -> {
                viewModelScope.launch {
                    _state.update {
                        when(val result = insertNote(loadedState)) {
                            is AppResult.Success -> {
                                it.copy(loadState = loadedState.copy(
                                    newLogId = result.data
                                ))
                            }
                            is AppResult.Error -> {
                                when(result.error) {
                                    DataError.Database.DISK_FULL -> TODO() //TODO: do not reset state and show toast
                                    is EntityError.Title -> {
                                        it.copy(loadState = loadedState.copy(
                                            titleError = result.error
                                        ))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is LogCreatorEvent.OnTitleTextFieldChanged -> {
                _state.update {
                    it.copy(
                        loadState = loadedState.copy(
                            titleValue = event.text,
                            titleError = if (event.text.length <= TitleRestrictions.LOG.maxChars) {
                                null
                            } else {
                                EntityError.Title.TOO_LONG
                            }
                        )
                    )
                }
            }
        }
    }
    private suspend inline fun insertNote(loadedState: Loaded): AppResult<Long, DataError> {
        return repository.insertLog(
            NewLogNote(
                categoryId = parentCategoryId,
                title = loadedState.titleValue
            )
        )
    }
}
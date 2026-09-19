package io.github.dwthr.digitallogs.logs.presentation.details.log

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.NewTag
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToLogError
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.DataError
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsDialogType.AddTag
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsDialogType.EditTitle
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsDialogType.RemoveTagFromLog
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsStateType.IsLoading
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsStateType.Loaded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LogDetailsViewModel(
    private val repository: LocalRepository,
    private val logId: Long
) : ViewModel() {

    private val _logWithTags = repository.getTagsWithLog(logId)
        .stateInWhileSubscribed(null)

    private val _parentCategory: StateFlow<Category?> = _logWithTags.flatMapLatest {
        it?.log?.let { loadedLog ->
            repository.getCategoryById(loadedLog.categoryId)
        } ?: emptyFlow()
    }.stateInWhileSubscribed(null)

    private val _addTagDialogText = MutableStateFlow(TextFieldValue())
    @OptIn(FlowPreview::class)
    private val _tagSearchResult = _addTagDialogText
        .debounce(100)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.searchTagsByLabel(query.text)
        }.stateInWhileSubscribed(
            emptyList()
        )

    private val _state = MutableStateFlow(LogDetailsState())
    internal val state = combine(_state, _logWithTags, _parentCategory, _addTagDialogText, _tagSearchResult) { state, logWithTags, parentCategory, addTagDialogText, tagSearchResult ->
        state.copy(
            loadState = if (logWithTags == null || parentCategory == null) {
	            IsLoading
            } else {
                var newLoadState = state.loadState
                when(state.loadState) {
                    is Loaded -> Unit
                    IsLoading -> {
                        newLoadState = Loaded(
                            log = logWithTags.log,
                            parentCategoryTitle = parentCategory.title,
                            editTitleDialogText = logWithTags.log.title,
                        )
                    }
                }

                newLoadState.copy(
                    log = logWithTags.log,
                    parentCategoryTitle = parentCategory.title,
                    tags = logWithTags.tags,
                    addTagDialogTFV = addTagDialogText,
                    addTagsFilterResults = tagSearchResult,
                )
            }
        )
    }.stateInWhileSubscribed(
        LogDetailsState()
    )

    fun onEvent(event: LogDetailsEvent) {
        state.value.loadState.let { loadedState ->
            if (loadedState !is Loaded) return
            when(event) {
                LogDetailsEvent.OnBack -> Unit

                LogDetailsEvent.OnTitleEditClicked -> {
                    _state.update { //TODO: Open tag editor dialog
                        it.copy(loadState = loadedState.copy(
                            dialogTypeVisible = EditTitle,
                            editTitleDialogText = loadedState.log.title
                        ))
                    }
                }
                LogDetailsEvent.OnTagAddClicked -> {
                    _state.update { //TODO: Open tag editor dialog
                        it.copy(loadState = loadedState.copy(
                            dialogTypeVisible = AddTag
                        ))
                    }
                }

                is LogDetailsEvent.OnTagDeleteClicked -> {
                    _state.update { it.copy(
                        loadState = loadedState.copy(
                            dialogTypeVisible = RemoveTagFromLog(event.tag)
                        )
                    ) }
                }

                LogDetailsEvent.OnDialogCancel -> closeDialog(loadedState)
                LogDetailsEvent.OnDialogConfirm -> {
                    viewModelScope.launch {
                        when(val dialogType = loadedState.dialogTypeVisible) {
                            AddTag -> {
                                val existingTagId = _tagSearchResult.value.find { it.label == loadedState.addTagDialogTFV.text }?.id
                                var resultTagId: Long? = existingTagId

                                if (existingTagId == null) {
                                    resultTagId = createTagFromState(loadedState)
                                }
                                if (resultTagId == null) return@launch

                                //TODO: remake this
                                addTagToLog(loadedState, resultTagId)
                            }
                            EditTitle -> {
                                val result = repository.updateLog(
                                    log = loadedState.log.editTitle(loadedState.editTitleDialogText)
                                )

                                when(result) {
	                                is AppResult.Error -> {
                                        when(result.error) {
	                                        DataError.Database.DISK_FULL -> TODO()
	                                        is EntityError.Title -> {
                                                _state.update { it.copy(
                                                    loadState = loadedState.copy(
                                                        titleError = result.error
                                                    )
                                                ) }
                                            }
                                        }
                                    }
	                                is AppResult.Success -> {
                                        _state.update { it.copy(
                                            loadState = loadedState.copy(
                                                titleError = null
                                            )
                                        ) }
                                        closeDialog(loadedState) //TODO: Open tag editor dialog
                                    }
                                }
                            }
	                        is RemoveTagFromLog -> {
                                viewModelScope.launch {
                                    repository.deleteTagFromLog(
                                        logId = loadedState.log.id, //TODO: Accidentally used deleteTagFromCategory; Require repository to take in entities instead of Longs
                                        tagId = dialogType.tag.id
                                    )
                                    closeDialog(loadedState)
                                }
                            }
                            null -> {
                                Log.v(this::class.simpleName, "OnDialogConfirm with dialog visibility being null")
                            }
                        }
                    }
                }

                is LogDetailsEvent.EditTitleDialogTextChanged -> {
                    _state.update { it.copy(loadState = loadedState.copy(
                        editTitleDialogText = event.text,
                        titleError = if (event.text.length <= TitleRestrictions.LOG.maxChars) {
                            null
                        } else {
                            EntityError.Title.TOO_LONG
                        }
                    )) }
                }
                is LogDetailsEvent.AddTagDialogTextChanged -> {
                    _addTagDialogText.update { event.textFieldValue }
                    _state.update { it.copy(
                        loadState = loadedState.copy(
                            addTagError = null,
                            addTagToLogError = null
                        )
                    ) }
                }
            }
        }
    }

    private fun closeDialog(loadedState: Loaded) {
        _state.update {
            it.copy(loadState = loadedState.copy(dialogTypeVisible = null))
        }
    }

    private suspend fun createTagFromState(loadedState: Loaded): Long? {
        return when(val result = repository.insertTag(NewTag(loadedState.addTagDialogTFV.text))) {
            is AppResult.Error -> { //TODO: Come up with dedicated add action (in sheet?) and error if incorrect title
                when(result.error) { //Insert tag error
                    DataError.Database.DISK_FULL -> TODO("Disk full")
                    is EntityError.Title -> {
                        _state.update { it.copy(
                            loadState = loadedState.copy(
                                addTagError = result.error
                            )
                        ) }
                        null
                    }
                }
            }
            is AppResult.Success -> result.data //TODO: Open tag editor dialog
        }
    }

    private suspend inline fun addTagToLog(loadedState: Loaded, resultTagId: Long) {
        when(val result = repository.addTagToLog(loadedState.log, repository.getTagById(resultTagId).first() ?: throw Exception("Could not fetch tag"))) { //Insert tag-log ref error
            is AppResult.Error -> {
                when(result.error) {
                    is AddTagToLogError.Log -> {
                        when(result.error.error) {
                            DataError.Database.DISK_FULL -> TODO("Disk full")
                            is EntityError.Title -> {
                                _state.update { it.copy(
                                    loadState = loadedState.copy(
                                        addTagToLogError = result.error.error
                                    )
                                ) }
                            }
                        }
                    }
                    is AddTagToLogError.Tag -> {
                        when(result.error.error) {
                            DataError.Database.DISK_FULL -> TODO("Disk full")
                            is EntityError.Title -> {
                                _state.update { it.copy(
                                    loadState = loadedState.copy(
                                        addTagToLogError = result.error.error
                                    )
                                ) }
                            }
                        }
                    }
                }
            }
            is AppResult.Success -> {
                _addTagDialogText.update { TextFieldValue() }
                closeDialog(loadedState)
            }
        }
    }
}
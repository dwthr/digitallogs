package io.github.dwthr.digitallogs.logs.presentation.details.category

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.CategoryWithTags
import io.github.dwthr.digitallogs.logs.domain.NewTag
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToCategoryError
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.DataError
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsDialogType.AddTag
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsDialogType.EditTitle
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsDialogType.RemoveTagFromCategory
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsStateType.IsLoading
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsStateType.Loaded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailsViewModel(
    private val repository: LocalRepository,
    private val categoryId: Long
) : ViewModel() {
    private val _categoryWithTags: StateFlow<CategoryWithTags?> = repository.getTagsWithCategory(categoryId)
        .stateInWhileSubscribed(null)

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

    private val _state = MutableStateFlow(CategoryDetailsState())
    internal val state = combine(_state, _categoryWithTags, _addTagDialogText, _tagSearchResult) { state, categoryWithTags, addTagDialogText, tagSearchResult ->
        state.copy(
            loadState = if (categoryWithTags == null) {
	            IsLoading
            } else {
                var newLoadState = state.loadState
                when(state.loadState) {
                    is Loaded -> Unit
                    IsLoading -> {
                        newLoadState = Loaded(
                            category = categoryWithTags.category,
                            tags = categoryWithTags.tags,
                            editTitleDialogText = categoryWithTags.category.title
                        )
                    }
                }

                newLoadState.copy(
                    addTagDialogTFV = addTagDialogText,
                    addTagsFilterResults = tagSearchResult,
                    category = categoryWithTags.category,
                    tags = categoryWithTags.tags,
                )
            }
        )
    }.stateInWhileSubscribed(
        CategoryDetailsState()
    )

    fun onEvent(event: CategoryDetailsEvent) {
        state.value.loadState.let { loadedState ->
            if (loadedState !is Loaded) return

            when(event) {
                CategoryDetailsEvent.OnBack -> Unit

                CategoryDetailsEvent.OnTitleEditClicked -> {
                    _state.update {
                        it.copy(loadState = loadedState.copy(
                            editTitleDialogText = loadedState.category.title,
                            dialogTypeVisible = EditTitle
                        ))
                    }
                }
                CategoryDetailsEvent.OnTagAddClicked -> {
                    _state.update { //TODO: Open tag editor dialog
                        it.copy(loadState = loadedState.copy(
                            dialogTypeVisible = AddTag
                        ))
                    }
                }

                is CategoryDetailsEvent.OnTagDeleteClicked -> {
                    _state.update { it.copy(
                        loadState = loadedState.copy(
                            dialogTypeVisible = RemoveTagFromCategory(event.tag)
                        )
                    ) }
                }

                CategoryDetailsEvent.OnDialogCancel -> closeDialog(loadedState)
                CategoryDetailsEvent.OnDialogConfirm -> {
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
                                addTagToCategoryFromState(loadedState, resultTagId)
                            }
                            EditTitle -> {
                                val result = repository.updateCategory(
                                    category = loadedState.category.editTitle(loadedState.editTitleDialogText)
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
                                        _state.update { //TODO: Open tag editor dialog
                                            it.copy(loadState = loadedState.copy(
                                                dialogTypeVisible = null,
                                                addTagError = null,
                                                titleError = null,
                                                addTagToCategoryError = null
                                            ))
                                        }
                                    }
                                }
                            }
	                        is RemoveTagFromCategory -> {
                                viewModelScope.launch {
                                    repository.deleteTagFromCategory(
                                        categoryId = loadedState.category.id,
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

                is CategoryDetailsEvent.EditTitleDialogTextChanged -> {
                    _state.update { it.copy(loadState = loadedState.copy(
                        editTitleDialogText = event.text,
                        titleError = if (event.text.length <= TitleRestrictions.CATEGORY.maxChars) {
                            null
                        } else {
                            EntityError.Title.TOO_LONG
                        },
                    )) }
                }

                is CategoryDetailsEvent.AddTagDialogTextChanged -> {
                    _state.update { it.copy(
                        loadState = loadedState.copy(
                            addTagError = null,
                            addTagToCategoryError = null
                        )
                    ) }
                    _addTagDialogText.update { event.textFieldValue }
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
            is AppResult.Error -> { //TODO: Come up with dedicated addTag flow (in sheet?) and error if incorrect title
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

    private suspend inline fun addTagToCategoryFromState(loadedState: Loaded, resultTagId: Long) {
        when(val result = repository.addTagToCategory(loadedState.category, repository.getTagById(resultTagId).first() ?: throw Exception("Could not fetch tag"))) { //Insert tag-log ref error
            is AppResult.Error -> {
                when(result.error) {
                    is AddTagToCategoryError.Category -> {
                        when(result.error.error) {
	                        DataError.Database.DISK_FULL -> TODO("Disk full")
	                        is EntityError.Title -> {
                                _state.update { it.copy(
                                    loadState = loadedState.copy(
                                        addTagToCategoryError = result.error.error
                                    )
                                ) }
                            }
                        }
                    }
                    is AddTagToCategoryError.Tag -> {
                        when(result.error.error) {
                            DataError.Database.DISK_FULL -> TODO("Disk full")
                            is EntityError.Title -> {
                                _state.update { it.copy(
                                    loadState = loadedState.copy(
                                        addTagToCategoryError = result.error.error
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
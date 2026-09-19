@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.dwthr.digitallogs.logs.presentation.itemcreator.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.logs.domain.NewCategory
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.DataError
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryCreatorViewModel(
    private val repository: LocalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryCreatorState())
    val state = _state.asStateFlow()

    fun onEvent(event: CategoryCreatorEvent) {
        when(event) {
            CategoryCreatorEvent.OnCancel -> { }
            CategoryCreatorEvent.OnConfirm -> {
                viewModelScope.launch {
                    _state.update {
                        when(val result = insertCategory(it)) {
                            is AppResult.Success -> {
                                it.copy(
                                    newCategoryId = result.data
                                )
                            }
                            is AppResult.Error -> {
                                when(result.error) {
                                    DataError.Database.DISK_FULL -> TODO() //TODO: do not reset state and show toast
                                    is EntityError.Title -> {
                                        it.copy(
                                            titleError = result.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is CategoryCreatorEvent.SetSelectedAuthenticationIndex -> {
                _state.update {
                    it.copy(
                        authenticationRadioSelectedIndex = event.index
                    )
                }
            }
            is CategoryCreatorEvent.OnTitleTextFieldChanged -> {
                _state.update {
                    it.copy(
                        titleValue = event.text,
                        titleError = if (event.text.length <= TitleRestrictions.CATEGORY.maxChars) {
                            null
                        } else {
                            EntityError.Title.TOO_LONG
                        }
                    )
                }
            }

            CategoryCreatorEvent.ToggleAuth -> {
                _state.update {
                    it.copy(
                        isAuthenticationEnabled = !it.isAuthenticationEnabled
                    )
                }
            }
        }
    }

    private suspend inline fun insertCategory(state: CategoryCreatorState): AppResult<Long, DataError> {
        return repository.insertCategory(
            NewCategory(
                title = state.titleValue,
            )
        )
    }
}
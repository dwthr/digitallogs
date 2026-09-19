package io.github.dwthr.digitallogs.logs.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesRepository
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
	private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
	private val preferencesState: StateFlow<UserPreferencesState?> = userPreferencesRepository.getUserPreferencesState()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
	private val _state = MutableStateFlow(SettingsScreenState())
	internal val state = combine(preferencesState, _state) { preferences, state ->
		state.copy(
			preferencesLoadState = preferences?.let {
				PreferencesLoadState.Loaded(
					allowScreenshots = it.allowScreenshots,
					destructiveCooldownEnabled = it.destructiveCooldownEnabled
				)
			} ?: PreferencesLoadState.Loading
		)
	}.stateInWhileSubscribed(SettingsScreenState())

	fun onEvent(event: SettingsEvent) {
		_state.value.let { state ->
			val preferencesLoaded = state.preferencesLoadState as? PreferencesLoadState.Loaded

			when(event) {
				is SettingsEvent.OnAllowScreenshotsToggled -> {
					viewModelScope.launch {
						userPreferencesRepository.toggleAllowScreenshots(event.value)
					}
				}
				SettingsEvent.OnBack -> Unit
				SettingsEvent.OnExportData -> Unit
				SettingsEvent.OnImportData -> Unit
				is SettingsEvent.OnDestructiveCooldownToggled -> {
					viewModelScope.launch {
						userPreferencesRepository.toggleDestructiveActions(event.value)
					}
				}
			}
		}
	}
}
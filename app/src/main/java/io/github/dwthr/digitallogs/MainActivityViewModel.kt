package io.github.dwthr.digitallogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesRepository
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesState
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesStateDefaults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModel(
	private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
	private val _micPermissionResult = Channel<Boolean>(1)
	val micPermissionResult = _micPermissionResult.receiveAsFlow()

	private val _preferencesState: StateFlow<UserPreferencesState?> = userPreferencesRepository.getUserPreferencesState()
		.stateInWhileSubscribed(null)

	val prefsScreenshotsAllowed = _preferencesState.mapLatest { prefs ->
		prefs?.allowScreenshots ?: UserPreferencesStateDefaults.state.allowScreenshots
	}.stateInWhileSubscribed(UserPreferencesStateDefaults.state.allowScreenshots)

	val prefsDestructiveCooldownEnabled = _preferencesState.mapLatest { prefs ->
		prefs?.destructiveCooldownEnabled ?: UserPreferencesStateDefaults.state.destructiveCooldownEnabled
	}.stateInWhileSubscribed(UserPreferencesStateDefaults.state.destructiveCooldownEnabled)

	suspend fun getUserPreferences(): UserPreferencesState = _preferencesState.filterNotNull().first()
	fun micPermissionGranted(isGranted: Boolean) = viewModelScope.launch {
		_micPermissionResult.send(isGranted)
	}
}
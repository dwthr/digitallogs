package io.github.dwthr.digitallogs.logs.domain.repository.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
	fun getUserPreferencesState(): Flow<UserPreferencesState>
	suspend fun setUserPreferencesState(newUserPreferencesState: UserPreferencesState)
	suspend fun toggleAllowScreenshots(newValue: Boolean)
	suspend fun toggleDestructiveActions(newValue: Boolean)
}
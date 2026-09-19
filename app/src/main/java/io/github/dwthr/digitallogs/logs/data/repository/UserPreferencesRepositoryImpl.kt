package io.github.dwthr.digitallogs.logs.data.repository

import androidx.datastore.core.DataStore
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesRepository
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesState
import io.github.dwthr.digitallogs.proto.AppPreferences
import io.github.dwthr.digitallogs.proto.copy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
	val dataStore: DataStore<AppPreferences>
): UserPreferencesRepository {
	override fun getUserPreferencesState(): Flow<UserPreferencesState> {
		return dataStore.data.map { //TODO: Mapper?
			UserPreferencesState(
				allowScreenshots = it.allowScreenshots,
				destructiveCooldownEnabled = it.destructiveCooldownEnabled
			)
		}
	}

	override suspend fun setUserPreferencesState(newUserPreferencesState: UserPreferencesState) {
		dataStore.updateData {
			it.copy {
				allowScreenshots = newUserPreferencesState.allowScreenshots
				destructiveCooldownEnabled = newUserPreferencesState.destructiveCooldownEnabled
			}
		}
	}

	override suspend fun toggleAllowScreenshots(newValue: Boolean) {
		dataStore.updateData { it.copy { allowScreenshots = newValue } }
	}

	override suspend fun toggleDestructiveActions(newValue: Boolean) {
		dataStore.updateData { it.copy { destructiveCooldownEnabled = newValue } }
	}
}
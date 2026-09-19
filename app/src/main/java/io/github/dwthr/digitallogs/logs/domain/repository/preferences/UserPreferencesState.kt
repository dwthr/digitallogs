package io.github.dwthr.digitallogs.logs.domain.repository.preferences

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesState(
	val allowScreenshots: Boolean,
	val destructiveCooldownEnabled: Boolean
)

object UserPreferencesStateDefaults {
	val state = UserPreferencesState(
		allowScreenshots = false,
		destructiveCooldownEnabled = true
	)
}
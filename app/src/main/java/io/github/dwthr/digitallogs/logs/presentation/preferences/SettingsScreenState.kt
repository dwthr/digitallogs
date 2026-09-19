package io.github.dwthr.digitallogs.logs.presentation.preferences

internal data class SettingsScreenState( //TODO: Have export/import dialog before committing + ability to select export types (file, html)
	val preferencesLoadState: PreferencesLoadState = PreferencesLoadState.Loading
)

internal sealed interface PreferencesLoadState {
	data object Loading: PreferencesLoadState
	data class Loaded(
		val allowScreenshots: Boolean,
		val destructiveCooldownEnabled: Boolean
	): PreferencesLoadState
}
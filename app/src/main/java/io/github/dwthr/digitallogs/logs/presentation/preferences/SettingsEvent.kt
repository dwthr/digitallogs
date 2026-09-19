package io.github.dwthr.digitallogs.logs.presentation.preferences

sealed interface SettingsEvent {
	data class OnAllowScreenshotsToggled(val value: Boolean): SettingsEvent
	data class OnDestructiveCooldownToggled(val value: Boolean): SettingsEvent
	data object OnBack: SettingsEvent
	data object OnImportData: SettingsEvent
	data object OnExportData: SettingsEvent
}
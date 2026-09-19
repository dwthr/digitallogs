package io.github.dwthr.digitallogs.logs.data

sealed interface Preferences { //TODO: Get rid of this?
	companion object {
		const val PREFS_NAME = "preferences.pb"
	}
}
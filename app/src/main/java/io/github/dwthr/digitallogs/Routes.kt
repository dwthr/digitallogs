package io.github.dwthr.digitallogs

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes: NavKey {
	@Serializable
	data object Home: NavKey, Routes

	@Serializable
	data object CategoryCreator: NavKey, CreatorRoute, Routes

	@Serializable
	data class LogCreator(val parentCategoryId: Long): NavKey, CreatorRoute, Routes

	/**
	 * When [clearOnNavBack] is true, indicate that this route should be removed on any back
	 * navigation regardless of the typical behavior.
	 */
	@Serializable
	data class LogsList(val categoryId: Long, val clearOnNavBack: Boolean = false): NavKey, Routes

	@Serializable
	data object RecentLogsList: NavKey, Routes

	@Serializable
	data object AllLogsList: NavKey, Routes

	@Serializable
	data class LogEditor(val logId: Long): NavKey, Routes, DetailPaneRoute

	@Serializable
	data class LogDetails(val logId: Long): NavKey, Routes, DetailPaneRoute {
		@Serializable
		data object ScreenLogDetails: NavKey, Routes
	}

	@Serializable
	data class CategoryDetails(val categoryId: Long): NavKey, Routes {
		@Serializable
		data object ScreenCategoryDetails: NavKey, Routes
	}

	@Serializable
	data object Settings: NavKey, Routes
}

sealed interface DetailPaneRoute: NavKey
sealed interface CreatorRoute: NavKey
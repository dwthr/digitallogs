package io.github.dwthr.digitallogs.logs.presentation.loglist

import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.Tag

internal data class LogListState(
	val searchQuery: String = "",
	val selectedLogs: Set<LogNote> = emptySet(),
	val selectedSortTypeIndex: Int = 1,
	val sheetTypeVisible: LogListSheetType? = null,
	val dialogTypeVisible: LogListDialog? = null,
	val tagSheetLog: LogNote? = null,
	val tagSheetTags: Set<Tag> = emptySet(),
	val loadState: LogListLoadState = LogListLoadState.Loading
)

internal sealed interface LogListLoadState {
	sealed interface Loaded: LogListLoadState {
		val logs: List<LogNote>
	}
	object Loading: LogListLoadState
	data class LoadedWithCategory (
		override val logs: List<LogNote>,
		val parentCategoryTitle: String
	): Loaded, LogListLoadState
	data class LoadedRecents(
		override val logs: List<LogNote>
	): Loaded, LogListLoadState
	data class LoadedAll(
		override val logs: List<LogNote>
	): Loaded, LogListLoadState
}

internal sealed interface LogListSheetType {
	data object SortType: LogListSheetType
//	data object TagSheet: LogListSheetType
}

internal sealed interface LogListDialog {
	data object DeleteSelectedConfirmation: LogListDialog
}

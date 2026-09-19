package io.github.dwthr.digitallogs.logs.presentation.details.log

import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError

internal data class LogDetailsState (
	val titleCharLimit: Int = TitleRestrictions.LOG.maxChars,
	val loadState: LogDetailsStateType = LogDetailsStateType.IsLoading,
)

internal sealed interface LogDetailsStateType {
	data object IsLoading: LogDetailsStateType
	data class Loaded(
		val log: LogNote,
		val parentCategoryTitle: String,
		val titleError: EntityError.Title? = null,
		val addTagError: EntityError.Title? = null,
		val addTagToLogError: EntityError.Title? = null,
		val tags: List<Tag> = emptyList(),
		val dialogTypeVisible: LogDetailsDialogType? = null,
		val addTagDialogTFV: TextFieldValue = TextFieldValue(),
		val editTitleDialogText: String,
		val addTagsFilterResults: List<Tag> = emptyList(),
	): LogDetailsStateType
}

internal sealed interface LogDetailsDialogType {
	data object AddTag: LogDetailsDialogType
	data object EditTitle: LogDetailsDialogType
	data class RemoveTagFromLog(val tag: Tag): LogDetailsDialogType
}
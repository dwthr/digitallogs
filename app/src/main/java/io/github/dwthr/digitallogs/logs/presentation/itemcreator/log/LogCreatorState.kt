package io.github.dwthr.digitallogs.logs.presentation.itemcreator.log

import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError

internal data class LogCreatorState(
    val titleCharLimit: Int = TitleRestrictions.LOG.maxChars,
    val loadState: LogCreatorLoadState = LogCreatorLoadState.IsLoading
)

internal sealed interface LogCreatorLoadState {
    data object IsLoading: LogCreatorLoadState
    data object IsError: LogCreatorLoadState
    data class Loaded(
        val parentCategory: Category,
        val titleValue: String,
        val titleError: EntityError.Title? = null,
        val newLogId: Long? = null,
    ): LogCreatorLoadState
}
package io.github.dwthr.digitallogs.logs.presentation.details.category

import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError

internal data class CategoryDetailsState (
	val titleCharLimit: Int = TitleRestrictions.CATEGORY.maxChars,
	val loadState: CategoryDetailsStateType = CategoryDetailsStateType.IsLoading,
)

internal sealed interface CategoryDetailsStateType {
	data object IsLoading: CategoryDetailsStateType
	data class Loaded(
		val category: Category,
		val tags: List<Tag>,
		val titleError: EntityError.Title? = null,
		val addTagError: EntityError.Title? = null, //TODO: replace both tag errors with a data class or pair
		val addTagToCategoryError: EntityError.Title? = null,
		val dialogTypeVisible: CategoryDetailsDialogType? = null,
		val addTagDialogTFV: TextFieldValue = TextFieldValue(),
		val editTitleDialogText: String,
		val addTagsFilterResults: List<Tag> = emptyList()
	): CategoryDetailsStateType
}

internal sealed interface CategoryDetailsDialogType {
	data object AddTag: CategoryDetailsDialogType
	data object EditTitle: CategoryDetailsDialogType
	data class RemoveTagFromCategory(val tag: Tag): CategoryDetailsDialogType
}
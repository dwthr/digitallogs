package io.github.dwthr.digitallogs.logs.presentation.itemcreator.category

import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError

data class CategoryCreatorState(
	val titleValue: String = "",
	val titleCharLimit: Int = TitleRestrictions.CATEGORY.maxChars,
	val authenticationRadioSelectedIndex: Int? = null,
	val isAuthenticationEnabled: Boolean = false,
	val titleError: EntityError.Title? = null,
	val newCategoryId: Long? = null,
)
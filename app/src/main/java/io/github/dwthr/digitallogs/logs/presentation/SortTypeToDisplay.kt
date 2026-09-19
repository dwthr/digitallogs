package io.github.dwthr.digitallogs.logs.presentation

import io.github.dwthr.digitallogs.logs.domain.CategorySortType
import io.github.dwthr.digitallogs.logs.domain.LogSortType

fun LogSortType.toDisplayString(): String {
	return when(this) {
		LogSortType.DATE_CREATED_ASC -> "Oldest to newest"
		LogSortType.DATE_CREATED_DESC -> "Newest to oldest"
		LogSortType.LAST_MODIFIED_ASC -> "Least recently modified"
		LogSortType.LAST_MODIFIED_DESC -> "Most recently modified"
		LogSortType.TITLE_ASC -> "Title (Z to A)"
		LogSortType.TITLE_DESC -> "Title (A to Z)"
	}
}

fun CategorySortType.toDisplayString(): String {
	return when(this) {
		CategorySortType.TITLE_ASC -> "Title (Z to A)"
		CategorySortType.TITLE_DESC -> "Title (A to Z)"
	}
}
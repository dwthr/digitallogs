package io.github.dwthr.digitallogs.logs.data.repository

import io.github.dwthr.digitallogs.logs.data.mappers.toCategory
import io.github.dwthr.digitallogs.logs.data.mappers.toCategoryEntity
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.CategorySortType
import io.github.dwthr.digitallogs.logs.domain.CategoryWithTags
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogEntry
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.LogSortType
import io.github.dwthr.digitallogs.logs.domain.LogWithTags
import io.github.dwthr.digitallogs.logs.domain.NewCategory
import io.github.dwthr.digitallogs.logs.domain.NewLogNote
import io.github.dwthr.digitallogs.logs.domain.NewTag
import io.github.dwthr.digitallogs.logs.domain.SearchQuery
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.domain.TagSortType
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToCategoryError
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToLogError
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.DataError
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

class FakeLocalRepository: LocalRepository {

	private val categories = MutableStateFlow(emptyList<Category>())
	private val logs = MutableStateFlow(emptyList<LogNote>())
	private val tags = MutableStateFlow(emptyList<Tag>())

	override suspend fun insertCategory(newCategory: NewCategory): AppResult<Long, DataError> {
		categories.update { categories -> categories + newCategory.toCategoryEntity().toCategory() }
		TODO()
	}

	override suspend fun updateCategory(category: Category): AppResult<Unit, DataError> {
		TODO("Not yet implemented")
	}

	override suspend fun insertLog(newLogNote: NewLogNote): AppResult<Long, DataError> {
		TODO("Not yet implemented")
	}

	override suspend fun updateLog(log: LogNote): AppResult<Unit, DataError> {
		TODO("Not yet implemented")
	}

	override suspend fun insertTag(newTag: NewTag): AppResult<Long, DataError> {
		TODO("Not yet implemented")
	}

	override suspend fun updateTag(tag: Tag): AppResult<Unit, DataError> {
		TODO("Not yet implemented")
	}

	override suspend fun addTagToLog(
		log: LogNote,
		tag: Tag,
	): AppResult<Unit, AddTagToLogError> {
		TODO("Not yet implemented")
	}

	override suspend fun addTagToCategory(
		category: Category,
		tag: Tag,
	): AppResult<Unit, AddTagToCategoryError> {
		TODO("Not yet implemented")
	}

	override suspend fun deleteCategories(vararg categories: Category): Int {
		TODO("Not yet implemented")
	}

	override suspend fun deleteLogs(vararg logs: LogNote): Int {
		TODO("Not yet implemented")
	}

	override suspend fun deleteTags(vararg tags: Tag): Int {
		TODO("Not yet implemented")
	}

	override suspend fun deleteEntriesFromLog(
		logId: Long,
		onLastMediaRefDelete: (List<String>) -> Unit,
		vararg logEntry: LogEntry
	) {
		TODO("Not yet implemented")
	}

	override suspend fun deleteTagFromLog(logId: Long, tagId: Long) {
		TODO("Not yet implemented")
	}

	override suspend fun deleteTagFromCategory(categoryId: Long, tagId: Long) {
		TODO("Not yet implemented")
	}

	override fun getCategoryById(id: Long): Flow<Category?> {
		TODO("Not yet implemented")
	}

	override fun getLogById(id: Long): Flow<LogNote?> {
		TODO("Not yet implemented")
	}

	override fun getTagById(id: Long): Flow<Tag?> {
		TODO("Not yet implemented")
	}

	override suspend fun searchQueryLog(searchQuery: SearchQuery): List<LogNote> {
		TODO("Not yet implemented")
	}

	override fun searchCategoriesByTitle(
		title: String,
		limit: Int,
	): Flow<List<Category>> {
		var result = categories.value.filter { it.title.contains(title) }
		if (limit >= 0) result = result.take(limit)

		return flowOf(result)
	}

	override fun searchLogsByTitle(
		title: String,
		limit: Int,
	): Flow<List<LogNote>> {
		var result = logs.value.filter { it.title.contains(title) }
		if (limit >= 0) result = result.take(limit)

		return flowOf(result)	}

	override fun searchTagsByLabel(
		label: String,
		limit: Int,
	): Flow<List<Tag>> {
		var result = tags.value.filter { it.label.contains(label) }
		if (limit >= 0) result = result.take(limit)

		return flowOf(result)	}

	override fun getAllCategories(sortedBy: CategorySortType): Flow<List<Category>> {
		TODO("Not yet implemented")
	}

	override fun getLogsFromCategory(
		notebookId: Long,
		sortedBy: LogSortType,
	): Flow<List<LogNote>> {
		TODO("Not yet implemented")
	}

	override fun getLogsFromTag(tagId: Long): Flow<List<LogNote>> {
		TODO("Not yet implemented")
	}

	override fun getAllLogs(sortedBy: LogSortType): Flow<List<LogNote>> {
		TODO("Not yet implemented")
	}

	override fun getLogWithCategory(noteId: Long): Flow<LogAndCategory> {
		TODO("Not yet implemented")
	}

	override fun getRecentlyModifiedLogsWithCategory(count: Int): Flow<List<LogAndCategory>> {
		TODO("Not yet implemented")
	}

	override fun getAllTags(sortedBy: TagSortType): Flow<List<Tag>> {
		TODO("Not yet implemented")
	}

	override fun getTagsWithCategory(categoryId: Long): Flow<CategoryWithTags> {
		TODO("Not yet implemented")
	}

	override fun getTagsWithLog(logId: Long): Flow<LogWithTags> {
		TODO("Not yet implemented")
	}

	override suspend fun newMediaLog(
		log: LogNote,
		mediaRefUuid: String,
	): Long {
		TODO("Not yet implemented")
	}


}
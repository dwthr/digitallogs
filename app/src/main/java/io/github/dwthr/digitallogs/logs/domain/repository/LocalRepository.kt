package io.github.dwthr.digitallogs.logs.domain.repository

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
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    //upsert
    suspend fun insertCategory(newCategory: NewCategory): AppResult<Long, DataError>
    suspend fun updateCategory(category: Category): AppResult<Unit, DataError>
    suspend fun insertLog(newLogNote: NewLogNote): AppResult<Long, DataError>
    suspend fun updateLog(log: LogNote): AppResult<Unit, DataError>
    suspend fun insertTag(newTag: NewTag): AppResult<Long, DataError>
    suspend fun updateTag(tag: Tag): AppResult<Unit, DataError>
    suspend fun addTagToLog(log: LogNote, tag: Tag): AppResult<Unit, AddTagToLogError>
    suspend fun addTagToCategory(category: Category, tag: Tag): AppResult<Unit, AddTagToCategoryError>
    //delete //TODO: add delete results
    suspend fun deleteCategories(vararg categories: Category): Int
	suspend fun deleteLogs(vararg logs: LogNote): Int
	suspend fun deleteTags(vararg tags: Tag): Int
	suspend fun deleteEntriesFromLog(logId: Long, onLastMediaRefDelete: (List<String>) -> Unit, vararg logEntry: LogEntry)

	suspend fun deleteTagFromLog(logId: Long, tagId: Long)
    suspend fun deleteTagFromCategory(categoryId: Long, tagId: Long)
    fun getCategoryById(id: Long): Flow<Category?>
    fun getLogById(id: Long): Flow<LogNote?>
    fun getTagById(id: Long): Flow<Tag?>
    suspend fun searchQueryLog(searchQuery: SearchQuery): List<LogNote>
    fun searchCategoriesByTitle(title: String, limit: Int = -1): Flow<List<Category>>//Result<List<Category>>

    fun searchLogsByTitle(title: String, limit: Int = -1): Flow<List<LogNote>>//Result<List<Category>>
    fun searchTagsByLabel(label: String, limit: Int = -1): Flow<List<Tag>>//Result<List<Category>>
    fun getAllCategories(sortedBy: CategorySortType): Flow<List<Category>>
    fun getLogsFromCategory(notebookId: Long, sortedBy: LogSortType): Flow<List<LogNote>>
    fun getLogsFromTag(tagId: Long): Flow<List<LogNote>>

    fun getAllLogs(sortedBy: LogSortType): Flow<List<LogNote>>
    fun getLogWithCategory(noteId: Long): Flow<LogAndCategory>
    fun getRecentlyModifiedLogsWithCategory(count: Int): Flow<List<LogAndCategory>>
    fun getAllTags(sortedBy: TagSortType): Flow<List<Tag>>
    fun getTagsWithCategory(categoryId: Long): Flow<CategoryWithTags>
    fun getTagsWithLog(logId: Long): Flow<LogWithTags>
	suspend fun newMediaLog(log: LogNote, mediaRefUuid: String): Long
}
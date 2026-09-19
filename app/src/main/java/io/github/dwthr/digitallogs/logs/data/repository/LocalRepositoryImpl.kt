package io.github.dwthr.digitallogs.logs.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteFullException
import android.util.Log
import io.github.dwthr.digitallogs.logs.data.AddTagValidator
import io.github.dwthr.digitallogs.logs.data.CategoryValidator
import io.github.dwthr.digitallogs.logs.data.LogValidator
import io.github.dwthr.digitallogs.logs.data.TagValidator
import io.github.dwthr.digitallogs.logs.data.database.RawQueries
import io.github.dwthr.digitallogs.logs.data.database.dao.CategoryDao
import io.github.dwthr.digitallogs.logs.data.database.dao.EntryDao
import io.github.dwthr.digitallogs.logs.data.database.dao.LogDao
import io.github.dwthr.digitallogs.logs.data.database.dao.TagDao
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.EntryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogTagCrossRef
import io.github.dwthr.digitallogs.logs.data.mappers.toCategoriesFlow
import io.github.dwthr.digitallogs.logs.data.mappers.toCategory
import io.github.dwthr.digitallogs.logs.data.mappers.toCategoryEntity
import io.github.dwthr.digitallogs.logs.data.mappers.toLog
import io.github.dwthr.digitallogs.logs.data.mappers.toLogAndCategoryFlow
import io.github.dwthr.digitallogs.logs.data.mappers.toLogEntity
import io.github.dwthr.digitallogs.logs.data.mappers.toLogWithEntries
import io.github.dwthr.digitallogs.logs.data.mappers.toLogsFlow
import io.github.dwthr.digitallogs.logs.data.mappers.toTag
import io.github.dwthr.digitallogs.logs.data.mappers.toTagEntity
import io.github.dwthr.digitallogs.logs.data.mappers.toTagsFlow
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.CategorySortType
import io.github.dwthr.digitallogs.logs.domain.CategoryWithTags
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogEntry
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.LogSortType
import io.github.dwthr.digitallogs.logs.domain.LogWithTags
import io.github.dwthr.digitallogs.logs.domain.NewCategory
import io.github.dwthr.digitallogs.logs.domain.NewLogNote
import io.github.dwthr.digitallogs.logs.domain.NewTag
import io.github.dwthr.digitallogs.logs.domain.QueryAnd
import io.github.dwthr.digitallogs.logs.domain.SearchQuery
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.domain.TagSortType
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToCategoryError
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToLogError
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.DataError
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class LocalRepositoryImpl( //TODO: Have query builder for sorting/grouping instead of current impl
    private val categoryDao: CategoryDao,
    private val logDao: LogDao,
    private val tagDao: TagDao,
    private val entryDao: EntryDao,
    private val defaultDispatcher: CoroutineDispatcher //TODO: https://developer.android.com/topic/architecture/data-layer#make_an_operation_live_longer_than_the_screen
//    private val remoteNotesDataSource: RemoteDataSource,
): LocalRepository {
    override suspend fun insertCategory(newCategory: NewCategory): AppResult<Long, DataError> = withContext(defaultDispatcher) {
	    CategoryValidator.validateTitle(newCategory.title)?.let { validationError ->
			return@withContext AppResult.Error(validationError)
	    }

        return@withContext try {
	        val newId = categoryDao.insertCategory(newCategory.toCategoryEntity())
	        Log.i(this::class.java.simpleName, "Inserted category: ${newCategory.title}")

	        AppResult.Success(newId)
        } catch (e: SQLiteConstraintException) {
	        AppResult.Error(
		        CategoryValidator.checkCategoryDuplicateTitle(categoryDao, newCategory.title) ?: throw e
			)
        } catch (e: SQLiteFullException) {
            Log.e(this::class.java.simpleName, "Database or disk is full", e) //TODO
            return@withContext AppResult.Error(DataError.Database.DISK_FULL) //TODO: Add some sort of caching instead of throwing and losing user data?
        }
    }
    override suspend fun updateCategory(category: Category): AppResult<Unit, DataError> = withContext(defaultDispatcher) {
        CategoryValidator.validateTitle(category.title)?.let { validationError ->
			return@withContext AppResult.Error(validationError)
        }

        return@withContext try {
	        categoryDao.updateCategory(category.toCategoryEntity())
	        Log.i(this::class.java.simpleName, "Updated category: ${category.title}")

	        AppResult.Success(Unit)
        } catch (e: SQLiteConstraintException) {
	        AppResult.Error(
		        CategoryValidator.checkCategoryDuplicateTitle(categoryDao, category.title) ?: throw e
			)
        } catch (e: SQLiteFullException) {
            Log.e(this::class.java.simpleName, "Database or disk is full", e)
	        return@withContext AppResult.Error(DataError.Database.DISK_FULL)
        }
    }
    override suspend fun insertLog(newLogNote: NewLogNote): AppResult<Long, DataError> = withContext(defaultDispatcher) {
        LogValidator.validateTitle(newLogNote.title)?.let { validationError ->
			return@withContext AppResult.Error(validationError)
        }

	    return@withContext try {
		    val newId = logDao.insertLog(newLogNote.toLogEntity())
		    Log.i(this::class.java.simpleName, "Inserted log: ${newLogNote.title}")

		    AppResult.Success(newId)
	    } catch (e: SQLiteConstraintException) {
		    AppResult.Error(
			    LogValidator.checkLogDuplicateTitle(logDao, newLogNote.title) ?: throw e
		    )
	    } catch (e: SQLiteFullException) {
		    Log.e(this::class.java.simpleName, "Database or disk is full", e)
		    return@withContext AppResult.Error(DataError.Database.DISK_FULL)
	    }
    }
    override suspend fun updateLog(log: LogNote): AppResult<Unit, DataError> = withContext(defaultDispatcher) {
	    LogValidator.validateTitle(log.title)?.let { validationError ->
		    return@withContext AppResult.Error(validationError)
	    }

        return@withContext try {
	        logDao.updateLogWithEntries(log.toLogWithEntries())
	        Log.i(this::class.java.simpleName, "Updated log: ${log.title}")

	        AppResult.Success(Unit)
        } catch (e: SQLiteConstraintException) {
	        AppResult.Error(
	            LogValidator.checkLogDuplicateTitle(logDao, log.title) ?: throw e
			)
		} catch (e: SQLiteFullException) {
            Log.e(this::class.java.simpleName, "Database or disk is full", e)
	        return@withContext AppResult.Error(DataError.Database.DISK_FULL)
        }
    }
    override suspend fun insertTag(newTag: NewTag): AppResult<Long, DataError> = withContext(defaultDispatcher) {
	    TagValidator.validateTitle(newTag.label)?.let { validationError ->
		    return@withContext AppResult.Error(validationError)
	    }

	    return@withContext try {
		    val newId = tagDao.insertTag(newTag.toTagEntity())
		    Log.i(this::class.java.simpleName, "Inserted tag: ${newTag.label}")

		    AppResult.Success(newId)
		} catch (e: SQLiteConstraintException) {
		    AppResult.Error(
		        TagValidator.checkTagDuplicateLabel(tagDao, newTag.label) ?: throw e
		    )
		} catch (e: SQLiteFullException) {
		    Log.e(this::class.java.simpleName, "Database or disk is full", e)
		    return@withContext AppResult.Error(DataError.Database.DISK_FULL)
	    }
    }
    override suspend fun updateTag(tag: Tag): AppResult<Unit, DataError> = withContext(defaultDispatcher) {
	    TagValidator.validateTitle(tag.label)?.let { validationError ->
		    return@withContext AppResult.Error(validationError)
	    }

	    return@withContext try {
		    val newId = tagDao.updateTag(tag.toTagEntity())
		    Log.i(this::class.java.simpleName, "Updated tag: ${tag.label}")
		    AppResult.Success(newId)
	    } catch (e: SQLiteConstraintException) {
		    AppResult.Error(
			    TagValidator.checkTagDuplicateLabel(tagDao, tag.label) ?: throw e
		    )
	    } catch (e: SQLiteFullException) {
		    Log.e(this::class.java.simpleName, "Database or disk is full", e)
		    return@withContext AppResult.Error(DataError.Database.DISK_FULL)
	    }
    }

    override suspend fun addTagToLog(
        log: LogNote,
        tag: Tag
    ): AppResult<Unit, AddTagToLogError> = withContext(defaultDispatcher) { //TODO: Error describing log/tag error for nonexistent and invalid ids
	    return@withContext try {
		    tagDao.addTagsToLog(LogTagCrossRef(log.id, tag.id))
		    AppResult.Success(Unit)
	    } catch (e: SQLiteConstraintException) {
		    AddTagValidator.checkAddTagToLog(
			    tagDao = tagDao,
			    tag = tag,
			    logDao = logDao,
			    log = log
		    ) ?: throw e
	    }
    }

    override suspend fun addTagToCategory(
        category: Category,
        tag: Tag
    ): AppResult<Unit, AddTagToCategoryError> = withContext(defaultDispatcher) {
	    return@withContext try {
		    tagDao.addTagsToCategory(CategoryTagCrossRef(category.id, tag.id))
		    AppResult.Success(Unit)
	    } catch (e: SQLiteConstraintException) {
		    return@withContext AddTagValidator.checkAddTagToCategory(
			    tagDao = tagDao,
			    tag = tag,
			    categoryDao = categoryDao,
			    category = category
		    ) ?: throw e //TODO: Add some sort of caching instead of throwing and losing user data?
	    }
    }

    override suspend fun deleteCategories(vararg categories: Category) = withContext(defaultDispatcher) {
        categoryDao.deleteCategories(categories.map { it.toCategoryEntity() })// TODO: Return success or failure
    }
    override suspend fun deleteLogs(vararg logs: LogNote) = withContext(defaultDispatcher) {
        logDao.deleteLogs(logs.map { it.toLogEntity() })
    }

    override suspend fun deleteTags(vararg tags: Tag) = withContext(defaultDispatcher) {
        tagDao.deleteTags(tags.map { it.toTagEntity() })
    }

    override suspend fun deleteEntriesFromLog(
	    logId: Long,
	    onLastMediaRefDelete: (List<String>) -> Unit,
	    vararg logEntry: LogEntry,
    ) = withContext(defaultDispatcher) {
		val timestamps = logEntry
			.map { it.timestamp }
			.toTypedArray()
	    val deletedMediaRefs = logEntry
		    .map { it.data }
		    .filterIsInstance<EntryData.Media>()
		    .map { it.fileReferenceUuid }
		    .distinct()

        entryDao.deleteEntry(logId, *timestamps)

	    val remainingMediaRefs = entryDao.getEntriesByMediaRefIds(deletedMediaRefs).map { it.mediaReferenceId }
	    val mediaIdsToDelete = deletedMediaRefs.toSet() - remainingMediaRefs.toSet()

	    onLastMediaRefDelete(mediaIdsToDelete.filterNotNull())
    }

    override suspend fun deleteTagFromLog(
        logId: Long,
        tagId: Long
    )  = withContext(defaultDispatcher) {
        tagDao.removeTagFromLog(LogTagCrossRef(logId, tagId))
    }

    override suspend fun deleteTagFromCategory(
        categoryId: Long,
        tagId: Long
    ) = withContext(defaultDispatcher) {
        tagDao.removeTagFromCategory(CategoryTagCrossRef(categoryId, tagId))
    }

	override fun getCategoryById(id: Long): Flow<Category?> { //TODO: return AppResult
        return categoryDao.getCategoryById(id).map { it?.toCategory() }
    }

    override fun getLogById(id: Long): Flow<LogNote?> { //TODO: return AppResult
        return logDao.getLogWithEntriesById(id).map { it?.toLog() }
    }

    override fun getTagById(id: Long): Flow<Tag?> { //TODO: return AppResult
        return tagDao.getTagById(id).map { it?.toTag() }
    }

    /**
     * Each query in a [SearchQuery] currently adds all the unique results together.
     * [QueryAnd] groups filter logs based on certain attributes and the logic is canceled if
     * the user asks for impossible Log state. Such as, having multiple parent Notebooks; In
     * order to save compute.
     */
    override suspend fun searchQueryLog(searchQuery: SearchQuery): List<LogNote> = withContext(defaultDispatcher) {
	    logDao.searchLogs(RawQueries.buildSearchQuery(searchQuery))
		    .first()
		    .map { it.toLog() }
    }

    override fun searchCategoriesByTitle(title: String, limit: Int): Flow<List<Category>> {
        return categoryDao.searchCategories(RawQueries.searchCategoriesByTitle(RawQueries.UserInputSQLString(title)))
            .toCategoriesFlow()
    }

    override fun searchLogsByTitle(title: String, limit: Int): Flow<List<LogNote>> {
        return logDao.searchLogs(RawQueries.searchLogsByTitle(RawQueries.UserInputSQLString(title)))
            .toLogsFlow()//FIXME: This sanitizer is garbage and searches literally
    }

    override fun searchTagsByLabel(label: String, limit: Int): Flow<List<Tag>> {
        return tagDao.searchTags(RawQueries.searchTagsByLabel(RawQueries.UserInputSQLString(label)))
            .toTagsFlow()
    }

    override fun getAllCategories( //TODO: Use Paging3
        sortedBy: CategorySortType
    ): Flow<List<Category>> {
        return when(sortedBy){
	        CategorySortType.TITLE_DESC -> {
                categoryDao.getCategoriesByTitleDesc().toCategoriesFlow()
            }
	        CategorySortType.TITLE_ASC -> {
                categoryDao.getCategoriesByTitleAsc().toCategoriesFlow()
            }
        }
    }

    override fun getAllLogs(
        sortedBy: LogSortType,
    ): Flow<List<LogNote>> {
        return when(sortedBy){
	        LogSortType.DATE_CREATED_DESC -> {
                logDao.getAllLogsByDateCreatedDesc().toLogsFlow()
            }
	        LogSortType.DATE_CREATED_ASC -> {
                logDao.getAllLogsByDateCreatedAsc().toLogsFlow()
            }
	        LogSortType.LAST_MODIFIED_DESC -> {
                logDao.getAllLogsByLastModifiedDesc().toLogsFlow()
            }
	        LogSortType.LAST_MODIFIED_ASC -> {
                logDao.getAllLogsByLastModifiedAsc().toLogsFlow()
            }
	        LogSortType.TITLE_DESC -> {
                logDao.getAllLogsByTitleDesc().toLogsFlow()
            }
	        LogSortType.TITLE_ASC -> {
                logDao.getAllLogsByTitleDesc().toLogsFlow()
            }
        }
    }

    override fun getAllTags(sortedBy: TagSortType): Flow<List<Tag>> {
        return when(sortedBy) {
	        TagSortType.LABEL_ASC -> tagDao.getTagsByLabelAsc().toTagsFlow()
	        TagSortType.LABEL_DESC -> tagDao.getTagsByLabelDesc().toTagsFlow()
        }
    }

    override fun getLogWithCategory(noteId: Long): Flow<LogAndCategory> {
        return logDao.getLogWithCategory(noteId).map {
            LogAndCategory(
	            log = it.logWithEntry.toLog(),
	            parentCategory = it.category.toCategory()
            )
        }
    }

    override fun getRecentlyModifiedLogsWithCategory(count: Int): Flow<List<LogAndCategory>> {
        return logDao.getMostRecentLogs(count).toLogAndCategoryFlow()
    }

    override fun getLogsFromCategory(
        notebookId: Long, sortedBy: LogSortType
    ): Flow<List<LogNote>> {
        return when(sortedBy){
	        LogSortType.DATE_CREATED_DESC -> {
                logDao.getLogsFromCategoryByDateCreatedDesc(notebookId).toLogsFlow()
            }
	        LogSortType.DATE_CREATED_ASC -> {
                logDao.getLogsFromCategoryByDateCreatedAsc(notebookId).toLogsFlow()
            }
	        LogSortType.LAST_MODIFIED_DESC -> {
                logDao.getLogsFromCategoryByLastModifiedDesc(notebookId).toLogsFlow()
            }
	        LogSortType.LAST_MODIFIED_ASC -> {
                logDao.getLogsFromCategoryByLastModifiedAsc(notebookId).toLogsFlow()
            }
	        LogSortType.TITLE_DESC -> {
                logDao.getLogsFromCategoryByTitleDesc(notebookId).toLogsFlow()
            }
	        LogSortType.TITLE_ASC -> {
                logDao.getLogsFromCategoryByTitleAsc(notebookId).toLogsFlow()
            }
        }
    }

    override fun getLogsFromTag(tagId: Long): Flow<List<LogNote>> {
        return tagDao.getAllLogsByTag(tagId).map {
            it.logsWithEntries.map { logWithEntries -> logWithEntries.toLog() }
        }
    }

    override fun getTagsWithCategory(categoryId: Long): Flow<CategoryWithTags> {//Flow<List<Pair<Category, List<Tag>>>> {
        return categoryDao.getTagsFromCategory(categoryId).map { notebookWithTags ->
            CategoryWithTags(
                category = notebookWithTags.category.toCategory(),
                tags = notebookWithTags.tags.map { it.toTag() }
            )
        }
    }

    override fun getTagsWithLog(logId: Long): Flow<LogWithTags> {//Flow<List<Pair<Log, List<Tag>>>> {
        return logDao.getTagsFromLogs(logId).map { noteWithTags ->
            LogWithTags(
                log = noteWithTags.logWithEntries.toLog(),
                tags = noteWithTags.tags.map { it.toTag() }
            )
        }
    }

    override suspend fun newMediaLog(log: LogNote, mediaRefUuid: String): Long = withContext(defaultDispatcher) {
        entryDao.insertEntry(
            entryEntity = EntryEntity(
	            logId = log.id,
	            timestamp = Clock.System.now(),
	            mediaReferenceId = mediaRefUuid,
	            text = null //TODO: non-null for captions
            ),
        )
    }
}

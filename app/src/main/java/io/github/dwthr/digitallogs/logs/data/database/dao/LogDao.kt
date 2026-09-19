package io.github.dwthr.digitallogs.logs.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import io.github.dwthr.digitallogs.logs.data.database.entities.EntryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogEntity
import io.github.dwthr.digitallogs.logs.data.database.relations.LogWithEntries
import io.github.dwthr.digitallogs.logs.data.database.relations.LogWithEntriesAndCategory
import io.github.dwthr.digitallogs.logs.data.database.relations.LogWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

	@Insert
	suspend fun insertLog(log: LogEntity): Long

	@Upsert
	suspend fun upsertEntries(entryEntities: List<EntryEntity>)

	@Update
	suspend fun updateLog(log: LogEntity): Int

	@Transaction
	suspend fun updateLogWithEntries(logWithEntries: LogWithEntries) {
		updateLog(logWithEntries.log)
		upsertEntries(logWithEntries.entryEntity)
	}

    @Delete
    suspend fun deleteLogs(logs: List<LogEntity>): Int

	@Transaction
	@Query("SELECT * FROM Logs WHERE id = :logId")
	fun getLogWithEntriesById(logId: Long): Flow<LogWithEntries?>

	@Transaction
	@Query("SELECT * FROM Logs WHERE id = :logId")
	fun getLogWithCategory(logId: Long): Flow<LogWithEntriesAndCategory>

	@Query("SELECT * FROM Logs WHERE id != :id")
	fun getLogsNotWithId(id: Long): Flow<List<LogEntity>>

	@Query("SELECT * FROM Logs WHERE categoryId != :categoryId")
	fun getLogsNotFromCategory(categoryId: Long): Flow<List<LogEntity>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId")
	fun getLogsFromCategory(categoryId: Long): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId ORDER BY dateCreated ASC")
	fun getLogsFromCategoryByDateCreatedAsc(categoryId: Long): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId ORDER BY dateCreated DESC")
	fun getLogsFromCategoryByDateCreatedDesc(categoryId: Long): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId ORDER BY title ASC")
	fun getLogsFromCategoryByTitleAsc(categoryId: Long): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId ORDER BY title DESC")
	fun getLogsFromCategoryByTitleDesc(categoryId: Long): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId ORDER BY lastModified ASC")
	fun getLogsFromCategoryByLastModifiedAsc(categoryId: Long): Flow<List<LogWithEntries>>
	@Transaction
	@Query("SELECT * FROM Logs WHERE categoryId = :categoryId ORDER BY lastModified DESC")
	fun getLogsFromCategoryByLastModifiedDesc(categoryId: Long): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs ORDER BY dateCreated ASC")
	fun getAllLogsByDateCreatedAsc(): Flow<List<LogWithEntries>>
	@Transaction
	@Query("SELECT * FROM Logs ORDER BY dateCreated DESC")
	fun getAllLogsByDateCreatedDesc(): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs ORDER BY title ASC")
	fun getAllLogsByTitleAsc(): Flow<List<LogWithEntries>>
	@Transaction
	@Query("SELECT * FROM Logs ORDER BY title DESC")
	fun getAllLogsByTitleDesc(): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs ORDER BY lastModified ASC")
	fun getAllLogsByLastModifiedAsc(): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs ORDER BY lastModified DESC")
	fun getAllLogsByLastModifiedDesc(): Flow<List<LogWithEntries>>

//	//TODO: Matchinfo() Offset(return text location)
//	@Query("""SELECT * FROM Notes
//		JOIN Note_FTS ON Notes.id == Note_FTS.rowid
//		WHERE Note_FTS.searchTextData MATCH :searchQuery LIMIT :limit""") //TODO: The LIKE operator uses the modulus symbol (%) to match search-queries in the middle of a phrase. FTS on the other hand uses asterisks (*) for this purpose. Update the SearchViewModel class to reflect this change. Navigate to SearchViewModel.kt inside the search package, and change the search method to use asterisks:
//	fun searchNotesByText(searchQuery: String, limit: Int): Flow<List<NoteEntity>>

	@Transaction
	@RawQuery(observedEntities = [LogEntity::class])
	fun searchLogs(searchQuery: RoomRawQuery): Flow<List<LogWithEntries>>

	@Transaction
	@Query("SELECT * FROM Logs ORDER BY lastModified DESC LIMIT :limit")
	fun getMostRecentLogs(limit: Int): Flow<List<LogWithEntriesAndCategory>>

	@Transaction
	@Query("SELECT * FROM Logs WHERE id = :logId")
	fun getTagsFromLogs(logId: Long): Flow<LogWithTags>
}
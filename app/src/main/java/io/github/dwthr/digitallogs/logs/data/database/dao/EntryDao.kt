package io.github.dwthr.digitallogs.logs.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Update
import io.github.dwthr.digitallogs.logs.data.database.entities.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface EntryDao {

    @Insert
    suspend fun insertEntry(entryEntity: EntryEntity): Long

    @Update
    suspend fun updateEntry(entryEntity: EntryEntity)

    @Query("DELETE FROM `Log Entries` WHERE logId = :logId AND timestamp = :timestamp")
    suspend fun deleteEntry(logId: Long, vararg timestamp: Instant): Int

    @Query("SELECT * FROM `Log Entries` WHERE timestamp = :instant")
    fun getEntryByExactInstant(instant: Instant): Flow<EntryEntity?>

    @Query("SELECT * FROM `Log Entries` WHERE timestamp BETWEEN :start AND :end")
    fun getEntryByInstantTimespan(start: Instant, end: Instant): Flow<EntryEntity?>

    @Query("SELECT * FROM `log entries` WHERE mediaReferenceId IN (:mediaRefIds)")
    suspend fun getEntriesByMediaRefIds(mediaRefIds: List<String>): List<EntryEntity>

    @RawQuery(observedEntities = [EntryEntity::class])
    fun searchEntries(searchQuery: RoomRawQuery): Flow<List<EntryEntity>>
}
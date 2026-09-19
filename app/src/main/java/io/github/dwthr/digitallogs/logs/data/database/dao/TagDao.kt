package io.github.dwthr.digitallogs.logs.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import androidx.room.Update
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.LogTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.TagEntity
import io.github.dwthr.digitallogs.logs.data.database.relations.TagWithCategories
import io.github.dwthr.digitallogs.logs.data.database.relations.TagWithLogs
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tagEntity: TagEntity): Long

    @Update
    suspend fun updateTag(tagEntity: TagEntity)

    @Query("SELECT * FROM Tags WHERE id = :tagId")
    fun getTagById(tagId: Long): Flow<TagEntity?>

    @Insert
    suspend fun addTagsToCategory(vararg categoryTag: CategoryTagCrossRef)

    @Insert
    suspend fun addTagsToLog(vararg logTag: LogTagCrossRef)

    @Delete
    suspend fun deleteTags(tagEntity: List<TagEntity>): Int

    @Delete
    suspend fun removeTagFromCategory(vararg notebookTag: CategoryTagCrossRef)

    @Delete
    suspend fun removeTagFromLog(vararg logTag: LogTagCrossRef)

    @Transaction
    @Query("SELECT * FROM Tags ORDER BY label ASC")
    fun getTagsByLabelAsc(): Flow<List<TagEntity>>
    @Transaction
    @Query("SELECT * FROM Tags ORDER BY label DESC")
    fun getTagsByLabelDesc(): Flow<List<TagEntity>>

    @Transaction
    @RawQuery(observedEntities = [TagEntity::class])
    fun searchTags(searchQuery: RoomRawQuery): Flow<List<TagEntity>>

    @Transaction
    @Query("SELECT * FROM Tags WHERE id = :tagId")
    fun getAllCategoriesByTag(tagId: Long): Flow<TagWithCategories>

    @Transaction
    @Query("SELECT * FROM Tags WHERE id = :tagId")
    fun getLogsWithoutTag(tagId: Long): Flow<TagWithLogs>

    @Transaction
    @Query("SELECT * FROM Tags WHERE id = :tagId")
    fun getAllLogsByTag(tagId: Long): Flow<TagWithLogs>
}
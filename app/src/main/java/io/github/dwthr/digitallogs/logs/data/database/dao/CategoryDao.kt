package io.github.dwthr.digitallogs.logs.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import androidx.room.Update
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryEntity
import io.github.dwthr.digitallogs.logs.data.database.relations.CategoryWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    @Delete
    suspend fun deleteCategories(categories: List<CategoryEntity>): Int

    @Query("DELETE FROM Categories WHERE id = (:categoryIds)")
    suspend fun deleteCategoriesWithIds(categoryIds: List<Long>)

    @Transaction
    @Query("SELECT * FROM Categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>

    @Transaction
    @Query("SELECT * FROM Categories ORDER BY title ASC")
    fun getCategoriesByTitleAsc(): Flow<List<CategoryEntity>>
    @Transaction
    @Query("SELECT * FROM Categories ORDER BY title DESC")
    fun getCategoriesByTitleDesc(): Flow<List<CategoryEntity>>

    @Transaction
    @Query("SELECT * FROM Categories WHERE id = :categoryId")
    fun getTagsFromCategory(categoryId: Long): Flow<CategoryWithTags>

    @RawQuery(observedEntities = [CategoryEntity::class])
    fun searchCategories(searchQuery: RoomRawQuery): Flow<List<CategoryEntity>>
}
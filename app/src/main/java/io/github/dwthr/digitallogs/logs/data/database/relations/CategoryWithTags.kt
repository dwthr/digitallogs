package io.github.dwthr.digitallogs.logs.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.TagEntity

data class CategoryWithTags(
	@Embedded val category: CategoryEntity,
	@Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
	        value = CategoryTagCrossRef::class,
	        parentColumn = "categoryId",
	        entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

package io.github.dwthr.digitallogs.logs.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogEntity

data class CategoryWithLogs(
	@Embedded val category: CategoryEntity,
	@Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val logs: List<LogEntity>
)

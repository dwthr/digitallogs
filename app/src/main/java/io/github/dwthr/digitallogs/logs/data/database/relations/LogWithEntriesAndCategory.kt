package io.github.dwthr.digitallogs.logs.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryEntity

data class LogWithEntriesAndCategory(
	@Embedded val logWithEntry: LogWithEntries,
	@Relation(
		parentColumn = "categoryId",
		entityColumn = "id"
	)
	val category: CategoryEntity
)

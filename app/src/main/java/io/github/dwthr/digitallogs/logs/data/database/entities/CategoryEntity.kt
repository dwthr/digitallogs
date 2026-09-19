package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
	tableName = "Categories",
	indices = [
		Index(value = ["title"], unique = true),
	]
)
data class CategoryEntity(
	val title: String,
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0 //TODO: Replace with title for FTS + unique title?
)
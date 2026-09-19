package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
	tableName = "Media_File_References"
)
data class MediaFileReferenceEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
)
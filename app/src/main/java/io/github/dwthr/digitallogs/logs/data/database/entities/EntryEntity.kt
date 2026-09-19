package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.time.Instant

@Entity(
	tableName = "Log Entries",
	primaryKeys = ["logId", "timestamp"],
	foreignKeys = [
		ForeignKey(
			entity = LogEntity::class,
			parentColumns = ["id"],
			childColumns = ["logId"],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = ["logId", "timestamp"]),
		Index(value = ["timestamp"]),
	]
)
data class EntryEntity(
	val logId: Long,
	val timestamp: Instant,
	val mediaReferenceId: String?,
	val text: String?,
)

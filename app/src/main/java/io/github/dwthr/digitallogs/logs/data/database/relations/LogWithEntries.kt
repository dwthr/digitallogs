package io.github.dwthr.digitallogs.logs.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import io.github.dwthr.digitallogs.logs.data.database.entities.EntryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogEntity

data class LogWithEntries(
	@Embedded val log: LogEntity,
	@Relation(
		entity = EntryEntity::class,
		parentColumn = "id",
		entityColumn = "logId"
	)
	val entryEntity: List<EntryEntity>
)

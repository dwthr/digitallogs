package io.github.dwthr.digitallogs.logs.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.dwthr.digitallogs.logs.data.database.entities.LogTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.TagEntity

data class LogWithTags(
	@Embedded val logWithEntries: LogWithEntries,
	@Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
	        value = LogTagCrossRef::class,
	        parentColumn = "logId",
	        entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

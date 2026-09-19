package io.github.dwthr.digitallogs.logs.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.dwthr.digitallogs.logs.data.database.entities.LogEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.TagEntity

data class TagWithLogs(
    @Embedded val tagEntity: TagEntity,
    @Relation(
	    entity = LogEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
	        value = LogTagCrossRef::class,
	        parentColumn = "tagId",
	        entityColumn = "logId"
        )
    )
    val logsWithEntries: List<LogWithEntries>
)

package io.github.dwthr.digitallogs.logs.data.mappers

import io.github.dwthr.digitallogs.logs.data.database.entities.TagEntity
import io.github.dwthr.digitallogs.logs.domain.NewTag
import io.github.dwthr.digitallogs.logs.domain.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun NewTag.toTagEntity(): TagEntity = TagEntity(
    label = label,
    id = 0
)

fun Tag.toTagEntity(): TagEntity = TagEntity(
    label = label,
    id = id
)

fun TagEntity.toTag(): Tag = Tag(
    label = label,
    id = id
)

fun Flow<List<TagEntity>>.toTagsFlow(): Flow<List<Tag>> = this.map {
    tagEntities -> tagEntities.map {
        tagEntity -> tagEntity.toTag()
    }
}
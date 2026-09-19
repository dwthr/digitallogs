package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Tags",
    indices = [
        Index(value = ["label"], unique = true)
    ]
)
data class TagEntity(
    val label: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

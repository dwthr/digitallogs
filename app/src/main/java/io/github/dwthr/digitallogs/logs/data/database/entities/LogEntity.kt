package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(
    tableName = "Logs",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [ //FIXME?: Too many indices?
        Index(value = ["categoryId", "title"], unique = true),
        Index(value = ["categoryId", "dateCreated"]),
        Index(value = ["categoryId", "lastModified"]),
        Index(value = ["title"]),
        Index(value = ["dateCreated"]),
        Index(value = ["lastModified"]),
    ]
)
data class LogEntity(
    val title: String,
    val dateCreated: Instant,
    var lastModified: Instant,
    val categoryId: Long,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
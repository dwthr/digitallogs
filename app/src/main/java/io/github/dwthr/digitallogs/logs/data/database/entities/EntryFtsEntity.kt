package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import kotlin.time.Instant

@Fts4(
	contentEntity = EntryEntity::class,
	tokenizer = FtsOptions.TOKENIZER_UNICODE61,
	tokenizerArgs = ["remove_diacritics=2"],
	prefix = [2, 3, 4]
)
@Entity(tableName = "Entry_FTS")
data class EntryFtsEntity(
	val logId: Long,
	val timestamp: Instant,
	val text: String,
)

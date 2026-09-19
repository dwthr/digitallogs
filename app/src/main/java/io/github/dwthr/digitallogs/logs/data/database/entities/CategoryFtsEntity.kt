package io.github.dwthr.digitallogs.logs.data.database.entities

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(
	contentEntity = CategoryEntity::class,
	tokenizer = FtsOptions.TOKENIZER_UNICODE61,
	tokenizerArgs = ["remove_diacritics=2"],
	prefix = [2, 3, 4]
)
@Entity(tableName = "Category_FTS")
data class CategoryFtsEntity(
	val title: String,
)
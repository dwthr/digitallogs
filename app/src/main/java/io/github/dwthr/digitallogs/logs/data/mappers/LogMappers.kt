package io.github.dwthr.digitallogs.logs.data.mappers

import android.util.Log
import io.github.dwthr.digitallogs.logs.data.database.entities.LogEntity
import io.github.dwthr.digitallogs.logs.data.database.relations.LogWithEntries
import io.github.dwthr.digitallogs.logs.data.database.relations.LogWithEntriesAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.NewLogNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

fun NewLogNote.toLogEntity(): LogEntity = LogEntity(
	title = title,
	dateCreated = Clock.System.now(),
	lastModified = Clock.System.now(),
	categoryId = categoryId,
	id = 0,
)

fun LogNote.toLogEntity(): LogEntity {
	return LogEntity(
		title = title,
		dateCreated = dateCreated,
		lastModified = lastModified,
		categoryId = categoryId,
		id = id,
	)
}

fun LogNote.toLogWithEntries(): LogWithEntries {
	return LogWithEntries(
		log = this.toLogEntity(),
		entryEntity = entries.map { it.toEntryEntity(this) }
	)
}

fun LogWithEntries.toLog(): LogNote = LogNote(
	title = this.log.title,
	dateCreated = this.log.dateCreated,
	lastModified = this.log.lastModified,
	categoryId = this.log.categoryId,
	id = this.log.id,
	entries = this.entryEntity.mapNotNull {
		try {
			it.toLogEntry()
		} catch (e: IllegalStateException) {
			Log.w(this::class.simpleName, "Ignoring blank entity from timestamp instant: ${it.timestamp}", e) //TODO: better handling
			null
		}
	},
)

fun Flow<List<LogWithEntries>>.toLogsFlow(): Flow<List<LogNote>> = this.map {
	noteEntities -> noteEntities.map {
		noteEntity -> noteEntity.toLog()
	}
}

fun Flow<List<LogWithEntriesAndCategory>>.toLogAndCategoryFlow(): Flow<List<LogAndCategory>> {
	return this.map { noteAndNotebookEntities ->
		noteAndNotebookEntities.map {
			LogAndCategory(
				log = it.logWithEntry.toLog(),
				parentCategory = it.category.toCategory()
			)
		}
	}
}
package io.github.dwthr.digitallogs.logs.data.mappers

import io.github.dwthr.digitallogs.logs.data.database.entities.EntryEntity
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.domain.LogEntry
import io.github.dwthr.digitallogs.logs.domain.LogNote

fun LogEntry.toEntryEntity(withLog: LogNote): EntryEntity {
	return EntryEntity(
		logId = withLog.id,
		timestamp = this.timestamp,
		mediaReferenceId = (this.data as? EntryData.Media)?.fileReferenceUuid,
		text = (this.data as? EntryData.Text)?.text
	)
}

fun EntryEntity.toLogEntry(): LogEntry {
	return LogEntry(
		timestamp = this.timestamp,
		data = if(this.mediaReferenceId != null) {
			EntryData.Media(this.mediaReferenceId)
		} else {
			EntryData.Text(this.text ?: throw IllegalStateException("Blank entity")) //TODO: Allow captioning media
		}
	)
}
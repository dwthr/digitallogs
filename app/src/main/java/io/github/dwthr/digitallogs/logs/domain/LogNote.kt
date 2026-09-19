package io.github.dwthr.digitallogs.logs.domain

import kotlin.time.Clock
import kotlin.time.Instant

sealed interface LogBase //TODO: Move all into data layer?

//data class UninitializedNote(
//    val id: Long
//): LogBase

data class LogNote(
    val id: Long,
    val categoryId: Long,
    val title: String, //TODO: @Annotation for length?
    val dateCreated: Instant,
    val lastModified: Instant,
    val entries: List<LogEntry>,
): LogBase {
    fun editTitle(newTitle: String): LogNote {
        return this.copy(title = newTitle, lastModified = Clock.System.now())
    }

    fun transformData(
        transform: (List<LogEntry>) -> List<LogEntry>
    ): LogNote {
        return this.copy(
            lastModified = Clock.System.now(),
            entries = transform(this.entries)
        )
    }

    /**
     * Adds a log at the time specified in [atTimestamp] and returns the log's
     * [lastModified] value to [Clock.System.now].
     *
     * @param atTimestamp Specifies what time the log is saved as. This is not meant to be changed unless
     * the log is meant to specify a delayed action. For example, an audio log would represent
     * whenever the user initially pressed the button and not when the recording is stopped. This
     * parameter does not affect what the log's last modified value is set to.
     */
    fun addTimestampedLog(
	    newLog: EntryData,
	    atTimestamp: Instant = Clock.System.now()
    ): LogNote {
        return this.copy(
            lastModified = Clock.System.now(),
            entries = this.entries + LogEntry(atTimestamp, newLog)
        )
    }
}

data class NewLogNote(
    val categoryId: Long,
    val title: String
): LogBase
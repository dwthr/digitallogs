package io.github.dwthr.digitallogs.audio.presentation

import io.github.dwthr.digitallogs.audio.data.AudioException

fun AudioException.getExceptionDisplayText(): String {
	return when(this) {
		is AudioException.Record.CacheFileRecordFailure -> "Unable to create cache file"
		is AudioException.Record.MissingCacheFile -> "Cache file missing, unable to save"
		is AudioException.Record.NonNullRecorderStopBeforeStart -> "Called stop on record before started"
		is AudioException.Record.RecorderAlreadyInUse -> "Record started when recorder is already in use"
		is AudioException.Record.ReferenceFileSaveFailure -> "Unable to save recording"
	}
}
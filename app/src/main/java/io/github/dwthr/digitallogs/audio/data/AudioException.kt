package io.github.dwthr.digitallogs.audio.data

sealed interface AudioException {
	sealed interface Generic: AudioException, Playback, Record {
	}

	sealed interface Record: AudioException {
		class CacheFileRecordFailure: Exception(), Record
		class RecorderAlreadyInUse: Exception(), Record
		class NonNullRecorderStopBeforeStart: Exception(), Record
		class MissingCacheFile: Exception(), Record
		class ReferenceFileSaveFailure: Exception(), Record
	}

	sealed interface Playback: AudioException {
	}
}

class RecordInvalidData: RuntimeException()
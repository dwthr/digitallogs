package io.github.dwthr.digitallogs.logs.data.database

import androidx.room.TypeConverter

class Converters {

    //TODO: see https://www.creativedeletion.com/2015/03/19/persisting_future_datetimes.html
    @TypeConverter
    fun instantToTimestamp(instant: kotlin.time.Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
    @TypeConverter
    fun timestampToInstant(timestamp: Long?): kotlin.time.Instant? {
        return timestamp?.let { kotlin.time.Instant.fromEpochMilliseconds(it) }
    }
}
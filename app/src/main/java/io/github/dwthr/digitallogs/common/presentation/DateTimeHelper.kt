package io.github.dwthr.digitallogs.common.presentation

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.toJavaInstant

object DateTimeHelper { //TODO: Try to fully convert to kotlin.time
	/**
	 * Returns [java.time.ZonedDateTime] using system default zone
	 */
	fun epochMillisToZonedDateTime(millis: Long): ZonedDateTime {
		return ZonedDateTime.ofInstant(
			Instant.ofEpochMilli(millis),
			ZoneId.systemDefault()
		)
	}
	fun instantToLongDisplayString(instant: kotlin.time.Instant): String {
		return instant.toJavaInstant().atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))
	}

	fun instantToShortDisplayString(instant: kotlin.time.Instant): String {
		return instant.toJavaInstant().atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
	}

	fun instantToHoursMinutes(instant: kotlin.time.Instant): String {
		return instant.toJavaInstant().atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofPattern("H:mm"))
	}

	fun instantToMinutesSeconds(instant: kotlin.time.Instant): String {
		return instant.toJavaInstant().atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofPattern("m:ss"))
	}

	fun ZonedDateTime.toDisplayString(): String {
		return this.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))//.withLocale(Locale.getDefault()) //TODO: Chronology and decimal style?
	}
//TODO: See: https://www.creativedeletion.com/2015/03/19/persisting_future_datetimes.html
}
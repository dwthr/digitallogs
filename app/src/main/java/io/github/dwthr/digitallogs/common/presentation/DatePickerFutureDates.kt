package io.github.dwthr.digitallogs.common.presentation

import androidx.compose.material3.SelectableDates
import kotlin.time.Clock

object DatePickerFutureDates: SelectableDates {
	override fun isSelectableDate(utcTimeMillis: Long): Boolean {
		val now = Clock.System.now().toEpochMilliseconds()
		return utcTimeMillis > now
	}
}
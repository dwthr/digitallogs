package io.github.dwthr.digitallogs.logs.domain

import io.github.dwthr.digitallogs.common.presentation.DateTimeHelper

object LogEntryShareHandler {
	fun Collection<LogEntry>.getShareableText(): String = this.joinToString("\n") {
		"${DateTimeHelper.instantToShortDisplayString(it.timestamp)} - ${
			when(it.data) {
				is EntryData.Media -> "[media]" //TODO: media caption
				is EntryData.Text -> it.data.text
			}
		}"
	}
}
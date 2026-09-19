package io.github.dwthr.digitallogs.logs.domain

data class LogWithTags(
	val log: LogNote,
	val tags: List<Tag>
)

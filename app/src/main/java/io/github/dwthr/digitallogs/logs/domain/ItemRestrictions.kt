package io.github.dwthr.digitallogs.logs.domain

enum class TitleRestrictions(val maxChars: Int) {
	LOG(48),
	CATEGORY(32),
	TAG(24)
}
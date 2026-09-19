package io.github.dwthr.digitallogs.logs.domain

sealed interface TagBase //TODO: Move all into data layer?

data class NewTag(
    val label: String
): TagBase

data class Tag( //TODO: add color
    val label: String,
    val id: Long
): TagBase
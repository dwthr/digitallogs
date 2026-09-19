package io.github.dwthr.digitallogs.logs.domain

import androidx.annotation.experimental.Experimental

sealed interface CategoryBase //TODO: Move all into data layer?

data class Category(
    val title: String,
    val id: Long,
): CategoryBase {
    fun editTitle(newTitle: String): Category {
        return this.copy(title = newTitle)
    }

    fun generateNewLogTitle(): String { //TODO: Handle title comparison via suspend repo function - Ex: (list<String> -> String)?
        return "" //TODO: Implement or remove
    }
}

data class NewCategory(
    val title: String,
//    val entryPrefix: List<TextTemplate>
): CategoryBase
package io.github.dwthr.digitallogs.logs.data.mappers

import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryEntity
import io.github.dwthr.digitallogs.logs.domain.NewCategory
import io.github.dwthr.digitallogs.logs.domain.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun NewCategory.toCategoryEntity(): CategoryEntity{
	return CategoryEntity(
		title = title,
		id = 0
	)
}
fun Category.toCategoryEntity(): CategoryEntity{
    return CategoryEntity(
	    title = title,
	    id = id
    )
}
fun CategoryEntity.toCategory(): Category {
	return Category(
		title = title,
		id = id,
	)
}
fun Flow<List<CategoryEntity>>.toCategoriesFlow(): Flow<List<Category>> {
	return this.map {
		notebookEntities -> notebookEntities.map {
			notebookEntity -> notebookEntity.toCategory()
		}
	}
}

//fun Category.toNotebookEntity(): OldNotebookEntity{
//    return OldNotebookEntity(
//        title = title,
//        defaultNotebookType = defaultNotebookType,
//        autoNamingFormat = autoNamingFormat,
//        authentication = authentication,
//        notebookId = notebookId
//    )
//}
//fun OldNotebookEntity.toNotebook(): Category {
//    return Category(
//        title = title,
//        defaultNotebookType = defaultNotebookType,
//        autoNamingFormat = autoNamingFormat,
//        authentication = authentication,
//        notebookId = notebookId
//    )
//}
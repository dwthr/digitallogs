package io.github.dwthr.digitallogs.logs.data.database.dao

//class FakeCategoryDao: CategoryDao {
//	private val categories = MutableStateFlow(emptyList<Category>())
//
//	override suspend fun insertCategory(category: CategoryEntity): Long {
//		categories.update { it + category.toCategory() }
//		return categories.value.lastIndex.toLong()
//	}
//
//	override suspend fun updateCategory(category: CategoryEntity): Int {
//		val index = category.id.toInt()
//		if (categories.value.getOrNull(index) == null) return 0
//
//		categories.update {
//			val list = it.toMutableList()
//			list[index] = category.toCategory()
//			list.toList()
//		}
//		return index
//	}
//
//	override suspend fun deleteCategories(categories: List<CategoryEntity>): Int {
//		TODO("Not yet implemented")
//	}
//
//	override suspend fun deleteCategoriesWithIds(categoryIds: List<Long>) {
//		TODO("Not yet implemented")
//	}
//
//	override fun getCategoryById(categoryId: Long): Flow<CategoryEntity?> {
//		TODO("Not yet implemented")
//	}
//
//	override fun getCategoriesByTitleAsc(): Flow<List<CategoryEntity>> {
//		TODO("Not yet implemented")
//	}
//
//	override fun getCategoriesByTitleDesc(): Flow<List<CategoryEntity>> {
//		TODO("Not yet implemented")
//	}
//
//	override fun getTagsFromCategory(categoryId: Long): Flow<CategoryWithTags> {
//		TODO("Not yet implemented")
//	}
//
//	override fun searchCategories(searchQuery: RoomRawQuery): Flow<List<CategoryEntity>> {
//		TODO("Not yet implemented")
//	}
//
//}
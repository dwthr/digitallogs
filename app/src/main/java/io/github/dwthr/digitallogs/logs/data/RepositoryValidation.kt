package io.github.dwthr.digitallogs.logs.data

import android.util.Log
import io.github.dwthr.digitallogs.logs.data.database.RawQueries
import io.github.dwthr.digitallogs.logs.data.database.dao.CategoryDao
import io.github.dwthr.digitallogs.logs.data.database.dao.LogDao
import io.github.dwthr.digitallogs.logs.data.database.dao.TagDao
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToCategoryError
import io.github.dwthr.digitallogs.logs.domain.error.AddTagToLogError
import io.github.dwthr.digitallogs.logs.domain.error.AppResult
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import kotlinx.coroutines.flow.first

internal object CategoryValidator {
	fun validateTitle(title: String): EntityError.Title? {
		if (title.isBlank()) {
			return EntityError.Title.IS_BLANK
		}
		if (title.length > TitleRestrictions.CATEGORY.maxChars) {
			return EntityError.Title.TOO_LONG
		}
		return null
	}

	suspend fun checkCategoryDuplicateTitle(
		categoryDao: CategoryDao,
		title: String
	): EntityError.Title? {
		if (categoryDao.searchCategories(RawQueries.searchCategoriesByTitle(
				RawQueries.UserInputSQLString(title)
			)).first().isNotEmpty()
		) {
			Log.w(this::class.java.simpleName, "Unique constraint violation: Duplicate title")
			return EntityError.Title.CONFLICT
		}
		return null
	}
}

internal object LogValidator {
	fun validateTitle(title: String): EntityError.Title? {
		if (title.isBlank()) {
			return EntityError.Title.IS_BLANK
		}
		if (title.length > TitleRestrictions.LOG.maxChars) {
			return EntityError.Title.TOO_LONG
		}
		return null
	}

	suspend fun checkLogDuplicateTitle(
		logDao: LogDao,
		title: String
	): EntityError.Title? {
		if (logDao.searchLogs(RawQueries.searchLogsByTitle(
				RawQueries.UserInputSQLString(title)
			)).first().isNotEmpty()
		) {
			Log.w(this::class.java.simpleName, "Unique constraint violation: Duplicate title")
			return EntityError.Title.CONFLICT
		}
		return null
	}
}

internal object TagValidator {
	fun validateTitle(title: String): EntityError.Title? {
		if (title.isBlank()) {
			return EntityError.Title.IS_BLANK
		}
		if (title.length > TitleRestrictions.TAG.maxChars) {
			return EntityError.Title.TOO_LONG
		}
		return null
	}

	suspend fun checkTagDuplicateLabel(
		tagDao: TagDao,
		label: String
	): EntityError.Title? {
		if (tagDao.searchTags(RawQueries.searchTagsByLabel(
				RawQueries.UserInputSQLString(label)
			)).first().isNotEmpty()
		) {
			Log.w(this::class.java.simpleName, "Unique constraint violation: Duplicate label")
			return EntityError.Title.CONFLICT
		}
		return null
	}
}

internal object AddTagValidator {
	suspend fun checkAddTagToCategory(
		tagDao: TagDao,
		tag: Tag,
		categoryDao: CategoryDao,
		category: Category
	): AppResult.Error<Unit, AddTagToCategoryError>? {
		if (tagDao.getTagById(tag.id).first() != null) {
			Log.w(this::class.java.simpleName, "Unique constraint violation: Duplicate tag id")
			return AppResult.Error(AddTagToCategoryError.Tag(EntityError.Title.CONFLICT))
		}
		if (categoryDao.getCategoryById(category.id).first() != null) {
			Log.e(this::class.java.simpleName, "Unique constraint violation: Duplicate category id")
			return AppResult.Error(AddTagToCategoryError.Category(EntityError.Title.CONFLICT))
		}
		return null
	}
	suspend fun checkAddTagToLog(
		tagDao: TagDao,
		tag: Tag,
		logDao: LogDao,
		log: LogNote
	): AppResult.Error<Unit, AddTagToLogError>? {
		if (tagDao.getTagById(tag.id).first() != null) {
			Log.w(this::class.java.simpleName, "Unique constraint violation: Duplicate tag id")
			return AppResult.Error(AddTagToLogError.Tag(EntityError.Title.CONFLICT))
		}
		if (logDao.getLogWithEntriesById(log.id).first() != null) {
			Log.e(this::class.java.simpleName, "Unique constraint violation: Duplicate log id")
			return AppResult.Error(AddTagToLogError.Log(EntityError.Title.CONFLICT))
		}
		return null
	}
}
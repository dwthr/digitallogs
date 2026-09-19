package io.github.dwthr.digitallogs.logs.data.database

import androidx.room.RoomRawQuery
import io.github.dwthr.digitallogs.logs.domain.CategoryItem
import io.github.dwthr.digitallogs.logs.domain.LogItem
import io.github.dwthr.digitallogs.logs.domain.LogText
import io.github.dwthr.digitallogs.logs.domain.LogicalQuery
import io.github.dwthr.digitallogs.logs.domain.QueryAnd
import io.github.dwthr.digitallogs.logs.domain.QueryableObject
import io.github.dwthr.digitallogs.logs.domain.SearchQuery
import io.github.dwthr.digitallogs.logs.domain.TagItem

object RawQueries {
	@JvmInline
	value class UserInputSQLString(private val string: String){
		override fun toString(): String {
			return string.querySanitizeInput()
		}
	}

	fun String.querySanitizeInput(): String {
		return "\"" + this.replace(Regex.fromLiteral(literal = "\""), replacement = "\"\"") + "*\""

	}

	fun buildSearchQuery(searchQuery: SearchQuery): RoomRawQuery { //TODO: add logEntry support
		if (searchQuery.queries.isEmpty()) return RoomRawQuery("SELECT * FROM Logs WHERE 0")

		var queryString = ""

		var isSearchingLogs = false
		var isSearchingFTS = false
		var isSearchingTags = false
		var isSearchingCategories = false

		val stringBindList = mutableListOf<UserInputSQLString>()

		fun LogicalQuery.toSQLString(): String {
			fun QueryableObject.toSQLString(): String {
				return when(this) {
					is LogItem -> {
						isSearchingLogs = true
						"Logs.id ${if (this.isInverted) "!=" else "="} ${this.log.id}"
					}
					is LogText -> { //TODO: Performance benefit if concat all match queries?
						isSearchingFTS = true
						stringBindList.add(UserInputSQLString(this.data))
						stringBindList.add(UserInputSQLString(this.data))
						"Log_FTS.rowid ${if (this.isInverted) "NOT IN" else "IN"} (" +
								"SELECT Log_FTS.rowid\n" +
								"FROM Log_FTS\n" +
								"WHERE Log_FTS MATCH ?\n" +
								"UNION\n" +
								"SELECT Log_FTS.rowid\n" +
								"FROM Log_FTS\n" +
								"JOIN Entry_FTS ON Entry_FTS.logId = Log_FTS.rowid\n" +
								"WHERE Entry_FTS MATCH ?" +
								")"
					}
					is CategoryItem -> {
						isSearchingCategories = true
						"Logs.categoryId ${if (this.isInverted) "!=" else "="} ${this.category.id}"
					}
					is TagItem -> {
						isSearchingTags = true
						if (this.isInverted) {
							"""NOT EXISTS (
								SELECT * FROM LogTagCrossRef junct
								WHERE junct.logId = Logs.id 
								AND junct.tagId = ${this.tag.id})"""
						} else {
							"Tags.id = ${this.tag.id}"
						}
					}
				}
			}
			return when(this) {
				is QueryAnd -> {
					"\n(" + this.getValues().joinToString(
						separator = "\nAND "
					) {
						it.toSQLString()
					} + ")"
				}
				is LogItem -> this.toSQLString()
				is LogText -> this.toSQLString()
				is CategoryItem -> this.toSQLString()
				is TagItem -> this.toSQLString()
			}
		}

		val queryBody = searchQuery.queries.joinToString(
			separator = "\nOR "
		) {
			it.toSQLString()
		}

		queryString += "SELECT * FROM Logs" //TODO: Temporary, replace later

		if (isSearchingFTS) {
			queryString += "\nJOIN Log_FTS ON Logs.id = Log_FTS.rowId"
		}
		if (isSearchingTags) {
			queryString += "\nLEFT JOIN LogTagCrossRef ON LogTagCrossRef.logId = Logs.id" +
					"\nLEFT JOIN Tags ON LogTagCrossRef.tagId = Tags.id"
		}
		if (isSearchingTags || isSearchingFTS || isSearchingCategories || isSearchingLogs) {
			queryString += "\nWHERE\n"
		}
		queryString += queryBody

		return RoomRawQuery(
			sql = queryString,
			onBindStatement = {
				stringBindList.onEachIndexed { index, userInputString ->
					it.bindText(index + 1, userInputString.toString())
				}
			},
		)
	}

	fun searchLogsByTitle(title: UserInputSQLString): RoomRawQuery {
		return RoomRawQuery(
			sql = """SELECT * FROM Logs 
				JOIN Log_FTS ON Logs.id == Log_FTS.rowid
				WHERE Log_FTS.title MATCH ?""".trimIndent(),
			onBindStatement = {
				it.bindText(1, title.toString())
			},
		)
	}

	fun searchCategoriesByTitle(title: UserInputSQLString): RoomRawQuery {
		return RoomRawQuery(
			sql = """SELECT * FROM Categories 
				JOIN Category_FTS ON Categories.id == Category_FTS.rowid
				WHERE Category_FTS.title MATCH ?""",
			onBindStatement = {
				it.bindText(1, title.toString())
			},
		)
	}

	fun searchTagsByLabel(label: UserInputSQLString): RoomRawQuery {
		return RoomRawQuery(
			sql = """SELECT * FROM Tags 
				JOIN Tag_FTS ON Tags.id == Tag_FTS.rowid
				WHERE Tag_FTS.label MATCH ?""",
			onBindStatement = {
				it.bindText(1, label.toString())
			},
		)
	}
}
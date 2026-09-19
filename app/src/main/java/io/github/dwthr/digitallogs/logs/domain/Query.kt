package io.github.dwthr.digitallogs.logs.domain

sealed interface Query

/**
 * Represents [QueryableObject] items and logical specifiers.
 * @see [QueryAnd]
 * */
sealed interface LogicalQuery: Query
/**
 * Represents data that is stored and can be specified directly and searched. When [isInverted] is true,
 * this equivalent to NOT.
 * @see [CategoryItem]
 * @see [LogItem]
 * @see [LogText]
 * @see [TagItem]
 */
sealed interface QueryableObject: Query, LogicalQuery {
	val isInverted: Boolean
}

fun LogicalQuery.createAndQueryWith(otherQuery: LogicalQuery): QueryAnd {
	return when(this) {
		is QueryAnd -> this.addQuery(otherQuery)
		is QueryableObject -> {
			when(otherQuery) {
				is QueryAnd -> {
					otherQuery.addQuery(this)
				}
				is QueryableObject -> {
					QueryAnd(this, otherQuery)
				}
			}
		}
	}
}

data class QueryAnd(
	private val _firstValue: QueryableObject,
	private val _otherValue: QueryableObject,
	private val _additionalOtherValues: List<QueryableObject> = emptyList(),
): LogicalQuery {
	fun getValues(): List<QueryableObject> {
		return listOf(_firstValue, _otherValue) + _additionalOtherValues
	}
	fun getLast(): QueryableObject {
		return _additionalOtherValues.lastOrNull() ?: _otherValue
	}
	fun addQuery(query: LogicalQuery): QueryAnd {
		return when(query) {
			is QueryAnd -> {
				QueryAnd( //Combines other AndQuery; Does not allow AndQuery recursion
					_firstValue,
					_otherValue,
					_additionalOtherValues + query._firstValue + query._otherValue + query._additionalOtherValues
				)
			}
			is QueryableObject -> {
				QueryAnd(_firstValue, _otherValue, _additionalOtherValues + query)
			}
		}
	}
	fun removeLastQuery(): LogicalQuery {
		return if (_additionalOtherValues.isNotEmpty()) {
			this.copy(
				_firstValue = _firstValue,
				_otherValue = _otherValue,
				_additionalOtherValues = _additionalOtherValues.subList(0, _additionalOtherValues.size - 1)
			)
		} else {
			_firstValue
		}
	}
}

data class SearchQuery(
//	val baseQuery: BaseSearchQuery,
	val queries: List<LogicalQuery>
): Query {
	fun add(andJoinWithLast: Boolean, newQuery: LogicalQuery): SearchQuery {
		return this.copy(
			queries = queries.lastOrNull()?.let { lastQuery ->
				if (andJoinWithLast) { //Enforce last element is QueryAnd
					when (lastQuery) {
						is QueryableObject -> { //Replace last element with AND query
							val query = queries.toMutableList()

							query[queries.lastIndex] = lastQuery
								.createAndQueryWith(newQuery)

							query
						}
						is QueryAnd -> {
							listOf(lastQuery.addQuery(newQuery))
						}
					}
				} else {
					queries + newQuery
				}
			} ?: listOf(newQuery)
		)
	}

	fun removeLastQuery(): SearchQuery {
		return this.queries.lastOrNull()?.let { lastQuery ->
			when(lastQuery) {
				is QueryAnd -> this.copy(queries = queries.subList(0, queries.size - 1) + lastQuery.removeLastQuery())
				is QueryableObject -> this.copy(queries = queries.subList(0, queries.size - 1))
			}
		} ?: this
	}
}

//data class BaseSearchQuery(
//	val query: List<LogicalQueryable>
//): SearchQuery

sealed interface CategoryQuery: QueryableObject
data class CategoryItem(
	val category: Category,
	override val isInverted: Boolean = false
): CategoryQuery

sealed interface LogQuery: QueryableObject
data class LogItem(
	val log: LogNote,
	override val isInverted: Boolean = false
): LogQuery
data class LogText(
	val data: String,
	override val isInverted: Boolean = false
): LogQuery

sealed interface TagQuery: QueryableObject
data class TagItem(
	val tag: Tag,
	override val isInverted: Boolean = false
): TagQuery
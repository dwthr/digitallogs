package io.github.dwthr.digitallogs.logs.presentation.search

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.UiText
import io.github.dwthr.digitallogs.logs.domain.CategoryItem
import io.github.dwthr.digitallogs.logs.domain.LogItem
import io.github.dwthr.digitallogs.logs.domain.LogText
import io.github.dwthr.digitallogs.logs.domain.LogicalQuery
import io.github.dwthr.digitallogs.logs.domain.QueryAnd
import io.github.dwthr.digitallogs.logs.domain.QueryableObject
import io.github.dwthr.digitallogs.logs.domain.SearchQuery
import io.github.dwthr.digitallogs.logs.domain.TagItem
import io.github.dwthr.digitallogs.logs.presentation.search.QueryDataToUIConverter.toChipInputLabel
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputType.LogicalOperators
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputType.Metadata
import io.github.dwthr.digitallogs.logs.presentation.search.QueryInputType.Queryable

data class QueryInputState( //TODO: OR, XOR
	val searchFieldTFV: TextFieldValue = TextFieldValue(),
	val logicalOperator: LogicalOperators? = null,
	val inputIsInverted: Boolean = false,//QueryInputType.Metadata? = null,
	val queryItem: Queryable = Queryable.TEXT, //TODO: Should be null to indicate no user input
	val queryData: SearchQuery = SearchQuery(emptyList())
) {
	fun updateQueryDataAndClear(queryDataTransform: (SearchQuery) -> SearchQuery): QueryInputState {
		val previousQuery = queryData
		return this.clear().copy(queryData = queryDataTransform(previousQuery))
	}
	fun textFieldValueChanged(newTextFieldValue: TextFieldValue): QueryInputState {
		return this.copy(searchFieldTFV = newTextFieldValue)
	}
	fun queryInputTypeClicked(inputType: QueryInputType): QueryInputState {
		return when (inputType) {
			LogicalOperators.AND -> this.copy(logicalOperator = LogicalOperators.AND)
			Metadata.NOT -> this.copy(inputIsInverted = !inputIsInverted)
			is Queryable -> this.copy(queryItem = inputType)
		}
	}
	fun clear(): QueryInputState {
		return QueryInputState()
	}
	fun deleteLast(): QueryInputState {
		var result = this.copy(searchFieldTFV = TextFieldValue(""))

		if (searchFieldTFV.text.isNotEmpty()) return result
		result = result.copy(queryItem = Queryable.TEXT)

		if (queryItem != Queryable.TEXT) return result //TODO: This is ambiguous and doesn't reflect user input
		result = result.copy(inputIsInverted = false)

		if (inputIsInverted) return result
		result = result.copy(logicalOperator = null)

		if (logicalOperator != null) return result

		//Fetch previous queryData item (and logical query) then move it to input state
//		var newInputLogicalOperator: LogicalOperators? = null
		val lastQueryDataQueryable: QueryableObject? = this.queryData.queries.lastOrNull()?.let {
			when(it) {
				is QueryAnd -> {
					result = result.copy(logicalOperator = LogicalOperators.AND)
					it.getLast()
				}
				is QueryableObject -> it
			}
		}
		when(lastQueryDataQueryable) {
			is LogItem -> {
				result = result.copy(
					queryItem = Queryable.LOG,
					searchFieldTFV = TextFieldValue(
						text = lastQueryDataQueryable.log.title,
					)
				)
			}
			is LogText -> {
				result = result.copy(
					queryItem = Queryable.TEXT,
					searchFieldTFV = TextFieldValue(
						text = lastQueryDataQueryable.data,
					)
				)
			}
			is CategoryItem -> {
				result = result.copy(
					queryItem = Queryable.CATEGORY,
					searchFieldTFV = TextFieldValue(
						text = lastQueryDataQueryable.category.title,
					)
				)
			}
			is TagItem -> {
				result = result.copy(
					queryItem = Queryable.TAG,
					searchFieldTFV = TextFieldValue(
						text = lastQueryDataQueryable.tag.label,
					)
				)
			}
			null -> {
				return this.clear()
			}
		}
		return result.copy(
			searchFieldTFV = result.searchFieldTFV.copy(
				selection = TextRange(result.searchFieldTFV.text.length)
			),
			inputIsInverted = lastQueryDataQueryable.isInverted,
			queryData = queryData.removeLastQuery()
		)
	}
	fun toUIChipBuilderState(): List<UiText> {
		return listOfNotNull(
			logicalOperator?.toChipInputLabel(),
			if (inputIsInverted) Metadata.NOT.toChipInputLabel() else null,
			queryItem.toChipInputLabel()
		)
	}
	fun getQueryInputSuggestions(): List<QueryInputType> { //FIXME: QueryInput needs to assume ALWAYS INVALID. Have AND determine when next Searchable/NOT is injected in the last AND wrapper
//		else -> { //FIXME: For now, 2 consecutive [Searchable] are treated as 'OR' queries. Inputting an 'AND' continues the block TODO: Change?
//				QueryInputType.Searchable.entries + QueryInputType.Metadata.entries + QueryInputType.LogicalOperators.entries
//			}
		val queryable = Queryable.entries - Queryable.LOG - Queryable.TEXT
		val validStandaloneQuery = Metadata.entries + queryable
		val queryWithLogical = LogicalOperators.entries + validStandaloneQuery
		
		return if (inputIsInverted) {
			queryable
		} else {
			when(logicalOperator) { //TODO: Do not show "text"
				LogicalOperators.AND -> validStandaloneQuery
				null -> {
					//Prevents logical queries if there are no valid queries before it; cannot have emptyQueryData AND another query
					if (queryData.queries.lastOrNull() != null) queryWithLogical else
						validStandaloneQuery
				}
			}
		}
	}
}

object QueryDataToUIConverter {
	fun LogicalQuery.toQueryDataChipLabel(): List<UiText> { //TODO: Have extension function for unpacking and
		fun QueryableObject.asChipString(): List<UiText> {
			val result = mutableListOf<UiText>()
//			if (this.isInverted) result += "Not"
			result += if (this.isInverted) {
				when(this) {
					is LogItem -> UiText.StringResource(R.string.search_excluding_log, this.log.title)
					is LogText -> UiText.StringResource(R.string.search_without_text, this.data)
					is CategoryItem -> UiText.StringResource(
						R.string.search_not_from_category,
						this.category.title
					)
					is TagItem -> UiText.StringResource(R.string.search_without_tag, this.tag.label)
				}
			} else {
				when(this) {
					is LogItem -> UiText.StringResource(R.string.search_is_log, this.log.title) //TODO: Remove
					is LogText -> UiText.StringResource(R.string.search_with_text, this.data)
					is CategoryItem -> UiText.StringResource(
						R.string.search_from_category,
						this.category.title
					)
					is TagItem -> UiText.StringResource(R.string.search_with_tag, this.tag.label)
				}
			}

			return result.toList()
		}
		fun QueryAnd.unpackAnd(): List<UiText> {
			val result = mutableListOf<UiText>()
			this.getValues().onEachIndexed { index, queryable ->
				result += queryable.toQueryDataChipLabel()
				if (index < this.getValues().size - 1) {
					result += UiText.StringResource(R.string.search_and)
				}
			}
			return result.toList()
		}

		return when(this) {
			is QueryableObject -> this.asChipString()
			is QueryAnd -> this.unpackAnd()
		}
	}
	fun QueryInputType.toChipInputLabel(): UiText? {
		return when(this) {
			LogicalOperators.AND -> UiText.StringResource(R.string.search_and)
			Metadata.NOT -> UiText.StringResource(R.string.search_not)
			Queryable.LOG -> UiText.StringResource(R.string.search_log_title)
			Queryable.TEXT -> null//"Text:"
			Queryable.CATEGORY -> UiText.StringResource(R.string.search_category_title)
			Queryable.TAG -> UiText.StringResource(R.string.search_tag_label)
		}
	}
}

sealed interface QueryInputType {
	enum class Queryable: QueryInputType {
		LOG,
		TEXT,
		CATEGORY,
		TAG
	}
	enum class LogicalOperators: QueryInputType {
		AND
	}
	enum class Metadata: QueryInputType {
		NOT
	}
}

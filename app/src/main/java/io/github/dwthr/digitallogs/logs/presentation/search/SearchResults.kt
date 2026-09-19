package io.github.dwthr.digitallogs.logs.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.minus
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.logs.domain.LogEntryShareHandler.getShareableText
import io.github.dwthr.digitallogs.logs.domain.LogNote

@Composable
fun SearchResults(
	previousSearchQuery: String,
	logSearchResult: List<LogNote>,
	onLogResultClick: (LogNote) -> Unit,
	queryInputSuggestions: List<QueryInputType>,
	onQuerySuggestionClick: (QueryInputType) -> Unit,
	modifier: Modifier = Modifier,
	contentPadding: PaddingValues = PaddingValues(0.dp),
) {
	val hasPreviousSearchQuery by remember(previousSearchQuery) {
		derivedStateOf { previousSearchQuery.isEmpty() }
	}
	val logSearchResultIsEmpty by remember(logSearchResult) {
		derivedStateOf { logSearchResult.isEmpty() }
	}

	val startContentPadding = contentPadding.calculateStartPadding(LocalLayoutDirection.current)
	val endContentPadding = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
	val topContentPadding = contentPadding.calculateTopPadding()
	Column(
		modifier = modifier
	) {
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			contentPadding = PaddingValues(horizontal = 12.dp) + PaddingValues(
				start = startContentPadding,
				end = endContentPadding
			),
		) {
			items(queryInputSuggestions) {
				SuggestionChip( //TODO: Have chip dropdown
					onClick = { onQuerySuggestionClick(it) },
					label = { Text(it.toString()) }
				)
			}
		}
		Card(
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
			modifier = Modifier
				.fillMaxSize()
				.padding(top = 12.dp)
		) {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
				contentPadding = contentPadding - PaddingValues(
					top = topContentPadding,
				),
				modifier = Modifier
					.padding(top = 8.dp)
					.padding(horizontal = 12.dp)
			) {
				if (!hasPreviousSearchQuery) {
					item {
						Column(
							modifier = Modifier
								.padding(12.dp)
						) {
							Text(
								text = stringResource(R.string.search_results),
								style = MaterialTheme.typography.titleLargeEmphasized,
								modifier = Modifier
									.padding(vertical = 4.dp)
							)
							Spacer(Modifier.size(8.dp))
							if (logSearchResultIsEmpty) {
								Text(
									text = stringResource(R.string.search_no_results_found),
									style = MaterialTheme.typography.bodyMedium,
								)
							}
						}
					}
				}
				if (logSearchResult.isNotEmpty()) {
					itemsIndexed(logSearchResult) { index, log ->
						SegmentedListItem(
							onClick = { onLogResultClick(log) },
							content = {
								Text(log.title)
							},
							supportingContent = {
								Column {
									HorizontalDivider(Modifier.fillMaxWidth())
									Spacer(Modifier.size(8.dp))
									Text(
										text = log.entries.getShareableText(),
										maxLines = 3,
										overflow = TextOverflow.MiddleEllipsis
									)
								}
							},
							trailingContent = {

							},
							colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
							shapes = ListItemDefaults.segmentedShapes(index, logSearchResult.size),
						)
					}
				}
			}
		}
	}
}
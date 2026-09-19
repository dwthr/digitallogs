package io.github.dwthr.digitallogs.logs.presentation.loglist.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.window.core.layout.WindowSizeClass
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.ScenePaneLayout
import io.github.dwthr.digitallogs.common.presentation.composables.ExpressiveNavBackButton
import io.github.dwthr.digitallogs.logs.presentation.loglist.LogListEvent
import io.github.dwthr.digitallogs.logs.presentation.loglist.LogListLoadState
import io.github.dwthr.digitallogs.logs.presentation.loglist.LogListState
import kotlin.math.pow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LogListScaffold(
	loadState: LogListLoadState,
	state: LogListState,
	onEvent: (LogListEvent) -> Unit,
	scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
	loadingContent: @Composable (PaddingValues) -> Unit,
	content: @Composable (PaddingValues, Color) -> Unit
) {
	val editToolbarVisible by remember(state.selectedLogs.isEmpty()) { derivedStateOf { state.selectedLogs.isNotEmpty() } }
	val detailsButtonVisible by remember(state.selectedLogs.size) { derivedStateOf { state.selectedLogs.size == 1 } }

	val isSinglePane = ScenePaneLayout.isSinglePane()
	val backgroundColor = if (isSinglePane) MaterialTheme.colorScheme.surfaceDim else
		MaterialTheme.colorScheme.surfaceContainer

	Scaffold(
		floatingActionButtonPosition = if (editToolbarVisible || !isSinglePane) {
			FabPosition.Center
		} else {
			FabPosition.End
		},
		floatingActionButton = { //TODO: Make it work with toolbar
			if (editToolbarVisible) {
				HorizontalFloatingToolbar(
					//TODO: Check back for toolbar/documentation updates when 1.5.0 releases
					expanded = true,
					colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
					modifier = Modifier
				) {
					IconButton(
						onClick = { onEvent(LogListEvent.OnLogSelectionClear) },
					) {
						Icon(
							painter = painterResource(R.drawable.outline_deselect_24),
							contentDescription = stringResource(R.string.clear_selection),
						)
					}
//						IconButton( //TODO: Implement sharing
//							onClick = { },
//						) {
//							Icon(
//								painter = painterResource(R.drawable.baseline_share_24),
//								contentDescription = null,
//							)
//						}
					IconButton( //TODO: Implement home + log list screen toolbar so button order is predictable
						onClick = { onEvent(LogListEvent.OnSelectedLogsDelete) },
					) {
						Icon(
							painter = painterResource(R.drawable.outline_delete_24),
							contentDescription = stringResource(R.string.delete_selected),
						)
					}
					if (detailsButtonVisible) {
						IconButton(
							onClick = { onEvent(LogListEvent.OnToolbarDetailsClicked) },
						) {
							Icon(
								painter = painterResource(R.drawable.outline_info_24),
								contentDescription = stringResource(R.string.open_details),
							)
						}
					}
				}
			} else {
				val isWide = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
					WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND
				)
				val textContent = @Composable { Text(stringResource(R.string.create_log)) }
				val iconContent = @Composable {
					Icon(
						painter = painterResource(R.drawable.baseline_add_24),
						contentDescription = stringResource(R.string.create_log),
						modifier = Modifier
							.size(
								if (isWide) FloatingActionButtonDefaults.LargeIconSize
								else FloatingActionButtonDefaults.MediumIconSize
							)
					)
				}
				val onClick = {
					onEvent(LogListEvent.OnLogSelectionClear)
					onEvent(LogListEvent.OnLogAddClick)
				}
				val expanded = !isSinglePane

				if (isWide) {
					LargeExtendedFloatingActionButton(
						text = textContent,
						icon = iconContent,
						onClick = onClick,
						expanded = expanded,
					)
				} else {
					MediumExtendedFloatingActionButton(
						text = textContent,
						icon = iconContent,
						onClick = onClick,
						expanded = expanded
					)
				}
			}
		},
		containerColor = backgroundColor,
		topBar = {
			val collapsedFractionCutoff = 0.35f
			val topAppBarIsCollapsed by remember { derivedStateOf {
				scrollBehavior.state.collapsedFraction > collapsedFractionCutoff
			} }
			val alphaValue by remember(scrollBehavior.state.collapsedFraction) {
				derivedStateOf {
					1 - ( (scrollBehavior.state.collapsedFraction / collapsedFractionCutoff).pow(8) )
				}
			}
			MediumFlexibleTopAppBar(
				title = {
					Text(
						text = when(loadState) {
							is LogListLoadState.LoadedRecents -> stringResource(R.string.recent_logs)
							is LogListLoadState.LoadedWithCategory -> stringResource(R.string.viewing_logs)
							is LogListLoadState.LoadedAll -> stringResource(R.string.all_logs)
							LogListLoadState.Loading -> stringResource(R.string.loading)
						},
						style = MaterialTheme.typography.headlineMedium,
						maxLines = 2
					)
				},
				subtitle =
					if (loadState !is LogListLoadState.LoadedWithCategory || topAppBarIsCollapsed) {
						null
					} else {{
						Text(
							text = stringResource(
								R.string.logs_from_category_title,
								loadState.parentCategoryTitle
							),
							maxLines = 2,
							modifier = Modifier
								.alpha(alphaValue)
						)
					}},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
						scrolledContainerColor = backgroundColor
                    ),
				windowInsets = TopAppBarDefaults.windowInsets.run {
					if (isSinglePane) return@run this
					exclude(only(WindowInsetsSides.End))
				},
				navigationIcon = {
					ExpressiveNavBackButton { onEvent(LogListEvent.OnBackButtonClicked(
						isSinglePane = isSinglePane
					)) }
				},
				actions = {
					if (loadState is LogListLoadState.LoadedWithCategory) {
						IconButton(
							onClick = {
								onEvent(LogListEvent.OnSortButtonClicked)
							},
//                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
						) {
							Icon(
								painter = painterResource(R.drawable.baseline_sort_24),
								contentDescription = stringResource(R.string.sort_logs),
							)
						}
					}
				},
				scrollBehavior = scrollBehavior,
			)
		},
		modifier = Modifier
			.fillMaxSize()
			.nestedScroll(scrollBehavior.nestedScrollConnection),
		content = { innerPadding ->
			val adjustedPadding = ScenePaneLayout.innerPaddingListPane(innerPadding)

			if (loadState is LogListLoadState.Loaded) {
				content(adjustedPadding, backgroundColor)
			} else {
				loadingContent(adjustedPadding)
			}
		}
	)
}
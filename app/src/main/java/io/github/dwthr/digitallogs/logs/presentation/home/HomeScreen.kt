package io.github.dwthr.digitallogs.logs.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.AppDialogDefaults
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.common.presentation.composables.DisplaySection
import io.github.dwthr.digitallogs.common.presentation.composables.ItemSortSheet
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.domain.CategorySortType
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogEntry
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.presentation.home.composables.CategoriesSectionContent
import io.github.dwthr.digitallogs.logs.presentation.home.composables.ChipSearchInputField
import io.github.dwthr.digitallogs.logs.presentation.home.composables.RecentLogsPreviewCarousel
import io.github.dwthr.digitallogs.logs.presentation.search.SearchResults
import io.github.dwthr.digitallogs.logs.presentation.toDisplayString
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Composable
fun HomeScreenRoot( //TODO: Have extra information explaining category vs log
    prefsDialogCooldownEnabled: Boolean,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(MyApp.appModule.localRepository)
    },
    onClickCreateCategory: () -> Unit,
    onNavigateToCategory: (Category) -> Unit,
    onLogClick: (LogNote) -> Unit,
    onSettingsClick: () -> Unit,
    onRecentShowAllClick: () -> Unit,
    onCategoryDetailsClick: (Long) -> Unit,
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onEvent = { event ->
            when(event) {
                is HomeEvent.OnClickCreateCategory -> onClickCreateCategory()
                is HomeEvent.OnCategoryClick -> {
                    if (state.selectedCategories.isEmpty()) {
                        onNavigateToCategory(event.category)
                    }
                }
                is HomeEvent.OnLogClick -> onLogClick(event.log)
                is HomeEvent.OnRecentShowAllClick -> onRecentShowAllClick()
                is HomeEvent.OnToolbarDetailsClicked -> {
                    if (state.selectedCategories.size == 1) {
                        onCategoryDetailsClick(state.selectedCategories.single().id)
                    }
                }
                HomeEvent.OnClickSettings -> onSettingsClick()
                else -> Unit
            }
            viewModel.onEvent(event)
        },
        prefsDialogCooldownEnabled = prefsDialogCooldownEnabled
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
	ExperimentalFoundationApi::class
)
@Composable
private fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    prefsDialogCooldownEnabled: Boolean
){
    val scrollState = rememberScrollState()
    val dropdownExpanded by remember(state.autocompleteOptions) {
        derivedStateOf { state.autocompleteOptions.isNotEmpty() }
    }
    val editToolbarVisible by remember(state.selectedCategories) { derivedStateOf { state.selectedCategories.isNotEmpty() } }
    val isDetailsVisible by remember(state.selectedCategories) { derivedStateOf { state.selectedCategories.size == 1 } }
    val sheetState = rememberBottomSheetState(SheetValue.Hidden)

    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    Scaffold( //TODO: make settings / search tonal or filled?
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onEvent(HomeEvent.OnClickSettings)
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_settings_24),
                            contentDescription = stringResource(R.string.open_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                ),
                actions = {
                    if (state.categories.isEmpty()) return@TopAppBar
                    IconButton(
                        onClick = { scope.launch { searchBarState.animateToExpanded() } },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = stringResource(R.string.open_search),
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.home))
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (editToolbarVisible) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    modifier = Modifier
//                            .align(Alignment.BottomCenter)
                ) {
                    IconButton(
                        onClick = {
                            onEvent(HomeEvent.OnLogSelectionClear)
                        },
//                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_deselect_24),
                            contentDescription = stringResource(R.string.clear_selection),
                        )
                    }
                    IconButton(
                        onClick = {
                            onEvent(HomeEvent.OnSelectedCategoriesDelete)
                        },
//                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_delete_24),
                            contentDescription = stringResource(R.string.delete_selected),
                        )
                    }
                    if (isDetailsVisible) {
                        IconButton(
                            onClick = {
                                onEvent(HomeEvent.OnToolbarDetailsClicked)
                            },
//                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_info_24),
                                contentDescription = stringResource(R.string.open_details),
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        val queryChips = state.searchQueryChips.map { it.asString() }
        val searchFieldChips = state.searchFieldChips.map { it.asString() }
        val previousSearchQueryText = state.previousSearchQueryText.map {
            it.asString()
        }.joinToString(" ") { it }

        ExpandedFullScreenContainedSearchBar(
            //FIXME: weird navigation issue when clicking on note if search data is not cleared
            state = searchBarState,
            windowInsets = { SearchBarDefaults.fullScreenWindowInsets.only(WindowInsetsSides.Top) },
            inputField = {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { }
                ) {
                    ChipSearchInputField(
                        query = state.searchFieldTextValue,
                        onQueryChange = { onEvent(HomeEvent.OnSearchQueryTextChange(it)) },
                        expanded = searchBarState.currentValue == SearchBarValue.Expanded,
                        leadingContent = {
                            IconButton(
	                            onClick = {
                                    scope.launch { searchBarState.animateToCollapsed() }
                                },
	                            shapes = IconButtonDefaults.shapes(),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_24),
                                    contentDescription = stringResource(R.string.close_search)
                                )
                            }
                        },
                        queryChips = queryChips,
                        searchFieldChips = searchFieldChips,
                        searchFieldChipSelectedIndex = state.searchChipSelectedIndex,
                        placeholder = { Text(stringResource(R.string.search_logs)) },
                        keyboardActions = KeyboardActions(onSearch = { onEvent(HomeEvent.OnSearchEnter) }),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.Backspace -> {
                                            onEvent(HomeEvent.OnSearchBackspace)
                                            false
                                        }

                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        shape = MenuDefaults.shape,
                        onDismissRequest = { }
                    ) {
                        state.autocompleteOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = {
                                    Text(option)
                                },
                                onClick = {
                                    onEvent(HomeEvent.OnAutocompleteOptionClick(index))
                                },
                            )
                        }
                    }
                }
            }
        ) {
            SearchResults(
                onLogResultClick = {
                    scope.launch { searchBarState.animateToCollapsed() }
                    onEvent(HomeEvent.OnLogClick(it))
                },
                onQuerySuggestionClick = {
                    onEvent(HomeEvent.OnTypeSuggestionChipClick(it))
                },
                previousSearchQuery = previousSearchQueryText,
                logSearchResult = state.logSearchResult,
                queryInputSuggestions = state.nextTypeSuggestionChips,
                contentPadding = innerPadding
            )
        }

        val innerPaddingHorizontal = PaddingValues(
            start = innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
            end = innerPadding.calculateRightPadding(LocalLayoutDirection.current)
        )
        val headerPadding = 8.dp

        when(state.dialogTypeVisible) {
            CategoryListDialog.ConfirmDeleteSelected -> {
                Dialog(
                    isVisible = true,
                    onConfirm = {
                        onEvent(HomeEvent.OnDialogConfirm)
                    },
                    onDismiss = {
                        onEvent(HomeEvent.OnDialogDismiss)
                    },
                    confirmTimeoutMillis = AppDialogDefaults.DIALOG_DELETE_COOLDOWN.takeIf {
                        prefsDialogCooldownEnabled
                    },
                    supportingText = pluralStringResource(
                        R.plurals.deleteNumberOfCategories,
                        state.selectedCategories.size,
                        state.selectedCategories.size
                    )
                )
            }
            null -> Unit
        }

        when(state.sheetTypeVisible) {
            HomeSheetType.Sort -> {
                val sortOptions = remember { CategorySortType.entries.map { it.toDisplayString() } }
                ItemSortSheet(
                    sortOptions = sortOptions,
                    onOptionSelect = {
                        onEvent(HomeEvent.OnSortOptionClicked(it))
                        onEvent(HomeEvent.OnSheetDismiss)
                    },
                    selectedOptionIndex = state.selectedSortTypeIndex,
                    onDismiss = {
                        onEvent(HomeEvent.OnSheetDismiss)
                    },
                    sheetState = sheetState,
                    contentPadding = innerPaddingHorizontal
                )
            }
            null -> Unit
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                    )
                )
                .verticalScroll(scrollState)
        ) {
            if (state.recentLogs.isNotEmpty()) {
                RecentLogsPreviewCarousel(
	                recentLogs = state.recentLogs,
	                innerPaddingHorizontal = innerPaddingHorizontal,
	                onEvent = onEvent
                )
            }
            Spacer(modifier = Modifier.height(headerPadding))
            Card(
                colors = CardDefaults.elevatedCardColors(),
                modifier = Modifier
                    .fillMaxWidth()
//                        .weight(1f)
            ) {
                DisplaySection(
                    headerText = stringResource(R.string.categories_header),
                    headerPadding = innerPaddingHorizontal,
                    sectionContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    headerTrailingContent = {
                        Row {
                            if (state.categories.isEmpty()) {
                                FilledTonalButton(
                                    onClick = {
                                        onEvent(HomeEvent.OnClickCreateCategory)
                                    },
                                    shapes = ButtonDefaults.shapes(
                                        shape = IconButtonDefaults.smallSquareShape,
                                    ),
                                    colors = ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_add_24),
                                        contentDescription = stringResource(R.string.create_new_category),
                                        modifier = Modifier
                                            .size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.create_category_onboarding))
                                }
                                return@Row
                            }
                            FilledTonalIconButton(
                                onClick = { onEvent(HomeEvent.OnCategoriesSortButtonClicked) },
                                shapes = IconButtonDefaults.shapes(
                                    shape = IconButtonDefaults.smallSquareShape
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_sort_24),
                                    contentDescription = stringResource(R.string.sort_categories)
                                )
                            }
                            FilledIconButton(
                                onClick = {
                                    onEvent(HomeEvent.OnClickCreateCategory)
                                },
                                shapes = IconButtonDefaults.shapes(
                                    shape = IconButtonDefaults.smallSquareShape,
                                ),
                                colors = IconButtonDefaults.filledIconButtonColors(),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_add_24),
                                    contentDescription = stringResource(R.string.create_new_category),
                                    modifier = Modifier
                                        .size(IconButtonDefaults.smallIconSize)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    if (state.categories.isNotEmpty()) {
                        CategoriesSectionContent(
                            categories = state.categories,
                            selectedCategories = state.selectedCategories,
                            innerPaddingHorizontal = innerPaddingHorizontal,
                            screenVerticalPadding = innerPadding.calculateTopPadding()
                                    + innerPadding.calculateBottomPadding()
                                    + TopAppBarDefaults.TopAppBarExpandedHeight,
                            bottomPadding = innerPadding.calculateBottomPadding(),
                            onEvent = onEvent
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.onboarding_category_create_description),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewScreenHome(){

    val recentLogs = mutableListOf<LogNote>()
    repeat(3) {
        recentLogs +=
            LogNote(
                title = "Calculus CH. 2-$it : Derivatives&Integrals ${it + 1}",
                dateCreated = Clock.System.now(),
                categoryId = 0,
                lastModified = Clock.System.now(),
                id = 0,
                entries = listOf(
                    LogEntry(
	                    timestamp = Clock.System.now(),
	                    data = EntryData.Text("sample")
                    )
                )
            )
    }
    val categories = mutableListOf<Category>()
    repeat(4) {
        categories +=
            Category(
	            "New Category $it",
	            id = 0,
            )
    }

    HomeScreen(
	    state = HomeState(
		    categories = categories,
		    recentLogs = recentLogs.mapIndexed { index, logNote ->
			    LogAndCategory(
				    logNote,
				    categories[index % categories.lastIndex]
			    )
		    },
	    ),
	    onEvent = {},
	    prefsDialogCooldownEnabled = true,
    )
}
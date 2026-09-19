package io.github.dwthr.digitallogs.logs.presentation.loglist

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.AppDialogDefaults
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.common.presentation.composables.ItemSortSheet
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.LogSortType
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.presentation.loglist.LogListEvent.OnSheetDismiss
import io.github.dwthr.digitallogs.logs.presentation.loglist.LogListEvent.OnSortOptionClicked
import io.github.dwthr.digitallogs.logs.presentation.loglist.composables.LogListScaffold
import io.github.dwthr.digitallogs.logs.presentation.toDisplayString
import kotlin.time.Clock

@Composable
fun LogListScreenRoot(
    parentCategoryId: Long,
    prefsDialogCooldownEnabled: Boolean,
    viewModel: LogListViewModel = viewModel {
        LogListViewModel(
	        MyApp.appModule.localRepository,
            listViewType = ListViewType.FromCategory(parentCategoryId)
        )
    },
    onBack: (Boolean) -> Unit,
    onNavigateLog: (LogNote) -> Unit,
    onLogAddClick: (Long) -> Unit,
    onDetailsClick: (Long) -> Unit,
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    LogListScreen(
        state = state,
        onEvent = { event ->
            when(event){
                is LogListEvent.OnLogClick -> {
                    if(state.selectedLogs.isEmpty()) {
                        onNavigateLog(event.log)
                    }
                }
                is LogListEvent.OnLogAddClick -> onLogAddClick(parentCategoryId)
                is LogListEvent.OnBackButtonClicked -> {
                    onBack(event.isSinglePane)
                }
                is LogListEvent.OnToolbarDetailsClicked -> {
                    if (state.selectedLogs.size == 1) {
                        onDetailsClick(state.selectedLogs.single().id)
                    }
                }
                else -> Unit
            }
            viewModel.onEvent(event)
        },
        prefsDialogCooldownEnabled = prefsDialogCooldownEnabled
    )
}

@Composable
fun RecentLogsListScreenRoot(
    prefsDialogCooldownEnabled: Boolean,
    viewModel: LogListViewModel = viewModel {
        LogListViewModel(
            MyApp.appModule.localRepository,
            listViewType = ListViewType.FromAllRecent
        )
    },
    onBack: (Boolean) -> Unit,
    onNavigateLog : (LogNote) -> Unit,
    onDetailsClick: (Long) -> Unit
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    LogListScreen(
        state = state,
        onEvent = { event ->
            when(event){
                is LogListEvent.OnLogClick -> {
                    if(state.selectedLogs.isEmpty()) {
                        onNavigateLog(event.log)
                    }
                }
                is LogListEvent.OnLogAddClick -> {
                    Log.v("LogListScreen", "OnLogAddClick fired from invalid screen")
                }
                is LogListEvent.OnBackButtonClicked -> {
                    onBack(event.isSinglePane)
                }
                is LogListEvent.OnToolbarDetailsClicked -> {
                    if (state.selectedLogs.size == 1) {
                        onDetailsClick(state.selectedLogs.single().id)
                    }
                }
                else -> Unit
            }
            viewModel.onEvent(event)
        },
        prefsDialogCooldownEnabled = prefsDialogCooldownEnabled
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LogListScreen(
    state: LogListState,
    onEvent: (LogListEvent) -> Unit,
    prefsDialogCooldownEnabled: Boolean
){
    val sheetState = rememberBottomSheetState(SheetValue.Hidden)

    LogListScaffold(
        loadState = state.loadState,
        state = state,
        onEvent = onEvent,
        loadingContent = {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
            )
        }
    ) { innerPadding, backgroundColor ->
        if (state.loadState !is LogListLoadState.Loaded) return@LogListScaffold

        val innerPaddingHorizontal = PaddingValues(
            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
        )

        when(state.dialogTypeVisible) {
            LogListDialog.DeleteSelectedConfirmation -> {
                Dialog(
                    isVisible = true,
                    onConfirm = {
                        onEvent(LogListEvent.OnDialogConfirm)
                    },
                    onDismiss = {
                        onEvent(LogListEvent.OnDialogDismiss)
                    },
                    supportingText = pluralStringResource(
                        R.plurals.deleteNumberOfLogs,
                        state.selectedLogs.size,
                        state.selectedLogs.size
                    ),
                    confirmTimeoutMillis = AppDialogDefaults.DIALOG_DELETE_COOLDOWN.takeIf {
                        prefsDialogCooldownEnabled
                    },
                )
            }
            null -> Unit
        }

        when(state.sheetTypeVisible) {
            LogListSheetType.SortType -> {
                val sortTypeOptions = remember { LogSortType.entries.map { it.toDisplayString() } }
                ItemSortSheet(
	                sortOptions = sortTypeOptions,
	                onOptionSelect = {
		                onEvent(OnSortOptionClicked(it))
		                onEvent(OnSheetDismiss)
	                },
	                selectedOptionIndex = state.selectedSortTypeIndex,
	                onDismiss = {
		                onEvent(OnSheetDismiss)
	                },
	                sheetState = sheetState,
	                contentPadding = innerPaddingHorizontal,
                )
            }
//                LogListSheetType.TagSheet -> {
//                    ModalBottomSheet(
//                        onDismissRequest = {
//                            onEvent(OnSheetDismiss)
//                        },
//                        sheetState = sheetState
//                    ) {
//                        ItemTagListSheet( //TODO: Replace
//	                        tagsInput = state.tagSheetTags,
//	                        header = { Text("Tags:") },
//	                        onTagDeleteClicked = { },
//                        )
//                    }
//                }
            null -> Unit
        }

        val logListEmpty by remember(state.loadState.logs) {
            derivedStateOf { state.loadState.logs.isEmpty() }
        }

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (logListEmpty) {
                item { Text(text = stringResource(R.string.no_logs_to_display)) }
                return@LazyColumn
            }
            itemsIndexed(
                items = state.loadState.logs,
                key = { index, log -> log.title }
            ) { itemIndex, log ->
                val isSelected = log in state.selectedLogs
                ListItem(
                    selected = isSelected,
                    onClick = { onEvent(LogListEvent.OnLogClick(log)) },
                    content = { Text(log.title) },
                    overlineContent = { },
                    shapes = ListItemDefaults.shapes(),
                    colors = ListItemDefaults.colors(
                        containerColor = backgroundColor
                    ),
                    onLongClick = {
                        onEvent(LogListEvent.OnListItemSelectedChanged(itemIndex))
                    }, //TODO: Temporary?
                    onLongClickLabel = stringResource(R.string.toggle_selection_label),
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun PreviewAllLogListScreen(){

    val logs = mutableListOf<LogNote>()
    repeat(20) {
        logs +=
            LogNote(
	            title = "Log Preview ${it + 1}",
	            dateCreated = Clock.System.now(),
	            categoryId = 0,
	            lastModified = Clock.System.now(),
	            id = 0,
                entries = listOf()
            )
    }
    val tags = mutableListOf<Tag>()
    val names = mutableListOf("Diary", "Cooking", "Cool stuff", "Reading", "Shopping")
    repeat(names.size) {
        tags +=
            Tag(
	            label = names[it],
	            id = 0
            )
    }

    LogListScreen(
	    state = LogListState(
		    loadState = LogListLoadState.LoadedWithCategory(
			    logs = logs,
			    parentCategoryTitle = "NotebookTitle"
		    ),
	    ),
	    onEvent = {},
	    prefsDialogCooldownEnabled = true
    )
}
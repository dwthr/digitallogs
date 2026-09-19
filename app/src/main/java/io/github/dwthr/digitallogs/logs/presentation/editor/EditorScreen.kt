package io.github.dwthr.digitallogs.logs.presentation.editor

import android.content.ClipData
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.ScenePaneLayout
import io.github.dwthr.digitallogs.common.presentation.composables.AppDialogDefaults
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.common.presentation.composables.ExpressiveNavBackButton
import io.github.dwthr.digitallogs.common.presentation.composables.NotificationDialog
import io.github.dwthr.digitallogs.logs.domain.LogEntryShareHandler.getShareableText
import io.github.dwthr.digitallogs.logs.presentation.editor.composables.AudioRecordBottomSheet
import io.github.dwthr.digitallogs.logs.presentation.editor.composables.EditorEntryItem
import io.github.dwthr.digitallogs.logs.presentation.editor.composables.RecordIndicatorItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@Composable
internal fun LogEditorScreenRoot( //FIXME: The loading takes too long and is not seamless
	noteId: Long,
	prefsDialogCooldownEnabled: Boolean,
	viewModel: EditorViewModel = viewModel {
        EditorViewModel(
	        repository = MyApp.appModule.localRepository,
	        noteId = noteId,
	        audioHandler = MyApp.appModule.appAudioHandler,
	        audioPlayer = MyApp.appModule.appAudioHandler.providePlayer()
        )
    },
	onBack: () -> Unit,
	onDualPaneDeleted: () -> Unit,
	onShare: (String) -> Unit,
	onNavigatePermissions: () -> Unit,
	onRequestRecordMic: () -> Pair<Flow<Boolean>, Boolean>
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    LogNoteEditorScreen(
	    state = state,
	    onEvent = { event ->
		    when(event) {
			    is EditorEvent.OnBack -> onBack()
			    is EditorEvent.OnMicRecordToggle -> {
					if (event.isRecording) {
						val (resultFlow, shouldShowRequestPermissionRationale) = onRequestRecordMic()
						CoroutineScope(Dispatchers.Main).launch { //TODO: Move elsewhere
							resultFlow.collect { isGranted ->
								if (isGranted) {
									viewModel.onEvent(EditorEvent.OnMicRecordPermissionGranted)
								} else {
									if (shouldShowRequestPermissionRationale) {
										viewModel.onEvent(EditorEvent.OnMicRecordPermissionRejected)
									} else {
										viewModel.onEvent(EditorEvent.OnMicRecordPermissionDisabled)
									}
								}
							}
						}
					}
			    }
			    is EditorEvent.OnNavigatePermissions -> onNavigatePermissions()
			    is EditorEvent.OnShareSelectedEntries -> {
					(state.loadState as? LogEditorLoadState.Loaded)?.let { state ->
						onShare(state.selectedEntries.getShareableText())
					}
				}
			    else -> Unit
		    }
		    viewModel.onEvent(event)
	    },
	    onDualPaneDeleted,
	    prefsDialogCooldownEnabled = prefsDialogCooldownEnabled,
	    player = viewModel.audioPlayer
    )
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
	ExperimentalMediaQueryApi::class
)
@Composable
private fun LogNoteEditorScreen(
	state: LogEditorState,
	onEvent: (EditorEvent) -> Unit,
	onDualPaneDeleted: () -> Unit,
	prefsDialogCooldownEnabled: Boolean,
	player: Player,
) { //TODO: Allow editing of log entry
	val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val focusRequester = remember { FocusRequester() }
	val focusManager = LocalFocusManager.current

	val mutableInteractionSource by remember { mutableStateOf(MutableInteractionSource()) }
	val isLoaded = state.loadState is LogEditorLoadState.Loaded
	if (state.loadState is LogEditorLoadState.IsError) onDualPaneDeleted() //TODO: specify error / handle differently

	var toolbarExpandedState by rememberSaveable { mutableStateOf(true) }
	val isRecording = isLoaded && state.loadState.audioState is AudioState.MicRecording
	val localClipboard = LocalClipboard.current

	val isSinglePane = ScenePaneLayout.isSinglePane()
	val backgroundColor = if (isSinglePane) {
		MaterialTheme.colorScheme.surfaceDim
	} else {
		MaterialTheme.colorScheme.surfaceBright
	}


	Scaffold(
		topBar = {
			MediumFlexibleTopAppBar(
				navigationIcon = {
					if(isSinglePane) {
						ExpressiveNavBackButton { onEvent(EditorEvent.OnBack) }
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = backgroundColor,
					scrolledContainerColor = backgroundColor
				),
				windowInsets = TopAppBarDefaults.windowInsets.run {
					if (isSinglePane) return@run this
					exclude(only(WindowInsetsSides.Start))
				},
				title = {
					if (!isLoaded) return@MediumFlexibleTopAppBar
					Text(
						text = state.loadState.logTitle,
						style = MaterialTheme.typography.headlineMedium
					)
				},
				scrollBehavior = topAppBarScrollBehavior
			)
		},
		floatingActionButtonPosition = FabPosition.Center,
		floatingActionButton = {
			if (!isLoaded) return@Scaffold

			if (state.loadState.selectedEntries.isNotEmpty()) {
				HorizontalFloatingToolbar(
					expanded = true,
					colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
					modifier = Modifier
				) {
					IconButton(
						onClick = { onEvent(EditorEvent.OnEntrySelectionClear) },
					) {
						Icon(
							painter = painterResource(R.drawable.outline_deselect_24),
							contentDescription = stringResource(R.string.clear_selection),
						)
					}
					IconButton(
						onClick = {
							onEvent(EditorEvent.OnShareSelectedEntries)
						}
					) {
						Icon(
							painter = painterResource(R.drawable.baseline_share_24),
							contentDescription = stringResource(R.string.share),
						)
					}
					val coroutineScope = rememberCoroutineScope()
					IconButton( //TODO: better implementation? (with media support?) and correct date copying
						onClick = {
							coroutineScope.launch {
								localClipboard.setClipEntry(
									clipEntry = ClipEntry(
										ClipData.newPlainText(
											"timestamped entries",
											state.loadState.selectedEntries.getShareableText()
										)
									)
								)
							}
							onEvent(EditorEvent.OnEntrySelectionClear)
						}
					) {
						Icon(
							painter = painterResource(R.drawable.outline_content_copy_24),
							contentDescription = stringResource(R.string.copy_to_clipboard),
						)
					}
					IconButton(
						onClick = { onEvent(EditorEvent.OnSelectedEntriesDelete) },
					) {
						Icon(
							painter = painterResource(R.drawable.outline_delete_24),
							contentDescription = stringResource(R.string.delete_selected),
						)
					}
				}
			} else {
				HorizontalFloatingToolbar(
					expanded = toolbarExpandedState,
					colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
						toolbarContainerColor = if (!isSinglePane) MaterialTheme.colorScheme.surfaceDim else
							FloatingToolbarDefaults.standardFloatingToolbarColors().toolbarContainerColor
					),
					floatingActionButton = {
						FloatingToolbarDefaults.StandardFloatingActionButton(
							onClick = {
								focusManager.clearFocus()
								onEvent(EditorEvent.OnTextEntryAdd)
							}
						) {
							Icon(
								painter = painterResource(R.drawable.baseline_add_24),
								contentDescription = stringResource(R.string.add_entry),
							)
						}
					},
					modifier = Modifier
				) {
					//TODO: tooltip box?
					Row(
						modifier = Modifier
					) {
						OutlinedTextField(
							value = state.loadState.logEntryTextValue,
							onValueChange = {
								onEvent(EditorEvent.OnLogTextFieldChanged(it))
							},
							colors = OutlinedTextFieldDefaults.colors(
								focusedBorderColor = Color.Transparent,
								unfocusedBorderColor = Color.Transparent
							),
							placeholder = {
								Text("Enter log entry")
							},
							trailingIcon = { //TODO: mediaQuery { hasMicrophone}
								IconToggleButton(
									checked = isRecording,
									onCheckedChange = { onEvent(EditorEvent.OnMicRecordToggle(!isRecording)) },
									colors = IconButtonDefaults.iconToggleButtonColors(
										checkedContentColor = MaterialTheme.colorScheme.tertiary,
										checkedContainerColor = MaterialTheme.colorScheme.onTertiary
									),
								) {
									if (isRecording) {
										Icon(
											painter = painterResource(R.drawable.baseline_mic_24),
											contentDescription = stringResource(R.string.stop_recording),
										)
									} else {
										Icon(
											painter = painterResource(R.drawable.outline_mic_24),
											contentDescription = stringResource(R.string.start_recording)
										)
									}
								}
							},
							singleLine = true,
							modifier = Modifier
								.widthIn(max = TextFieldDefaults.MinWidth)
						)
					}
				}
			}
		},
		containerColor = backgroundColor,
		modifier = Modifier
			.fillMaxSize()
			.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
	) { innerPadding ->
		val adjustedInnerPadding = ScenePaneLayout.innerPaddingDetailPane(innerPadding)
		if (!isLoaded) {
			LinearProgressIndicator(
				modifier = Modifier
					.fillMaxWidth()
					.padding(adjustedInnerPadding)
			)
			return@Scaffold
		}

		when(state.visibleDialogType) {
			DialogType.MicPermissionRejected -> {
				NotificationDialog(
					isVisible = true,
					headline = stringResource(R.string.permission_required),
					supportingText = stringResource(R.string.mic_permission_dialog_text),
					onDismiss = { onEvent(EditorEvent.OnDialogConfirm) },
				)
			}
			DialogType.MicPermissionDisabled -> {
				NotificationDialog(
					isVisible = true,
					headline = stringResource(R.string.permission_required),
					supportingText = stringResource(R.string.mic_permission_dialog_text_settings),
					onDismiss = {
						onEvent(EditorEvent.OnDialogConfirm)
						onEvent(EditorEvent.OnNavigatePermissions)
					},
				)
			}
			null -> Unit

			DialogType.SelectDeleteConfirmation -> {
				Dialog(
					isVisible = true,
					onConfirm = {
						onEvent(EditorEvent.OnDialogConfirm)
					},
					onDismiss = {
						onEvent(EditorEvent.OnDialogDismiss)
					},
					supportingText = pluralStringResource(
						R.plurals.deleteNumberOfEntries,
						state.loadState.selectedEntries.size,
						state.loadState.selectedEntries.size
					),
					confirmTimeoutMillis = AppDialogDefaults.DIALOG_DELETE_COOLDOWN.takeIf {
						prefsDialogCooldownEnabled
					},
				)
			}
		}

		if (isRecording) {
			AudioRecordBottomSheet(
				onEvent = onEvent,
				displayAmplitudes = state.loadState.recordAmplitude,
				barCount = state.recordAmplitudeCount,
			)
		}

		LazyColumn(
			contentPadding = PaddingValues(
				bottom = 32.dp + FloatingToolbarDefaults.ContainerSize
			),
			verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					top = adjustedInnerPadding.calculateTopPadding(),
					start = adjustedInnerPadding.calculateStartPadding(LocalLayoutDirection.current),
					end = adjustedInnerPadding.calculateEndPadding(LocalLayoutDirection.current)
				)
				.padding(horizontal = 8.dp)
				.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
		) {
			itemsIndexed(
				items = state.loadState.logEntries,
				key = { _, logEntry ->
					logEntry.timestamp //TODO: Change if planning on allowing user to edit entry text
				}
			) { index, logEntry ->
				val isSelected = logEntry in state.loadState.selectedEntries
				val isExpanded = logEntry == state.loadState.expandedEntry
				val isSelectionEmpty by remember(state.loadState.selectedEntries) {
					derivedStateOf { state.loadState.selectedEntries.isEmpty() }
				}
				val entriesCount by remember { derivedStateOf {
					state.loadState.logEntries.size
				} }
				EditorEntryItem(
					index = index,
					logEntry = logEntry,
					onEvent = onEvent,
					isExpanded = isExpanded,
					isSelected = isSelected,
					isSelectionEmpty = isSelectionEmpty,
					isRecording = isRecording,
					entriesCount = entriesCount,
					loadingRefId = state.loadState.loadingRefId,
					audioState = state.loadState.audioState,
					mutableInteractionSource = mutableInteractionSource,
					player = player
				)
			}
			if (state.loadState.recordMediaTimestamp != null) {
				item {
					RecordIndicatorItem(
						startTimestamp = state.loadState.recordMediaTimestamp
					)
				}
			}
		}
	}
}

//@Preview( // TODO : https://developer.android.com/develop/ui/compose/tooling/previews
//    showSystemUi = true,
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    device = Devices.PIXEL
//)
//@Composable
//private fun PreviewScreen(){
//    NoteEditorScreen(
//	    state = NoteEditorState(
//		    loadState = LoadState.Loaded(
//			    layoutType = EditorLayoutType.Log(
//				    data = listOf()
//			    ),
//			    log = LoggerNote(
//				    id = 0,
//				    notebookId = 0,
//				    title = "Notetitle",
//				    dateCreated = Clock.System.now(),
//				    lastModified = Clock.System.now(),
//				    data = listOf(),
////	            layoutType = EditorLayoutType.Default(
////		            text = "dfsuidh"
////	            ),
//			    ),
//			    parentNotebook = LoggerNotebook(
//				    title = "parentNotebook",
//				    dateCreated = Clock.System.now(),
//				    authentication = null,
//				    id = 0,
//			    )
//		    ),
////        isLoading = true,
//	    ),
//	    onEvent = {}
//    )
//}
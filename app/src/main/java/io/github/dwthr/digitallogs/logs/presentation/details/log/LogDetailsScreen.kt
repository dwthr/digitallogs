package io.github.dwthr.digitallogs.logs.presentation.details.log

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.DateTimeHelper
import io.github.dwthr.digitallogs.common.presentation.ScenePaneLayout
import io.github.dwthr.digitallogs.common.presentation.composables.AppDialogDefaults
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceContainer
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceItem
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.domain.LogEntry
import io.github.dwthr.digitallogs.logs.domain.LogNote
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.presentation.composables.OutlinedErrorableTextField
import io.github.dwthr.digitallogs.logs.presentation.details.composables.AddTagDropdownTextField
import io.github.dwthr.digitallogs.logs.presentation.details.composables.DetailsScaffold
import io.github.dwthr.digitallogs.logs.presentation.details.composables.TagListSection
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsDialogType.AddTag
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsDialogType.EditTitle
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsDialogType.RemoveTagFromLog
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.AddTagDialogTextChanged
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.EditTitleDialogTextChanged
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.OnBack
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.OnDialogCancel
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.OnDialogConfirm
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.OnTagAddClicked
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.OnTagDeleteClicked
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsEvent.OnTitleEditClicked
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.getDisplayText
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LogDetailsScreenRoot(
    state: LogDetailsState,
    onEvent: (LogDetailsEvent) -> Unit,
    prefsDialogCooldownEnabled: Boolean
) {
    val isLoaded = state.loadState is LogDetailsStateType.Loaded
    val sheetState = rememberBottomSheetState(SheetValue.Hidden)
    val focusRequester = remember { FocusRequester() }

    DetailsScaffold(
        screenTitle = stringResource(R.string.log_details),
        onBack = {
            onEvent(OnBack) }
    ) { innerPadding, _ ->
        if (!isLoaded){
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            )
            return@DetailsScaffold
        }

        val scrollState = rememberScrollState()

        when(state.loadState.dialogTypeVisible) {
            AddTag -> {
                var dropdownMenuExpanded by remember { mutableStateOf(true) }
                val dropdownVisible by remember(dropdownMenuExpanded, state.loadState.addTagsFilterResults) {
                    derivedStateOf {
                        dropdownMenuExpanded && state.loadState.addTagsFilterResults.isNotEmpty()
                    }
                }
                Dialog(
                    isVisible = true,
                    headline = stringResource(R.string.add_tag),
                    onConfirm = {
                        onEvent(OnDialogConfirm)
                    },
                    onDismiss = {
                        onEvent(OnDialogCancel)
                    },
                    content = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            AddTagDropdownTextField(
                                isExpanded = dropdownMenuExpanded,
                                dropdownVisible = dropdownVisible,
                                dropdownExpandedChanged = { dropdownMenuExpanded = !dropdownMenuExpanded },
                                text = state.loadState.addTagDialogTFV,
                                textChanged = {
                                    onEvent(AddTagDialogTextChanged(it))
                                },
                                tags = state.loadState.addTagsFilterResults,
                                addTagErrorText = state.loadState.addTagError?.getDisplayText(),
                                addTagToLogErrorText = state.loadState.addTagToLogError?.getDisplayText(),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .fillMaxWidth()
                            )
                        }
                    },
                )
            }
            EditTitle -> { //TODO: Scroll to end of textfield when tapped
                Dialog(
                    isVisible = true,
                    headline = stringResource(R.string.edit_title),
                    onConfirm = {
                        onEvent(OnDialogConfirm)
                    },
                    onDismiss = {
                        onEvent(OnDialogCancel)
                    },
                    content = {
                        OutlinedErrorableTextField(
                            text = state.loadState.editTitleDialogText,
                            label = null,
                            onValueChange = {
                                onEvent(EditTitleDialogTextChanged(it))
                            },
                            onClearTextField = {
                                onEvent(EditTitleDialogTextChanged(""))
                            },
                            errorText = state.loadState.titleError?.getDisplayText(),
                            charLimit = state.titleCharLimit,
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    },
                )
            }
            is RemoveTagFromLog -> {
                Dialog(
                    isVisible = true,
                    onConfirm = {
                        onEvent(OnDialogConfirm)
                    },
                    onDismiss = {
                        onEvent(OnDialogCancel)
                    },
                    supportingText = "Remove tag \"${state.loadState.dialogTypeVisible.tag.label}\" from log?",
                    confirmTimeoutMillis = AppDialogDefaults.DIALOG_DELETE_COOLDOWN.takeIf {
                        prefsDialogCooldownEnabled
                    },
                )
            }
            null -> Unit
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(
                    state = scrollState
                )
        ) {
            val prefContainerColor = if (ScenePaneLayout.isSinglePane()) {
                MaterialTheme.colorScheme.surfaceBright
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }

            PreferenceContainer {
                PreferenceItem(
                    attribute = stringResource(R.string.log_title_label),
                    containerColor = prefContainerColor,
                    description = state.loadState.log.title,
                    trailingContent = {
                        IconButton(
                            onClick = { onEvent(OnTitleEditClicked) },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_edit_24),
                                contentDescription = stringResource(R.string.edit_title),
                            )
                        }
                    }
                )
                PreferenceItem(
                    attribute = "Category",
                    containerColor = prefContainerColor,
                    description = state.loadState.parentCategoryTitle, //TODO: Go-to button?
                )
                PreferenceItem( //TODO: Remove log from state and move DateTimeHelper to viewmodel
                    attribute = "Date created",
                    containerColor = prefContainerColor,
                    description = DateTimeHelper.instantToLongDisplayString(state.loadState.log.dateCreated),
                )
                PreferenceItem(
                    attribute = "Last modified",
                    containerColor = prefContainerColor,
                    description = DateTimeHelper.instantToLongDisplayString(state.loadState.log.lastModified),
                )
            }

            TagListSection( //TODO: Edit mode for tags?
                onTagAddClicked = { onEvent(OnTagAddClicked) },
                onTagDeleteClicked = { onEvent(OnTagDeleteClicked(it)) },
                tagsInput = state.loadState.tags.toSet()
            )
        }
    }
}

@Preview( // TODO : Create class that provides sample data
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun PreviewScreen(){
    LogDetailsScreenRoot(
	    state = LogDetailsState(
		    loadState = LogDetailsStateType.Loaded(
			    log = LogNote(
				    id = 0,
				    categoryId = 0,
				    title = "sample title",
				    dateCreated = Clock.System.now(),
				    lastModified = Clock.System.now(),
				    entries = listOf(LogEntry(Clock.System.now(), EntryData.Text("asdf"))),
			    ),
			    tags = listOf(Tag("test", 0)),
			    parentCategoryTitle = "Parent nb",
			    editTitleDialogText = "Parent nb",
		    )
	    ),
	    onEvent = {},
	    prefsDialogCooldownEnabled = true
    )
}
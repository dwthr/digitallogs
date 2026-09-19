package io.github.dwthr.digitallogs.logs.presentation.details.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.ScenePaneLayout
import io.github.dwthr.digitallogs.common.presentation.composables.AppDialogDefaults
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceContainer
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceItem
import io.github.dwthr.digitallogs.logs.presentation.composables.OutlinedErrorableTextField
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsDialogType.AddTag
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsDialogType.EditTitle
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsDialogType.RemoveTagFromCategory
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.AddTagDialogTextChanged
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.EditTitleDialogTextChanged
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.OnBack
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.OnDialogCancel
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.OnDialogConfirm
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.OnTagAddClicked
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.OnTagDeleteClicked
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsEvent.OnTitleEditClicked
import io.github.dwthr.digitallogs.logs.presentation.details.composables.AddTagDropdownTextField
import io.github.dwthr.digitallogs.logs.presentation.details.composables.DetailsScaffold
import io.github.dwthr.digitallogs.logs.presentation.details.composables.TagListSection
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.getDisplayText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CategoryDetailsScreenRoot(
    state: CategoryDetailsState,
    onEvent: (CategoryDetailsEvent) -> Unit,
    prefsDialogCooldownEnabled: Boolean
) {
    val sheetState = rememberBottomSheetState(SheetValue.Hidden)
    val isLoaded = state.loadState is CategoryDetailsStateType.Loaded

    DetailsScaffold(
        screenTitle = stringResource(R.string.category_details),
        onBack = { onEvent(OnBack) },
        defaultScaffoldInsets = true,
        defaultScaffoldPadding = true
    ) { innerPadding, _ ->
        val scrollState = rememberScrollState()

        if (!isLoaded){
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            )
            return@DetailsScaffold
        }

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
                        ExposedDropdownMenuBox(
                            expanded = dropdownMenuExpanded,
                            onExpandedChange = { dropdownMenuExpanded = !dropdownMenuExpanded }
                        ) {
                            AddTagDropdownTextField(
                                isExpanded = dropdownMenuExpanded,
                                dropdownVisible = dropdownVisible,
                                dropdownExpandedChanged = { dropdownMenuExpanded = !dropdownMenuExpanded },
                                text = state.loadState.addTagDialogTFV,
                                textChanged = { onEvent(AddTagDialogTextChanged(it)) },
                                tags = state.loadState.addTagsFilterResults,
                                addTagErrorText = state.loadState.addTagError?.getDisplayText(),
                                addTagToLogErrorText = state.loadState.addTagToCategoryError?.getDisplayText()
                            )
                        }
                    },
                )
            }
            EditTitle -> { //TODO: Scroll to end of textfield when tapped
                Dialog(
                    isVisible = true,
                    headline = stringResource(R.string.edit_title_headline),
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
                            charLimit = state.titleCharLimit
                        )
                    },
                )
            }
            is RemoveTagFromCategory -> {
                Dialog(
                    isVisible = true,
                    onConfirm = {
                        onEvent(OnDialogConfirm)
                    },
                    onDismiss = {
                        onEvent(OnDialogCancel)
                    },
                    supportingText = stringResource(
                        R.string.remove_tag_from_category,
                        state.loadState.dialogTypeVisible.tag.label
                    ),
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
                    attribute = stringResource(R.string.category_title_label),
                    containerColor = prefContainerColor,
                    description = state.loadState.category.title,
                    trailingContent = {
                        IconButton(
                            onClick = { onEvent(OnTitleEditClicked) },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_edit_24),
                                contentDescription = stringResource(R.string.edit_title)
                            )
                        }
                    }
                )
            }

            TagListSection(
                onTagAddClicked = { onEvent(OnTagAddClicked) },
                onTagDeleteClicked = { onEvent(OnTagDeleteClicked(it)) },
                tagsInput = state.loadState.tags.toSet()
            )
        }
    }
}
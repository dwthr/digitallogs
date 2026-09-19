package io.github.dwthr.digitallogs.logs.presentation.itemcreator.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.logs.presentation.composables.FilledErrorableTextField
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.getDisplayText

@Composable
fun LogCreatorScreenRoot(
    parentCategoryId: Long,
    viewModel: LogCreatorViewModel = viewModel {
        LogCreatorViewModel(
	        repository = MyApp.appModule.localRepository,
	        parentCategoryId = parentCategoryId
        )
    },
    onLogCreated: (Long) -> Unit,
    onCancel: () -> Unit
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    LogCreatorScreen(
        state = state,
        onEvent = { event ->
            when(event){
                is LogCreatorEvent.OnCancel -> onCancel()
                else -> Unit
            }
            viewModel.onEvent(event)
        }
    )

    val loadedState = state.loadState as? LogCreatorLoadState.Loaded
    loadedState?.newLogId?.let { onLogCreated(it) }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LogCreatorScreen(
    state: LogCreatorState,
    onEvent: (LogCreatorEvent) -> Unit
) {
    val isLoaded = (state.loadState is LogCreatorLoadState.Loaded)

    val focusRequester = remember {
        FocusRequester()
    }

    val focusManager = LocalFocusManager.current

    if (!isLoaded) return
    val errorText = state.loadState.titleError?.getDisplayText()
    Dialog(
        isVisible = true,
        headline = stringResource(R.string.create_log),
        confirmContent = {
            TextButton(
                onClick = { onEvent(LogCreatorEvent.OnConfirm) },
                enabled = it && errorText == null,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = stringResource(R.string.confirm),
                )
            }
        },
        dismissContent = {
            TextButton(
                onClick = { onEvent(LogCreatorEvent.OnCancel) },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
        onDismissRequest = { onEvent(LogCreatorEvent.OnCancel) },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
            ) {
                FilledErrorableTextField(
                    text = state.loadState.titleValue,
                    label = "Title",
                    onValueChange = {
                        onEvent(LogCreatorEvent.OnTitleTextFieldChanged(it))
                    },
                    onClearTextField = {
                        onEvent(LogCreatorEvent.OnTitleTextFieldChanged(""))
                    },
                    errorText = errorText,
                    charLimit = state.titleCharLimit,
                    modifier = Modifier
                )
            }
        }
    )
}

//@Preview(
//    showSystemUi = true,
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    device = Devices.PIXEL
//)
//@Composable
//private fun PreviewTitleCreator(){
//    NoteCreatorScreen(
//        state = LogCreatorState(
//            loadState = LoadState.Loaded(
//                titleValue = "",
//                parentNotebook = TaskNotebook(
//                    title = "Sample parent category :^)",
//                    dateCreated = Clock.System.now(),
//                    authentication = AuthenticationType.SYSTEM,
//                    id = 2,
//                    deleteUponCompletion = false,
//                ),
//            ),
//        ),
//        onEvent = {
//        }
//    )
//}
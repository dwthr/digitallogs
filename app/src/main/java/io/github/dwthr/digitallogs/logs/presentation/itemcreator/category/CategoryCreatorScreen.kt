package io.github.dwthr.digitallogs.logs.presentation.itemcreator.category

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.Dialog
import io.github.dwthr.digitallogs.logs.presentation.composables.FilledErrorableTextField
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.category.CategoryCreatorEvent.OnCancel
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.category.CategoryCreatorEvent.OnConfirm
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.category.CategoryCreatorEvent.OnTitleTextFieldChanged
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.getDisplayText

@Composable
fun CategoryCreatorScreenRoot(
	onCategoryCreated: (Long) -> Unit,
	viewModel: CategoryCreatorViewModel = viewModel {
		CategoryCreatorViewModel(
			repository = MyApp.appModule.localRepository,
		)
	},
	onCancel: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

   CategoryCreatorScreen(
	    state = state,
	    onEvent = { event ->
		    when (event) {
			    is OnCancel -> onCancel()
			    else -> Unit
		    }
		    viewModel.onEvent(event)
	    },
    )

	state.newCategoryId?.let { onCategoryCreated(it) }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun CategoryCreatorScreen(
	state: CategoryCreatorState,
	onEvent: (CategoryCreatorEvent) -> Unit,
) {
	val scrollState = rememberScrollState()
	val sheetState = rememberBottomSheetState(SheetValue.Hidden)
	val focusRequester = remember {
		FocusRequester()
	}
	val focusManager = LocalFocusManager.current

	val errorText = state.titleError?.getDisplayText()
	Dialog(
		isVisible = true,
		headline = stringResource(R.string.create_category),
		confirmContent = {
			TextButton(
				onClick = { onEvent(OnConfirm) },
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
				onClick = { onEvent(OnCancel) },
				shapes = ButtonDefaults.shapes(),
			) {
				Text(
					text = stringResource(R.string.cancel),
				)
			}
		},
		onDismissRequest = { onEvent(OnCancel) },
		content = {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(16.dp),
				modifier = Modifier
			) {
				FilledErrorableTextField(
					text = state.titleValue,
					label = "Title",
					onValueChange = {
						onEvent(OnTitleTextFieldChanged(it))
					},
					onClearTextField = {
						onEvent(OnTitleTextFieldChanged(""))
					},
					errorText = errorText,
					charLimit = state.titleCharLimit,
					modifier = Modifier
				)
			}
		}
	)
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun PreviewTitleCreator(){
    CategoryCreatorScreen(
	    state = CategoryCreatorState(
		    titleValue = "",
		    isAuthenticationEnabled = true,
//		    authenticationTypeRadioOptions = AuthenticationType.displayNames,
	    ),
	    onEvent = {
	    }
    )
}
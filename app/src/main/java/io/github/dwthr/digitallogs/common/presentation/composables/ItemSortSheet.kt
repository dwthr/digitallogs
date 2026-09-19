package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemSortSheet( //TODO: Filter chip?
	sortOptions: List<String>,
	onOptionSelect: (Int) -> Unit,
	onDismiss: () -> Unit,
	sheetState: SheetState,
	selectedOptionIndex: Int,
	contentPadding: PaddingValues
){
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState
	) {
		Column(
			modifier = Modifier
				.padding(contentPadding)
				.padding(horizontal = 12.dp)
		) {
			RadioButtonSelection(
				radioOptions = sortOptions,
				selectedOption = selectedOptionIndex,
				onOptionSelected = onOptionSelect,
				header = {
					Text(
						text = stringResource(R.string.sheet_sort_by),
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				},
				radioOptionIndent = 4.dp
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview() {
	ItemSortSheet(
		sortOptions = listOf("OptionA","OptionB","OptionC","OptionD"),
		onOptionSelect = {  },
		onDismiss = {  },
		sheetState = rememberBottomSheetState(SheetValue.Hidden),
		selectedOptionIndex = 0,
		contentPadding = PaddingValues(0.dp)
	)
}
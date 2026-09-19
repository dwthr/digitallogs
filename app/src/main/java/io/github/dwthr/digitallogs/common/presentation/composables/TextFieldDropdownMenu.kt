package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldDropdownMenu(
	itemList: List<String>,
	selectedItemIndex: Int,
	onSelectedItemIndexChanged: (Int) -> Unit,
	textFieldLabel: @Composable (() -> Unit)? = null
) {
	var isExpanded by remember {
		mutableStateOf(false)
	}

	ExposedDropdownMenuBox(
		expanded = isExpanded,
		onExpandedChange = { isExpanded = !isExpanded }
	) {
		OutlinedTextField(
			value = itemList[selectedItemIndex],
			label = textFieldLabel,
			onValueChange = {},
			readOnly = true,
			trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
			modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
		)

		ExposedDropdownMenu(
			expanded = isExpanded,
			onDismissRequest = { isExpanded = false }
		) {
			itemList.forEachIndexed { index, text ->
				DropdownMenuItem(
					text = { Text(text) },
					onClick = {
						onSelectedItemIndexChanged(index)
						isExpanded = false
					}
				)
			}
		}
	}
}
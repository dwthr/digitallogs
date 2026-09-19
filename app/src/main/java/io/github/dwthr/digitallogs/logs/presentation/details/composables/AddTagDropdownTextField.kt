package io.github.dwthr.digitallogs.logs.presentation.details.composables

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.github.dwthr.digitallogs.logs.domain.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagDropdownTextField(
	isExpanded: Boolean,
	dropdownVisible: Boolean,
	dropdownExpandedChanged: (Boolean) -> Unit,
	text: TextFieldValue,
	textChanged: (TextFieldValue) -> Unit,
	tags: List<Tag>,
	addTagErrorText: String?,
	addTagToLogErrorText: String?,
	modifier: Modifier = Modifier
) {
	ExposedDropdownMenuBox(
		expanded = isExpanded,
		onExpandedChange = { dropdownExpandedChanged(!isExpanded) }
	) {
		OutlinedTextField(
			value = text,
			onValueChange = {
				textChanged(it)
			},
			trailingIcon = {
				ExposedDropdownMenuDefaults.TrailingIcon(
					expanded = isExpanded
				)
			},
			singleLine = true,
			supportingText = addTagErrorText?.let { {
				Text("Creating tag: $it")
			} } ?: addTagToLogErrorText?.let { {
				Text("Adding tag to log: $it")
			} },
			isError = addTagErrorText != null || addTagToLogErrorText != null,
			modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
		)
		ExposedDropdownMenu(
			expanded = dropdownVisible,
			shape = MenuDefaults.shape,
			onDismissRequest = { dropdownExpandedChanged(false) }
		) {
			tags.forEachIndexed { index, tag ->
				key(tag) {
					DropdownMenuItem(
						text = {
							Text(tag.label)
						},
						onClick = { //FIXME: Cursor should be at end before filling
							textChanged(
								TextFieldValue(tag.label, TextRange(tag.label.length))
							)
							dropdownExpandedChanged(false)
						},
					)
				}
			}
		}
	}
}
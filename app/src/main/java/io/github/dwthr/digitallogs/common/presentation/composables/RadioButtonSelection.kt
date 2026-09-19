package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonSelection( //TODO: Right sided radio button option? (M3?)
    radioOptions: List<String>,
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    radioOptionIndent: Dp = 0.dp,
    enabled: Boolean = true,
) { // TODO: Handle less than 2 options (Annotation?)
    Column(modifier = modifier) {
        header?.invoke()
        Spacer(Modifier.size(8.dp))

        Column(Modifier.selectableGroup()) {
            radioOptions.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (index == selectedOption),
                            enabled = enabled,
                            onClick = { onOptionSelected(index) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = radioOptionIndent),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (index == selectedOption),
                        enabled = enabled,
                        onClick = null // null recommended for accessibility with screen readers
                    )
                    Text(
                        text = radioOptions[index], //TODO: Have string conversion handled in viewModel or something to avoid side-effects
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRadioButtonSelection(){
    RadioButtonSelection(
	    radioOptions = listOf("Option 1", "Option 2", "Option 3"),
	    selectedOption = 2,
	    onOptionSelected = {  },
        header = {
            Text("Example header")
        }
    )
}
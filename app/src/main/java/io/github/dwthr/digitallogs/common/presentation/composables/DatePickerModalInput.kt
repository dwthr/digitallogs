package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DatePickerModalInput(
	datePickerState: DatePickerState,
	onDateSelected: (Long?) -> Unit,
	onDismiss: () -> Unit
) {
	DatePickerDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(
				onClick = {
					onDateSelected(datePickerState.selectedDateMillis)
					onDismiss()
				},
				shapes = ButtonDefaults.shapes(),
				content = {
					Text("OK")
				}
			)
		},
		dismissButton = {
			TextButton(
				onClick = onDismiss,
				shapes = ButtonDefaults.shapes(),
				content = {
					Text("Cancel")
				}
			)
		},
		modifier = Modifier.fillMaxSize()
	) {
		DatePicker(
			state = datePickerState,
		)
	}
}
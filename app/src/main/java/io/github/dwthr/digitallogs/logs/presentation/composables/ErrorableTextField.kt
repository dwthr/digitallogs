package io.github.dwthr.digitallogs.logs.presentation.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R

@Composable
fun FilledErrorableTextField(
	text: String,
	label: String?,
	onValueChange: (String) -> Unit,
	onClearTextField: () -> Unit,
	modifier: Modifier = Modifier,
	errorText: String? = null,
	charLimit: Int
) {
	TextField(
		value = text,
		onValueChange = onValueChange,
		leadingIcon = {
			Icon(
				painter = painterResource(R.drawable.baseline_edit_24),
				contentDescription = null
			)
		},
		label = {
			label?.let {
				Text(it)
			}
		},
		singleLine = true,
		supportingText = {
			if (errorText != null) {
				Text(errorText)
			} else {
				Row(
					horizontalArrangement = Arrangement.End,
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = "${text.length}/$charLimit",
					)
				}
			}
		},
		isError = errorText != null,
		trailingIcon = { // Icons similar to profile pics should instead be 30.dp in size
			if (errorText != null) {
				Icon(
					painter = painterResource(R.drawable.baseline_error_24),
					contentDescription = null
				)
			} else {
				AnimatedVisibility(
					visible = text.isNotBlank(),
					enter = fadeIn() + expandIn(),
					exit = fadeOut() + shrinkOut(),
					modifier = Modifier.padding(horizontal = 16.dp)
				) {
					IconButton(
						onClick = onClearTextField,
						modifier = Modifier.size(24.dp)
					) {
						Icon(
							painter = painterResource(R.drawable.baseline_close_24),
							contentDescription = stringResource(R.string.clear_text_field),
							tint = MaterialTheme.colorScheme.onSurface,
						)
					}
				}
			}
		},
		modifier = modifier
			.fillMaxWidth()
	)
}

@Composable
fun OutlinedErrorableTextField(
	text: String,
	label: String?,
	onValueChange: (String) -> Unit,
	onClearTextField: () -> Unit,
	modifier: Modifier = Modifier,
	errorText: String? = null,
	charLimit: Int
) {
	OutlinedTextField(
		value = text,
		onValueChange = onValueChange,
		leadingIcon = {
			Icon(
				painter = painterResource(R.drawable.baseline_edit_24),
				contentDescription = null
			)
		},
		label = {
			label?.let {
				Text(it)
			}
		},
		singleLine = true,
		supportingText = {
			if (errorText != null) {
				Text(errorText)
			} else {
				Row(
					horizontalArrangement = Arrangement.End,
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = "${text.length}/$charLimit",
					)
				}
			}
		},
		isError = errorText != null,
		trailingIcon = { // Icons similar to profile pics should instead be 30.dp in size
			if (errorText != null) {
				Icon(
					painter = painterResource(R.drawable.baseline_error_24),
					contentDescription = null
				)
			} else {
				AnimatedVisibility(
					visible = text.isNotBlank(),
					enter = fadeIn() + expandIn(),
					exit = fadeOut() + shrinkOut(),
					modifier = Modifier.padding(horizontal = 16.dp)
				) {
					IconButton(
						onClick = onClearTextField,
						modifier = Modifier.size(24.dp)
					) {
						Icon(
							painter = painterResource(R.drawable.baseline_close_24),
							contentDescription = stringResource(R.string.clear_text_field),
							tint = MaterialTheme.colorScheme.onSurface,
						)
					}
				}
			}
		},
		modifier = modifier
			.fillMaxWidth()
	)
}
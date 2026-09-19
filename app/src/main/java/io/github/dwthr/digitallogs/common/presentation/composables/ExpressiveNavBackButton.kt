package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.dwthr.digitallogs.R

@Composable
fun ExpressiveNavBackButton(
	onClick: () -> Unit
) {
	FilledTonalIconButton(
		onClick = onClick,
		colors = IconButtonDefaults.filledTonalIconButtonColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainer
		),
		shapes = IconButtonDefaults.shapes(
			shape = IconButtonDefaults.smallRoundShape,
			pressedShape = IconButtonDefaults.smallRoundShape
		),
	) {
		Icon(
			painter = painterResource(R.drawable.baseline_arrow_back_24),
			tint = MaterialTheme.colorScheme.onSurface,
			contentDescription = stringResource(R.string.navigate_back),
		)
	}
}
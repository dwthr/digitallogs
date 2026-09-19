package io.github.dwthr.digitallogs.logs.presentation.itemcreator

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.logs.domain.error.EntityError

@Composable
fun EntityError.Title.getDisplayText(): String {
	return when(this) {
		EntityError.Title.IS_BLANK -> stringResource(R.string.error_title_can_not_be_blank)
		EntityError.Title.TOO_LONG -> stringResource(R.string.error_title_is_too_long)
		EntityError.Title.CONFLICT -> stringResource(R.string.error_title_already_exists)
	}
}
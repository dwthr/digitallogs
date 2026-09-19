package io.github.dwthr.digitallogs.common.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
	data class Basic(val text: String): UiText()
	class StringResource(
		@param:StringRes val resId: Int,
		vararg val args: Any
	): UiText()

	@Composable
	fun asString(): String = when(this) {
		is Basic -> text
		is StringResource -> stringResource(resId, *args)
	}
}

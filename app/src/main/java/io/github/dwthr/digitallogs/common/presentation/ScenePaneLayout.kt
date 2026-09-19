package io.github.dwthr.digitallogs.common.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
object ScenePaneLayout {
	@Composable
	fun isSinglePane(): Boolean {
		return LocalListDetailSceneScope.current == null
	}

	@Composable
	fun innerPaddingDetailPane(scaffoldInnerPadding: PaddingValues): PaddingValues { //TODO: Derived state?
		val layoutDirection = LocalLayoutDirection.current

		val endPadding = scaffoldInnerPadding.calculateEndPadding(layoutDirection)

		return PaddingValues(
			start = 0.dp,
			top = scaffoldInnerPadding.calculateTopPadding(),
			end = endPadding,
			bottom = scaffoldInnerPadding.calculateBottomPadding(),
		)
	}

	@Composable
	fun innerPaddingListPane(scaffoldInnerPadding: PaddingValues): PaddingValues { //TODO: Derived state?
		val layoutDirection = LocalLayoutDirection.current

		val startPadding = scaffoldInnerPadding.calculateStartPadding(layoutDirection)

		return PaddingValues(
			start = startPadding,
			top = scaffoldInnerPadding.calculateTopPadding(),
			end = 0.dp,
			bottom = scaffoldInnerPadding.calculateBottomPadding(),
		)
	}
}
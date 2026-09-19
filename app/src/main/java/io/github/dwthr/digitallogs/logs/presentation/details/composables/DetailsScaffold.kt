package io.github.dwthr.digitallogs.logs.presentation.details.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import io.github.dwthr.digitallogs.common.presentation.ScenePaneLayout
import io.github.dwthr.digitallogs.common.presentation.composables.ExpressiveNavBackButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
	ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun DetailsScaffold(
	screenTitle: String,
	subHeader: String? = null,
	onBack: () -> Unit,
	defaultScaffoldPadding: Boolean = false,
	defaultScaffoldInsets: Boolean = false,
	content: @Composable (PaddingValues, Color) -> Unit
) {
	val isSinglePane = ScenePaneLayout.isSinglePane()
	val backgroundColor = if (isSinglePane) {
		MaterialTheme.colorScheme.surfaceDim
	} else {
		MaterialTheme.colorScheme.surfaceBright
	}

	Scaffold(
		topBar = {
			TopAppBar(
				windowInsets = TopAppBarDefaults.windowInsets.run {
					if (isSinglePane || defaultScaffoldInsets) return@run this
					exclude(only(WindowInsetsSides.Start))
				},
				navigationIcon = {
					if (isSinglePane) {
						ExpressiveNavBackButton { onBack() }
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = backgroundColor
				),
				title = {
					Text(
						text = screenTitle,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1
					)
				},
				subtitle = {
					subHeader?.let {
						Text(
							text = it,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1
						)
					}
				},
			)
		},
		containerColor = backgroundColor ,
		modifier = Modifier
			.fillMaxSize()
	) { innerPadding ->
		content(
			if (!defaultScaffoldPadding) {
				ScenePaneLayout.innerPaddingDetailPane(innerPadding)
			} else {
				innerPadding
			},
			backgroundColor
		)
	}
}
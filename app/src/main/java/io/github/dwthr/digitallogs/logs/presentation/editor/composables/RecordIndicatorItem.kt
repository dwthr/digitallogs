@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.dwthr.digitallogs.logs.presentation.editor.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import io.github.dwthr.digitallogs.common.presentation.DateTimeHelper
import kotlin.time.Instant

@Composable
fun RecordIndicatorItem(
	startTimestamp: Instant,
) {
	val timestampText by remember {
		derivedStateOf {
			DateTimeHelper.instantToShortDisplayString(startTimestamp)
		}
	}
	SegmentedListItem(
		selected = true,
		onClick = {  },
		shapes = ListItemDefaults.segmentedShapes(0,0),
		leadingContent = {
			Text(
				text = timestampText,
				style = MaterialTheme.typography.labelMedium,
				modifier = Modifier
					.width(112.dp) //FIXME: Static width not ideal
			)
		},
		content = { //TODO: recording duration
			LoadingIndicator(
				color = MaterialTheme.colorScheme.tertiary,
				polygons = listOf(
					RoundedPolygon.circle(),
					RoundedPolygon.star(
						numVerticesPerRadius = 8
					),
					RoundedPolygon.star(
						numVerticesPerRadius = 4
					),
				),
				modifier = Modifier
					.padding(horizontal = 16.dp)
			)
		},
	)
}
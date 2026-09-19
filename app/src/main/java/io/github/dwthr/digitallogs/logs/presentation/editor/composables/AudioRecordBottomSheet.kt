package io.github.dwthr.digitallogs.logs.presentation.editor.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.logs.presentation.editor.EditorEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioRecordBottomSheet(
	onEvent: (EditorEvent) -> Unit,
	barCount: Int,
	barColor: Color = MaterialTheme.colorScheme.outline,
	loudAudioBarColor: Color = barColor.copy(red = 255f),
	loudAudioThresholdInclusive: Float = 1f,
	minimumBarHeight: Float = 0.05f,
	displayAmplitudes: List<Float>,
) {
	ModalBottomSheet(
		onDismissRequest = {
			onEvent(EditorEvent.OnMicRecordCancel)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(0.4f)
				.padding(horizontal = 8.dp)
		) {
			Canvas(
				modifier = Modifier
					.fillMaxSize()
			) {

				val canvasWidth = size.width
				val canvasHeight = size.height
				val barGapPx = 6f
				val totalGapsWidth = barGapPx * (barCount - 1)
				val barWidthPx = (canvasWidth - totalGapsWidth) / barCount
				val stepPx = barWidthPx + barGapPx

				val barAmplitudes = (barCount - displayAmplitudes.size).let { difference ->
					if (difference > 0) {
						List(difference) { 0f } + displayAmplitudes
					} else {
						displayAmplitudes
					}
				}

				barAmplitudes.forEachIndexed { index, amplitude ->
					val normalizedAmplitude = amplitude
						.coerceIn(0f, 1f)

					val barHeight = (canvasHeight * normalizedAmplitude).coerceAtLeast(minimumBarHeight)

					val startY = (canvasHeight - barHeight) / 2
					val endY = startY + barHeight

					val x = (index * stepPx) + (barWidthPx / 2)

					drawLine(
						color = if (normalizedAmplitude >= loudAudioThresholdInclusive) {
							loudAudioBarColor
						} else {
							barColor
						},
						start = Offset(x, startY),
						end = Offset(x, endY),
						strokeWidth = barWidthPx,
						cap = if (normalizedAmplitude != 1f) StrokeCap.Round else StrokeCap.Butt
					)
				}
			}
			FilledIconButton(
				onClick = {
					onEvent(EditorEvent.OnMicRecordToggle(false))
				},
				shapes = IconButtonShapes(
					shape = IconButtonDefaults.mediumRoundShape,
					pressedShape = IconButtonDefaults.mediumPressedShape
				),
				modifier = Modifier
					.align(Alignment.Center)
					.size(IconButtonDefaults.mediumContainerSize())
					.zIndex(0.5f)
			) {
				Icon(
					painter = painterResource(R.drawable.baseline_mic_24),
					contentDescription = stringResource(R.string.stop_recording),
					modifier = Modifier
						.size(IconButtonDefaults.mediumIconSize)
				)
			}

			FilledTonalIconButton(
				onClick = {
					onEvent(EditorEvent.OnMicRecordCancel)
				},
				shapes = IconButtonDefaults.shapes(),
				modifier = Modifier
					.align(Alignment.TopEnd)
			) {
				Icon(
					painter = painterResource(R.drawable.baseline_close_24),
					contentDescription = stringResource(R.string.cancel_recording)
				)
			}
		}
	}
}
@file:kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import io.github.dwthr.digitallogs.R

@OptIn(UnstableApi::class)
@Composable
fun AudioItemPlayer(
	player: Player,
	modifier: Modifier = Modifier,
) {
	OutlinedCard(
		modifier = modifier,
	) {
		Column(
			modifier = Modifier
				.padding(2.dp)
				.padding(end = 4.dp)
				.padding(vertical = 8.dp)
		) {
			Row(
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				PlayPauseButton(
					player = player,
					content = {
						FilledIconToggleButton(
							checked = this.showPlay,
							onCheckedChange = { this.onClick() },
							shapes = IconToggleButtonShapes(
								shape = IconButtonDefaults.smallSquareShape,
								checkedShape = IconButtonDefaults.smallRoundShape,
								pressedShape = IconButtonDefaults.smallPressedShape
							),
							colors = IconButtonDefaults.iconToggleButtonColors(
								checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
								checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
								containerColor = MaterialTheme.colorScheme.primaryContainer,
								contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
							),
						) {
							if (this.showPlay) {
								Icon(
									painter = painterResource(R.drawable.outline_play_arrow_24),
									contentDescription = stringResource(R.string.play)
								)
							} else {
								Icon(
									painter = painterResource(R.drawable.baseline_pause_24),
									contentDescription = stringResource(R.string.pause)
								)
							}
						}
					}
				)
//				PlayPauseButton(
//					player = player,
//					onClick = {
//						this.onClick()
//					},
//					colors = IconButtonDefaults.iconButtonColors(
//						containerColor = MaterialTheme.colorScheme.tertiary,
//						contentColor = MaterialTheme.colorScheme.onTertiary
//					)
//				)
				ProgressSlider(player = player)
			}
			Box(
				contentAlignment = Alignment.CenterEnd,
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 4.dp)
			) {
				PositionAndDurationText(
					player = player,
				)
			}
		}
	}
}
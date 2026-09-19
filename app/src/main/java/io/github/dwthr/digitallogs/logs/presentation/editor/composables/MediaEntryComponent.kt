package io.github.dwthr.digitallogs.logs.presentation.editor.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.AudioItemPlayer
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.presentation.editor.AudioState

@Composable
fun MediaEntryComponent(
	onAudioEntryPressed: () -> Unit,
	entry: EntryData.Media,
	index: Int,
	audioState: AudioState?,
	loadingRefId: String?,
	isSelected: Boolean,
	containerIsExpanded: Boolean,
	mediaLoadAnimationKey: Any,
	player: Player,
) {
	val isPlayingThisItem = audioState == AudioState.AudioPlaying(entry.fileReferenceUuid)
	val isLoadingThisItem = loadingRefId == entry.fileReferenceUuid
//							        Player(player)
	SharedTransitionLayout {
		AnimatedContent(targetState = isPlayingThisItem) { isPlaying ->
			if (!isPlaying) {
				val playButtonContainerColor by animateColorAsState(targetValue =
					if (isSelected || containerIsExpanded) {
						MaterialTheme.colorScheme.background
					} else {
						IconButtonDefaults.filledTonalIconButtonColors().containerColor
					}
				)
				val playButtonContentColor by animateColorAsState(
					targetValue = if (isSelected || containerIsExpanded) {
						MaterialTheme.colorScheme.onBackground
					} else {
						IconButtonDefaults.filledTonalIconButtonColors().contentColor
					}
				)
				FilledTonalIconButton(
					onClick = onAudioEntryPressed,
					shapes = IconButtonDefaults.shapes(),
					colors = IconButtonDefaults.filledTonalIconButtonColors(
						containerColor = playButtonContainerColor,
						contentColor = playButtonContentColor
					),
					modifier = Modifier
						.sharedElement(
							sharedContentState = rememberSharedContentState(
								key = index
							),
							animatedVisibilityScope = this
						)
				) {
					if (isLoadingThisItem) {
						CircularProgressIndicator(
							color = MaterialTheme.colorScheme.tertiary
						)
						Icon(
							painter = painterResource(R.drawable.baseline_pause_24),
							contentDescription = stringResource(R.string.pause),
						)
					} else {
						Icon(
							painter = painterResource(R.drawable.outline_play_arrow_24),
							contentDescription = stringResource(R.string.play),
						)
					}
				}
//															Text(
//																text = "0:00" //TODO: store metadata
//															)
			} else {
				AudioItemPlayer( //TODO: make player external so it doesn't reload each time isExpanded changes
					player = player,
					modifier = Modifier
						.sharedElement(
							sharedContentState = rememberSharedContentState(
								key = mediaLoadAnimationKey
							),
							animatedVisibilityScope = this
						),
				)
			}
		}
	}
}
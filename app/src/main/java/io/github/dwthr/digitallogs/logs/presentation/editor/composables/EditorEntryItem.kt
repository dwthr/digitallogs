package io.github.dwthr.digitallogs.logs.presentation.editor.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import io.github.dwthr.digitallogs.common.presentation.DateTimeHelper
import io.github.dwthr.digitallogs.common.presentation.ScenePaneLayout
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.domain.LogEntry
import io.github.dwthr.digitallogs.logs.presentation.editor.AudioState
import io.github.dwthr.digitallogs.logs.presentation.editor.EditorEvent


@Composable
fun LazyItemScope.EditorEntryItem(
	index: Int,
	logEntry: LogEntry,
	onEvent: (EditorEvent) -> Unit,
	isExpanded: Boolean,
	isSelected: Boolean,
	isSelectionEmpty: Boolean,
	isRecording: Boolean,
	entriesCount: Int,
	loadingRefId: String?,
	audioState: AudioState?,
	mutableInteractionSource: MutableInteractionSource,
	player: Player,
) {
	val (timestamp, entry) = logEntry
	val expandedAnimationKey = "${index}_CARD_LIST_ITEM"
	SharedTransitionLayout {
		AnimatedContent(targetState = isExpanded) {isExpanded ->
			if (isExpanded) {
				Card(
					colors = if (isSelected) {
						CardDefaults.cardColors(
							containerColor = ListItemDefaults.colors().selectedContainerColor
						)
					} else {
						CardDefaults.cardColors()
					},
					modifier = Modifier
						.fillParentMaxWidth()
						.combinedClickable(
							interactionSource = mutableInteractionSource,
							indication = LocalIndication.current,
							onClick = {
								if (isSelectionEmpty) {
									onEvent(EditorEvent.OnEntryExpandedChanged(null))
								} else {
									onEvent(
										EditorEvent.OnEntrySelectionToggle(
											logEntry
										)
									)
								}
							},
							onLongClick = {
								onEvent(EditorEvent.OnEntrySelectionToggle(logEntry))
							}
						)
						.sharedBounds(
							sharedContentState = rememberSharedContentState(
								key = expandedAnimationKey
							),
							animatedVisibilityScope = this
						)
				) {
					Row(
						modifier = Modifier
							.padding(horizontal = 8.dp)
							.padding(8.dp)
					) {
						Text(
							text = DateTimeHelper.instantToLongDisplayString(logEntry.timestamp),
							style = MaterialTheme.typography.labelMedium,
							modifier = Modifier
								.width(128.dp)
								.padding(top = 4.dp)
						)
						Spacer(Modifier.size(12.dp))
						when(entry) {
							is EntryData.Media -> {
								MediaEntryComponent(
									onAudioEntryPressed = {
										onEvent(EditorEvent.OnAudioEntryPressed(entry.fileReferenceUuid))
									},
									entry = entry,
									index = index,
									audioState = audioState,
									loadingRefId = loadingRefId,
									isSelected = isSelected,
									mediaLoadAnimationKey = index,
									player = player,
									containerIsExpanded = true
								)
							}
							is EntryData.Text -> {
								Text(
									text = entry.text,
									style = MaterialTheme.typography.bodyLarge,
									modifier = Modifier
										.padding(vertical = 4.dp)
								)
							}
						}
					}
				}
			} else {
				SegmentedListItem(
					selected = isSelected,
					onClick = {
						if (isSelectionEmpty) {
							onEvent(EditorEvent.OnEntryExpandedChanged(entry = logEntry))
						} else {
							onEvent(EditorEvent.OnEntrySelectionToggle(logEntry))
						}
					},
					onLongClick = {
						onEvent(EditorEvent.OnEntrySelectionToggle(logEntry))
					},
					colors = ListItemDefaults.segmentedColors(
						containerColor = if (ScenePaneLayout.isSinglePane()) {
							ListItemDefaults.segmentedColors().containerColor
						} else {
							MaterialTheme.colorScheme.surfaceContainer
						}
					),
					shapes = ListItemDefaults.segmentedShapes(
						index = index,
						count = entriesCount
								+ (1.takeIf { isRecording } ?: 0)
					),
					leadingContent = {
						Text(
							text = DateTimeHelper.instantToShortDisplayString(timestamp),
							style = MaterialTheme.typography.labelMedium,
							modifier = Modifier
								.width(128.dp)
						)
					},
					content = {
						Row {
							when(entry) { //TODO: Properly handle video/image
								is EntryData.Text -> {
									Text(
										text = entry.text,
										maxLines = 3,
										overflow = TextOverflow.Ellipsis,
										style = MaterialTheme.typography.bodyLarge
									)
								}
								is EntryData.Media -> {
									MediaEntryComponent(
										onAudioEntryPressed = { onEvent(EditorEvent.OnAudioEntryPressed(entry.fileReferenceUuid)) },
										entry = entry,
										index = index,
										audioState = audioState,
										loadingRefId = loadingRefId,
										isSelected = isSelected,
										mediaLoadAnimationKey = index,
										player = player,
										containerIsExpanded = false
									)
								}
							}
						}
					},
					modifier = Modifier
						.sharedBounds(
							sharedContentState = rememberSharedContentState(
								key = expandedAnimationKey
							),
							animatedVisibilityScope = this
						)
				)
			}
		}
	}
}
package io.github.dwthr.digitallogs.logs.presentation.home.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.common.presentation.theme.Typography
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.domain.LogEntryShareHandler.getShareableText
import io.github.dwthr.digitallogs.logs.presentation.home.HomeEvent

@Composable
fun HorizontalPreviewCarousel(
	carouselItems: List<LogAndCategory>,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	previewItemHeight: Dp,
	previewCardColors: CardColors = CardDefaults.cardColors(),
	onEvent: (HomeEvent) -> Unit
){
	val carouselState = rememberCarouselState { carouselItems.count() }
	HorizontalUncontainedCarousel(
		state = carouselState,
		itemWidth = 180.dp,
		itemSpacing = 8.dp,
		contentPadding = contentPadding,
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight(),
		content = { index ->
			val item = carouselItems[index]

			val displayText = item.log.entries.getShareableText()
			val parentCategoryTitle = item.parentCategory.title

			key(displayText, parentCategoryTitle) {
				NotePreviewCard(
					title = item.log.title,
					label = item.parentCategory.title,
					maxHeight = previewItemHeight,
					body = {
						Text( // Content
							text = displayText,
							style = Typography.bodyMedium,
							maxLines = 8,
							overflow = TextOverflow.Clip,
							modifier = Modifier
								.fillMaxWidth()
						)
					},
					onClick = {
						onEvent(HomeEvent.OnLogClick(item.log))
					},
					cardColors = previewCardColors,
					modifier = Modifier
						.maskClip(MaterialTheme.shapes.extraLarge)
				)
			}
		},
	)
}
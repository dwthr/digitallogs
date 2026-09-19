package io.github.dwthr.digitallogs.logs.presentation.home.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.DisplaySection
import io.github.dwthr.digitallogs.logs.domain.LogAndCategory
import io.github.dwthr.digitallogs.logs.presentation.home.HomeEvent
import io.github.dwthr.digitallogs.logs.presentation.home.composables.HorizontalPreviewCarousel

@Composable
fun RecentLogsPreviewCarousel(
    recentLogs: List<LogAndCategory>,
    innerPaddingHorizontal: PaddingValues,
    onEvent: (HomeEvent) -> Unit
) {
    DisplaySection(
        headerText = stringResource(R.string.recent_logs),
        headerPadding = innerPaddingHorizontal + PaddingValues(horizontal = 12.dp),
        sectionContainerColor = MaterialTheme.colorScheme.surfaceDim,
        headerLeadingContent = {
            IconButton(
                onClick = {
                    onEvent(HomeEvent.OnRecentShowAllClick)
                },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_forward_24),
                    contentDescription = stringResource(R.string.show_recent_logs),
                    modifier = Modifier
                        .size(IconButtonDefaults.smallIconSize)
                )
            }
        }
    ) {
	    HorizontalPreviewCarousel(
		    carouselItems = recentLogs,
		    previewItemHeight = 250.dp,
		    previewCardColors = CardDefaults.cardColors(
			    containerColor = MaterialTheme.colorScheme.surfaceContainer
		    ),
		    contentPadding = innerPaddingHorizontal + PaddingValues(horizontal = 8.dp),
		    onEvent = onEvent
	    )
    }
}
package io.github.dwthr.digitallogs.common.presentation.composables

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.common.presentation.theme.Typography
import io.github.dwthr.digitallogs.logs.presentation.home.composables.NotePreviewCard

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplaySection(
    headerText: String,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(0.dp),
    headerLeadingContent: @Composable (() -> Unit) = {},
    headerTrailingContent: @Composable () -> Unit = {},
    sectionContainerColor: Color = MaterialTheme.colorScheme.background,
    titleContentSpacing: Dp = 12.dp,
    content: @Composable () -> Unit,
){
    Surface(
        color = sectionContainerColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Spacer(Modifier.size(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(headerPadding)
                    .padding(top = 4.dp)
            ) {
                Box {
                    Row {
                        Text(
                            text = headerText,
                            textAlign = TextAlign.Start,
                            style = Typography.titleLarge,
                            modifier = Modifier
                                .heightIn(48.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .padding(start = 4.dp)
                        )
                        headerLeadingContent.invoke()
                    }
                }
                headerTrailingContent.invoke()
            }
            Spacer(Modifier.size(titleContentSpacing))
            content()
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun PreviewExpandableSection(){
    DisplaySection(
        headerText = "Title"
    ) {
        LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier
            .fillMaxWidth(),
        ){
            val cardHeight = 160.dp
            item(
                content = {
//                    CreateQuickNoteButton(
//                        onClick = {},
//                        modifier = Modifier
//                            .height(cardHeight)
//                            .padding(5.5.dp)
//                    )
                }
            )
            items(3) {
                NotePreviewCard(
	                title = "title $it",
	                label = "label $it",
	                body = { Text("Body Text Body Text Body Text Body Text Body Text Body Text Body Text ") },
	                onClick = {},
	                modifier = Modifier
		                .height(cardHeight)
		                .padding(4.dp),
	                maxHeight = 250.dp,
                )
            }
        }
    }
}
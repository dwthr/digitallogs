package io.github.dwthr.digitallogs.logs.presentation.home.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.common.presentation.theme.Typography

@Composable
fun NotePreviewCard(
    label: String?,
    title: String,
    body: @Composable (() -> Unit),
    maxHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColors: CardColors = CardDefaults.cardColors(),
    titleTrailingIcon: @Composable (() -> Unit)? = null
){
    Card(
        colors = cardColors,
        modifier = modifier
            .clickable { onClick() }
            .heightIn(maxHeight)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp))
            {
                Text( // Title
                    text = title,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                )
                titleTrailingIcon?.invoke()
            }
            if (label != null) {
                Text( // Subtitle
                    text = label,
                    style = Typography.labelSmall,
                    textAlign = TextAlign.Left,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .padding(horizontal = 8.dp)
                )
            }
            HorizontalDivider()
            Spacer(Modifier.size(8.dp))
            body.invoke()
//            Text( // Content
//                text = body,
//                style = Typography.bodySmall,
//                maxLines = 8,
//                overflow = TextOverflow.Clip,
//                modifier = Modifier
//                    .fillMaxWidth()
//            )
        }
    }
}

@Preview(
//    showSystemUi = true,
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    device = Devices.PIXEL
)
@Composable
fun PreviewNotePreviewCard(){
    NotePreviewCard(
        title = "This is a very long Title Because I want to test the preview",
        label = "subtitle",
        body = { Text("This is a sample body paragraph. This is a sample body paragraph. This is a sample body paragraph. This is a sample body paragraph. This is a sample body paragraph. This is a sample body paragraph.") },
        onClick = {},
        maxHeight = 250.dp
    )
}
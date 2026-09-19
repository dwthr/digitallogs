package io.github.dwthr.digitallogs.logs.presentation.loglist.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.theme.Typography
import io.github.dwthr.digitallogs.logs.domain.Tag

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemTagListSheet(
    onTagDeleteClicked: (Tag) -> Unit,
    tagsInput: Set<Tag>,
    header: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ){

        header?.invoke()
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        ) {
            tagsInput.forEach { tag ->
                InputChip(
                    selected = false,
                    onClick = {  }, //TODO: Change name?
                    label = { Text(tag.label) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onTagDeleteClicked(tag) },
                            modifier = Modifier
                                .size(18.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_close_24),
                                contentDescription = stringResource(R.string.remove_tag),
                            )
                        }
                    },
//                    modifier = Modifier.border(
//                        BorderStroke(2.dp, color = tag.color)
//                    )
                )
            }
        }
    }
}


@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun ItemTagSheetPreview(){
    val tags = emptyList<Tag>().toMutableList()

    repeat(5){
        tags += Tag(
            label = "Tag $it",
            id = it.toLong()
        )
    }

    ItemTagListSheet(
	    tagsInput = tags.toSet(),
        header = {
            Text(
                text = "Tags",
                style = Typography.titleMedium //TODO: Right one?
            )
        },
        onTagDeleteClicked = {}
    )
}
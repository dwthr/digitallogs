package io.github.dwthr.digitallogs.common.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.theme.Typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipSection(
    title: String,
    headerIcon: @Composable (() -> Unit)?,
    content: @Composable (() -> Unit)
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding()
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            headerIcon?.invoke()
            Text(
                text = title,
                style = Typography.titleMedium.copy(
                    lineHeightStyle = LineHeightStyle(
                        LineHeightStyle.Alignment.Bottom,
                        trim = LineHeightStyle.Trim.LastLineBottom
                    )
                ),
                modifier = Modifier
                    .padding(start = 2.dp)
            )
        }
        Spacer(modifier = Modifier.padding(top = 12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp)
        ) {
            content()
        }
        Spacer(Modifier.padding(bottom = 8.dp))
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun ChipSectionPreview() {
    ChipSection(
        title = "Chip Section",
        headerIcon = { Icon(painterResource(R.drawable.baseline_date_range_24), null, tint = MaterialTheme.colorScheme.tertiary) }
    ){
        AssistChip(
            onClick = {  },
            label = { Text("Washer / Dryer") }
        )
        AssistChip(
            onClick = {  },
            label = { Text("Ramp Access") }
        )
        AssistChip(
            onClick = {  },
            label = { Text("Washer / Dryer") }
        )
        AssistChip(
            onClick = {  },
            label = { Text("Ramp Access") }
        )
    }
}
package io.github.dwthr.digitallogs.common.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.theme.Typography

@Composable
fun TextWithSwitchOption(
    text: String,
    isToggled: Boolean,
    onToggled: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        Switch(
            checked = isToggled, // disabled if null
            onCheckedChange = onToggled,
            thumbContent = {
                Icon(
                    painter = if (isToggled) {
                        painterResource(R.drawable.baseline_check_24)
                    } else {
                        painterResource(R.drawable.baseline_close_24)
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL
)
@Composable
private fun PreviewTextWithSwitchOption(){
    TextWithSwitchOption("Sample Text", false, {

    })
}
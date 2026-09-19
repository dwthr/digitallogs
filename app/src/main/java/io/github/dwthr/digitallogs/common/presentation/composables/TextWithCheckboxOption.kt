package io.github.dwthr.digitallogs.common.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import io.github.dwthr.digitallogs.common.presentation.theme.Typography

@Composable
fun TextWithCheckboxOption(
    text: String,
    toggledState: Boolean,
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
        Checkbox(
	        checked = toggledState,
	        onCheckedChange = onToggled
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
private fun PreviewTextWithCheckboxOption(){
    TextWithCheckboxOption("Sample Text", false, {})
}
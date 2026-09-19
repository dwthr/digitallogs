package io.github.dwthr.digitallogs.logs.presentation.itemcreator.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemCreatorFormButtons(
    onClickCancel: () -> Unit,
    onClickConfirm: () -> Unit
){
    HorizontalDivider(Modifier.padding(vertical = 16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(bottom = 8.dp)
    ) {
        OutlinedButton(
            onClick = onClickCancel
        ) {
            Text("Cancel")
        }
        Button(
            onClick = onClickConfirm
        ) {
            Text("Confirm")
        }
    }
}
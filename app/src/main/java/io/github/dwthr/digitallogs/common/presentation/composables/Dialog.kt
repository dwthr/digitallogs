package io.github.dwthr.digitallogs.common.presentation.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.dwthr.digitallogs.R
import kotlinx.coroutines.delay

object AppDialogDefaults {
    const val DIALOG_DELETE_COOLDOWN = 400L
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationDialog(
    isVisible: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    headline: String? = null,
    supportingText: String? = null,
    onDismiss: () -> Unit,
    dismissText: String = stringResource(R.string.dialog_acknowledge),
    confirmTimeoutMillis: Long? = null,
    headerContentDividerVisible: Boolean = false,
    content: @Composable (() -> Unit)? = null,
){
    Dialog(
	    isVisible = isVisible,
	    icon = icon,
	    headline = headline,
	    supportingText = supportingText,
	    onConfirm = onDismiss, //passed to onConfirm for positioning reasons
	    confirmText = dismissText,
	    onDismiss = { }, //Not used for positioning reasons
	    dismissText = "",
	    confirmTimeoutMillis = confirmTimeoutMillis,
	    headerContentDividerVisible = headerContentDividerVisible,
	    content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Dialog(
    isVisible: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    headline: String? = null,
    supportingText: String? = null,
    onConfirm: () -> Unit,
    confirmText: String = stringResource(R.string.confirm),
    onDismiss: () -> Unit,
    dismissText: String = stringResource(R.string.cancel),
    confirmTimeoutMillis: Long? = null,
    headerContentDividerVisible: Boolean = false,
    content: @Composable (() -> Unit)? = null,
){
    Dialog(
	    isVisible = isVisible,
	    icon = icon,
	    headline = headline,
	    supportingText = supportingText,
	    confirmContent = {
            TextButton(
                onClick = onConfirm,
                enabled = it,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = confirmText,
                )
            }
        },
	    onDismissRequest = onDismiss,
	    dismissContent = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = dismissText,
                )
            }
        },
	    confirmTimeoutMillis = confirmTimeoutMillis,
	    headerContentDividerVisible = headerContentDividerVisible,
	    content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Dialog(
    isVisible: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    headline: String? = null,
    supportingText: String? = null,
    confirmContent: @Composable (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    dismissContent: @Composable () -> Unit,
    confirmTimeoutMillis: Long? = null,
    headerContentDividerVisible: Boolean = false,
    content: @Composable (() -> Unit)? = null,
){
    var allowConfirmAfterTimeout by remember {
        mutableStateOf(confirmTimeoutMillis == null)
    }

    LaunchedEffect(isVisible, confirmTimeoutMillis) {
        if (confirmTimeoutMillis == null) {
            allowConfirmAfterTimeout = true
        } else {
            if (!allowConfirmAfterTimeout) {
                delay(confirmTimeoutMillis)
                allowConfirmAfterTimeout = true
            }
        }
    }

    if (!isVisible) return

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(dismissOnClickOutside = false),
        icon = icon,
        title = {
            headline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        text = {
            supportingText?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content?.let {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (headerContentDividerVisible) {
                                32.dp
                            } else {
                                0.dp
                            }
                        )
                ) {
                    if (headerContentDividerVisible) {
                        HorizontalDivider()
                        Spacer(Modifier.padding(vertical = 4.dp))
                        it.invoke()
                        Spacer(Modifier.padding(vertical = 4.dp))
                        HorizontalDivider()
                    } else {
                        it.invoke()
                    }
                }
            }
        },
        dismissButton = dismissContent,
        confirmButton = {
            confirmContent(allowConfirmAfterTimeout || confirmTimeoutMillis == null)
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun PreviewDialog(){
    NotificationDialog(
        isVisible = true,
        onDismiss = {

        },
        supportingText = "dialog text",
        headline = null,
        confirmTimeoutMillis = 2,
        content = {

        }
    )
}
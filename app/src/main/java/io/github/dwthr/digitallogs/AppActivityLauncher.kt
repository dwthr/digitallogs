package io.github.dwthr.digitallogs

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleCoroutineScope
import io.github.dwthr.digitallogs.logs.domain.repository.preferences.UserPreferencesState
import io.github.dwthr.digitallogs.transfer.ImportExportHandlerException
import io.github.dwthr.digitallogs.transfer.getExceptionDisplayText
import kotlinx.coroutines.launch

class AppActivityLauncher(
	activity: ComponentActivity,
	lifecycleCoroutineScope: LifecycleCoroutineScope,
	onGetPreferences: suspend () -> UserPreferencesState,
	onMicPermissionGranted: (Boolean) -> Unit
) {
	val importAppDataActivity = activity.registerForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { importURI ->

		importURI?.let {
			with(activity.applicationContext) {
				try {
					MyApp.appModule.importExportDataHandler.importData(importURI)
					Toast.makeText(
						this,
						getString(R.string.import_success),
						Toast.LENGTH_SHORT
					).show()
				} catch (e: Exception) {
					if (e !is ImportExportHandlerException.Import) throw e
					Toast.makeText(
						this,
						e.getExceptionDisplayText(),
						Toast.LENGTH_SHORT
					).show()
				}
			}
		}
	}

	val exportAppDataActivity = activity.registerForActivityResult(
		ActivityResultContracts.CreateDocument("application/octet-stream")
	) { exportURILocation ->

		exportURILocation?.let {
			lifecycleCoroutineScope.launch {
				with(activity.applicationContext) {
					try {
						MyApp.appModule.importExportDataHandler.exportData(
							exportContentUri = exportURILocation,
							preferencesStateExport = onGetPreferences(),
						)
						Toast.makeText(
							this,
							getString(R.string.backup_success),
							Toast.LENGTH_SHORT
						).show() //TODO: Use pending intent to show on app restart
					} catch (e: Exception) {
						if (e !is ImportExportHandlerException.Export) throw e
						Toast.makeText(
							this,
							e.getExceptionDisplayText(),
							Toast.LENGTH_SHORT
						).show()
					}
				}
			}
		}
	}

	val microphonePermissionRequester = activity.registerForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		callback = { isGranted -> onMicPermissionGranted(isGranted) }
	)

	//TODO: If single entry is audio/image/video entry then share instead of text
	fun shareText(text: String, appContext: Context) { //TODO: Find out if sharing entries individually are best
		val sendIntent = Intent(Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(Intent.EXTRA_TEXT, text)
		val shareIntent = Intent.createChooser(sendIntent, null)

//		if (Build.VERSION.SDK_INT >= 34) {
//			val customActions = arrayOf( //TODO: Implement: https://developer.android.com/training/sharing/send#adding-rich-content-previews
//				Builder(
//					Icon.createWithResource(appContext, R.drawable.baseline_save_24),
//					"Save to file",
//					PendingIntent.getBroadcast(
//						appContext,
//						1,
//						Intent(Intent.ACTION_VIEW),
//						PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
//					)
//				).build()
//			)
//			shareIntent.putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, customActions)
//		}
		appContext.startActivity(shareIntent)
	}

	//	val videoActivity = registerForActivityResult(
//		ActivityResultContracts.CaptureVideo()
//	) { captured ->
//
//	}
}
@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.dwthr.digitallogs

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import io.github.dwthr.digitallogs.common.presentation.theme.DigitalLogsTheme
import io.github.dwthr.digitallogs.logs.data.Preferences
import io.github.dwthr.digitallogs.logs.domain.serializers.AppPreferencesSerializer
import io.github.dwthr.digitallogs.logs.presentation.details.category.CategoryDetailsNavigation
import io.github.dwthr.digitallogs.logs.presentation.details.log.LogDetailsNavigation
import io.github.dwthr.digitallogs.logs.presentation.editor.LogEditorScreenRoot
import io.github.dwthr.digitallogs.logs.presentation.home.HomeScreenRoot
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.category.CategoryCreatorScreenRoot
import io.github.dwthr.digitallogs.logs.presentation.itemcreator.log.LogCreatorScreenRoot
import io.github.dwthr.digitallogs.logs.presentation.loglist.LogListScreenRoot
import io.github.dwthr.digitallogs.logs.presentation.loglist.RecentLogsListScreenRoot
import io.github.dwthr.digitallogs.logs.presentation.preferences.SettingsScreenRoot
import io.github.dwthr.digitallogs.proto.AppPreferences

//TODO : Read https://github.com/android/architecture-samples
//TODO : what is partition?
//TODO : Implement use cases? Read https://developer.android.com/topic/architecture - Talks about use cases. Also see: https://developer.android.com/topic/architecture/domain-layer
//TODO : Implement feature for video logs and bookmarking
//TODO: More internal visibility modifiers?
//TODO: Disable buttons/radio when animated visibility is false
//TODO: Allow pinning tags with content to home screen
//TODO: Minimize state passed to composables
//TODO: generic composable for scaffold/TopAppBar
//TODO: Read https://kotlinlang.org/docs/coding-conventions.html#class-layout
//TODO: https://developer.android.com/topic/architecture/recommendations#testing
//FIXME: TopAppBar "provide 1–2 essential actions"
// "If the product has many actions, place those in a toolbar. Avoid placing an overflow menu in the app bar when possible."
//TODO: Centralize button formatting declaration by type/use case?
//TODO: https://developer.android.com/develop/ui/compose/system/insets-ui
//TODO: Implement delete by tag capability
//FIXME: Double tapping nav item creates blank page and requires double back
//TODO: Move log editor to separate activity? (can also override onDestroy + onStop to save state. Lackner says onPause()?)
//TODO: Read: https://owasp.org/www-community/vulnerabilities/Deserialization_of_untrusted_data
//TODO: Sharing convert to text or json
//TODO: search between timestamped logs (ex: log entries between 01-01-2026 to 01-01-2027)
//TODO: catch migration db crash and offer to export data and report to developer
//FIXME: w: [ksp] ..AppDatabase.kt:37: Schema export directory was not provided to the annotation processor so Room cannot export the schema. You can either provide `room.schemaLocation` annotation processor argument by applying the Room Gradle plugin (id 'androidx.room') OR set exportSchema to false.
//FIXME: w: [ksp] ..NoteTagCrossRef.kt:27: The column tagId in the junction entity io.github.dwthr.digitallogs.logs.`data`.database.entities.NoteTagCrossRef is being used to resolve a relationship but it is not covered by any index. This might cause a full table scan when resolving the relationship, it is highly advised to create an index that covers this column.
//FIXME: w: [ksp] ..NoteDao.kt:32: The return value includes a data class with a @Relation. It is usually desired to annotate this function with @Transaction to avoid possibility of inconsistent results between the data class and its relations. See https://developer.android.com/reference/androidx/room/Transaction.html for details.
//TODO: Implement pagination
//TODO: Thread safety (not viewmodelScope)
//TODO: https://www.youtube.com/watch?v=CuWJBcOuNHI&t=237s
//FIXME?: warn when exiting screen when media is pressed: Handler (android.media.MediaCodec$EventHandler) {4af84d9} sending message to a Handler on a dead thread
//TODO: migrate deprecated functions (modal sheet, etc) at android.os.MessageQueue.logDeadThread(MessageQueue.java:381)
//TODO: Intent.ACTION_CREATE_NOTE?
/**TODO: Enforce for logging:
 * https://i.sstatic.net/z5Fim.png
 */
//TODO: Read https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0454-better-immutability-value-classes-MFVC.md
//TODO: Use coroutineContext.ensureActive()
//TODO: Interface for toasts
//TODO: Abstract away ids from domain-layer objects?
//TODO: Move all HorizontalFloatingToolbar to topAppBar floatingActionButton
//TODO: Read https://developer.android.com/guide/topics/large-screens/printing-file-management
//TODO: Implement experimental SheetScaffold?
//TODO: Show selection of log when editor is displayed on wide screens
//TODO: reorder animations
//TODO: Add more tests for rest of app including audio, search input chips, raw queries, and backups
//TODO: Class that decides screen background/card colors based on pane type?
//TODO: Better consolidate title validation
//TODO: Check new *ToggleButtonDefaults

val Context.dataStore: DataStore<AppPreferences> by dataStore( //TODO: Move elsewhere?
	fileName = Preferences.PREFS_NAME,
	serializer = AppPreferencesSerializer
)

class MainActivity : ComponentActivity() {

	private val mainActivityViewModel: MainActivityViewModel by viewModels<MainActivityViewModel>(
		factoryProducer = {
			viewModelFactory {
				initializer {
					MainActivityViewModel(MyApp.appModule.userPreferencesRepository)
				}
			}
		}
	)

	val appActivityLauncher = AppActivityLauncher(
		activity = this,
		lifecycleCoroutineScope = lifecycleScope,
		onGetPreferences = { mainActivityViewModel.getUserPreferences() },
		onMicPermissionGranted = { isGranted ->
			(mainActivityViewModel::micPermissionGranted)(isGranted)
		}
	)
	@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
	        val screenshotsAllowed by mainActivityViewModel.prefsScreenshotsAllowed.collectAsStateWithLifecycle()
	        val dialogCooldownEnabled by mainActivityViewModel.prefsDestructiveCooldownEnabled.collectAsStateWithLifecycle()

			LaunchedEffect(screenshotsAllowed) {
				if (screenshotsAllowed) {
					window.clearFlags(
						WindowManager.LayoutParams.FLAG_SECURE
					)
				} else {
					this@MainActivity.window?.setFlags(
						WindowManager.LayoutParams.FLAG_SECURE,
						WindowManager.LayoutParams.FLAG_SECURE
					)
				}

			}

            val rootBackStack = rememberNavBackStack(Routes.Home)


	        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
	        val dialogSceneStrategy = remember { DialogSceneStrategy<NavKey>() }
	        val detailPlaceholder: @Composable () -> Unit = {
		        Card(
			        colors = CardDefaults.cardColors(
				        containerColor = MaterialTheme.colorScheme.surfaceVariant
			        ),
			        modifier = Modifier
				        .fillMaxSize()
		        ) {
			        Box(
				        contentAlignment = Alignment.Center,
				        modifier = Modifier
					        .fillMaxSize()
					        .padding(8.dp)
			        ) {
				        Text(stringResource(R.string.select_a_log_placeholder))
			        }
		        }
	        }

	        fun NavBackStack<NavKey>.addDetail(route: DetailPaneRoute) {
		        rootBackStack.removeIf { it is DetailPaneRoute || it is CreatorRoute }
		        add(route)
	        }

	        fun navigateBack(dualPaneNavBack: Boolean = false) { //TODO: When API = 35, removeLast()
		        if (rootBackStack.size == 1) return

		        if (dualPaneNavBack) {
					rootBackStack.removeIf { it is DetailPaneRoute }
		        }

		        rootBackStack.removeIf { it is Routes.LogsList && it.clearOnNavBack }

		        when(rootBackStack.last()) {
			        is Routes.Home -> return
			        else -> rootBackStack.removeLastOrNull()
				}
			}

	        DigitalLogsTheme {
		        Surface(
			        color = MaterialTheme.colorScheme.surfaceContainer,
			        modifier = Modifier
				        .fillMaxSize()
		        ) {
			        SharedTransitionLayout {
				        NavDisplay(
					        backStack = rootBackStack, //TODO: Disallow duplicate screen by checking if previous entry is already last screen
					        entryDecorators = listOf(
						        rememberSaveableStateHolderNavEntryDecorator(),
						        rememberViewModelStoreNavEntryDecorator()
					        ),
					        onBack = {
								navigateBack()
					        },
					        transitionSpec = {
						        slideInHorizontally { it } togetherWith
								        slideOutHorizontally { -it }
					        },
					        popTransitionSpec = {
						        slideInHorizontally { -it } togetherWith
								        slideOutHorizontally { it }
					        },
					        predictivePopTransitionSpec = {
						        slideInHorizontally(tween(easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(easing = EaseInOutExpo), initialAlpha = 0.95f) togetherWith
								        slideOutHorizontally(tween(easing = FastOutSlowInEasing)) { it } + fadeOut(tween(easing = EaseInOutExpo), targetAlpha = 0.95f)
					        },
					        sharedTransitionScope = this,
					        sceneStrategies = listOf(listDetailStrategy, dialogSceneStrategy),
					        entryProvider = entryProvider {
						        entry<Routes.Home> { routeArgs ->
							        HomeScreenRoot(
								        onClickCreateCategory = { //TODO: container transform
									        rootBackStack.add(Routes.CategoryCreator) //TODO: When API = 35, addLast()
								        },
								        onNavigateToCategory = { clickedCategory ->
									        rootBackStack.add(Routes.LogsList(clickedCategory.id))
								        },
								        onLogClick = { clickedLog -> //TODO: container transform
									        rootBackStack.apply {
										        add( //This is so list screen displays in dual pane //TODO: Redesign?
											        Routes.LogsList(
												        categoryId = clickedLog.categoryId,
												        clearOnNavBack = true
											        )
										        )
										        addDetail(Routes.LogEditor(clickedLog.id))
									        }
								        },
								        onSettingsClick = { rootBackStack.add(Routes.Settings) },
								        onRecentShowAllClick = {
									        rootBackStack.add(Routes.RecentLogsList)
								        },
								        onCategoryDetailsClick = { categoryId ->
									        rootBackStack.add(
										        Routes.CategoryDetails(categoryId)
									        )
								        },
								        prefsDialogCooldownEnabled = dialogCooldownEnabled,
							        )
						        }
						        entry<Routes.LogCreator>(
							        metadata = DialogSceneStrategy.dialog()
						        ){ routeArgs ->
							        LogCreatorScreenRoot(
								        parentCategoryId = routeArgs.parentCategoryId,
								        onLogCreated = { newLogId ->
									        rootBackStack.addDetail(Routes.LogEditor(newLogId))
								        },
								        onCancel = { navigateBack() }
							        )
						        }
						        entry<Routes.CategoryCreator>(
									metadata = DialogSceneStrategy.dialog()
								) { routeArgs ->
							        CategoryCreatorScreenRoot(
								        onCategoryCreated = { newCategoryId ->
									        navigateBack()
									        rootBackStack.add(Routes.LogsList(newCategoryId))
								        },
								        onCancel = { navigateBack() }
							        )
						        }
						        entry<Routes.LogEditor>(
							        metadata = ListDetailSceneStrategy.detailPane()
						        ){ routeArgs ->
							        Card(
								        modifier = Modifier
									        .fillMaxSize()
							        ) {
								        LogEditorScreenRoot(
									        noteId = routeArgs.logId,
									        onBack = { navigateBack() },
									        onRequestRecordMic = {
										        appActivityLauncher.microphonePermissionRequester.launch(
											        Manifest.permission.RECORD_AUDIO
										        )

										        mainActivityViewModel.micPermissionResult to shouldShowRequestPermissionRationale(
											        Manifest.permission.RECORD_AUDIO
										        )
									        },
									        onNavigatePermissions = {
										        Intent(
											        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
											        Uri.fromParts("package", packageName, null)
										        ).also(::startActivity)
									        },
									        onShare = {
										        appActivityLauncher.shareText(it, this@MainActivity)
									        },
									        prefsDialogCooldownEnabled = dialogCooldownEnabled,
									        onDualPaneDeleted = {
												rootBackStack.removeIf { it is Routes.LogEditor }
									        },
								        )
							        }
						        }
						        entry<Routes.LogsList>(
							        metadata = ListDetailSceneStrategy.listPane(
								        detailPlaceholder = { detailPlaceholder() }
							        )
						        ) { routeArgs ->
							        Card(
								        modifier = Modifier
									        .fillMaxSize()
							        ) {
								        LogListScreenRoot(
									        parentCategoryId = routeArgs.categoryId,
									        onNavigateLog = { noteId ->
										        rootBackStack.addDetail(
											        Routes.LogEditor(noteId.id)
										        )
									        },
									        onLogAddClick = {
										        rootBackStack.add(
											        Routes.LogCreator(routeArgs.categoryId)
										        )
									        }, //TODO: container transform
									        onBack = { fromSinglePane ->
										        navigateBack(
											        dualPaneNavBack = !fromSinglePane
										        )
									        },
									        onDetailsClick = { noteId ->
										        rootBackStack.addDetail(Routes.LogDetails(noteId))
									        },
									        prefsDialogCooldownEnabled = dialogCooldownEnabled,
								        )
							        }
						        }
						        entry<Routes.RecentLogsList>(
							        metadata = ListDetailSceneStrategy.listPane(
								        detailPlaceholder = { detailPlaceholder() }
							        )
						        ) { routeArgs ->
							        RecentLogsListScreenRoot(
								        onNavigateLog = { noteId ->
									        rootBackStack.add(
										        Routes.LogEditor(noteId.id)
									        )
								        },
								        onBack = { fromSinglePane ->
									        navigateBack(
										        dualPaneNavBack = !fromSinglePane
									        )
								        },
								        onDetailsClick = { noteId ->
									        rootBackStack.add(
										        Routes.LogDetails(
											        noteId
										        )
									        )
								        },
								        prefsDialogCooldownEnabled = dialogCooldownEnabled,
							        )
						        }
						        entry<Routes.LogDetails>(
							        metadata = ListDetailSceneStrategy.detailPane()
						        ) { routeArgs ->
							        Card(
								        modifier = Modifier
									        .fillMaxSize()
							        ) {
								        LogDetailsNavigation(
									        logId = routeArgs.logId,
									        onBack = { navigateBack() },
									        prefsDialogCooldownEnabled = dialogCooldownEnabled
								        )
							        }
						        }
						        entry<Routes.CategoryDetails> { routeArgs ->
							        CategoryDetailsNavigation(
								        categoryId = routeArgs.categoryId,
								        onBack = { navigateBack() },
								        prefsDialogCooldownEnabled = dialogCooldownEnabled
							        )
						        }
						        entry<Routes.Settings> {
							        SettingsScreenRoot(
								        onBack = { navigateBack() },
								        onImportData = {
									        appActivityLauncher.importAppDataActivity.launch(
										        input = arrayOf("application/octet-stream")
									        )
									        //TODO: Ensure right kind of context
								        },
								        onExportData = {
									        appActivityLauncher.exportAppDataActivity.launch("Digital_Logs_Backup")
								        }
							        )
						        }
					        }
				        )
			        }
		        }
	        }
        }
    }
}
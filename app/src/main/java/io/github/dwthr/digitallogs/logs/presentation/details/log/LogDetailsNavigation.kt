package io.github.dwthr.digitallogs.logs.presentation.details.log

import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.Routes

@Composable
fun LogDetailsNavigation(
	logId: Long,
	modifier: Modifier = Modifier,
	prefsDialogCooldownEnabled: Boolean,
	viewModel: LogDetailsViewModel = viewModel {
		LogDetailsViewModel(
			repository = MyApp.appModule.localRepository,
			logId = logId
		)
	},
	onBack: () -> Unit,
){
	val state by viewModel.state.collectAsStateWithLifecycle()
	val logDetailsBackStack = rememberNavBackStack(Routes.LogDetails.ScreenLogDetails)

	NavDisplay(
		backStack = logDetailsBackStack,
		modifier = modifier,
		entryDecorators = listOf(
			rememberSaveableStateHolderNavEntryDecorator(),
			rememberViewModelStoreNavEntryDecorator()
		),
		onBack = {
			logDetailsBackStack.removeLastOrNull()
		}, //TODO: When API = 35, removeLast()
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
		entryProvider = entryProvider {
			entry<Routes.LogDetails.ScreenLogDetails> { routeArgs ->
				LogDetailsScreenRoot(
					state = state,
					onEvent = { event ->
						when (event) {
							LogDetailsEvent.OnBack -> onBack()
							else -> Unit
						}
						viewModel.onEvent(event)
					},
					prefsDialogCooldownEnabled = prefsDialogCooldownEnabled,
				)
			}
		}
	)
}
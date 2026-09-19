package io.github.dwthr.digitallogs.logs.presentation.details.category

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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.Routes

@Composable
fun CategoryDetailsNavigation(
	categoryId: Long,
	prefsDialogCooldownEnabled: Boolean,
	modifier: Modifier = Modifier,
	viewModel: CategoryDetailsViewModel = viewModel {
		CategoryDetailsViewModel(
			repository = MyApp.appModule.localRepository,
			categoryId = categoryId
		)
	},
	onBack: () -> Unit,
){
	val state by viewModel.state.collectAsStateWithLifecycle()
	val categoryDetailsBackStack = rememberNavBackStack(Routes.CategoryDetails.ScreenCategoryDetails)

	NavDisplay(
		backStack = categoryDetailsBackStack,
		modifier = modifier,
		onBack = {
			categoryDetailsBackStack.removeLastOrNull()
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
			entry<Routes.CategoryDetails.ScreenCategoryDetails> {
				CategoryDetailsScreenRoot(
					state = state,
					onEvent = { event ->
						when (event) {
							CategoryDetailsEvent.OnBack -> onBack()
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
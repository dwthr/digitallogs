package io.github.dwthr.digitallogs.logs.presentation.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dwthr.digitallogs.MyApp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.composables.ExpressiveNavBackButton
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceContainer
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceItem
import io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents.PreferenceItemSwitch

@Composable
fun SettingsScreenRoot(
	viewModel: SettingsViewModel = viewModel {
		SettingsViewModel(
			userPreferencesRepository = MyApp.appModule.userPreferencesRepository,
		)
	},
	onBack: () -> Unit,
	onImportData: () -> Unit,
	onExportData: () -> Unit
){
	val state by viewModel.state.collectAsStateWithLifecycle()

	SettingsScreen(
		state = state,
		onEvent = { event ->
			when(event) {
				SettingsEvent.OnBack -> onBack()
				SettingsEvent.OnExportData -> onExportData()
				SettingsEvent.OnImportData -> onImportData()
				else -> Unit
			}
			viewModel.onEvent(event)
		},
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
	state: SettingsScreenState,
	onEvent: (SettingsEvent) -> Unit
) {
	val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	Scaffold(
		topBar = {
			MediumFlexibleTopAppBar(
				navigationIcon = {
					ExpressiveNavBackButton { onEvent(SettingsEvent.OnBack) }
				},
				title = {
					Text(
						text = stringResource(R.string.settings),
						style = MaterialTheme.typography.headlineMedium
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceDim //TODO: app-wide base scaffold
				),
				scrollBehavior = topAppBarScrollBehavior
			)
		},
		containerColor = MaterialTheme.colorScheme.surfaceDim,
		modifier = Modifier
			.fillMaxSize()
			.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
	) { innerPadding ->

		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
		) {
			val preferencesState = state.preferencesLoadState as? PreferencesLoadState.Loaded
			if (preferencesState == null) {
				LinearProgressIndicator(
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxWidth()
				)
				return@Scaffold
			}

			PreferenceContainer(
				modifier = Modifier
			) {
				PreferenceItemSwitch(
					attribute = stringResource(R.string.allow_screenshots_pref_name),
					switchChecked = preferencesState.allowScreenshots,
					onCheckedChanged = { onEvent(SettingsEvent.OnAllowScreenshotsToggled(it)) }
				)
				PreferenceItemSwitch(
					attribute = stringResource(R.string.destructive_action_cooldown_pref_name),
					description = stringResource(R.string.destructive_action_cooldown_pref_desc),
					switchChecked = preferencesState.destructiveCooldownEnabled,
					onCheckedChanged = { onEvent(SettingsEvent.OnDestructiveCooldownToggled(it)) }
				)
			}

			PreferenceContainer(
				labelText = stringResource(R.string.data_backup_unencrypted_header),
				modifier = Modifier
			) {
				PreferenceItem(
					attribute = stringResource(R.string.backup_option_import),
					modifier = Modifier
						.clickable(
							onClick = {
								onEvent(SettingsEvent.OnImportData)
							}
						)
				)
				PreferenceItem(
					attribute = stringResource(R.string.backup_option_export),
					modifier = Modifier
						.clickable(
							onClick = {
								onEvent(SettingsEvent.OnExportData)
							}
						)
				)
			}
		}
	}
}

@Composable
@PreviewScreenSizes
fun Preview() {
	SettingsScreen(
		state = SettingsScreenState(
			preferencesLoadState = PreferencesLoadState.Loaded(
				allowScreenshots = true,
				destructiveCooldownEnabled = false
			)
		),
		onEvent = {

		}
	)
}
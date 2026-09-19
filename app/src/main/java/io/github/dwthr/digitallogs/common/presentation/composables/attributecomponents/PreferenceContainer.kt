package io.github.dwthr.digitallogs.common.presentation.composables.attributecomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R

/**
 * Mock UI implementation of [android.preference.Preference].
 * [PreferenceContainer] is meant to help mimic the grouped-list style composables found in the
 * Material 3 Expressive redesign of the settings page, since there do not seem to be any official
 * Jetpack Compose implementation.
 *
 * Navigation, toggleable settings with switches, progress bars, sliders, up to 3 lines, leading
 * & trailing icons or text appear to be within proper design. Optionally, use subtext to show the
 * current attribute value, describing the submenu, or other relevant information.
 *
 * Headers are optional. Single [PreferenceItem] groups are fine.
 *
 * When using both a [Switch] and a leading icon such as an arrow to
 * indicate a submenu, use a vertical divider.
**/
@ExperimentalMaterial3ExpressiveApi
@Composable
fun PreferenceContainer( //TODO: Use M3E segmented lists
	modifier: Modifier = Modifier,
	labelText: String? = null,
	horizontalMargin: Dp = 14.dp,
	content: @Composable ColumnScope.() -> Unit,
){
	Column(
		modifier = modifier.padding(horizontal = horizontalMargin)
	) {
		labelText?.let {
			Text(
				text = labelText,
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.titleSmall,
				modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
			)
		}
		Column(
			verticalArrangement = Arrangement.spacedBy(2.dp),
			modifier = Modifier
				.clip(MaterialTheme.shapes.largeIncreased)
		) {
			content(this)
		}
	}
}
/**
 * TODO: consider https://www.youtube.com/watch?v=MA6-ONDlXWU
 *  "I think it'd be more clear (and less error prone, as you found) if you extended an interface with a single composable invoke method, instead of extending the lambda signature. That way:
 *  1. the compose compiler can generate the overloads on the interface, because the composable annotation is already on there
 *  2. it's easier to understand at a glance for someone coming into the project
 *  Just because people didn't know you could extend a lambda and it's cool that you have the freedom to do it, doesn't mean you should"
 *  3. Reply:
 *  "Hey, thanks for commenting! Yeah, actually - my first stab at this was to use an interface like `interface ToolbarButton : @Composable () -> Unit`, but this caused the same runtime exception as the abstract class approach, which is how I ended up on the concrete classes. By changing to `interface ToolbarButton { @Composable operator fun invoke() }`, though, it does work, so I'd prefer that, for sure. 🎉
 *  And, of course - "unexpected" and "cool" aren't quality attributes that matter in most software projects, which is why I didn't present the solution as a recommendation, but as a call for feedback."
*/

enum class PreferenceDisplayType {
	MENU,
	DISPLAY,
	EDITABLE
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreferenceItemSubmenuSwitch( //TODO: Have onclick
	attribute: String,
	modifier: Modifier = Modifier,
	description: String? = null,
	leadingContent: @Composable (BoxScope.() -> Unit)? = null,
	switchChecked: Boolean,
	onCheckedChanged: (Boolean) -> Unit,
	disabled: Boolean = false
){
	PreferenceItem( //TODO: Ensure only allowed in container?
		attribute = attribute,
		modifier = modifier,
		description = description,
		leadingContent = leadingContent,
		trailingContent = {
			Icon(
				painterResource(R.drawable.baseline_keyboard_arrow_right_24),
				null,
				modifier = Modifier
					.size(IconButtonDefaults.largeIconSize)
					.alpha(0.38f)
			)
			VerticalDivider(Modifier.padding(horizontal = 2.dp))
			Spacer(Modifier.size(12.dp))
			Switch(
				checked = switchChecked,
				onCheckedChange = onCheckedChanged,
				thumbContent = {
					Icon(
						painter = if (switchChecked) {
							painterResource(R.drawable.baseline_check_24)
						} else {
							painterResource(R.drawable.baseline_close_24)
						},
						null,
						modifier = Modifier
							.size(SwitchDefaults.IconSize)
					)
				},
				enabled = !disabled
			)
		},
		disabled = disabled
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreferenceItemSwitch( //TODO: Have onclick
	attribute: String,
	modifier: Modifier = Modifier,
	description: String? = null,
	leadingContent: @Composable (BoxScope.() -> Unit)? = null,
	switchChecked: Boolean,
	onCheckedChanged: (Boolean) -> Unit,
	disabled: Boolean = false
){
	PreferenceItem(
		attribute = attribute,
		modifier = modifier
			.clickable(
				enabled = !disabled,
				onClick = { onCheckedChanged(!switchChecked) }
			), //TODO: Change?
		description = description,
		leadingContent = leadingContent,
		trailingContent = {
			Spacer(Modifier.size(12.dp))
			Switch(
				checked = switchChecked,
				onCheckedChange = { onCheckedChanged(it) },
				thumbContent = {
					Icon(
						painter = if (switchChecked) {
							painterResource(R.drawable.baseline_check_24)
						} else {
							painterResource(R.drawable.baseline_close_24)
						},
						null,
						modifier = Modifier
							.size(SwitchDefaults.IconSize)
					)
				},
				enabled = !disabled
			)
		},
		disabled = disabled
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreferenceItemSubmenu(
	attribute: String,
	modifier: Modifier = Modifier,
	description: String? = null,
	leadingContent: @Composable (BoxScope.() -> Unit)? = null,
	onClick: () -> Unit,
	onClickLabel: String? = null,
	role: Role? = null,
	interactionSource: MutableInteractionSource? = null,
	disabled: Boolean = false
){
	PreferenceItem(
		attribute = attribute,
		modifier = modifier
			.clickable(
				interactionSource = interactionSource,
				enabled = !disabled,
				onClickLabel = onClickLabel,
				role = role,
				onClick = onClick
			),
		description = description,
		leadingContent = leadingContent,
		trailingContent = {
			Icon(
				painterResource(R.drawable.baseline_keyboard_arrow_right_24),
				null,
				modifier = Modifier
					.size(IconButtonDefaults.largeIconSize)
					.alpha(0.38f)
			)
		},
		disabled = disabled
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreferenceItem(
	attribute: String,
	modifier: Modifier = Modifier,
	description: String? = null,
	containerColor: Color = MaterialTheme.colorScheme.surfaceBright,
	leadingContent: @Composable (BoxScope.() -> Unit)? = null,
	trailingContent: @Composable (BoxScope.() -> Unit)? = null,
	disabled: Boolean = false
){
	Row(
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = modifier
			.fillMaxWidth()
			.height(IntrinsicSize.Min)
			.background(
				color = containerColor,
				shape = MaterialTheme.shapes.extraSmall
			)
			.padding(14.dp)
			.padding(horizontal = 2.dp)
	) {
		val rowSpacing by remember { mutableStateOf(12.dp) }
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.wrapContentSize(Alignment.Center)
		){
			leadingContent?.invoke(this)
		}
		Spacer(Modifier.size(rowSpacing))
		Box(
			contentAlignment = Alignment.CenterStart,
			modifier = Modifier
				.fillMaxWidth()
				.defaultMinSize(minHeight = 48.dp)
				.wrapContentHeight()
				.weight(1f)
		) {
			Column(
				verticalArrangement = Arrangement.Center,
				modifier = Modifier.wrapContentHeight()
			) {
				Text(
					text = attribute,
					style = MaterialTheme.typography.titleMedium,
					modifier = if (disabled) {
						Modifier.alpha(0.38f)
					} else {
						Modifier
					}
				)
				description?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = if (disabled) {
							Modifier.alpha(0.38f)
						} else {
							Modifier
						}
					)
				}
			}
		}
		Spacer(Modifier.size(rowSpacing))
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.wrapContentSize(Alignment.Center)
		){
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier
					.fillMaxHeight()
					.wrapContentWidth()
			) {
				trailingContent?.invoke(this@Box)
			}
		}
//		when (type) {
//			PreferenceDisplayType.MENU -> TODO()
//			PreferenceDisplayType.DISPLAY -> null
//			PreferenceDisplayType.EDITABLE -> {
//				Column(
//					verticalArrangement = Arrangement.Center
//				) {
//					Icon(
//						painter = painterResource(R.drawable.baseline_edit_24),
//						contentDescription = "Edit item"
//					)
//				}
//			}
//		}
	}
}
//	colors = ListItemDefaults.colors().copy(
//			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
//			headlineColor = MaterialTheme.colorScheme.onSurface,
//			leadingIconColor = MaterialTheme.colorScheme.primary,
////			overlineColor = TODO(),
//			supportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//			trailingIconColor = MaterialTheme.colorScheme.primary,
//			disabledHeadlineColor = MaterialTheme.colorScheme.onSurfaceVariant,
//			disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//			//disabledTrailingIconColor = TODO()
//		),
//	)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun PreviewComposable(){
	PreferenceContainer {
		PreferenceItem(
			attribute = "Attr Name",
			description = "Attr Value",
			leadingContent = {
				val color = MaterialTheme.colorScheme.tertiary
				Icon(
					painterResource(R.drawable.baseline_search_24),
					null,
					tint = color.copy(alpha = 0.5f, color.red-60,color.green-60,color.blue-60),
					modifier = Modifier
						.clip(CircleShape)
						.background(color)
						.padding(8.dp)
				)
			},
			trailingContent = {
				Icon(
					painterResource(R.drawable.baseline_edit_24),
					null
				)
			}
		)
		PreferenceItemSubmenu(
			attribute = "Attr Name 2",
			description = "Attr Value",
			leadingContent = {
				val color = Color.Cyan
				Icon(
					painterResource(R.drawable.baseline_settings_24),
					null,
					tint = color.copy(
						alpha = 0.5f,
						color.red - 60,
						color.green - 60,
						color.blue - 60
					),
					modifier = Modifier
						.clip(CircleShape)
						.background(color)
						.padding(8.dp)
				)
			},
			onClick = { },
		)
		PreferenceItemSubmenuSwitch(
			attribute = "Attr Name 3",
			description = "Attr Value",
			switchChecked = true,
			onCheckedChanged = {  },
		)
		PreferenceItemSwitch(
			attribute = "Attr Name 4",
			description = "Attr Value",
			switchChecked = true,
			onCheckedChanged = {  },
		)
	}
}
package io.github.dwthr.digitallogs.logs.presentation.home.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.InputFieldHeight
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.SearchBarDefaults.inputFieldShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A text field to input a query in a search bar.
 *
 * This overload of [ChipSearchInputField] takes a [query] and [onQueryChange] callback to keep track of
 * the text content. Consider using the overload which takes a [TextFieldState] instead.
 *
 * @param query the query text to be shown in the input field.
 * @param onQueryChange the callback to be invoked when the input service updates the query. An
 *   updated text comes as a parameter of the callback.
 * @param onSearch the callback to be invoked when the input service triggers the
 *   [ImeAction.Search] action. The current [query] comes as a parameter of the callback.
 * @param expanded whether the search bar is expanded and showing search results.
 * @param onExpandedChange the callback to be invoked when the search bar's expanded state is
 *   changed.
 * @param modifier the [Modifier] to be applied to this input field.
 * @param enabled the enabled state of this input field. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param placeholder the placeholder to be displayed when the [query] is empty.
 * @param leadingContent the leading icon to be displayed at the start of the input field.
 * @param trailingIcon the trailing icon to be displayed at the end of the input field.
 * @param colors [TextFieldColors] that will be used to resolve the colors used for this input
 *   field in different states. See [SearchBarDefaults.inputFieldColors].
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this input field. You can use this to change the search bar's
 *   appearance or preview the search bar in different states. Log that if `null` is provided,
 *   interactions will still happen internally.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@ExperimentalMaterial3Api
@Composable
fun ChipSearchInputField( //TODO: Focus on end when tapped
	query: TextFieldValue,
	onQueryChange: (TextFieldValue) -> Unit,
	searchFieldChips: List<String>,
	queryChips: List<String>,
	searchFieldChipSelectedIndex: Int?,
	expanded: Boolean,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
	keyboardActions: KeyboardActions,
	placeholder: @Composable (() -> Unit)? = null,
	leadingContent: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	colors: TextFieldColors = inputFieldColors(),
	interactionSource: MutableInteractionSource? = null,
) {
	@Suppress("NAME_SHADOWING")
	val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

	val focused = interactionSource.collectIsFocusedAsState().value
	val focusRequester = remember { FocusRequester() }
	val focusManager = LocalFocusManager.current

//	val searchSemantics = getString(Strings.SearchBarSearch)
//	val suggestionsAvailableSemantics = getString(Strings.SuggestionsAvailable)

	val scrollState = rememberScrollState()
	LaunchedEffect(searchFieldChips) {//Smooth scroll when possible
		scrollState.animateScrollTo(scrollState.maxValue)
	}
	LaunchedEffect(query) { //Fast scroll text for usability
		scrollState.scrollTo(scrollState.maxValue)
	}

	val textColor =
		LocalTextStyle.current.color.takeOrElse {
			colors.textColor(enabled, isError = false, focused = focused)
		}
	fun Modifier.textFieldBackground(color: ColorProducer, shape: Shape): Modifier =
		this.drawWithCache {
			val outline = shape.createOutline(size, layoutDirection, this)
			onDrawBehind { drawOutline(outline, color = color()) }
		}

	val decorationBoxValue by remember(expanded, query, searchFieldChips, queryChips) {
		derivedStateOf {
			if (expanded) {
				query.text.ifEmpty {
					if (searchFieldChips.isEmpty() && queryChips.isEmpty() && query.text.isEmpty()) ""
					else " "
				}
			} else {
				query.text
			}
		}
	}

	BasicTextField(
		value = query,
		onValueChange = onQueryChange,
		modifier =
			modifier
				.sizeIn(
					minWidth = 360.dp,
					maxWidth = 720.dp,
					minHeight = InputFieldHeight,
				)
				.focusRequester(focusRequester),
//				.semantics {
//					contentDescription = searchSemantics
//					if (expanded) {
//						stateDescription = suggestionsAvailableSemantics
//					}
//				},
		enabled = enabled,
		singleLine = true,
		textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor)),
		cursorBrush = SolidColor(colors.cursorColor(isError = false)),
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		interactionSource = interactionSource,
		decorationBox =
			@Composable { innerTextField ->
				TextFieldDefaults.DecorationBox(
					value = decorationBoxValue,
					innerTextField = {
						Column {
							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(4.dp),
								modifier = Modifier.horizontalScroll(scrollState)
							) {
								if (expanded) {
									queryChips.forEachIndexed { index, chip ->
										key(chip) {
											InputChip(
												selected = index == searchFieldChipSelectedIndex,
												onClick = { },
												label = { Text(chip) },
											)
										}
									}
									searchFieldChips.forEachIndexed { index, string ->
										key(string) {
											InputChip(
												selected = true,
												onClick = { },
												label = { Text(string) },
											)
										}
									}
//									key(inputType) {
//										InputChip(
//											selected = query.isEmpty(),
//											onClick = { },
//											label = { Text(inputType.toString()) }
//										)
//									}
								} else {
									//TODO: Show query result
								}
								Box {
									innerTextField()
								}
							}
						}
					},
					enabled = enabled,
					singleLine = true,
					visualTransformation = VisualTransformation.None,
					interactionSource = interactionSource,
					placeholder = placeholder,
					leadingIcon =
						leadingContent?.let { leading ->
							{ Box(Modifier.offset(x = 4.dp)) { leading() } }
						},
					trailingIcon =
						trailingIcon?.let { trailing ->
							{ Box(Modifier.offset(x = (-4).dp)) { trailing() } }
						},
					shape = inputFieldShape,
					colors = colors,
					contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
					container = {
						val containerColor =
							animateColorAsState(
								targetValue =
									colors.containerColor(
										enabled = enabled,
										isError = false,
										focused = focused,
									),
								animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
							)
						Box(
							Modifier
								.textFieldBackground(containerColor::value, inputFieldShape)
						)
					},
				)
			},
	)

	val shouldClearFocus = !expanded && focused
	LaunchedEffect(expanded) {
		if (shouldClearFocus) {
			// Not strictly needed according to the motion spec, but since the animation
			// already has a delay, this works around b/261632544.
			delay(100)
			focusManager.clearFocus()
		}
	}
}
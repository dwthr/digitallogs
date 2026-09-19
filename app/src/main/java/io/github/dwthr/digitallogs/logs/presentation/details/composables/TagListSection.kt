package io.github.dwthr.digitallogs.logs.presentation.details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.R
import io.github.dwthr.digitallogs.common.presentation.theme.Typography
import io.github.dwthr.digitallogs.logs.domain.Tag
import io.github.dwthr.digitallogs.logs.presentation.loglist.composables.ItemTagListSheet

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagListSection(
	onTagAddClicked: () -> Unit,
	onTagDeleteClicked: (Tag) -> Unit,
	tagsInput: Set<Tag>
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = 64.dp)
			.padding(horizontal = 20.dp)
	) {
		ItemTagListSheet(
			onTagDeleteClicked = onTagDeleteClicked,
			tagsInput = tagsInput,
			header = {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = stringResource(R.string.tags_section_label),
						style = Typography.titleMedium
					)
					TextButton(
						onClick = onTagAddClicked,
						shapes = ButtonDefaults.shapes(),
						contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
					) {
						Icon(
							painterResource(R.drawable.baseline_add_24),
							null,
							modifier = Modifier.size(ButtonDefaults.IconSize)
						)
						Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
						Text(
							text = stringResource(R.string.add_tag),
						)
					}
				}
			}
		)
	}
}
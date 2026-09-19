package io.github.dwthr.digitallogs.logs.presentation.home.composables

//@Composable
//fun ItemTagEditorSheet( //TODO: Have multi select checkbox w/ filter
//	onClick: () -> Unit,
//	modifier: Modifier = Modifier
//){
//	Card(
//		colors = CardDefaults.cardColors(
//			containerColor = MaterialTheme.colorScheme.surfaceVariant
//		),
//		modifier = modifier
//			.clickable(onClick = { onClick })
////			.dashedBorder(3.dp, MaterialTheme.colorScheme.inverseSurface, 12.dp)
//	) {
//		Column(
//			horizontalAlignment = Alignment.CenterHorizontally,
//			verticalArrangement = Arrangement.spacedBy((-12).dp),
//			modifier = Modifier
//				.padding(vertical = 8.dp, horizontal = 12.dp)
//				.fillMaxWidth()
//				.fillMaxHeight(0.5f)
//		) {
//			ItemTagListSheet(
//				tagsInput = setOf(Tag(label = "fds", id = 0)),
//				header = { Text("Tags") },
//				onTagDeleteClicked = {},
//			)
//			HorizontalDivider()
//		}
//	}
//}
//
//@Preview
//@Composable
//fun PreviewItemTagEditorSheet(){
//	ItemTagEditorSheet(
//		{}
//	)
//}
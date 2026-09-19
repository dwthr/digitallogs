package io.github.dwthr.digitallogs.logs.presentation.home.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dwthr.digitallogs.logs.domain.Category
import io.github.dwthr.digitallogs.logs.presentation.home.HomeEvent

@Composable
fun CategoriesSectionContent(
    categories: List<Category>,
    selectedCategories: Set<Category>,
    innerPaddingHorizontal: PaddingValues,
    bottomPadding: Dp,
    screenVerticalPadding: Dp,
    onEvent: (HomeEvent) -> Unit,
) {
    val categoriesListState = rememberLazyListState()


//                        var dragStartIndex by remember { mutableStateOf<Int?>(null) }
//                        var dragHoverIndex by remember { mutableStateOf<Int?>(null) }
//
//                        val minDragIndex by remember {
//                            derivedStateOf {
//	                            dragStartIndex?.let { start ->
//	                                dragHoverIndex?.let { hover ->
//	                                    min(start, hover)
//	                                }
//	                            }
//                            }
//                        }
//                        val maxDragIndex by remember {
//                            derivedStateOf {
//	                            dragStartIndex?.let { start ->
//	                                dragHoverIndex?.let { hover ->
//	                                    max(start, hover)
//	                                }
//	                            }
//                            }
//                        }
//                        val categoriesListState = rememberLazyListState()

    val windowInfo = LocalWindowInfo.current
    val viewportSize = windowInfo.containerDpSize.height - screenVerticalPadding


    LazyColumn(
        state = categoriesListState,
        contentPadding = PaddingValues(bottom = bottomPadding) + innerPaddingHorizontal,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = Modifier
            .heightIn(max = viewportSize)
            .padding(horizontal = 8.dp)
//                            .nestedScroll(nestedScrollConnection)
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = category in selectedCategories
            SegmentedListItem(
                selected = isSelected,
//                                    selected = isSelected xor (
//                                        minDragIndex?.let { dragStartIndex ->
//                                            maxDragIndex?.let { dragCurrentIndex ->
//                                                index in dragStartIndex..dragCurrentIndex
//                                            }
//                                        } ?: false
//                                    ),
                shapes = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = categories.size
                ),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                ),
                onClick = { onEvent(HomeEvent.OnCategoryClick(category)) },
                onLongClick = {
                    onEvent(HomeEvent.OnListItemSelectedChanged(index))//TODO: implement multi-select with combined clickable modifier
                },
//                                    trailingContent = if (dragStartIndex == index) { {
//                                        Icon(
//                                            painter = painterResource(R.drawable.baseline_check_24),
//                                            contentDescription = null
//                                        )
//                                    } } else null,
                content = {
                    Text(
                        text = category.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                modifier = Modifier
//                                        .dragAndDropSource(
//                                            drawDragDecoration = {},
//                                            block = {
//                                                detectDragGestures(
//	                                                onDrag = { _, _ ->
//		                                                dragStartIndex = index
//                                                        dragHoverIndex = index
//
//                                                        startTransfer(
//                                                            DragAndDropTransferData(
//                                                                ClipData.newPlainText("","")
//                                                            )
//                                                        )
//	                                                },
//                                                )
//                                            }
//                                        )
//                                        .dragAndDropTarget(
//                                            shouldStartDragAndDrop = { true },
//                                            target = remember {
//                                                object : DragAndDropTarget {
//                                                    override fun onDrop(event: DragAndDropEvent): Boolean {
//                                                        println("onDrop: $dragStartIndex -> $dragHoverIndex")
//                                                        minDragIndex?.let { startIndex ->
//                                                            maxDragIndex?.let { endIndex ->
//                                                                onEvent(HomeEvent.OnListItemSelectedRange(startIndex, endIndex))
//                                                            }
//                                                        }
//                                                        dragStartIndex = null
//                                                        dragHoverIndex = null
//                                                        return true
//                                                    }
//
//                                                    override fun onEntered(event: DragAndDropEvent) {
//                                                        dragHoverIndex = index
//                                                    }
//
//                                                    override fun onEnded(event: DragAndDropEvent) {
//                                                        dragStartIndex = null
//                                                        dragHoverIndex = null
//                                                    }
//                                                }
//                                            }
//                                        )
            )
        }
    }
}
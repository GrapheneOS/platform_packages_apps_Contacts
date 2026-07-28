package com.android.contacts.ui.core

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/*
 * Shape for LazyColumn items that:
 * - rounds the top corners of the first item
 * - rounds the bottom corners of the last item
 * - rounds all corners of a selected item
 */
@Composable
internal fun itemClipShape(
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean = false,
    shape: CornerBasedShape = MaterialTheme.shapes.extraSmall,
): Shape {
    // All corners are the same in our shapes, so let's just pick a random one
    val cornerSize = shape.topStart
    val defaultCornerSize = CornerSize(2.dp)
    val topCornerSize = when {
        isFirst || isSelected -> cornerSize
        else -> defaultCornerSize
    }
    val bottomCornerSize = when {
        isLast || isSelected -> cornerSize
        else -> defaultCornerSize
    }
    return RoundedCornerShape(
        topStart = topCornerSize,
        topEnd = topCornerSize,
        bottomStart = bottomCornerSize,
        bottomEnd = bottomCornerSize,
    )
}

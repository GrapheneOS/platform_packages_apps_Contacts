package com.android.contacts.ui.common.components

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val InnerCornerSize = CornerSize(2.dp)

@Composable
internal fun cellShape(
    isFirst: Boolean,
    isLast: Boolean,
): Shape {
    val outerCornerSize = MaterialTheme.shapes.extraSmall.topStart

    return RoundedCornerShape(
        topStart = if (isFirst) outerCornerSize else InnerCornerSize,
        topEnd = if (isFirst) outerCornerSize else InnerCornerSize,
        bottomStart = if (isLast) outerCornerSize else InnerCornerSize,
        bottomEnd = if (isLast) outerCornerSize else InnerCornerSize,
    )
}

package com.android.contacts.data.photo.model

import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal data class SquareCrop(
    val targetWidth: Int,
    val targetHeight: Int,
    val crop: Rect,
) {
    companion object {
        fun of(width: Int, height: Int, maxSize: Int, upscale: Boolean = false): SquareCrop {
            require(width > 0 && height > 0) { "Invalid image size: ${width}x$height" }
            require(maxSize > 0) { "Invalid max size: $maxSize" }

            val shortSide = min(width, height)
            val side = if (upscale) maxSize else min(maxSize, shortSide)
            val scale = side.toFloat() / shortSide
            val targetWidth = max(side, (width * scale).roundToInt())
            val targetHeight = max(side, (height * scale).roundToInt())
            val left = (targetWidth - side) / 2
            val top = (targetHeight - side) / 2
            return SquareCrop(
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                crop = Rect(left, top, left + side, top + side),
            )
        }
    }
}

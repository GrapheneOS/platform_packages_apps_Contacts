package com.android.contacts.data.photo.model

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SquareCropTest {

    @Test
    fun landscape_scalesShortSideAndCropsHorizontalCenter() {
        assertEquals(
            SquareCrop(targetWidth = 960, targetHeight = 720, crop = Rect(120, 0, 840, 720)),
            SquareCrop.of(width = 4000, height = 3000, maxSize = 720),
        )
    }

    @Test
    fun portrait_scalesShortSideAndCropsVerticalCenter() {
        assertEquals(
            SquareCrop(targetWidth = 720, targetHeight = 1280, crop = Rect(0, 280, 720, 1000)),
            SquareCrop.of(width = 1080, height = 1920, maxSize = 720),
        )
    }

    @Test
    fun square_scalesWithoutCropping() {
        assertEquals(
            SquareCrop(targetWidth = 720, targetHeight = 720, crop = Rect(0, 0, 720, 720)),
            SquareCrop.of(width = 2000, height = 2000, maxSize = 720),
        )
    }

    @Test
    fun smallerThanMaxSize_withUpscale_scalesUpAndCropsCenter() {
        assertEquals(
            SquareCrop(targetWidth = 144, targetHeight = 96, crop = Rect(24, 0, 120, 96)),
            SquareCrop.of(width = 60, height = 40, maxSize = 96, upscale = true),
        )
    }

    @Test
    fun smallerThanMaxSize_cropsWithoutUpscaling() {
        assertEquals(
            SquareCrop(targetWidth = 400, targetHeight = 300, crop = Rect(50, 0, 350, 300)),
            SquareCrop.of(width = 400, height = 300, maxSize = 720),
        )
    }

    @Test
    fun shortSideEqualToMaxSize_onlyCrops() {
        assertEquals(
            SquareCrop(targetWidth = 1000, targetHeight = 720, crop = Rect(140, 0, 860, 720)),
            SquareCrop.of(width = 1000, height = 720, maxSize = 720),
        )
    }

    @Test
    fun oddLongSideRemainder_cropStaysWithinTarget() {
        val result = SquareCrop.of(width = 721, height = 100, maxSize = 100)

        assertEquals(Rect(310, 0, 410, 100), result.crop)
        assertEquals(721, result.targetWidth)
    }

    @Test
    fun extremeAspectRatio_keepsSquareSide() {
        val result = SquareCrop.of(width = 10000, height = 3, maxSize = 720)

        assertEquals(3, result.crop.width())
        assertEquals(3, result.crop.height())
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidImageSize_throws() {
        SquareCrop.of(width = 0, height = 100, maxSize = 720)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMaxSize_throws() {
        SquareCrop.of(width = 100, height = 100, maxSize = 0)
    }
}

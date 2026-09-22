package com.android.contacts.ui.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private const val DARK_THEME_LUMINANCE_THRESHOLD = 0.5f
private const val FULL_HUE_CIRCLE_DEGREES = 360f
private const val GOLDEN_ANGLE_DEGREES = 137.508f
private const val FNV_OFFSET_BASIS = -0x7ee3623b
private const val FNV_PRIME = 0x01000193

private const val LIGHT_THEME_AVATAR_BACKGROUND_SATURATION = 0.58f
private const val LIGHT_THEME_AVATAR_BACKGROUND_LIGHTNESS = 0.82f
private const val LIGHT_THEME_AVATAR_CONTENT_SATURATION = 0.82f
private const val LIGHT_THEME_AVATAR_CONTENT_LIGHTNESS = 0.22f

private const val DARK_THEME_AVATAR_BACKGROUND_SATURATION = 0.48f
private const val DARK_THEME_AVATAR_BACKGROUND_LIGHTNESS = 0.30f
private const val DARK_THEME_AVATAR_CONTENT_SATURATION = 0.70f
private const val DARK_THEME_AVATAR_CONTENT_LIGHTNESS = 0.88f

internal data class ContactAvatarFallbackColors(
    val background: Color,
    val content: Color,
)

@Composable
internal fun resolvedFallbackColors(
    colorSeed: String?,
    isSelected: Boolean,
): ContactAvatarFallbackColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < DARK_THEME_LUMINANCE_THRESHOLD

    return when {
        isSelected -> ContactAvatarFallbackColors(
            background = colorScheme.primary,
            content = colorScheme.onPrimary,
        )

        colorSeed.isNullOrBlank() -> ContactAvatarFallbackColors(
            background = colorScheme.primaryContainer,
            content = colorScheme.onPrimaryContainer,
        )

        else -> remember(colorSeed, isDarkTheme) {
            contactAvatarFallbackColors(
                colorSeed = colorSeed,
                isDarkTheme = isDarkTheme,
            )
        }
    }
}

internal fun contactAvatarFallbackColors(
    colorSeed: String,
    isDarkTheme: Boolean,
): ContactAvatarFallbackColors {
    val hue = contactAvatarHue(colorSeed = colorSeed)

    return when {
        isDarkTheme -> ContactAvatarFallbackColors(
            background = Color.hsl(
                hue = hue,
                saturation = DARK_THEME_AVATAR_BACKGROUND_SATURATION,
                lightness = DARK_THEME_AVATAR_BACKGROUND_LIGHTNESS,
            ),
            content = Color.hsl(
                hue = hue,
                saturation = DARK_THEME_AVATAR_CONTENT_SATURATION,
                lightness = DARK_THEME_AVATAR_CONTENT_LIGHTNESS,
            ),
        )

        else -> ContactAvatarFallbackColors(
            background = Color.hsl(
                hue = hue,
                saturation = LIGHT_THEME_AVATAR_BACKGROUND_SATURATION,
                lightness = LIGHT_THEME_AVATAR_BACKGROUND_LIGHTNESS,
            ),
            content = Color.hsl(
                hue = hue,
                saturation = LIGHT_THEME_AVATAR_CONTENT_SATURATION,
                lightness = LIGHT_THEME_AVATAR_CONTENT_LIGHTNESS,
            ),
        )
    }
}

private fun contactAvatarHue(colorSeed: String): Float {
    val positiveHash = colorSeed.stableHashCode() and Int.MAX_VALUE

    return (positiveHash * GOLDEN_ANGLE_DEGREES) % FULL_HUE_CIRCLE_DEGREES
}

private fun String.stableHashCode(): Int {
    var hash = FNV_OFFSET_BASIS

    forEach { character ->
        hash = hash xor character.code
        hash *= FNV_PRIME
    }

    return hash
}

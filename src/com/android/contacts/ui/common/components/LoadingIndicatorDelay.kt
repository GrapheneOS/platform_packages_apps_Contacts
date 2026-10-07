package com.android.contacts.ui.common.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

// If content loads faster than this never flash a loading UI.
internal val LOADING_INDICATOR_DELAY = 500.milliseconds

@Composable
internal fun rememberIsLoadingIndicatorVisible(isLoading: Boolean): Boolean {
    var isVisible by remember(isLoading) { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(LOADING_INDICATOR_DELAY)
            isVisible = true
        }
    }

    return isVisible
}

@Composable
internal fun Modifier.delayedIndicator(): Modifier {
    val isVisible = rememberIsLoadingIndicatorVisible(true)
    val alpha by animateFloatAsState(if (isVisible) 1f else 0f)
    return this then Modifier.graphicsLayer { this.alpha = alpha }
}

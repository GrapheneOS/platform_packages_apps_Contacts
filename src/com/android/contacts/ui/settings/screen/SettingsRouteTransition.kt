package com.android.contacts.ui.settings.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.android.contacts.ui.settings.screen.model.SettingsNavRoute

private const val SLIDE_OFFSET_DIVISOR = 3

internal fun AnimatedContentTransitionScope<SettingsNavRoute>.routeTransition(): ContentTransform {
    val isGoingDeeper = targetState.depth > initialState.depth
    val slideTowards = when {
        isGoingDeeper -> SlideDirection.Start
        else -> SlideDirection.End
    }

    val enter = slideIntoContainer(slideTowards) { it / SLIDE_OFFSET_DIVISOR } + fadeIn()
    val exit = slideOutOfContainer(slideTowards) { it / SLIDE_OFFSET_DIVISOR } + fadeOut()

    return enter togetherWith exit
}

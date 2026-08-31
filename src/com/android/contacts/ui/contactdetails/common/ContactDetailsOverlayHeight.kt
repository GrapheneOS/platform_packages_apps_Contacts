package com.android.contacts.ui.contactdetails.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged

private const val UNMEASURED = -1

@Stable
internal class ContactDetailsOverlayHeight {
    var pixels: Int by mutableIntStateOf(UNMEASURED)

    val isMeasured: Boolean
        get() {
            return pixels != UNMEASURED
        }
}

@Composable
internal fun rememberOverlayHeight(): ContactDetailsOverlayHeight {
    return remember { ContactDetailsOverlayHeight() }
}

internal fun Modifier.measuredInto(overlayHeight: ContactDetailsOverlayHeight): Modifier {
    return onSizeChanged { size ->
        overlayHeight.pixels = size.height
    }
}

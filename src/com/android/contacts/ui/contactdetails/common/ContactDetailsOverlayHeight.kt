package com.android.contacts.ui.contactdetails.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified

@Stable
internal class ContactDetailsOverlayHeight {
    var value: Dp by mutableStateOf(Dp.Unspecified)

    val isMeasured: Boolean
        get() {
            return value.isSpecified
        }
}

@Composable
internal fun rememberOverlayHeight(): ContactDetailsOverlayHeight {
    return remember { ContactDetailsOverlayHeight() }
}

@Composable
internal fun Modifier.measuredInto(overlayHeight: ContactDetailsOverlayHeight): Modifier {
    val density = LocalDensity.current

    return onSizeChanged { size ->
        with(density) {
            overlayHeight.value = size.height.toDp()
        }
    }
}

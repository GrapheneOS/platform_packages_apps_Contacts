package com.android.contacts.ui.contactdetails.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens

@Composable
internal fun horizontalContentPadding(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = Tokens.screenHorizontalPadding +
            contentPadding.calculateStartPadding(layoutDirection),
        end = Tokens.screenHorizontalPadding +
            contentPadding.calculateEndPadding(layoutDirection),
    )
}

@Composable
internal fun screenContentPadding(contentPadding: PaddingValues): PaddingValues {
    return PaddingValues(
        bottom = Tokens.screenBottomPadding + contentPadding.calculateBottomPadding(),
    )
}

package com.android.contacts.ui.common.util

import android.content.res.Resources
import android.icu.text.MessageFormat
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import java.util.Locale
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun messageFormatResource(
    @StringRes id: Int,
    args: ImmutableMap<String, Any> = persistentMapOf(),
): String {
    val msgFormat = MessageFormat(
        stringResource(id),
        LocalLocale.current.platformLocale,
    )
    return msgFormat.format(args)
}

internal fun Resources.getMessageFormat(
    @StringRes id: Int,
    args: Map<String, Any> = emptyMap(),
): String {
    val msgFormat = MessageFormat(
        getString(id),
        Locale.getDefault(),
    )
    return msgFormat.format(args)
}

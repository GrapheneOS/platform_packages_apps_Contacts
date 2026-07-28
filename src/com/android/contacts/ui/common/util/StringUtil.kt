package com.android.contacts.ui.common.util

import android.icu.text.MessageFormat
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
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

@Composable
internal fun expandStringTemplate(
    template: String,
    vararg values: String,
): String {
    return TextUtils.expandTemplate(template, *values).toString()
}

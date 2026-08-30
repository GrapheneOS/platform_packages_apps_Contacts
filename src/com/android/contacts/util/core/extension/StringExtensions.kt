package com.android.contacts.util.core.extension

internal fun String.trimmedOrNull(): String? {
    return trim().takeIf { trimmed -> trimmed.isNotEmpty() }
}

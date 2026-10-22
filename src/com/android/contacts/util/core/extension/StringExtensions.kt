package com.android.contacts.util.core.extension

import java.nio.charset.Charset

internal fun String.trimmedOrNull(): String? {
    return trim().takeIf { trimmed -> trimmed.isNotEmpty() }
}

internal fun String.limit(limit: Int, encoding: Charset?): String {
    return when {
        encoding != null -> limitByEncoding(limit, encoding)
        else -> limitByCharacters(limit)
    }
}

private fun String.limitByEncoding(limit: Int, encoding: Charset): String {
    var index = 0
    while (index < length) {
        val next = offsetByCodePoints(index, 1)
        if (substring(0, next).toByteArray(encoding).size > limit) break
        index = next
    }
    return substring(0, index)
}

private fun String.limitByCharacters(limit: Int): String {
    val limited = take(limit)
    return when {
        limited.lastOrNull()?.isHighSurrogate() == true -> limited.dropLast(1)
        else -> limited
    }
}

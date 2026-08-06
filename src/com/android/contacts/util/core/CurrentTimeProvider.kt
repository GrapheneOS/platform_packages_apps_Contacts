package com.android.contacts.util.core

internal fun interface CurrentTimeProvider {
    fun currentTimeMillis(): Long
}

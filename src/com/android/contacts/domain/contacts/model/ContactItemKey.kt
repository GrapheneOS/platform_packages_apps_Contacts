package com.android.contacts.domain.contacts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

internal sealed interface ContactItemKey : Parcelable {

    @Parcelize
    data class Local(
        val id: Long,
    ) : ContactItemKey

    @Parcelize
    data class Directory(
        val directoryId: Long,
        val id: Long,
        val lookupKey: String,
    ) : ContactItemKey
}

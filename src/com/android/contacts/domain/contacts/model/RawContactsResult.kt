package com.android.contacts.domain.contacts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class RawContactsResult(
    val contactId: Long,
    val isUserProfile: Boolean,
    val rawContacts: List<RawContactWithAccount>,
) : Parcelable

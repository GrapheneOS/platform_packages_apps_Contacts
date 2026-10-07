package com.android.contacts.data.contacts.model

import android.net.Uri

internal data class ContactLookupResult(
    val id: Long,
    val key: String,
    val uri: Uri,
)

package com.android.contacts.ui.contactdetails.screen.model

import android.net.Uri

internal data class ContactDetailsArguments(
    val lookupUri: Uri,
    val excludedMimeTypes: Set<String>,
    val prioritizedMimeType: String?,
)

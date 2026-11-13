package com.android.contacts.data.contacts.model

import com.android.contacts.data.contactdetails.model.ContactPhoto

internal data class DirectoryContactColumn(
    val directoryId: Long,
    val id: Long,
    val lookupKey: String,
    val displayNamePrimary: String? = null,
    val displayNameAlternative: String? = null,
    val photo: ContactPhoto? = null,
)

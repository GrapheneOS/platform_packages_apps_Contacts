package com.android.contacts.tests

import com.android.contacts.data.contacts.model.RawContact

internal object RawContactFactory {
    fun build(
        id: Long = 1L,
        photoUri: String? = null,
        displayName: String? = null,
        displayNameAlt: String? = null,
        accountName: String? = null,
        accountType: String? = null,
        accountDataSet: String? = null,
    ): RawContact {
        return RawContact(
            id = id,
            photoUri = photoUri,
            displayName = displayName,
            displayNameAlt = displayNameAlt,
            accountName = accountName,
            accountType = accountType,
            accountDataSet = accountDataSet,
        )
    }
}

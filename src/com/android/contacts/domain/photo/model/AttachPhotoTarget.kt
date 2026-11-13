package com.android.contacts.domain.photo.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface AttachPhotoTarget {

    sealed interface Ready : AttachPhotoTarget {

        data class ExistingRawContact(
            val rawContactId: Long,
        ) : Ready

        data class NewRawContact(
            val account: AccountModel,
        ) : Ready
    }

    data object NeedsAccountSelection : AttachPhotoTarget

    data object NotFound : AttachPhotoTarget
}

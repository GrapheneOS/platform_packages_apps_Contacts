package com.android.contacts.tests.factory

import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey

internal object ContactItemFactory {
    fun build(
        key: ContactItemKey = ContactItemKey.Local(1L),
        name: String = "Contact",
        contactPhoto: ContactPhoto? = null,
        field: ContactItem.Field? = null,
        sectionLabel: String? = null,
        isFavorite: Boolean = false,
    ) = ContactItem(
        key = key,
        name = name,
        contactPhoto = contactPhoto,
        field = field,
        sectionLabel = sectionLabel,
        isFavorite = isFavorite,
    )
}

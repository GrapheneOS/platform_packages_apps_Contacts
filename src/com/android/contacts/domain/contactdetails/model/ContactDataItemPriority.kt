package com.android.contacts.domain.contactdetails.model

import com.android.contacts.data.contactdetails.model.ContactDataItem

internal val CONTACT_DATA_ITEM_PRIORITY: Comparator<ContactDataItem> =
    compareByDescending<ContactDataItem> { dataItem ->
        dataItem.isSuperPrimary
    }.thenByDescending { dataItem ->
        dataItem.isPrimary
    }

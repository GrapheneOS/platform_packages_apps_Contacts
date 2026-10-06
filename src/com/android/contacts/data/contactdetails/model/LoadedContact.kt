package com.android.contacts.data.contactdetails.model

import com.android.contacts.model.Contact
import com.android.contacts.model.ContactLoader

internal class LoadedContact(
    internal val contact: Contact,
    internal val loader: ContactLoader,
)

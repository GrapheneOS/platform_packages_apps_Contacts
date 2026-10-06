package com.android.contacts.ui.contactdetails

import android.content.Intent
import android.provider.ContactsContract.QuickContact
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsArguments

internal fun Intent.toContactDetailsArguments(): ContactDetailsArguments? {
    val lookupUri = data ?: return null

    return ContactDetailsArguments(
        lookupUri = lookupUri,
        excludedMimeTypes = getStringArrayExtra(QuickContact.EXTRA_EXCLUDE_MIMES).orEmpty().toSet(),
        prioritizedMimeType = getStringExtra(QuickContact.EXTRA_PRIORITIZED_MIMETYPE),
    )
}

internal fun Intent.isContactEdited(): Boolean {
    return getBooleanExtra(ContactDetailsActivity.EXTRA_CONTACT_EDITED, false)
}

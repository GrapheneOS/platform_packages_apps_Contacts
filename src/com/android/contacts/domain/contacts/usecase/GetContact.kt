package com.android.contacts.domain.contacts.usecase

import android.content.ContentUris
import android.provider.ContactsContract.Contacts
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.model.Contact
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

internal fun interface GetContact {
    operator fun invoke(contactId: Long): Flow<Contact>
}

internal class GetContactImpl @Inject constructor(
    private val contactDetailsRepository: ContactDetailsRepository,
) : GetContact {

    override fun invoke(contactId: Long): Flow<Contact> {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        return contactDetailsRepository
            .observeContactDetails(contactUri, excludedMimeTypes = emptySet())
            .mapNotNull { result ->
                (result as? ContactDetailsResult.Loaded)?.source?.contact
            }
    }
}

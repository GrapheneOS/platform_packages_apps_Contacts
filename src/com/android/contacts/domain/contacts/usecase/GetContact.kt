package com.android.contacts.domain.contacts.usecase

import android.content.ContentUris
import android.provider.ContactsContract.Contacts
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.domain.contacts.mapper.ContactItemMapper
import com.android.contacts.domain.contacts.model.ContactItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

internal fun interface GetContact {
    operator fun invoke(contactId: Long): Flow<ContactItem>
}

internal class GetContactImpl @Inject constructor(
    private val contactDetailsRepository: ContactDetailsRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val contactItemMapper: ContactItemMapper,
) : GetContact {

    override fun invoke(contactId: Long): Flow<ContactItem> {
        return combine(
            contactDetailsFlow(contactId),
            displayOrderFlow(),
            contactItemMapper::map,
        )
    }

    private fun contactDetailsFlow(contactId: Long): Flow<ContactDetails> {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        val details = contactDetailsRepository
            .observeContactDetails(contactUri, excludedMimeTypes = emptySet())
            .filterIsInstance<ContactDetailsResult.Loaded>()
            .map { it.details }
        return details
    }

    private fun displayOrderFlow(): Flow<DisplayOrder> {
        return displaySettingsRepository.observeDisplaySettings()
            .map { it.displayOrder }
            .distinctUntilChanged()
    }
}

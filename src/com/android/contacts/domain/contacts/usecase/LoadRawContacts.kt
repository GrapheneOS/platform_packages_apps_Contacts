package com.android.contacts.domain.contacts.usecase

import android.net.Uri
import com.android.contacts.data.contacts.model.RawContactsMetadata
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.domain.contacts.mapper.RawContactWithAccountMapper
import com.android.contacts.domain.contacts.model.RawContactsResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun interface LoadRawContacts {
    operator fun invoke(
        contactUri: Uri,
        onlyWritable: Boolean,
    ): Flow<RawContactsResult?>
}

internal class LoadRawContactsImpl @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val rawContactWithAccountMapper: RawContactWithAccountMapper,
) : LoadRawContacts {

    override fun invoke(
        contactUri: Uri,
        onlyWritable: Boolean,
    ): Flow<RawContactsResult?> {
        return contactsRepository
            .loadRawContacts(contactUri)
            .map { metadata -> buildResult(metadata, onlyWritable) }
    }

    private fun buildResult(
        metadata: RawContactsMetadata?,
        onlyWritable: Boolean,
    ): RawContactsResult? {
        if (metadata == null) return null

        val rawContactsWithAccount = metadata.rawContacts
            .mapNotNull(rawContactWithAccountMapper::map)

        val rawContactsWithAccountFiltered = when {
            onlyWritable -> rawContactsWithAccount.filter { it.account.areContactsWritable }
            else -> rawContactsWithAccount
        }

        return RawContactsResult(
            contactId = metadata.contactId,
            isUserProfile = metadata.isUserProfile,
            rawContacts = rawContactsWithAccountFiltered,
        )
    }
}

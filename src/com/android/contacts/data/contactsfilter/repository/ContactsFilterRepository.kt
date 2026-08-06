package com.android.contacts.data.contactsfilter.repository

import com.android.contacts.data.contactsfilter.model.ContactsFilter
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.list.ContactListFilter
import com.android.contacts.list.ContactListFilterController
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface ContactsFilterRepository {
    suspend fun getContactsFilter(): ContactsFilter?
}

internal class ContactsFilterRepositoryImpl @Inject constructor(
    private val contactListFilterController: ContactListFilterController,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContactsFilterRepository {

    override suspend fun getContactsFilter(): ContactsFilter? {
        return withContext(ioDispatcher) {
            when (contactListFilterController.persistedFilter?.filterType) {
                ContactListFilter.FILTER_TYPE_DEFAULT -> ContactsFilter.ALL_ACCOUNTS
                ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS -> ContactsFilter.ALL_ACCOUNTS
                ContactListFilter.FILTER_TYPE_CUSTOM -> ContactsFilter.CUSTOM
                else -> null
            }
        }
    }
}

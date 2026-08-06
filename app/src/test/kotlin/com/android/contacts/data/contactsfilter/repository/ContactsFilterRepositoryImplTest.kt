package com.android.contacts.data.contactsfilter.repository

import com.android.contacts.data.contactsfilter.model.ContactsFilter
import com.android.contacts.list.ContactListFilter
import com.android.contacts.list.ContactListFilterController
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ContactsFilterRepositoryImplTest {

    private val contactListFilterController = mockk<ContactListFilterController>()

    private val repository = ContactsFilterRepositoryImpl(
        contactListFilterController = contactListFilterController,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun getContactsFilter_whenDefaultFilterIsPersisted_returnsAllAccounts() = runTest {
        givenPersistedFilter(ContactListFilter.FILTER_TYPE_DEFAULT)

        assertEquals(ContactsFilter.ALL_ACCOUNTS, repository.getContactsFilter())
    }

    @Test
    fun getContactsFilter_whenAllAccountsFilterIsPersisted_returnsAllAccounts() = runTest {
        givenPersistedFilter(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS)

        assertEquals(ContactsFilter.ALL_ACCOUNTS, repository.getContactsFilter())
    }

    @Test
    fun getContactsFilter_whenCustomFilterIsPersisted_returnsCustom() = runTest {
        givenPersistedFilter(ContactListFilter.FILTER_TYPE_CUSTOM)

        assertEquals(ContactsFilter.CUSTOM, repository.getContactsFilter())
    }

    @Test
    fun getContactsFilter_whenAnotherFilterTypeIsPersisted_returnsNull() = runTest {
        givenPersistedFilter(ContactListFilter.FILTER_TYPE_ACCOUNT)

        assertNull(repository.getContactsFilter())
    }

    @Test
    fun getContactsFilter_whenNothingIsPersisted_returnsNull() = runTest {
        every { contactListFilterController.persistedFilter } returns null

        assertNull(repository.getContactsFilter())
    }

    private fun givenPersistedFilter(filterType: Int) {
        every {
            contactListFilterController.persistedFilter
        } returns ContactListFilter.createFilterWithType(filterType)
    }
}

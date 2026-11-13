package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.ACCOUNT_WITHOUT_DATA_SET_SELECTION
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.ACCOUNT_WITH_DATA_SET_SELECTION
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.EXCLUDE_CONTACT_SELECTION
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.LOCAL_ACCOUNT_SELECTION
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.tests.addressBookIndexExtras
import com.android.contacts.data.tests.givenQueryResult
import com.android.contacts.data.tests.givenQueryRows
import com.android.contacts.data.tests.givenRegisteredContentObserver
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.tests.factory.AccountModelFactory
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QueryContactsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = QueryContactsRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun queryContacts_withoutSearchQuery_queriesContentUriWithAddressBookIndex() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContacts(searchQuery = null, sortOrder = SortOrder.GIVEN_NAME_FIRST).first()

        val query = queries.single()
        assertEquals(
            Contacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true")
                .appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY,
                    ContactsContract.Directory.DEFAULT.toString(),
                )
                .build(),
            query.uri,
        )
        assertArrayEquals(PROJECTION, query.projection)
        assertNull(query.selection)
        assertNull(query.selectionArgs)
        assertEquals(Contacts.SORT_KEY_PRIMARY, query.sortOrder)
    }

    @Test
    fun queryContacts_withSearchQuery_queriesFilterUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContacts(
            searchQuery = "alice b",
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
        ).first()

        assertEquals(
            Contacts.ENTERPRISE_CONTENT_FILTER_URI.buildUpon()
                .appendPath("alice b")
                .appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY,
                    ContactsContract.Directory.DEFAULT.toString(),
                )
                .build(),
            queries.single().uri,
        )
    }

    @Test
    fun queryContacts_withFamilyNameFirst_sortsByAlternativeKey() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContacts(searchQuery = null, sortOrder = SortOrder.FAMILY_NAME_FIRST).first()

        assertEquals(Contacts.SORT_KEY_ALTERNATIVE, queries.single().sortOrder)
    }

    @Test
    fun queryContacts_withAccount_selectsAccountContacts() = runTest {
        val queries = contentResolver.givenQueryRows()
        val account = AccountModelFactory.build(
            name = "user@example.org",
            type = "com.example",
            dataSet = "plus",
        )

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST, account = account).first()

        val query = queries.single()
        assertAccountParametersAbsent(query.uri)
        assertEquals("($ACCOUNT_WITH_DATA_SET_SELECTION)", query.selection)
        assertArrayEquals(
            arrayOf("user@example.org", "com.example", "plus"),
            query.selectionArgs,
        )
    }

    @Test
    fun queryContacts_withAccountWithoutDataSet_selectsNullDataSet() = runTest {
        val queries = contentResolver.givenQueryRows()
        val account = AccountModelFactory.build(name = "user@example.org", type = "com.example")

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST, account = account).first()

        val query = queries.single()
        assertEquals("($ACCOUNT_WITHOUT_DATA_SET_SELECTION)", query.selection)
        assertArrayEquals(arrayOf("user@example.org", "com.example"), query.selectionArgs)
    }

    @Test
    fun queryContacts_withSearchQueryAndAccount_selectsAccountContacts() = runTest {
        val queries = contentResolver.givenQueryRows()
        val account = AccountModelFactory.build(name = "user@example.org", type = "com.example")

        subject.queryContacts(
            searchQuery = "alice",
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            account = account,
            excludedContactId = 42L,
        ).first()

        val query = queries.single()
        assertAccountParametersAbsent(query.uri)
        assertEquals(
            "($ACCOUNT_WITHOUT_DATA_SET_SELECTION) AND ($EXCLUDE_CONTACT_SELECTION)",
            query.selection,
        )
        assertArrayEquals(
            arrayOf("user@example.org", "com.example", "42"),
            query.selectionArgs,
        )
    }

    @Test
    fun queryContacts_withAccountWithoutName_doesNotFilterByAccount() = runTest {
        val queries = contentResolver.givenQueryRows()
        val account = AccountModelFactory.build(name = null, type = "com.example")

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST, account = account).first()

        val query = queries.single()
        assertAccountParametersAbsent(query.uri)
        assertNull(query.selection)
        assertNull(query.selectionArgs)
    }

    @Test
    fun queryContacts_withAccountWithoutType_doesNotFilterByAccount() = runTest {
        val queries = contentResolver.givenQueryRows()
        val account = AccountModelFactory.build(name = "user@example.org", type = null)

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST, account = account).first()

        val query = queries.single()
        assertNull(query.selection)
        assertNull(query.selectionArgs)
    }

    @Test
    fun queryContacts_withNullAccount_selectsLocalContacts() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST, account = AccountModel.NULL)
            .first()

        val query = queries.single()
        assertAccountParametersAbsent(query.uri)
        assertEquals("($LOCAL_ACCOUNT_SELECTION)", query.selection)
        assertNull(query.selectionArgs)
    }

    @Test
    fun queryContacts_withExcludedContact_excludesIt() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST, excludedContactId = 42L).first()

        val query = queries.single()
        assertEquals("($EXCLUDE_CONTACT_SELECTION)", query.selection)
        assertArrayEquals(arrayOf("42"), query.selectionArgs)
    }

    @Test
    fun queryContacts_withNullAccountAndExcludedContact_combinesSelections() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContacts(
            searchQuery = null,
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            account = AccountModel.NULL,
            excludedContactId = 42L,
        ).first()

        val query = queries.single()
        assertEquals(
            "($LOCAL_ACCOUNT_SELECTION) AND ($EXCLUDE_CONTACT_SELECTION)",
            query.selection,
        )
        assertArrayEquals(arrayOf("42"), query.selectionArgs)
    }

    @Test
    fun queryContacts_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice Smith", "Smith, Alice", "content://photo/1", 1),
            arrayOf(2L, null, null, null, 0),
            arrayOf(3L, "Bob", "Bob", " ", 0),
        )

        val result = subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(
            listOf(
                ContactColumn(
                    id = 1L,
                    displayNamePrimary = "Alice Smith",
                    displayNameAlternative = "Smith, Alice",
                    photo = ContactPhoto.Uri("content://photo/1"),
                    isStarred = true,
                ),
                ContactColumn(id = 2L),
                ContactColumn(
                    id = 3L,
                    displayNamePrimary = "Bob",
                    displayNameAlternative = "Bob",
                ),
            ),
            result,
        )
    }

    @Test
    fun queryContacts_withAddressBookIndex_mapsSectionLabels() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", null, null, 0),
            arrayOf(2L, "Amy", null, null, 0),
            arrayOf(3L, "Bob", null, null, 0),
            extras = addressBookIndexExtras(arrayOf("A", "B"), intArrayOf(2, 1)),
        )

        val result = subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(listOf("A", "A", "B"), result.map { it.sectionLabel })
    }

    @Test
    fun queryContacts_withMismatchedAddressBookIndex_ignoresSectionLabels() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", null, null, 0),
            extras = addressBookIndexExtras(arrayOf("A", "B"), intArrayOf(1)),
        )

        val result = subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).first()

        assertNull(result.single().sectionLabel)
    }

    @Test
    fun queryContacts_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun queryContacts_observesContactsUri() = runTest {
        val uriSlot = slot<Uri>()
        contentResolver.givenRegisteredContentObserver(uriSlot)
        contentResolver.givenQueryRows()

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(Contacts.CONTENT_URI, uriSlot.captured)
    }

    @Test
    fun queryContacts_whenContactsChange_emitsAgain() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows(arrayOf(1L, "Alice", null, null, 0))

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).test {
            assertEquals(1L, awaitItem().single().id)

            contentResolver.givenQueryRows(arrayOf(2L, "Bob", null, null, 0))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem().single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryContacts_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows()

        subject.queryContacts(null, SortOrder.GIVEN_NAME_FIRST).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
    }

    private fun assertAccountParametersAbsent(uri: Uri) {
        assertNull(uri.getQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME))
        assertNull(uri.getQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE))
        assertNull(uri.getQueryParameter(ContactsContract.RawContacts.DATA_SET))
    }
}

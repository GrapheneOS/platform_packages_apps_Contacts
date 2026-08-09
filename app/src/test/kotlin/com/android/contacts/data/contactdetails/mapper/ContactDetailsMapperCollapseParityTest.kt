package com.android.contacts.data.contactdetails.mapper

import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.DisplayNameSources
import com.android.contacts.Collapser
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.Contact
import com.android.contacts.model.RawContact
import com.android.contacts.model.account.AccountType
import com.android.contacts.model.account.AccountType.EditType
import com.android.contacts.model.account.BaseAccountType.SimpleInflater
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.model.dataitem.DataKind
import com.android.contacts.quickcontact.InvisibleContactUtil
import com.google.common.collect.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactDetailsMapperCollapseParityTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val accountTypeManager = mockk<AccountTypeManager>()
    private val accountType = mockk<AccountType>()

    private val mapper = ContactDetailsMapperImpl(
        context = context,
        accountTypeManager = accountTypeManager,
    )

    @Before
    fun setUp() {
        mockkStatic(InvisibleContactUtil::class)
        every { InvisibleContactUtil.isInvisibleAndAddable(any(), any()) } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun collapse_forEveryOrderingOfNanpForms_matchesLegacy() {
        val numbers = listOf("4155551212", "14155551212", "+14155551212", "4155559999")

        permutations(numbers).forEach { ordering ->
            assertParity(
                ordering.mapIndexed { index, number ->
                    row(id = index + 1L, data = number)
                },
            )
        }
    }

    @Test
    fun collapse_forEveryOrderingOfDuplicatedNanpForms_matchesLegacy() {
        val numbers = listOf("4155551212", "+14155551212", "4155551212", "14155551212")

        permutations(numbers).forEach { ordering ->
            assertParity(
                ordering.mapIndexed { index, number ->
                    row(id = index + 1L, data = number)
                },
            )
        }
    }

    @Test
    fun collapse_withKeypadLetters_matchesLegacyInBothOrders() {
        assertParity(
            listOf(
                row(id = 1L, data = "1800FLOWERS"),
                row(id = 2L, data = "18003569377"),
            ),
        )
        assertParity(
            listOf(
                row(id = 1L, data = "18003569377"),
                row(id = 2L, data = "1800FLOWERS"),
            ),
        )
    }

    @Test
    fun collapse_withWaitSymbolExtensions_matchesLegacy() {
        assertParity(
            listOf(
                row(id = 1L, data = "4155551212;123"),
                row(id = 2L, data = "4155551212;456"),
                row(id = 3L, data = "4155551212;123"),
            ),
        )
    }

    @Test
    fun collapse_withPoundAndStarPrefixes_matchesLegacy() {
        assertParity(
            listOf(
                row(id = 1L, data = "#555"),
                row(id = 2L, data = "*555"),
                row(id = 3L, data = "555"),
            ),
        )
    }

    @Test
    fun collapse_withPrimaryFlagsOnEitherSide_matchesLegacy() {
        assertParity(
            listOf(
                row(id = 1L, data = "+14155551212"),
                row(id = 2L, data = "4155551212", isPrimary = true, isSuperPrimary = true),
            ),
        )
        assertParity(
            listOf(
                row(id = 1L, data = "4155551212", isPrimary = true, isSuperPrimary = true),
                row(id = 2L, data = "+14155551212"),
            ),
        )
    }

    @Test
    fun collapse_withDifferentTypePrecedenceOnEitherSide_matchesLegacy() {
        assertParity(
            listOf(
                row(id = 1L, data = "4155551212", type = Phone.TYPE_OTHER),
                row(id = 2L, data = "+14155551212", type = Phone.TYPE_MOBILE),
            ),
        )
        assertParity(
            listOf(
                row(id = 1L, data = "+14155551212", type = Phone.TYPE_MOBILE),
                row(id = 2L, data = "4155551212", type = Phone.TYPE_OTHER),
            ),
        )
    }

    @Test
    fun collapse_withMoreItemsThanTheLegacyLimit_matchesLegacy() {
        assertParity((1L..21L).map { id -> row(id = id, data = "4155551212") })
    }

    @Test
    fun collapse_atTheLegacyLimit_matchesLegacy() {
        assertParity((1L..20L).map { id -> row(id = id, data = "4155551212") })
    }

    @Test
    fun collapse_withEmailAddresses_matchesLegacy() {
        assertParity(
            listOf(
                row(id = 1L, mimeType = Email.CONTENT_ITEM_TYPE, data = "user@example.org"),
                row(id = 2L, mimeType = Email.CONTENT_ITEM_TYPE, data = "user@example.org"),
                row(id = 3L, mimeType = Email.CONTENT_ITEM_TYPE, data = "USER@example.org"),
            ),
        )
    }

    @Test
    fun collapse_withNotes_matchesLegacy() {
        assertParity(
            listOf(
                row(id = 1L, mimeType = Note.CONTENT_ITEM_TYPE, data = "same"),
                row(id = 2L, mimeType = Note.CONTENT_ITEM_TYPE, data = "same"),
                row(id = 3L, mimeType = Note.CONTENT_ITEM_TYPE, data = "other"),
            ),
        )
    }

    @Test
    fun collapse_withEvents_matchesLegacy() {
        assertParity(
            listOf(
                eventRow(id = 1L, date = "1980-05-20", type = Event.TYPE_BIRTHDAY),
                eventRow(id = 2L, date = "1980-05-20", type = Event.TYPE_BIRTHDAY),
                eventRow(id = 3L, date = "1980-05-20", type = Event.TYPE_ANNIVERSARY),
                eventRow(id = 4L, date = "1990-01-01", type = Event.TYPE_BIRTHDAY),
            ),
        )
    }

    @Test
    fun collapse_withRelations_matchesLegacy() {
        assertParity(
            listOf(
                relationRow(id = 1L, name = "Sam", type = Relation.TYPE_FATHER),
                relationRow(id = 2L, name = "Sam", type = Relation.TYPE_FATHER),
                relationRow(id = 3L, name = "Sam", type = Relation.TYPE_FRIEND),
            ),
        )
    }

    @Test
    fun collapse_withImProtocols_matchesLegacy() {
        assertParity(
            listOf(
                imRow(id = 1L, data = "me@example.org", protocol = Im.PROTOCOL_AIM),
                imRow(id = 2L, data = "me@example.org", protocol = Im.PROTOCOL_AIM),
                imRow(id = 3L, data = "me@example.org", protocol = Im.PROTOCOL_JABBER),
                imRow(id = 4L, data = "me@example.org", protocol = null),
            ),
        )
    }

    @Test
    fun collapse_withInvalidImProtocolFirst_matchesLegacy() {
        assertParity(
            listOf(
                imRow(id = 1L, data = "me@example.org", protocol = null),
                imRow(id = 2L, data = "me@example.org", protocol = Im.PROTOCOL_CUSTOM),
                imRow(id = 3L, data = "me@example.org", protocol = Im.PROTOCOL_AIM),
            ),
        )
    }

    @Test
    fun collapse_withMixedMimeTypes_matchesLegacyPerMimeType() {
        assertParity(
            listOf(
                row(id = 1L, data = "4155551212"),
                row(id = 2L, mimeType = Email.CONTENT_ITEM_TYPE, data = "user@example.org"),
                row(id = 3L, data = "+14155551212"),
                row(id = 4L, mimeType = Email.CONTENT_ITEM_TYPE, data = "user@example.org"),
                row(id = 5L, mimeType = Note.CONTENT_ITEM_TYPE, data = "note"),
            ),
        )
    }

    private fun assertParity(rows: List<DataRow>) {
        val expected = legacyStates(rows)
        val actual = mappedStates(rows)

        assertEquals(rows.toString(), expected, actual)
    }

    private fun legacyStates(rows: List<DataRow>): Map<String, List<ItemState>> {
        val dataKind = anyDataKind()
        val dataItems = rows
            .map { row -> DataItem.createFrom(row.toContentValues()) }
            .onEach { dataItem -> dataItem.dataKind = dataKind }

        return dataItems.groupBy { dataItem -> dataItem.mimeType }
            .mapValues { (_, items) ->
                val collapsible = items.toMutableList()
                Collapser.collapseList(collapsible, context)
                collapsible.map(::toItemState)
            }
    }

    private fun mappedStates(rows: List<DataRow>): Map<String, List<ItemState>> {
        val dataKind = anyDataKind()
        every { accountTypeManager.getKindOrFallback(any(), any()) } returns dataKind
        val dataItems = rows.map { row -> DataItem.createFrom(row.toContentValues()) }

        val mapped = mapper.map(contactWith(dataItems), emptySet())

        return mapped.dataItems
            .map { contactDataItem ->
                dataItems.first { dataItem -> dataItem.id == contactDataItem.id }
            }
            .groupBy { dataItem -> dataItem.mimeType }
            .mapValues { (_, items) -> items.map(::toItemState) }
    }

    private fun toItemState(dataItem: DataItem): ItemState {
        val values = dataItem.contentValues

        return ItemState(
            id = dataItem.id,
            data = values.getAsString(Data.DATA1),
            type = values.getAsInteger(Data.DATA2),
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
        )
    }

    private fun contactWith(dataItems: List<DataItem>): Contact {
        val rawContact = mockk<RawContact>(relaxed = true)
        every { rawContact.id } returns RAW_CONTACT_ID
        every { rawContact.getAccountType(context) } returns accountType
        every { rawContact.dataItems } returns dataItems.toMutableList()

        val contact = mockk<Contact>(relaxed = true)
        every { contact.rawContacts } returns ImmutableList.of(rawContact)
        every { contact.displayNameSource } returns DisplayNameSources.STRUCTURED_NAME
        every { contact.photoUri } returns null
        every { contact.photoBinaryData } returns null
        every { contact.thumbnailPhotoBinaryData } returns null
        every { contact.isDirectoryEntry } returns false
        every { contact.isUserProfile } returns false
        every { contact.areAllRawContactsSimAccounts(context) } returns false
        return contact
    }

    private fun anyDataKind(): DataKind {
        return DataKind(Phone.CONTENT_ITEM_TYPE, -1, 10, true).apply {
            actionBody = SimpleInflater(Data.DATA1)
            typeColumn = Data.DATA2
            typeList = listOf(
                EditType(Phone.TYPE_MOBILE, -1),
                EditType(Phone.TYPE_HOME, -1),
                EditType(Phone.TYPE_WORK, -1),
                EditType(Phone.TYPE_OTHER, -1),
            )
        }
    }

    private fun row(
        id: Long,
        data: String,
        mimeType: String = Phone.CONTENT_ITEM_TYPE,
        type: Int? = Phone.TYPE_HOME,
        isPrimary: Boolean = false,
        isSuperPrimary: Boolean = false,
    ): DataRow {
        return DataRow(
            id = id,
            mimeType = mimeType,
            data = data,
            type = type,
            isPrimary = isPrimary,
            isSuperPrimary = isSuperPrimary,
        )
    }

    private fun eventRow(
        id: Long,
        date: String,
        type: Int,
    ): DataRow {
        return row(
            id = id,
            mimeType = Event.CONTENT_ITEM_TYPE,
            data = date,
            type = type,
        )
    }

    private fun relationRow(
        id: Long,
        name: String,
        type: Int,
    ): DataRow {
        return row(id = id, mimeType = Relation.CONTENT_ITEM_TYPE, data = name, type = type)
    }

    private fun imRow(
        id: Long,
        data: String,
        protocol: Int?,
    ): DataRow {
        return DataRow(
            id = id,
            mimeType = Im.CONTENT_ITEM_TYPE,
            data = data,
            type = null,
            isPrimary = false,
            isSuperPrimary = false,
            protocol = protocol,
        )
    }

    private fun <T> permutations(items: List<T>): List<List<T>> {
        if (items.size <= 1) {
            return listOf(items)
        }

        return items.flatMapIndexed { index, item ->
            permutations(items.filterIndexed { other, _ -> other != index })
                .map { rest -> listOf(item) + rest }
        }
    }

    private data class DataRow(
        val id: Long,
        val mimeType: String,
        val data: String,
        val type: Int?,
        val isPrimary: Boolean,
        val isSuperPrimary: Boolean,
        val protocol: Int? = null,
    ) {
        fun toContentValues(): ContentValues {
            return ContentValues().apply {
                put(Data._ID, id)
                put(Data.MIMETYPE, mimeType)
                put(Data.DATA1, data)
                type?.let { value -> put(Data.DATA2, value) }
                protocol?.let { value -> put(Data.DATA5, value) }
                put(Data.IS_PRIMARY, if (isPrimary) 1 else 0)
                put(Data.IS_SUPER_PRIMARY, if (isSuperPrimary) 1 else 0)
            }
        }
    }

    private data class ItemState(
        val id: Long,
        val data: String?,
        val type: Int?,
        val isPrimary: Boolean,
        val isSuperPrimary: Boolean,
    )

    private companion object {
        const val RAW_CONTACT_ID = 11L
    }
}

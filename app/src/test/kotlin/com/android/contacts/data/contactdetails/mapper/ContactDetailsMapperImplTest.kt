package com.android.contacts.data.contactdetails.mapper

import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.Directory
import android.provider.ContactsContract.DisplayNameSources
import com.android.contacts.data.contactdetails.model.ContactCapabilities
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.Contact
import com.android.contacts.model.RawContact
import com.android.contacts.model.account.AccountType
import com.android.contacts.model.dataitem.CustomDataItem
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.model.dataitem.DataKind
import com.android.contacts.model.dataitem.EmailDataItem
import com.android.contacts.model.dataitem.EventDataItem
import com.android.contacts.model.dataitem.ImDataItem
import com.android.contacts.model.dataitem.NicknameDataItem
import com.android.contacts.model.dataitem.NoteDataItem
import com.android.contacts.model.dataitem.OrganizationDataItem
import com.android.contacts.model.dataitem.PhoneDataItem
import com.android.contacts.model.dataitem.RelationDataItem
import com.android.contacts.model.dataitem.SipAddressDataItem
import com.android.contacts.model.dataitem.StructuredNameDataItem
import com.android.contacts.model.dataitem.StructuredPostalDataItem
import com.android.contacts.model.dataitem.WebsiteDataItem
import com.android.contacts.quickcontact.InvisibleContactUtil
import com.google.common.collect.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.nio.ByteBuffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactDetailsMapperImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val accountTypeManager = mockk<AccountTypeManager>()
    private val accountType = mockk<AccountType>()
    private val dataKind = DataKind()

    private val mapper = ContactDetailsMapperImpl(
        context = context,
        accountTypeManager = accountTypeManager,
    )

    @Before
    fun setUp() {
        every { accountTypeManager.getKindOrFallback(any(), any()) } returns dataKind
        mockkStatic(InvisibleContactUtil::class)
        every { InvisibleContactUtil.isInvisibleAndAddable(any(), any()) } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun map_withPhoneDataItem_mapsPhoneFields() {
        val item = dataItem<PhoneDataItem>(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            dataString = "+1 555-0100",
            typeValue = Phone.TYPE_CUSTOM,
            label = "Work cell",
        ) {
            every { number } returns "+15550100"
            every { formattedPhoneNumber } returns "+1 555-0100"
        }

        val phone = mapSingleItem<ContactDataItem.Phone>(item)

        assertEquals("+15550100", phone.number)
        assertEquals("+1 555-0100", phone.formattedNumber)
        assertEquals("+1 555-0100", phone.displayString)
        assertEquals("Work cell", phone.typeLabel)
    }

    @Test
    fun map_withVideoCapableCarrierPresence_marksPhoneCarrierVideoCallCapable() {
        val item = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE) {
            every { carrierPresence } returns Phone.CARRIER_PRESENCE_VT_CAPABLE
        }

        assertTrue(mapSingleItem<ContactDataItem.Phone>(item).isCarrierVideoCallCapable)
    }

    @Test
    fun map_withoutCarrierPresence_doesNotMarkPhoneCarrierVideoCallCapable() {
        val item = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE)

        assertFalse(mapSingleItem<ContactDataItem.Phone>(item).isCarrierVideoCallCapable)
    }

    @Test
    fun map_withPhoneCustomTypeAndBlankLabel_mapsEmptyTypeLabel() {
        val item = dataItem<PhoneDataItem>(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            typeValue = Phone.TYPE_CUSTOM,
            label = null,
        )

        assertEquals("", mapSingleItem<ContactDataItem.Phone>(item).typeLabel)
    }

    @Test
    fun map_withPhoneWithoutTypeColumn_mapsNullTypeLabel() {
        val item = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE)

        assertNull(mapSingleItem<ContactDataItem.Phone>(item).typeLabel)
    }

    @Test
    fun map_withSipAddressDataItem_mapsSipFields() {
        val item = dataItem<SipAddressDataItem>(
            mimeType = SipAddress.CONTENT_ITEM_TYPE,
            typeValue = SipAddress.TYPE_CUSTOM,
            label = "Desk",
        ) {
            every { sipAddress } returns "sip@example.org"
        }

        val sip = mapSingleItem<ContactDataItem.SipAddress>(item)

        assertEquals("sip@example.org", sip.address)
        assertEquals("Desk", sip.typeLabel)
    }

    @Test
    fun map_withEmailDataItem_mapsEmailFields() {
        val item = dataItem<EmailDataItem>(
            mimeType = Email.CONTENT_ITEM_TYPE,
            typeValue = Email.TYPE_CUSTOM,
            label = "Personal",
        ) {
            every { address } returns "user@example.org"
            every { data } returns "user@example.org"
        }

        val email = mapSingleItem<ContactDataItem.Email>(item)

        assertEquals("user@example.org", email.address)
        assertEquals("user@example.org", email.data)
        assertEquals("Personal", email.typeLabel)
    }

    @Test
    fun map_withPostalDataItem_mapsPostalFields() {
        val item = dataItem<StructuredPostalDataItem>(
            mimeType = StructuredPostal.CONTENT_ITEM_TYPE,
            typeValue = StructuredPostal.TYPE_CUSTOM,
            label = "Summer house",
        ) {
            every { formattedAddress } returns "1 Main St"
        }

        val postal = mapSingleItem<ContactDataItem.Postal>(item)

        assertEquals("1 Main St", postal.formattedAddress)
        assertEquals("Summer house", postal.typeLabel)
    }

    @Test
    fun map_withImDataItem_mapsProtocolFields() {
        val item = imDataItem(protocolValue = Im.PROTOCOL_AIM)

        val im = mapSingleItem<ContactDataItem.Im>(item)

        assertEquals("me@example.org", im.data)
        assertEquals(Im.PROTOCOL_AIM, im.protocol)
        assertFalse(im.isCustomProtocol)
        assertNotNull(im.protocolLabel)
    }

    @Test
    fun map_withImDataItemCreatedFromEmail_mapsGoogleTalkProtocol() {
        val item = imDataItem(protocolValue = Im.PROTOCOL_AIM, createdFromEmail = true)

        val im = mapSingleItem<ContactDataItem.Im>(item)

        assertEquals(Im.PROTOCOL_GOOGLE_TALK, im.protocol)
        assertFalse(im.isCustomProtocol)
    }

    @Test
    fun map_withInvalidImProtocol_mapsCustomProtocol() {
        val item = imDataItem(
            protocolValue = Im.PROTOCOL_AIM,
            hasValidProtocol = false,
            customProtocolValue = "MyChat",
        )

        val im = mapSingleItem<ContactDataItem.Im>(item)

        assertEquals(Im.PROTOCOL_CUSTOM, im.protocol)
        assertTrue(im.isCustomProtocol)
        assertEquals("MyChat", im.protocolLabel)
    }

    @Test
    fun map_withMissingImProtocol_mapsCustomProtocol() {
        val item = imDataItem(protocolValue = null)

        assertEquals(Im.PROTOCOL_CUSTOM, mapSingleItem<ContactDataItem.Im>(item).protocol)
    }

    @Test
    fun map_withOrganizationDataItem_mapsFormattedCompany() {
        val item = dataItem<OrganizationDataItem>(mimeType = Organization.CONTENT_ITEM_TYPE) {
            every { company } returns "Acme"
            every { department } returns null
            every { title } returns null
        }

        val organization = mapSingleItem<ContactDataItem.Organization>(item)

        assertEquals("Acme", organization.formattedCompany)
    }

    @Test
    fun map_withNicknameDataItem_mapsName() {
        val item = dataItem<NicknameDataItem>(mimeType = Nickname.CONTENT_ITEM_TYPE) {
            every { name } returns "Ace"
        }

        assertEquals("Ace", mapSingleItem<ContactDataItem.Nickname>(item).name)
    }

    @Test
    fun map_withNoteDataItem_mapsNote() {
        val item = dataItem<NoteDataItem>(mimeType = Note.CONTENT_ITEM_TYPE) {
            every { note } returns "Met at the conference"
        }

        assertEquals("Met at the conference", mapSingleItem<ContactDataItem.Note>(item).note)
    }

    @Test
    fun map_withWebsiteDataItem_mapsUrl() {
        val item = dataItem<WebsiteDataItem>(mimeType = Website.CONTENT_ITEM_TYPE) {
            every { url } returns "example.org"
        }

        assertEquals("example.org", mapSingleItem<ContactDataItem.Website>(item).url)
    }

    @Test
    fun map_withBirthdayEventDataItem_marksRecurringAnnually() {
        val event = mapSingleItem<ContactDataItem.Event>(eventDataItem(Event.TYPE_BIRTHDAY))

        assertTrue(event.isRecurringAnnually)
        assertEquals("1980-05-20", event.displayString)
        assertNotNull(event.formattedDate)
    }

    @Test
    fun map_withAnniversaryEventDataItem_marksRecurringAnnually() {
        val event = mapSingleItem<ContactDataItem.Event>(eventDataItem(Event.TYPE_ANNIVERSARY))

        assertTrue(event.isRecurringAnnually)
    }

    @Test
    fun map_withOtherEventDataItem_doesNotMarkRecurringAnnually() {
        val event = mapSingleItem<ContactDataItem.Event>(eventDataItem(Event.TYPE_OTHER))

        assertFalse(event.isRecurringAnnually)
        assertEquals("Graduation", event.typeLabel)
    }

    @Test
    fun map_withRelationDataItem_mapsRelationFields() {
        val item = dataItem<RelationDataItem>(
            mimeType = Relation.CONTENT_ITEM_TYPE,
            typeValue = Relation.TYPE_CUSTOM,
            label = "Neighbour",
        ) {
            every { name } returns "Sam"
        }

        val relation = mapSingleItem<ContactDataItem.Relation>(item)

        assertEquals("Sam", relation.name)
        assertEquals("Neighbour", relation.typeLabel)
    }

    @Test
    fun map_withCustomDataItem_mapsSummaryAndContent() {
        val item = dataItem<CustomDataItem>(mimeType = CustomDataItem.MIMETYPE_CUSTOM_FIELD) {
            every { summary } returns "Blood type"
            every { content } returns "0+"
        }

        val custom = mapSingleItem<ContactDataItem.Custom>(item)

        assertEquals("Blood type", custom.summary)
        assertEquals("0+", custom.content)
    }

    @Test
    fun map_withStructuredNameDataItem_mapsGivenName() {
        val item = dataItem<StructuredNameDataItem>(mimeType = StructuredName.CONTENT_ITEM_TYPE) {
            every { givenName } returns "Alex"
        }

        assertEquals("Alex", mapSingleItem<ContactDataItem.StructuredName>(item).givenName)
    }

    @Test
    fun map_withUnhandledMimeType_mapsGenericItem() {
        dataKind.typeColumn = Data.DATA3
        val item = dataItem<DataItem>(mimeType = THIRD_PARTY_MIME_TYPE)

        val generic = mapSingleItem<ContactDataItem.Generic>(item)

        assertEquals(THIRD_PARTY_MIME_TYPE, generic.mimeType)
        assertEquals(Data.DATA3, generic.typeColumn)
    }

    @Test
    fun map_mapsSharedDataItemFields() {
        val item = dataItem<PhoneDataItem>(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            id = 42L,
            isPrimary = true,
            isSuperPrimary = true,
        )

        val phone = mapSingleItem<ContactDataItem.Phone>(item)

        assertEquals(42L, phone.id)
        assertEquals(RAW_CONTACT_ID, phone.rawContactId)
        assertEquals(Phone.CONTENT_ITEM_TYPE, phone.mimeType)
        assertTrue(phone.isPrimary)
        assertTrue(phone.isSuperPrimary)
    }

    @Test
    fun map_withBlankDataString_skipsDataItem() {
        val item = dataItem<PhoneDataItem>(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            dataString = "",
        )

        assertTrue(mapDataItems(item).isEmpty())
    }

    @Test
    fun map_withMissingDataKind_skipsDataItem() {
        every { accountTypeManager.getKindOrFallback(any(), any()) } returns null
        val item = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE)

        assertTrue(mapDataItems(item).isEmpty())
    }

    @Test
    fun map_withExcludedMimeType_skipsDataItem() {
        val item = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE)

        val details = mapper.map(contact(item), setOf(Phone.CONTENT_ITEM_TYPE))

        assertTrue(details.dataItems.isEmpty())
    }

    @Test
    fun map_withRawContactWithoutId_skipsItsDataItems() {
        val item = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE)
        val rawContact = rawContact(item)
        every { rawContact.id } returns null
        val contact = contactMock()
        every { contact.rawContacts } returns ImmutableList.of(rawContact)

        assertTrue(mapper.map(contact, emptySet()).dataItems.isEmpty())
    }

    @Test
    fun map_withCollapsibleDataItems_keepsOnlyTheCollapsedItem() {
        val first = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 1L)
        val second = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 2L)
        every { first.shouldCollapseWith(second, context) } returns true

        assertEquals(listOf(1L), mapDataItems(first, second).map { it.id })
    }

    @Test
    fun map_withLaterDataItemClaimingTheDuplicate_keepsTheLaterItem() {
        val first = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 1L)
        val second = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 2L)
        every { second.shouldCollapseWith(first, context) } returns true

        assertEquals(listOf(2L), mapDataItems(first, second).map { it.id })
        verify { second.collapseWith(first) }
    }

    @Test
    fun map_withDuplicateClaimedInBothDirections_keepsTheEarlierItem() {
        val first = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 1L)
        val second = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 2L)
        every { first.shouldCollapseWith(second, context) } returns true
        every { second.shouldCollapseWith(first, context) } returns true

        assertEquals(listOf(1L), mapDataItems(first, second).map { it.id })
        verify { first.collapseWith(second) }
    }

    @Test
    fun map_withDistinctDataItemsOfOneMimeType_keepsBoth() {
        val first = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 1L)
        val second = dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = 2L)

        assertEquals(listOf(1L, 2L), mapDataItems(first, second).map { it.id })
    }

    @Test
    fun map_withMoreDataItemsThanTheCollapseLimit_keepsThemAll() {
        val items = (1L..21L).map { id ->
            dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = id)
        }
        items.forEach { item ->
            every { item.shouldCollapseWith(any(), any()) } returns true
        }

        assertEquals(items.size, mapDataItems(*items.toTypedArray()).size)
    }

    @Test
    fun map_withThreeCollapsibleDataItems_keepsOnlyTheFirst() {
        val items = (1L..3L).map { id ->
            dataItem<PhoneDataItem>(mimeType = Phone.CONTENT_ITEM_TYPE, id = id)
        }
        every { items[0].shouldCollapseWith(any(), any()) } returns true

        assertEquals(listOf(1L), mapDataItems(*items.toTypedArray()).map { it.id })
    }

    @Test
    fun map_withoutRawContacts_returnsNoDataItems() {
        val contact = contactMock()
        every { contact.rawContacts } returns null

        assertTrue(mapper.map(contact, emptySet()).dataItems.isEmpty())
    }

    @Test
    fun map_mapsContactIdentityAndName() {
        val contact = contactMock()
        every { contact.id } returns CONTACT_ID
        every { contact.lookupKey } returns "lookup-key"
        every { contact.nameRawContactId } returns RAW_CONTACT_ID
        every { contact.displayName } returns "Alex Doe"
        every { contact.altDisplayName } returns "Doe, Alex"
        every { contact.phoneticName } returns "eh-leks"
        every { contact.starred } returns true
        every { contact.customRingtone } returns "content://ringtone"

        val details = mapper.map(contact, emptySet())

        assertEquals(CONTACT_ID, details.contactId)
        assertEquals("lookup-key", details.lookupKey)
        assertEquals(RAW_CONTACT_ID, details.nameRawContactId)
        assertEquals("Alex Doe", details.displayName)
        assertEquals("Doe, Alex", details.alternativeDisplayName)
        assertEquals("eh-leks", details.phoneticName)
        assertTrue(details.isStarred)
        assertEquals("content://ringtone", details.customRingtone)
    }

    @Test
    fun map_mapsEveryKnownDisplayNameSource() {
        val sources = mapOf(
            DisplayNameSources.UNDEFINED to ContactDisplayNameSource.UNDEFINED,
            DisplayNameSources.EMAIL to ContactDisplayNameSource.EMAIL,
            DisplayNameSources.PHONE to ContactDisplayNameSource.PHONE,
            DisplayNameSources.ORGANIZATION to ContactDisplayNameSource.ORGANIZATION,
            DisplayNameSources.NICKNAME to ContactDisplayNameSource.NICKNAME,
            DisplayNameSources.STRUCTURED_PHONETIC_NAME to
                ContactDisplayNameSource.STRUCTURED_PHONETIC_NAME,
            DisplayNameSources.STRUCTURED_NAME to ContactDisplayNameSource.STRUCTURED_NAME,
        )

        sources.forEach { (platformSource, expected) ->
            val contact = contactMock()
            every { contact.displayNameSource } returns platformSource

            assertEquals(
                "display name source $platformSource",
                expected,
                mapper.map(contact, emptySet()).displayNameSource,
            )
        }
    }

    @Test
    fun map_withUnknownDisplayNameSource_mapsUndefined() {
        val contact = contactMock()
        every { contact.displayNameSource } returns UNKNOWN_DISPLAY_NAME_SOURCE

        val details = mapper.map(contact, emptySet())

        assertEquals(ContactDisplayNameSource.UNDEFINED, details.displayNameSource)
    }

    @Test
    fun map_withLoadedPhoto_mapsItAheadOfTheThumbnailAndTheUri() {
        val contact = contactMock()
        every { contact.photoBinaryData } returns byteArrayOf(4, 5)
        every { contact.thumbnailPhotoBinaryData } returns byteArrayOf(1, 2, 3)
        every { contact.photoUri } returns "content://photo"
        every { contact.photoId } returns PHOTO_ID

        val details = mapper.map(contact, emptySet())

        assertEquals(ContactPhoto.Bytes(ByteBuffer.wrap(byteArrayOf(4, 5))), details.photo)
        assertEquals(PHOTO_ID, details.photoId)
    }

    @Test
    fun map_withoutLoadedPhoto_mapsThumbnailBytes() {
        val contact = contactMock()
        every { contact.thumbnailPhotoBinaryData } returns byteArrayOf(1, 2, 3)
        every { contact.photoUri } returns "content://photo"

        val details = mapper.map(contact, emptySet())

        assertEquals(ContactPhoto.Bytes(ByteBuffer.wrap(byteArrayOf(1, 2, 3))), details.photo)
    }

    @Test
    fun map_withoutAnyPhotoBytes_mapsUriPhoto() {
        val contact = contactMock()
        every { contact.photoUri } returns "content://photo"

        val details = mapper.map(contact, emptySet())

        assertEquals(ContactPhoto.Uri("content://photo"), details.photo)
    }

    @Test
    fun map_withoutAnyPhoto_mapsNullPhoto() {
        assertNull(mapper.map(contactMock(), emptySet()).photo)
    }

    @Test
    fun map_withLocalContact_mapsNoSpecialCapabilities() {
        val capabilities = capabilities(contactMock())

        assertFalse(capabilities.isDirectoryEntry)
        assertFalse(capabilities.isAddableDirectoryContact)
        assertFalse(capabilities.isInvisibleAndAddable)
        assertFalse(capabilities.isUserProfile)
        assertFalse(capabilities.hasMultipleRawContacts)
        assertFalse(capabilities.areAllRawContactsSimAccounts)
    }

    @Test
    fun map_withExportableDirectoryContact_marksAddableDirectoryContact() {
        val contact = contactMock()
        every { contact.isDirectoryEntry } returns true
        every { contact.directoryExportSupport } returns Directory.EXPORT_SUPPORT_ANY_ACCOUNT

        val capabilities = capabilities(contact)

        assertTrue(capabilities.isDirectoryEntry)
        assertTrue(capabilities.isAddableDirectoryContact)
    }

    @Test
    fun map_withNonExportableDirectoryContact_marksNotAddable() {
        val contact = contactMock()
        every { contact.isDirectoryEntry } returns true

        val capabilities = capabilities(contact)

        assertTrue(capabilities.isDirectoryEntry)
        assertFalse(capabilities.isAddableDirectoryContact)
    }

    @Test
    fun map_withInvisibleContact_marksInvisibleAndAddable() {
        every { InvisibleContactUtil.isInvisibleAndAddable(any(), any()) } returns true

        assertTrue(capabilities(contactMock()).isInvisibleAndAddable)
    }

    @Test
    fun map_withUserProfile_marksUserProfile() {
        val contact = contactMock()
        every { contact.isUserProfile } returns true

        assertTrue(capabilities(contact).isUserProfile)
    }

    @Test
    fun map_withSimOnlyContact_marksAllRawContactsSimAccounts() {
        val contact = contactMock()
        every { contact.areAllRawContactsSimAccounts(context) } returns true

        assertTrue(capabilities(contact).areAllRawContactsSimAccounts)
    }

    @Test
    fun map_withSeveralRawContacts_marksMultipleRawContacts() {
        val contact = contactMock()
        every { contact.rawContacts } returns ImmutableList.of(rawContact(), rawContact())

        assertTrue(capabilities(contact).hasMultipleRawContacts)
    }

    private inline fun <reified T : ContactDataItem> mapSingleItem(item: DataItem): T {
        return mapDataItems(item).single() as T
    }

    private fun mapDataItems(vararg items: DataItem): List<ContactDataItem> {
        return mapper.map(contact(*items), emptySet()).dataItems
    }

    private fun capabilities(contact: Contact): ContactCapabilities {
        return mapper.map(contact, emptySet()).capabilities
    }

    private fun contact(vararg items: DataItem): Contact {
        val contact = contactMock()
        every { contact.rawContacts } returns ImmutableList.of(rawContact(*items))
        return contact
    }

    private fun contactMock(): Contact {
        val contact = mockk<Contact>(relaxed = true)
        every { contact.id } returns CONTACT_ID
        every { contact.lookupKey } returns null
        every { contact.lookupUri } returns null
        every { contact.nameRawContactId } returns RAW_CONTACT_ID
        every { contact.displayName } returns null
        every { contact.altDisplayName } returns null
        every { contact.phoneticName } returns null
        every { contact.displayNameSource } returns DisplayNameSources.STRUCTURED_NAME
        every { contact.starred } returns false
        every { contact.photoId } returns PHOTO_ID
        every { contact.photoUri } returns null
        every { contact.thumbnailPhotoBinaryData } returns null
        every { contact.photoBinaryData } returns null
        every { contact.customRingtone } returns null
        every { contact.rawContacts } returns ImmutableList.of()
        every { contact.isDirectoryEntry } returns false
        every { contact.directoryExportSupport } returns Directory.EXPORT_SUPPORT_NONE
        every { contact.isUserProfile } returns false
        every { contact.areAllRawContactsSimAccounts(context) } returns false
        return contact
    }

    private fun rawContact(vararg items: DataItem): RawContact {
        val rawContact = mockk<RawContact>(relaxed = true)
        every { rawContact.id } returns RAW_CONTACT_ID
        every { rawContact.getAccountType(context) } returns accountType
        every { rawContact.dataItems } returns items.toMutableList()
        return rawContact
    }

    private fun imDataItem(
        protocolValue: Int?,
        hasValidProtocol: Boolean = true,
        createdFromEmail: Boolean = false,
        customProtocolValue: String? = null,
    ): ImDataItem {
        val item = dataItem<ImDataItem>(mimeType = Im.CONTENT_ITEM_TYPE)
        every { item.data } returns "me@example.org"
        every { item.protocol } returns protocolValue
        every { item.isProtocolValid } returns hasValidProtocol
        every { item.isCreatedFromEmail } returns createdFromEmail
        every { item.customProtocol } returns customProtocolValue
        every { item.chatCapability } returns 0
        return item
    }

    private fun eventDataItem(eventType: Int): EventDataItem {
        val item = dataItem<EventDataItem>(
            mimeType = Event.CONTENT_ITEM_TYPE,
            dataString = "1980-05-20",
            typeValue = Event.TYPE_CUSTOM,
            label = "Graduation",
        )
        every { item.contentValues } returns ContentValues().apply { put(Event.TYPE, eventType) }
        return item
    }

    private inline fun <reified T : DataItem> dataItem(
        mimeType: String,
        id: Long = ITEM_ID,
        dataString: String? = "data",
        typeValue: Int? = null,
        label: String? = null,
        isPrimary: Boolean = false,
        isSuperPrimary: Boolean = false,
        block: T.() -> Unit = {},
    ): T {
        val item = mockk<T>(relaxed = true)
        var boundDataKind: DataKind? = dataKind
        every { item.id } returns id
        every { item.mimeType } returns mimeType
        every { item.rawContactId } returns RAW_CONTACT_ID
        every { item.dataKind } answers { boundDataKind }
        every { item.dataKind = any() } answers { boundDataKind = firstArg() }
        every { item.isPrimary } returns isPrimary
        every { item.isSuperPrimary } returns isSuperPrimary
        every { item.buildDataString(context, dataKind) } returns dataString
        every { item.buildDataStringForDisplay(context, dataKind) } returns dataString
        every { item.hasKindTypeColumn(dataKind) } returns (typeValue != null)
        every { item.getKindTypeColumn(dataKind) } returns (typeValue ?: 0)
        every { item.shouldCollapseWith(any(), any()) } returns false
        stubLabel(item, label)
        item.block()
        return item
    }

    private fun stubLabel(
        item: DataItem,
        label: String?,
    ) {
        when (item) {
            is EmailDataItem -> every { item.label } returns label
            is EventDataItem -> every { item.label } returns label
            is PhoneDataItem -> every { item.label } returns label
            is RelationDataItem -> every { item.label } returns label
            is SipAddressDataItem -> every { item.label } returns label
            is StructuredPostalDataItem -> every { item.label } returns label
            else -> Unit
        }
    }

    private companion object {
        const val CONTACT_ID = 7L
        const val RAW_CONTACT_ID = 11L
        const val ITEM_ID = 13L
        const val PHOTO_ID = 17L
        const val UNKNOWN_DISPLAY_NAME_SOURCE = 999
        const val THIRD_PARTY_MIME_TYPE = "vnd.android.cursor.item/vnd.example.profile"
    }
}

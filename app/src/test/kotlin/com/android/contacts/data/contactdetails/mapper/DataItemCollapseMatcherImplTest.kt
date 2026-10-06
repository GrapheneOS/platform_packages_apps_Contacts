package com.android.contacts.data.contactdetails.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.Data
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.model.dataitem.DataKind
import com.android.contacts.tests.factory.collapsibleDataItem
import com.android.contacts.tests.factory.collapsibleDataKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class DataItemCollapseMatcherImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val matcher = DataItemCollapseMatcherImpl(context)

    @Test
    fun shouldCollapse_withoutDataKindOnCurrentItem_returnsFalse() {
        val current = phone(number = "4155551212", dataKind = null)
        val other = phone(number = "4155551212")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withoutDataKindOnOtherItem_returnsFalse() {
        val current = phone(number = "4155551212")
        val other = phone(number = "4155551212", dataKind = null)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withoutActionBodyOnDataKind_collapsesUnrelatedItems() {
        val kind = DataKind().apply { typeColumn = Data.DATA2 }
        val current = phone(number = "4155551212", dataKind = kind)
        val other = phone(number = "4155559999", dataKind = kind)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEqualMimeTypesAndData_returnsTrue() {
        assertItemsCollapse(mimeType = null, data = null, otherMimeType = null, otherData = null)
        assertItemsCollapse(mimeType = "a", data = "b", otherMimeType = "a", otherData = "b")
        assertItemsCollapse(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            data = null,
            otherMimeType = Phone.CONTENT_ITEM_TYPE,
            otherData = null,
        )
        assertItemsCollapse(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            data = "1",
            otherMimeType = Phone.CONTENT_ITEM_TYPE,
            otherData = "1",
        )
    }

    @Test
    fun shouldCollapse_withDifferentMimeTypes_returnsFalse() {
        assertItemsDoNotCollapse(
            mimeType = "a",
            data = null,
            otherMimeType = null,
            otherData = null,
        )
        assertItemsDoNotCollapse(
            mimeType = "a",
            data = "b",
            otherMimeType = null,
            otherData = null,
        )
        assertItemsDoNotCollapse(mimeType = "a", data = "b", otherMimeType = null, otherData = "b")
        assertItemsDoNotCollapse(mimeType = "a", data = "b", otherMimeType = "x", otherData = "b")
    }

    @Test
    fun shouldCollapse_withEqualMimeTypesAndDifferentData_returnsFalse() {
        assertItemsDoNotCollapse(
            mimeType = null,
            data = "a",
            otherMimeType = null,
            otherData = null,
        )
        assertItemsDoNotCollapse(mimeType = "a", data = "b", otherMimeType = "a", otherData = null)
        assertItemsDoNotCollapse(mimeType = "a", data = "b", otherMimeType = "a", otherData = "x")
    }

    @Test
    fun shouldCollapse_withImAndNonImItem_returnsFalse() {
        val current = im(address = "me@example.org", protocol = Im.PROTOCOL_AIM)
        val other = note(text = "me@example.org")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withImItemsWithDifferentAddresses_returnsFalse() {
        val current = im(address = "me@example.org", protocol = Im.PROTOCOL_AIM)
        val other = im(address = "other@example.org", protocol = Im.PROTOCOL_AIM)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withImItemsWithSameProtocol_returnsTrue() {
        val current = im(address = "me@example.org", protocol = Im.PROTOCOL_AIM)
        val other = im(address = "me@example.org", protocol = Im.PROTOCOL_AIM)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withImItemsWithDifferentProtocols_returnsFalse() {
        val current = im(address = "me@example.org", protocol = Im.PROTOCOL_AIM)
        val other = im(address = "me@example.org", protocol = Im.PROTOCOL_JABBER)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withInvalidProtocolsOnBothSides_returnsTrue() {
        val current = im(address = "me@example.org", protocol = null)
        val other = im(address = "me@example.org", protocol = null)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withInvalidProtocolAndCustomProtocol_returnsTrue() {
        val current = im(address = "me@example.org", protocol = null)
        val other = im(address = "me@example.org", protocol = Im.PROTOCOL_CUSTOM)

        assertTrue(matcher.shouldCollapse(current, other))
        assertTrue(matcher.shouldCollapse(other, current))
    }

    @Test
    fun shouldCollapse_withInvalidProtocolAndKnownProtocol_returnsFalse() {
        val current = im(address = "me@example.org", protocol = null)
        val other = im(address = "me@example.org", protocol = Im.PROTOCOL_AIM)

        assertFalse(matcher.shouldCollapse(current, other))
        assertFalse(matcher.shouldCollapse(other, current))
    }

    @Test
    fun shouldCollapse_withEqualCustomProtocols_returnsTrue() {
        val current = customIm(address = "me@example.org", customProtocol = "matrix")
        val other = customIm(address = "me@example.org", customProtocol = "matrix")

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withDifferentCustomProtocols_returnsFalse() {
        val current = customIm(address = "me@example.org", customProtocol = "matrix")
        val other = customIm(address = "me@example.org", customProtocol = "xmpp")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEventAndNonEventItem_returnsFalse() {
        val current = event(date = "1980-05-20", type = Event.TYPE_BIRTHDAY)
        val other = note(text = "1980-05-20")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEventsWithDifferentDates_returnsFalse() {
        val current = event(date = "1980-05-20", type = Event.TYPE_BIRTHDAY)
        val other = event(date = "1990-01-01", type = Event.TYPE_BIRTHDAY)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEventsWithSameDateAndType_returnsTrue() {
        val current = event(date = "1980-05-20", type = Event.TYPE_BIRTHDAY)
        val other = event(date = "1980-05-20", type = Event.TYPE_BIRTHDAY)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEventsWithDifferentTypes_returnsFalse() {
        val current = event(date = "1980-05-20", type = Event.TYPE_BIRTHDAY)
        val other = event(date = "1980-05-20", type = Event.TYPE_ANNIVERSARY)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEventsWithoutTypes_returnsTrue() {
        val current = event(date = "1980-05-20", type = null)
        val other = event(date = "1980-05-20", type = null)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withEventTypeOnlyOnOneSide_returnsFalse() {
        val current = event(date = "1980-05-20", type = Event.TYPE_BIRTHDAY)
        val other = event(date = "1980-05-20", type = null)

        assertFalse(matcher.shouldCollapse(current, other))
        assertFalse(matcher.shouldCollapse(other, current))
    }

    @Test
    fun shouldCollapse_withCustomEventTypesWithEqualLabels_returnsTrue() {
        val current = event(date = "1980-05-20", type = Event.TYPE_CUSTOM, label = "First day")
        val other = event(date = "1980-05-20", type = Event.TYPE_CUSTOM, label = "First day")

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withCustomEventTypesWithDifferentLabels_returnsFalse() {
        val current = event(date = "1980-05-20", type = Event.TYPE_CUSTOM, label = "First day")
        val other = event(date = "1980-05-20", type = Event.TYPE_CUSTOM, label = "Last day")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withRelationAndNonRelationItem_returnsFalse() {
        val current = relation(name = "Sam", type = Relation.TYPE_FATHER)
        val other = note(text = "Sam")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withRelationsWithDifferentNames_returnsFalse() {
        val current = relation(name = "Sam", type = Relation.TYPE_FATHER)
        val other = relation(name = "Alex", type = Relation.TYPE_FATHER)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withRelationsWithSameNameAndType_returnsTrue() {
        val current = relation(name = "Sam", type = Relation.TYPE_FATHER)
        val other = relation(name = "Sam", type = Relation.TYPE_FATHER)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withRelationsWithDifferentTypes_returnsFalse() {
        val current = relation(name = "Sam", type = Relation.TYPE_FATHER)
        val other = relation(name = "Sam", type = Relation.TYPE_FRIEND)

        assertFalse(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withRelationsWithoutTypes_returnsTrue() {
        val current = relation(name = "Sam", type = null)
        val other = relation(name = "Sam", type = null)

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withRelationTypeOnlyOnOneSide_returnsFalse() {
        val current = relation(name = "Sam", type = Relation.TYPE_FATHER)
        val other = relation(name = "Sam", type = null)

        assertFalse(matcher.shouldCollapse(current, other))
        assertFalse(matcher.shouldCollapse(other, current))
    }

    @Test
    fun shouldCollapse_withCustomRelationTypesWithEqualLabels_returnsTrue() {
        val current = relation(name = "Sam", type = Relation.TYPE_CUSTOM, label = "Neighbour")
        val other = relation(name = "Sam", type = Relation.TYPE_CUSTOM, label = "Neighbour")

        assertTrue(matcher.shouldCollapse(current, other))
    }

    @Test
    fun shouldCollapse_withCustomRelationTypesWithDifferentLabels_returnsFalse() {
        val current = relation(name = "Sam", type = Relation.TYPE_CUSTOM, label = "Neighbour")
        val other = relation(name = "Sam", type = Relation.TYPE_CUSTOM, label = "Landlord")

        assertFalse(matcher.shouldCollapse(current, other))
    }

    private fun assertItemsCollapse(
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ) {
        assertItemsCollapseInAnyOrder(true, mimeType, data, otherMimeType, otherData)
    }

    private fun assertItemsDoNotCollapse(
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ) {
        assertItemsCollapseInAnyOrder(false, mimeType, data, otherMimeType, otherData)
    }

    private fun assertItemsCollapseInAnyOrder(
        expected: Boolean,
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ) {
        val message = "$mimeType/$data vs $otherMimeType/$otherData"
        assertEquals(message, expected, shouldCollapse(mimeType, data, otherMimeType, otherData))
        assertEquals(message, expected, shouldCollapse(otherMimeType, otherData, mimeType, data))
    }

    private fun shouldCollapse(
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ): Boolean {
        return matcher.shouldCollapse(
            collapsibleDataItem(mimeType = mimeType, data = data),
            collapsibleDataItem(mimeType = otherMimeType, data = otherData),
        )
    }

    private fun phone(
        number: String,
        dataKind: DataKind? = collapsibleDataKind(),
    ): DataItem {
        return collapsibleDataItem(
            mimeType = Phone.CONTENT_ITEM_TYPE,
            data = number,
            dataKind = dataKind,
        )
    }

    private fun note(text: String): DataItem {
        return collapsibleDataItem(mimeType = Note.CONTENT_ITEM_TYPE, data = text)
    }

    private fun im(
        address: String,
        protocol: Int?,
    ): DataItem {
        return collapsibleDataItem(
            mimeType = Im.CONTENT_ITEM_TYPE,
            data = address,
            protocol = protocol,
        )
    }

    private fun customIm(
        address: String,
        customProtocol: String,
    ): DataItem {
        return collapsibleDataItem(
            mimeType = Im.CONTENT_ITEM_TYPE,
            data = address,
            protocol = Im.PROTOCOL_CUSTOM,
            customProtocol = customProtocol,
        )
    }

    private fun event(
        date: String,
        type: Int?,
        label: String? = null,
    ): DataItem {
        return collapsibleDataItem(
            mimeType = Event.CONTENT_ITEM_TYPE,
            data = date,
            type = type,
            label = label,
        )
    }

    private fun relation(
        name: String,
        type: Int?,
        label: String? = null,
    ): DataItem {
        return collapsibleDataItem(
            mimeType = Relation.CONTENT_ITEM_TYPE,
            data = name,
            type = type,
            label = label,
        )
    }
}

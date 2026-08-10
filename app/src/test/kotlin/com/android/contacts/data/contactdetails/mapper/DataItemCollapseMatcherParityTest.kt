package com.android.contacts.data.contactdetails.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.tests.factory.collapsibleDataItem
import com.android.contacts.tests.factory.collapsibleDataKind
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DataItemCollapseMatcherParityTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val matcher = DataItemCollapseMatcherImpl(context)

    @Test
    fun shouldCollapse_forEveryPairOfPhoneNumbers_matchesLegacy() {
        assertParity(
            phoneRows(
                "4155551212",
                "14155551212",
                "+14155551212",
                "+1 (415) 555-1212",
                "415-555-1212",
                "4155559999",
                "1800FLOWERS",
                "18003569377",
                "4155551212;123",
                "4155551212;456",
                "#555",
                "*555",
                "555",
                "+49 (8092) 1234",
                "0049 (8092) 1234",
                "",
            ),
        )
    }

    @Test
    fun shouldCollapse_forEveryPairOfPhoneNumbersWithoutData_matchesLegacy() {
        assertParity(
            listOf(
                DataRow(mimeType = Phone.CONTENT_ITEM_TYPE, data = null),
                DataRow(mimeType = Phone.CONTENT_ITEM_TYPE, data = null),
                DataRow(mimeType = Phone.CONTENT_ITEM_TYPE, data = "4155551212"),
            ),
        )
    }

    @Test
    fun shouldCollapse_forEveryPairWithoutDataKind_matchesLegacy() {
        assertParity(
            listOf(
                DataRow(
                    mimeType = Phone.CONTENT_ITEM_TYPE,
                    data = "4155551212",
                    hasDataKind = false,
                ),
                DataRow(mimeType = Phone.CONTENT_ITEM_TYPE, data = "4155551212"),
                DataRow(
                    mimeType = Im.CONTENT_ITEM_TYPE,
                    data = "me@example.org",
                    hasDataKind = false,
                ),
                DataRow(mimeType = Im.CONTENT_ITEM_TYPE, data = "me@example.org"),
            ),
        )
    }

    @Test
    fun shouldCollapse_forEveryPairOfMixedMimeTypes_matchesLegacy() {
        assertParity(
            listOf(
                DataRow(mimeType = Phone.CONTENT_ITEM_TYPE, data = "4155551212"),
                DataRow(mimeType = Email.CONTENT_ITEM_TYPE, data = "user@example.org"),
                DataRow(mimeType = Email.CONTENT_ITEM_TYPE, data = "user@example.org"),
                DataRow(mimeType = Email.CONTENT_ITEM_TYPE, data = "USER@example.org"),
                DataRow(mimeType = Note.CONTENT_ITEM_TYPE, data = "same"),
                DataRow(mimeType = Note.CONTENT_ITEM_TYPE, data = "same"),
                DataRow(mimeType = Note.CONTENT_ITEM_TYPE, data = "other"),
                DataRow(mimeType = Im.CONTENT_ITEM_TYPE, data = "user@example.org"),
                DataRow(mimeType = Event.CONTENT_ITEM_TYPE, data = "1980-05-20"),
                DataRow(mimeType = Relation.CONTENT_ITEM_TYPE, data = "Sam"),
            ),
        )
    }

    @Test
    fun shouldCollapse_forEveryPairOfImItems_matchesLegacy() {
        assertParity(
            listOf(
                imRow(address = "me@example.org", protocol = Im.PROTOCOL_AIM),
                imRow(address = "me@example.org", protocol = Im.PROTOCOL_AIM),
                imRow(address = "me@example.org", protocol = Im.PROTOCOL_JABBER),
                imRow(address = "me@example.org", protocol = null),
                imRow(address = "me@example.org", protocol = Im.PROTOCOL_CUSTOM),
                imRow(
                    address = "me@example.org",
                    protocol = Im.PROTOCOL_CUSTOM,
                    customProtocol = "matrix",
                ),
                imRow(
                    address = "me@example.org",
                    protocol = Im.PROTOCOL_CUSTOM,
                    customProtocol = "xmpp",
                ),
                imRow(address = "other@example.org", protocol = Im.PROTOCOL_AIM),
            ),
        )
    }

    @Test
    fun shouldCollapse_forEveryPairOfEvents_matchesLegacy() {
        assertParity(
            listOf(
                eventRow(date = "1980-05-20", type = Event.TYPE_BIRTHDAY),
                eventRow(date = "1980-05-20", type = Event.TYPE_BIRTHDAY),
                eventRow(date = "1980-05-20", type = Event.TYPE_ANNIVERSARY),
                eventRow(date = "1980-05-20", type = null),
                eventRow(date = "1980-05-20", type = Event.TYPE_CUSTOM, label = "First day"),
                eventRow(date = "1980-05-20", type = Event.TYPE_CUSTOM, label = "Last day"),
                eventRow(date = "1990-01-01", type = Event.TYPE_BIRTHDAY),
            ),
        )
    }

    @Test
    fun shouldCollapse_forEveryPairOfRelations_matchesLegacy() {
        assertParity(
            listOf(
                relationRow(name = "Sam", type = Relation.TYPE_FATHER),
                relationRow(name = "Sam", type = Relation.TYPE_FATHER),
                relationRow(name = "Sam", type = Relation.TYPE_FRIEND),
                relationRow(name = "Sam", type = null),
                relationRow(name = "Sam", type = Relation.TYPE_CUSTOM, label = "Neighbour"),
                relationRow(name = "Sam", type = Relation.TYPE_CUSTOM, label = "Landlord"),
                relationRow(name = "Alex", type = Relation.TYPE_FATHER),
            ),
        )
    }

    private fun assertParity(rows: List<DataRow>) {
        val items = rows.map(::dataItem)

        items.forEachIndexed { index, current ->
            items.forEachIndexed { otherIndex, other ->
                assertEquals(
                    "${rows[index]} vs ${rows[otherIndex]}",
                    current.shouldCollapseWith(other, context),
                    matcher.shouldCollapse(current, other),
                )
            }
        }
    }

    private fun phoneRows(vararg numbers: String): List<DataRow> {
        return numbers.map { number -> DataRow(mimeType = Phone.CONTENT_ITEM_TYPE, data = number) }
    }

    private fun imRow(
        address: String,
        protocol: Int?,
        customProtocol: String? = null,
    ): DataRow {
        return DataRow(
            mimeType = Im.CONTENT_ITEM_TYPE,
            data = address,
            protocol = protocol,
            customProtocol = customProtocol,
        )
    }

    private fun eventRow(
        date: String,
        type: Int?,
        label: String? = null,
    ): DataRow {
        return DataRow(
            mimeType = Event.CONTENT_ITEM_TYPE,
            data = date,
            type = type,
            label = label,
        )
    }

    private fun relationRow(
        name: String,
        type: Int?,
        label: String? = null,
    ): DataRow {
        return DataRow(
            mimeType = Relation.CONTENT_ITEM_TYPE,
            data = name,
            type = type,
            label = label,
        )
    }

    private fun dataItem(dataRow: DataRow): DataItem {
        val dataKind = when {
            dataRow.hasDataKind -> collapsibleDataKind()
            else -> null
        }

        return collapsibleDataItem(
            mimeType = dataRow.mimeType,
            data = dataRow.data,
            type = dataRow.type,
            label = dataRow.label,
            protocol = dataRow.protocol,
            customProtocol = dataRow.customProtocol,
            dataKind = dataKind,
        )
    }

    private data class DataRow(
        val mimeType: String,
        val data: String?,
        val type: Int? = null,
        val label: String? = null,
        val protocol: Int? = null,
        val customProtocol: String? = null,
        val hasDataKind: Boolean = true,
    )
}

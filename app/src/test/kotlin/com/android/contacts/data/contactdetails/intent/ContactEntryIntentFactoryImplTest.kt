package com.android.contacts.data.contactdetails.intent

import android.app.SearchManager
import android.content.Intent
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.Contacts
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ContactEntryIntentFactoryImplTest {

    private val factory = ContactEntryIntentFactoryImpl()

    @Test
    fun create_forACall_buildsATelIntent() {
        val intent = factory.create(ContactEntryAction.Call(number = "555 0001"))

        assertEquals("tel", intent?.data?.scheme)
    }

    @Test
    fun create_forASipCall_buildsASipIntent() {
        val intent = factory.create(ContactEntryAction.SipCall(address = "anna@example.com"))

        assertEquals("sip", intent?.data?.scheme)
    }

    @Test
    fun create_forAnSms_buildsASendToIntent() {
        val intent = factory.create(ContactEntryAction.Sms(number = "555 0001"))

        assertEquals(Intent.ACTION_SENDTO, intent?.action)
        assertEquals("smsto", intent?.data?.scheme)
    }

    @Test
    fun create_forAnEmail_buildsAMailToIntent() {
        val intent = factory.create(ContactEntryAction.SendEmail(address = "anna@example.com"))

        assertEquals(Intent.ACTION_SENDTO, intent?.action)
        assertEquals("mailto", intent?.data?.scheme)
        assertEquals("anna@example.com", intent?.data?.schemeSpecificPart)
    }

    @Test
    fun create_forAGoogleTalkChat_buildsAnXmppIntent() {
        val action = ContactEntryAction.OpenChat(
            data = "anna@example.com",
            protocol = Im.PROTOCOL_GOOGLE_TALK,
            customProtocol = null,
        )

        val intent = factory.create(action)

        assertEquals("xmpp:anna@example.com?message", intent?.data.toString())
    }

    @Test
    fun create_forACustomChat_buildsAnImToIntent() {
        val action = ContactEntryAction.OpenChat(
            data = "anna",
            protocol = Im.PROTOCOL_CUSTOM,
            customProtocol = "ExampleChat",
        )

        val intent = factory.create(action)

        assertEquals("imto", intent?.data?.scheme)
        assertEquals("examplechat", intent?.data?.authority)
    }

    @Test
    fun create_forACustomChatWithoutAProtocol_buildsNothing() {
        val action = ContactEntryAction.OpenChat(
            data = "anna",
            protocol = Im.PROTOCOL_CUSTOM,
            customProtocol = null,
        )

        assertNull(factory.create(action))
    }

    @Test
    fun create_forARelation_buildsAContactSearch() {
        val intent = factory.create(ContactEntryAction.SearchContacts(query = "Alex"))

        assertEquals(Intent.ACTION_SEARCH, intent?.action)
        assertEquals("Alex", intent?.getStringExtra(SearchManager.QUERY))
        assertEquals(Contacts.CONTENT_TYPE, intent?.type)
    }

    @Test
    fun create_forAThirdPartyDataItem_buildsAViewIntentForTheDataRow() {
        val action = ContactEntryAction.ViewDataItem(
            dataId = 21L,
            mimeType = "vnd.example/thing",
        )

        val intent = factory.create(action)

        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals("vnd.example/thing", intent?.type)
        assertTrue(intent?.data.toString().endsWith("/21"))
    }

    @Test
    fun create_forAWebsite_buildsAViewIntentForTheParsedAddress() {
        val intent = factory.create(ContactEntryAction.OpenUrl(url = "example.com/anna"))

        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals("http://example.com/anna", intent?.data.toString())
    }

    @Test
    fun create_forAnEvent_buildsACalendarIntent() {
        val action = ContactEntryAction.ShowEventDate(
            date = "2026-08-15",
            isRecurringAnnually = false,
        )

        val intent = factory.create(action)

        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertTrue(intent?.data.toString().startsWith("content://com.android.calendar/time/"))
    }

    @Test
    fun create_forAnUnparsableEvent_buildsNothing() {
        val action = ContactEntryAction.ShowEventDate(
            date = "not a date",
            isRecurringAnnually = false,
        )

        assertNull(factory.create(action))
    }

    @Test
    fun create_forACallWithNote_buildsNothing() {
        val action = ContactEntryAction.CallWithNote(
            number = "555 0001",
            formattedNumber = null,
            numberLabel = null,
        )

        assertNull(factory.create(action))
    }
}

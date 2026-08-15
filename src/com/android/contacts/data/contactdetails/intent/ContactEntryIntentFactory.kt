package com.android.contacts.data.contactdetails.intent

import android.app.SearchManager
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.telecom.PhoneAccount
import android.util.Log
import androidx.core.net.toUri
import com.android.contacts.CallUtil
import com.android.contacts.ContactsUtils
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.quickcontact.WebAddress
import com.android.contacts.util.DateUtils
import com.android.contacts.util.StructuredPostalUtils
import java.text.ParseException
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

internal interface ContactEntryIntentFactory {
    fun create(action: ContactEntryAction): Intent?
}

internal class ContactEntryIntentFactoryImpl @Inject constructor() : ContactEntryIntentFactory {

    override fun create(action: ContactEntryAction): Intent? {
        return when (action) {
            is ContactEntryAction.Call -> CallUtil.getCallIntent(action.number)
            is ContactEntryAction.Sms -> sendToIntent(ContactsUtils.SCHEME_SMSTO, action.number)
            is ContactEntryAction.SendEmail -> sendToIntent(SCHEME_MAILTO, action.address)
            is ContactEntryAction.ShowOnMap -> mapIntent(action.address)
            is ContactEntryAction.ShowDirections -> directionsIntent(action.address)
            is ContactEntryAction.OpenUrl -> webIntent(action.url)
            is ContactEntryAction.OpenChat -> chatIntent(action)
            is ContactEntryAction.ShowEventDate -> eventIntent(action)
            is ContactEntryAction.SearchContacts -> searchIntent(action.query)
            is ContactEntryAction.ViewDataItem -> dataItemIntent(action)
            is ContactEntryAction.CallWithNote -> null

            is ContactEntryAction.VideoCall -> CallUtil.getVideoCallIntent(
                action.number,
                CALL_ORIGIN,
            )

            is ContactEntryAction.SipCall -> CallUtil.getCallIntent(
                Uri.fromParts(PhoneAccount.SCHEME_SIP, action.address, null),
            )
        }
    }

    private fun sendToIntent(
        scheme: String,
        target: String,
    ): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.fromParts(scheme, target, null))
    }

    private fun mapIntent(address: String): Intent {
        return StructuredPostalUtils.getViewPostalAddressIntent(address)
    }

    private fun directionsIntent(address: String): Intent {
        return StructuredPostalUtils.getViewPostalAddressDirectionsIntent(address)
    }

    private fun webIntent(url: String): Intent? {
        return try {
            Intent(Intent.ACTION_VIEW, WebAddress(url).toString().toUri())
        } catch (e: ParseException) {
            Log.e(TAG, "Could not parse a website entry", e)
            null
        }
    }

    private fun chatIntent(action: ContactEntryAction.OpenChat): Intent? {
        if (action.protocol == Im.PROTOCOL_GOOGLE_TALK) {
            val chatUri = "$SCHEME_XMPP:${action.data}$XMPP_MESSAGE".toUri()

            return Intent(Intent.ACTION_SENDTO, chatUri)
        }

        return imUri(action)?.let { uri -> Intent(Intent.ACTION_SENDTO, uri) }
    }

    private fun imUri(action: ContactEntryAction.OpenChat): Uri? {
        val host = when (action.protocol) {
            Im.PROTOCOL_CUSTOM -> action.customProtocol
            else -> ContactsUtils.lookupProviderNameFromId(action.protocol)
        }

        if (host.isNullOrEmpty()) {
            return null
        }

        return Uri.Builder()
            .scheme(ContactsUtils.SCHEME_IMTO)
            .authority(host.lowercase(Locale.getDefault()))
            .appendPath(action.data)
            .build()
    }

    private fun eventIntent(action: ContactEntryAction.ShowEventDate): Intent? {
        val date = DateUtils.parseDate(action.date, false) ?: return null

        if (action.isRecurringAnnually) {
            date.set(Calendar.YEAR, 0)
        }

        val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        ContentUris.appendId(builder, DateUtils.getNextAnnualDate(date).time)

        return Intent(Intent.ACTION_VIEW).setData(builder.build())
    }

    private fun searchIntent(query: String): Intent {
        return Intent(Intent.ACTION_SEARCH)
            .putExtra(SearchManager.QUERY, query)
            .setType(Contacts.CONTENT_TYPE)
    }

    private fun dataItemIntent(action: ContactEntryAction.ViewDataItem): Intent {
        val dataUri = ContentUris.withAppendedId(Data.CONTENT_URI, action.dataId)

        return Intent(Intent.ACTION_VIEW).setDataAndType(dataUri, action.mimeType)
    }

    private companion object {
        const val TAG = "ContactEntryIntentFactory"
        const val CALL_ORIGIN = "com.android.contacts.ui.contactdetails.ContactDetailsActivity"
        const val SCHEME_MAILTO = "mailto"
        const val SCHEME_XMPP = "xmpp"
        const val XMPP_MESSAGE = "?message"
    }
}

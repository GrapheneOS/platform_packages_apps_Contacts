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
import com.android.contacts.domain.contactdetails.model.ContactEntryAction as Action
import com.android.contacts.quickcontact.WebAddress
import com.android.contacts.util.DateUtils
import com.android.contacts.util.StructuredPostalUtils
import java.text.ParseException
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

internal interface ContactEntryIntentFactory {
    fun create(action: Action): Intent?
}

internal class ContactEntryIntentFactoryImpl @Inject constructor() : ContactEntryIntentFactory {

    override fun create(action: Action): Intent? {
        return when (action) {
            is Action.Call -> callIntent(action.number)
            is Action.CallWithNote -> null
            is Action.EditNumberBeforeCall -> dialIntent(action.number)
            is Action.VideoCall -> videoCallIntent(action.number)
            is Action.SipCall -> sipCallIntent(action.address)
            is Action.Sms -> sendToIntent(ContactsUtils.SCHEME_SMSTO, action.number)
            is Action.SendEmail -> sendToIntent(SCHEME_MAILTO, action.address)
            is Action.OpenChat -> chatIntent(action)
            is Action.ShowOnMap -> mapIntent(action.address)
            is Action.ShowDirections -> directionsIntent(action.address)
            is Action.OpenUrl -> webIntent(action.url)
            is Action.ShowEventDate -> eventIntent(action)
            is Action.SearchContacts -> searchIntent(action.query)
            is Action.ViewDataItem -> dataItemIntent(action)
        }
    }

    private fun callIntent(number: String): Intent {
        return CallUtil.getCallIntent(number)
    }

    private fun dialIntent(number: String): Intent {
        return Intent(Intent.ACTION_DIAL, CallUtil.getCallUri(number))
    }

    private fun videoCallIntent(number: String): Intent {
        return CallUtil.getVideoCallIntent(number)
    }

    private fun sipCallIntent(address: String): Intent {
        return CallUtil.getCallIntent(Uri.fromParts(PhoneAccount.SCHEME_SIP, address, null))
    }

    private fun sendToIntent(
        scheme: String,
        target: String,
    ): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.fromParts(scheme, target, null))
    }

    private fun chatIntent(action: Action.OpenChat): Intent? {
        val chatUri = when (action.protocol) {
            Im.PROTOCOL_GOOGLE_TALK -> xmppUri(action.data)
            else -> imUri(action)
        } ?: return null

        return Intent(Intent.ACTION_SENDTO, chatUri)
    }

    private fun xmppUri(address: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME_XMPP)
            .encodedOpaquePart(address + XMPP_MESSAGE_QUERY)
            .build()
    }

    private fun imUri(action: Action.OpenChat): Uri? {
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

    private fun eventIntent(action: Action.ShowEventDate): Intent? {
        val date = DateUtils.parseDate(action.date, false) ?: return null

        if (action.isRecurringAnnually) {
            date.set(Calendar.YEAR, 0)
        }

        val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath(CALENDAR_TIME_PATH)
        ContentUris.appendId(builder, DateUtils.getNextAnnualDate(date).time)

        return Intent(Intent.ACTION_VIEW).setData(builder.build())
    }

    private fun searchIntent(query: String): Intent {
        return Intent(Intent.ACTION_SEARCH)
            .putExtra(SearchManager.QUERY, query)
            .setType(Contacts.CONTENT_TYPE)
    }

    private fun dataItemIntent(action: Action.ViewDataItem): Intent {
        val dataUri = ContentUris.withAppendedId(Data.CONTENT_URI, action.dataId)

        return Intent(Intent.ACTION_VIEW).setDataAndType(dataUri, action.mimeType)
    }

    private companion object {
        const val TAG = "ContactEntryIntentFactory"
        const val SCHEME_MAILTO = "mailto"
        const val SCHEME_XMPP = "xmpp"
        const val XMPP_MESSAGE_QUERY = "?message"
        const val CALENDAR_TIME_PATH = "time"
    }
}

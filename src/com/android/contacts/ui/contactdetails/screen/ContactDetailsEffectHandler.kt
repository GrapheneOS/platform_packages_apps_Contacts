package com.android.contacts.ui.contactdetails.screen

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Intent
import android.icu.text.MessageFormat
import android.media.RingtoneManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.Intents
import android.telecom.PhoneAccount
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import com.android.contacts.CallUtil
import com.android.contacts.ContactsUtils
import com.android.contacts.R
import com.android.contacts.activities.ContactSelectionActivity
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.dialog.CallSubjectDialog
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.editor.ContactEditorFragment
import com.android.contacts.editor.EditorIntents
import com.android.contacts.editor.EditorUiUtils
import com.android.contacts.interactions.ContactDeletionInteraction
import com.android.contacts.list.UiIntentActions
import com.android.contacts.quickcontact.WebAddress
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
import com.android.contacts.util.DateUtils
import com.android.contacts.util.ImplicitIntentsUtil
import com.android.contacts.util.StructuredPostalUtils
import java.text.ParseException
import java.util.Calendar
import java.util.Locale

internal interface ContactDetailsEffectHandler {
    fun handle(effect: Effect)
}

internal class ContactDetailsEffectHandlerImpl(
    private val activity: Activity,
    private val clipboardManager: ClipboardManager,
    private val joinTargetLauncher: ActivityResultLauncher<Intent>,
    private val ringtoneLauncher: ActivityResultLauncher<Intent>,
    private val editorLauncher: ActivityResultLauncher<Intent>,
    private val directoryCopyLauncher: ActivityResultLauncher<Intent>,
) : ContactDetailsEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.EditContact -> editContact(effect)
            is Effect.AddDirectoryContact -> addDirectoryContact(effect.prefill)
            is Effect.ConfirmDelete -> confirmDelete(effect.lookupUri)
            is Effect.ShareContact -> shareContact(effect.lookupKey)
            is Effect.PickRingtone -> pickRingtone(effect.currentRingtone)
            is Effect.PickJoinTarget -> pickJoinTarget(effect.contactId)
            is Effect.ViewLinkedContacts -> viewLinkedContacts(effect.lookupUri)
            is Effect.PerformEntryAction -> performEntryAction(effect.action)
            is Effect.CallWithNote -> callWithNote(effect)
            is Effect.CopyToClipboard -> copyToClipboard(effect.label, effect.text)
        }
    }

    private fun editContact(effect: Effect.EditContact) {
        val intent = EditorIntents.createEditContactIntent(
            activity,
            effect.lookupUri,
            null,
            effect.photoId,
        )

        editorLauncher.launch(intent)
    }

    private fun addDirectoryContact(prefill: DirectoryContactPrefill) {
        directoryCopyLauncher.launch(directoryContactIntent(prefill))
    }

    private fun directoryContactIntent(prefill: DirectoryContactPrefill): Intent {
        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
            .setType(Contacts.CONTENT_ITEM_TYPE)
            .setPackage(activity.packageName)
            .putParcelableArrayListExtra(Intents.Insert.DATA, ArrayList(prefill.values))

        if (prefill.name != null) {
            intent.putExtra(Intents.Insert.NAME, prefill.name)
        }

        val account = prefill.account
        if (account != null) {
            intent.putExtra(Intents.Insert.EXTRA_ACCOUNT, account)
            intent.putExtra(Intents.Insert.EXTRA_DATA_SET, prefill.dataSet)
        }

        return intent.putExtra(ContactEditorFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION, true)
    }

    private fun confirmDelete(lookupUri: Uri) {
        ContactDeletionInteraction.start(activity, lookupUri, true)
    }

    private fun shareContact(lookupKey: String) {
        val shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey)
        val intent = Intent(Intent.ACTION_SEND)
            .setType(Contacts.CONTENT_VCARD_TYPE)
            .putExtra(Intent.EXTRA_STREAM, shareUri)

        try {
            ImplicitIntentsUtil.startActivityOutsideApp(
                activity,
                Intent.createChooser(intent, shareTitle()),
            )
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.share_error, e)
        }
    }

    private fun shareTitle(): CharSequence {
        val format = MessageFormat(
            activity.getString(R.string.title_share_via),
            Locale.getDefault(),
        )

        return format.format(mapOf("count" to 1))
    }

    private fun pickRingtone(currentRingtone: String?) {
        val ringtoneUri = EditorUiUtils.getRingtoneUriFromString(currentRingtone)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri)

        try {
            ringtoneLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.missing_app, e)
        }
    }

    private fun pickJoinTarget(contactId: Long) {
        val intent = Intent(activity, ContactSelectionActivity::class.java)
            .setAction(UiIntentActions.PICK_JOIN_CONTACT_ACTION)
            .putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, contactId)

        joinTargetLauncher.launch(intent)
    }

    private fun viewLinkedContacts(lookupUri: Uri) {
        val intent = EditorIntents.createViewLinkedContactsIntent(
            activity,
            lookupUri,
            null,
        )

        editorLauncher.launch(intent)
    }

    private fun callWithNote(effect: Effect.CallWithNote) {
        CallSubjectDialog.start(
            activity,
            effect.photoId,
            effect.photoUri?.let(Uri::parse),
            effect.lookupUri,
            effect.displayName,
            false,
            effect.number,
            effect.displayNumber,
            effect.numberLabel,
            null,
        )
    }

    private fun copyToClipboard(
        label: String?,
        text: String,
    ) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun performEntryAction(action: ContactEntryAction) {
        val intent = entryActionIntent(action) ?: return

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ImplicitIntentsUtil.startActivityInAppIfPossible(activity, intent)
        } catch (e: SecurityException) {
            showToast(R.string.missing_app, e)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.missing_app, e)
        }
    }

    private fun entryActionIntent(action: ContactEntryAction): Intent? {
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

        val host = when (action.protocol) {
            Im.PROTOCOL_CUSTOM -> action.customProtocol
            else -> ContactsUtils.lookupProviderNameFromId(action.protocol)
        }

        if (host.isNullOrEmpty()) {
            return null
        }

        val imUri = Uri.Builder()
            .scheme(ContactsUtils.SCHEME_IMTO)
            .authority(host.lowercase(Locale.getDefault()))
            .appendPath(action.data)
            .build()

        return Intent(Intent.ACTION_SENDTO, imUri)
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

    private fun showToast(
        messageResource: Int,
        cause: Exception,
    ) {
        Log.w(TAG, "Could not handle a contact details effect", cause)
        Toast.makeText(activity, messageResource, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val TAG = "ContactDetailsEffectHandler"
        const val CALL_ORIGIN = "com.android.contacts.ui.contactdetails.ContactDetailsActivity"
        const val SCHEME_MAILTO = "mailto"
        const val SCHEME_XMPP = "xmpp"
        const val XMPP_MESSAGE = "?message"
    }
}

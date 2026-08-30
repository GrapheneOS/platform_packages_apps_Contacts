package com.android.contacts.ui.contactdetails.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Intent
import android.icu.text.MessageFormat
import android.media.RingtoneManager
import android.net.Uri
import android.provider.CallLog.Calls
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Groups
import android.provider.ContactsContract.Intents
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import com.android.contacts.R
import com.android.contacts.activities.ContactSelectionActivity
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.dialog.CallSubjectDialog
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.editor.ContactEditorFragment
import com.android.contacts.editor.EditorIntents
import com.android.contacts.editor.EditorUiUtils
import com.android.contacts.interactions.ContactDeletionInteraction
import com.android.contacts.list.UiIntentActions
import com.android.contacts.ui.contactdetails.ContactDetailsLaunchers
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
import com.android.contacts.util.ImplicitIntentsUtil
import java.util.Locale

internal interface ContactDetailsEffectHandler {
    fun handle(effect: Effect)
}

internal class ContactDetailsEffectHandlerImpl(
    private val activity: Activity,
    private val clipboardManager: ClipboardManager,
    private val contactEntryIntentFactory: ContactEntryIntentFactory,
    private val launchers: ContactDetailsLaunchers,
) : ContactDetailsEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.EditContact -> editContact(effect)
            is Effect.AddDirectoryContact -> addDirectoryContact(effect.prefill)
            is Effect.ConfirmDelete -> confirmDelete(effect.lookupUri)
            is Effect.ShareContact -> shareContact(effect.lookupKey)
            is Effect.ViewCallLog -> viewCallLog()
            is Effect.PickRingtone -> pickRingtone(effect.currentRingtone)
            is Effect.PickJoinTarget -> pickJoinTarget(effect.contactId)
            is Effect.ViewGroupMembers -> viewGroupMembers(effect.groupId)
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

        launchers.editor.launch(intent)
    }

    private fun addDirectoryContact(prefill: DirectoryContactPrefill) {
        launchers.directoryCopy.launch(directoryContactIntent(prefill))
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
            launchers.ringtone.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.missing_app, e)
        }
    }

    private fun pickJoinTarget(contactId: Long) {
        val intent = Intent(activity, ContactSelectionActivity::class.java)
            .setAction(UiIntentActions.PICK_JOIN_CONTACT_ACTION)
            .putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, contactId)

        launchers.joinTarget.launch(intent)
    }

    private fun viewCallLog() {
        val telecomManager = activity.getSystemService(TelecomManager::class.java)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE)
            .setPackage(telecomManager?.defaultDialerPackage)

        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Could not open the call log", e)
        }
    }

    private fun viewGroupMembers(groupId: Long) {
        val groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId)
        val intent = Intent(Intent.ACTION_VIEW, groupUri)
            .setClass(activity, PeopleActivity::class.java)

        activity.startActivity(intent)
    }

    private fun viewLinkedContacts(lookupUri: Uri) {
        val intent = EditorIntents.createViewLinkedContactsIntent(
            activity,
            lookupUri,
            null,
        )

        launchers.editor.launch(intent)
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
        val intent = contactEntryIntentFactory.create(action) ?: return

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ImplicitIntentsUtil.startActivityInAppIfPossible(activity, intent)
        } catch (e: SecurityException) {
            showToast(R.string.missing_app, e)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.missing_app, e)
        }
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

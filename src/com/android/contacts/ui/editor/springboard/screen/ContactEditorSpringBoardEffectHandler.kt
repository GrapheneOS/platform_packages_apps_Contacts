package com.android.contacts.ui.editor.springboard.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.android.contacts.ContactSaveService
import com.android.contacts.R
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.activities.ContactSelectionActivity
import com.android.contacts.editor.EditorIntents
import com.android.contacts.list.UiIntentActions
import com.android.contacts.quickcontact.QuickContactActivity
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardEffect as Effect
import com.android.contacts.util.ImplicitIntentsUtil

internal interface ContactEditorSpringBoardEffectHandler {
    fun handle(effect: Effect)
}

internal class ContactEditorSpringBoardEffectHandlerImpl(
    private val activity: Activity,
    private val contactSelectLauncher: ActivityResultLauncher<Intent>,
    private val originalExtras: Bundle,
) : ContactEditorSpringBoardEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> close()
            Effect.ShowErrorAndClose -> showErrorAndClose()
            is Effect.EditContact -> editContact(effect.uri, effect.rawContactId)
            is Effect.CreateContact -> createContact(effect.uri)
            is Effect.SelectContactToJoin -> selectContactToJoin(effect.contactId)
            is Effect.UnlinkRawContacts -> unlinkRawContacts(effect.rawContactIds)
            is Effect.JoinContacts -> joinContacts(effect.contactId1, effect.contactId2)
        }
    }

    private fun close() {
        activity.finish()
    }

    private fun showErrorAndClose() {
        Toast.makeText(
            activity,
            R.string.editor_failed_to_load,
            Toast.LENGTH_SHORT,
        ).show()
        activity.setResult(Activity.RESULT_CANCELED)
        close()
    }

    private fun editContact(uri: Uri, rawContactId: Long) {
        val intent = EditorIntents.createEditContactIntentForRawContact(
            activity,
            uri,
            rawContactId,
            null,
        )
            .setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            .putExtras(originalExtras)
        ImplicitIntentsUtil.startActivityInApp(activity, intent)
        close()
    }

    private fun createContact(uri: Uri) {
        val intent = EditorIntents.createEditContactIntent(
            activity,
            uri,
            null,
            -1,
        )
            .setClass(activity, ContactEditorActivity::class.java)
            .putExtras(originalExtras)
        ImplicitIntentsUtil.startActivityInApp(activity, intent)
        close()
    }

    private fun selectContactToJoin(contactId: Long) {
        val intent = Intent(activity, ContactSelectionActivity::class.java)
            .setAction(UiIntentActions.PICK_JOIN_CONTACT_ACTION)
            .putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, contactId)
        contactSelectLauncher.launch(intent)
    }

    private fun unlinkRawContacts(rawContactIds: List<Long>) {
        val ids = rawContactIds.map { arrayOf(it).toLongArray() }.toTypedArray()
        val intent = ContactSaveService.createHardSplitContactIntent(activity, ids)
        activity.startService(intent)
        close()
    }

    private fun joinContacts(contactId1: Long, contactId2: Long) {
        val intent = ContactSaveService.createJoinContactsIntent(
            activity,
            contactId1,
            contactId2,
            QuickContactActivity::class.java,
            Intent.ACTION_VIEW,
        )
        activity.startService(intent)
        close()
    }
}

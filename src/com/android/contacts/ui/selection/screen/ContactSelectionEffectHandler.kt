package com.android.contacts.ui.selection.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import android.util.Log
import android.widget.Toast
import com.android.contacts.R
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.di.core.MainImmediateDispatcher
import com.android.contacts.editor.EditorIntents
import com.android.contacts.list.UiIntentActions
import com.android.contacts.ui.interactions.shortcut.BuildShortcutIntent
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import com.android.contacts.util.ImplicitIntentsUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface ContactSelectionEffectHandler {
    fun handle(effect: Effect)
}

internal class ContactSelectionEffectHandlerImpl(
    private val activity: Activity,
    private val originalExtras: Bundle,
    private val buildShortcutIntent: BuildShortcutIntent,
    @param:MainImmediateDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ContactSelectionEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close ->
                close(effect.result)
            is Effect.ForwardToEditor.CreateContact ->
                forwardToEditor(createContactIntent())
            is Effect.ForwardToEditor.EditContact ->
                forwardToEditor(editContactIntent(effect.lookupUri))
            is Effect.SendTo ->
                sendTo(effect)
        }
    }

    private fun close(result: Result) {
        when (result) {
            is Result.Shortcut -> createShortcut(result)
            else -> finishWith(result.toIntent())
        }
    }

    private fun Result.toIntent(): Intent? {
        return when (this) {
            is Result.Picked ->
                Intent().setData(uri)
            is Result.PickedMultiple ->
                clipData(uris, mimeType)?.let { Intent().apply { clipData = it } }
            is Result.GroupMember ->
                Intent().putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, contactId)
            is Result.GroupMembers ->
                Intent().putExtra(
                    UiIntentActions.TARGET_CONTACT_IDS_EXTRA_KEY,
                    contactIds.toLongArray(),
                )
            is Result.Shortcut,
            Result.Canceled,
            -> null
        }
    }

    private fun clipData(uris: List<Uri>, mimeType: String): ClipData? {
        val first = uris.firstOrNull() ?: return null
        return ClipData("", arrayOf(mimeType), ClipData.Item(first)).apply {
            uris.drop(1).forEach { addItem(ClipData.Item(it)) }
        }
    }

    private fun createShortcut(result: Result.Shortcut) {
        CoroutineScope(mainDispatcher).launch {
            val intent = when (result.target) {
                ShortcutTarget.CONTACT -> buildShortcutIntent.forContact(result.uri)
                ShortcutTarget.CALL -> buildShortcutIntent.forCall(result.uri)
                ShortcutTarget.MESSAGE -> buildShortcutIntent.forMessage(result.uri)
            }
            if (!activity.isFinishing && !activity.isDestroyed) {
                finishWith(intent)
            }
        }
    }

    private fun finishWith(intent: Intent?) {
        when {
            intent != null -> {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.setResult(Activity.RESULT_OK, intent)
            }
            else -> {
                activity.setResult(Activity.RESULT_CANCELED)
            }
        }
        activity.finish()
    }

    private fun createContactIntent(): Intent {
        return Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI)
            .putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true)
    }

    private fun editContactIntent(lookupUri: Uri): Intent {
        return EditorIntents.createEditContactIntent(activity, lookupUri, null, -1)
    }

    // The editor answers our caller directly, with the caller's extras
    private fun forwardToEditor(intent: Intent) {
        intent
            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            .putExtras(this@ContactSelectionEffectHandlerImpl.originalExtras)
        try {
            ImplicitIntentsUtil.startActivityInApp(activity, intent)
        } catch (e: ActivityNotFoundException) {
            handleActivityNotFound(intent, e)
        }
        activity.finish()
    }

    private fun sendTo(effect: Effect.SendTo) {
        val uri = Uri.fromParts(effect.target.scheme, effect.addresses.joinToString(","), null)
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
        val chooserIntent = Intent.createChooser(sendIntent, effect.chooserTitle)
        try {
            activity.startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            handleActivityNotFound(sendIntent, e)
        }
        activity.finish()
    }

    private fun handleActivityNotFound(
        intent: Intent,
        exception: ActivityNotFoundException,
    ) {
        Log.e(TAG, "Could not start $intent", exception)
        Toast.makeText(activity, R.string.missing_app, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val TAG = "ContactSelectionEffect"
    }
}

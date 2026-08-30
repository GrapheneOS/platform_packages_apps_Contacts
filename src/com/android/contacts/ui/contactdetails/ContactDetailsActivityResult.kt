package com.android.contacts.ui.contactdetails

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.core.content.IntentCompat
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.editor.EditorUiUtils
import com.android.contacts.interactions.ContactDeletionInteraction
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action

internal fun ActivityResult.isContactGone(): Boolean {
    return resultCode == ContactDeletionInteraction.RESULT_CODE_DELETED ||
        resultCode == ContactEditorActivity.RESULT_CODE_SPLIT
}

internal fun ActivityResult.toRebindIntent(): Intent? {
    return data?.takeIf { resultCode != Activity.RESULT_CANCELED }
}

internal fun ActivityResult.toJoinTargetAction(): Action? {
    if (resultCode != Activity.RESULT_OK) {
        return null
    }

    val contactUri = data?.data ?: return null

    return Action.JoinTargetPicked(ContentUris.parseId(contactUri))
}

internal fun ActivityResult.toRingtoneAction(): Action? {
    val data = data ?: return null
    val pickedUri = IntentCompat.getParcelableExtra(
        data,
        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
        Uri::class.java,
    )

    return Action.RingtonePicked(
        EditorUiUtils.getRingtoneStringFromUri(pickedUri),
    )
}

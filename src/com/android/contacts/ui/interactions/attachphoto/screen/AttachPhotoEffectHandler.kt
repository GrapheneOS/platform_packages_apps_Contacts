package com.android.contacts.ui.interactions.attachphoto.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoAction as Action
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoEffect as Effect
import com.android.contacts.util.ContactPhotoUtils

internal interface AttachPhotoEffectHandler {
    fun handle(effect: Effect)
}

internal class AttachPhotoEffectHandlerImpl(
    private val activity: Activity,
    private val pickContactLauncher: ActivityResultLauncher<Intent>,
    private val cropPhotoLauncher: ActivityResultLauncher<Intent>,
    private val pickDefaultAccountLauncher: ActivityResultLauncher<Intent>,
    private val onAction: (Action) -> Unit,
) : AttachPhotoEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            Effect.PickContact -> pickContact()
            is Effect.CropPhoto -> cropPhoto(effect)
            Effect.PickDefaultAccount -> pickDefaultAccount()
            is Effect.Close -> close(effect.isSuccess)
        }
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK)
            .setType(Contacts.CONTENT_TYPE)
            .setPackage(activity.packageName)
        pickContactLauncher.launch(intent)
    }

    private fun cropPhoto(effect: Effect.CropPhoto) {
        val intent = Intent(ACTION_CROP).apply {
            when {
                effect.mimeType != null ->
                    setDataAndType(effect.inputUri, effect.mimeType)
                else ->
                    data = effect.inputUri
            }
            ContactPhotoUtils.addPhotoPickerExtras(this, effect.outputUri)
            ContactPhotoUtils.addCropExtras(this, effect.size)
        }

        val handler = resolveCropHandler(intent) ?: run {
            onAction(Action.CropUnavailable)
            return
        }
        intent.setPackage(handler.activityInfo.packageName)

        try {
            cropPhotoLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Could not launch crop activity", e)
            onAction(Action.CropUnavailable)
        }
    }

    private fun resolveCropHandler(intent: Intent): ResolveInfo? {
        return activity.packageManager
            .queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY,
            )
            .firstOrNull()
    }

    private fun pickDefaultAccount() {
        val intent = Intent(Settings.ACTION_SET_DEFAULT_ACCOUNT)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pickDefaultAccountLauncher.launch(intent)
    }

    private fun close(isSuccess: Boolean) {
        activity.setResult(if (isSuccess) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        activity.finish()
    }

    private companion object {
        const val TAG = "AttachPhotoEffectHandler"
        const val ACTION_CROP = "com.android.camera.action.CROP"
    }
}

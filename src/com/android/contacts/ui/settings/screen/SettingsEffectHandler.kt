package com.android.contacts.ui.settings.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Settings as ContactsContractSettings
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.android.contacts.activities.LicenseActivity
import com.android.contacts.compat.TelecomManagerUtil
import com.android.contacts.interactions.ExportDialogFragment
import com.android.contacts.interactions.ImportDialogFragment
import com.android.contacts.list.AccountFilterActivity
import com.android.contacts.logging.ScreenEvent.ScreenType
import com.android.contacts.ui.settings.SettingsActivity
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import com.android.contacts.util.ImplicitIntentsUtil

internal interface SettingsEffectHandler {
    fun handle(effect: Effect.Host)
}

internal class SettingsEffectHandlerImpl(
    private val activity: Activity,
    private val newLocalProfileExtra: String?,
    private val telecomManager: TelecomManager,
    private val clipboardManager: ClipboardManager,
    private val contactsFilterLauncher: ActivityResultLauncher<Intent>,
) : SettingsEffectHandler {

    override fun handle(effect: Effect.Host) {
        when (effect) {
            is Effect.OpenProfile -> openProfile(effect.contactId)
            is Effect.CopyBuildVersion -> copyBuildVersion(effect.buildVersion)
            is Effect.CreateProfile -> createProfile()
            is Effect.OpenAddAccount -> openAddAccount()
            is Effect.OpenDefaultAccountPicker -> openDefaultAccountPicker()
            is Effect.OpenContactsFilter -> openContactsFilter()
            is Effect.ShowImportDialog -> showImportDialog()
            is Effect.ShowExportDialog -> showExportDialog()
            is Effect.OpenBlockedNumbers -> openBlockedNumbers()
            is Effect.OpenLicenses -> openLicenses()
            is Effect.OpenAppPermissions -> openAppPermissions()
        }
    }

    private fun openProfile(contactId: Long) {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)

        ImplicitIntentsUtil.startQuickContact(activity, contactUri, ScreenType.ME_CONTACT)
    }

    private fun copyBuildVersion(buildVersion: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, buildVersion))
    }

    private fun createProfile() {
        val intent = Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI)
        newLocalProfileExtra?.let { intent.putExtra(it, true) }

        ImplicitIntentsUtil.startActivityInApp(activity, intent)
    }

    private fun openAddAccount() {
        ImplicitIntentsUtil.startActivityOutsideApp(
            activity,
            ImplicitIntentsUtil.getIntentForAddingAccount(),
        )
    }

    private fun openDefaultAccountPicker() {
        startActivity(Intent(ContactsContractSettings.ACTION_SET_DEFAULT_ACCOUNT))
    }

    private fun openContactsFilter() {
        contactsFilterLauncher.launch(Intent(activity, AccountFilterActivity::class.java))
    }

    private fun showImportDialog() {
        ImportDialogFragment.show(activity.fragmentManager)
    }

    private fun showExportDialog() {
        ExportDialogFragment.show(
            activity.fragmentManager,
            SettingsActivity::class.java,
            ExportDialogFragment.EXPORT_MODE_ALL_CONTACTS,
        )
    }

    private fun openBlockedNumbers() {
        val intent = TelecomManagerUtil.createManageBlockedNumbersIntent(telecomManager) ?: return

        startActivity(intent)
    }

    private fun openAppPermissions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", activity.packageName, null))

        startActivity(intent)
    }

    private fun openLicenses() {
        startActivity(Intent(activity, LicenseActivity::class.java))
    }

    private fun startActivity(intent: Intent) {
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Could not start $intent", e)
        }
    }

    private companion object {
        const val TAG = "SettingsEffectHandler"
    }
}

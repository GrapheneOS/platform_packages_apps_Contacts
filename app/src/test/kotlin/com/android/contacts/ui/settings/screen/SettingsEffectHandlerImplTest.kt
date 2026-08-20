package com.android.contacts.ui.settings.screen

import android.app.Activity
import android.app.FragmentManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Intent
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Settings as ContactsContractSettings
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.activity.result.ActivityResultLauncher
import com.android.contacts.activities.LicenseActivity
import com.android.contacts.interactions.ExportDialogFragment
import com.android.contacts.interactions.ImportDialogFragment
import com.android.contacts.list.AccountFilterActivity
import com.android.contacts.logging.ScreenEvent.ScreenType
import com.android.contacts.ui.settings.SettingsActivity
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import com.android.contacts.util.ImplicitIntentsUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SettingsEffectHandlerImplTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val telecomManager = mockk<TelecomManager>()
    private val clipboardManager = mockk<ClipboardManager>(relaxed = true)
    private val contactsFilterLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val fragmentManager = mockk<FragmentManager>(relaxed = true)

    private val effectHandler = SettingsEffectHandlerImpl(
        activity = activity,
        newLocalProfileExtra = NEW_LOCAL_PROFILE_EXTRA,
        telecomManager = telecomManager,
        clipboardManager = clipboardManager,
        contactsFilterLauncher = contactsFilterLauncher,
    )

    @Before
    fun setUp() {
        mockkStatic(ImplicitIntentsUtil::class)
        mockkStatic(ImportDialogFragment::class)
        mockkStatic(ExportDialogFragment::class)
        every { activity.fragmentManager } returns fragmentManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun openProfile_opensQuickContactForTheProfile() {
        effectHandler.handle(Effect.OpenProfile(contactId = 7L))

        verify {
            ImplicitIntentsUtil.startQuickContact(
                activity,
                ContentUris.withAppendedId(Contacts.CONTENT_URI, 7L),
                ScreenType.ME_CONTACT,
            )
        }
    }

    @Test
    fun copyBuildVersion_putsTheVersionOnTheClipboard() {
        val clipSlot = slot<ClipData>()

        effectHandler.handle(Effect.CopyBuildVersion(BUILD_VERSION))

        verify { clipboardManager.setPrimaryClip(capture(clipSlot)) }
        assertEquals(BUILD_VERSION, clipSlot.captured.getItemAt(0).text)
    }

    @Test
    fun createProfile_startsContactInsertWithTheProfileExtra() {
        val intentSlot = slot<Intent>()

        effectHandler.handle(Effect.CreateProfile)

        verify { ImplicitIntentsUtil.startActivityInApp(activity, capture(intentSlot)) }
        assertEquals(Intent.ACTION_INSERT, intentSlot.captured.action)
        assertEquals(Contacts.CONTENT_URI, intentSlot.captured.data)
        assertTrue(intentSlot.captured.getBooleanExtra(NEW_LOCAL_PROFILE_EXTRA, false))
    }

    @Test
    fun openAddAccount_startsTheSystemAccountScreen() {
        effectHandler.handle(Effect.OpenAddAccount)

        verify { ImplicitIntentsUtil.startActivityOutsideApp(activity, any()) }
    }

    @Test
    fun openDefaultAccountPicker_startsTheSystemPicker() {
        effectHandler.handle(Effect.OpenDefaultAccountPicker)

        assertEquals(
            ContactsContractSettings.ACTION_SET_DEFAULT_ACCOUNT,
            startedIntent().action,
        )
    }

    @Test
    fun openContactsFilter_launchesTheFilterForResult() {
        val intentSlot = slot<Intent>()

        effectHandler.handle(Effect.OpenContactsFilter)

        verify { contactsFilterLauncher.launch(capture(intentSlot)) }
        assertEquals(
            AccountFilterActivity::class.java.name,
            intentSlot.captured.component?.className,
        )
    }

    @Test
    fun showImportDialog_showsTheLegacyDialog() {
        effectHandler.handle(Effect.ShowImportDialog)

        verify { ImportDialogFragment.show(fragmentManager) }
    }

    @Test
    fun showExportDialog_showsTheLegacyDialogHostedByTheSettings() {
        effectHandler.handle(Effect.ShowExportDialog)

        verify {
            ExportDialogFragment.show(
                fragmentManager,
                SettingsActivity::class.java,
                ExportDialogFragment.EXPORT_MODE_ALL_CONTACTS,
            )
        }
    }

    @Test
    fun openBlockedNumbers_startsTheTelecomScreen() {
        val blockedNumbersIntent = Intent("blocked_numbers")
        every { telecomManager.createManageBlockedNumbersIntent() }
            .returns(blockedNumbersIntent)

        effectHandler.handle(Effect.OpenBlockedNumbers)

        verify { activity.startActivity(blockedNumbersIntent) }
    }

    @Test
    fun openBlockedNumbers_whenTelecomHasNoIntent_startsNothing() {
        every { telecomManager.createManageBlockedNumbersIntent() }
            .returns(null)

        effectHandler.handle(Effect.OpenBlockedNumbers)

        verify(exactly = 0) { activity.startActivity(any()) }
    }

    @Test
    fun openAppPermissions_startsTheSystemAppDetails() {
        every { activity.packageName } returns "com.android.contacts"

        effectHandler.handle(Effect.OpenAppPermissions)

        val intent = startedIntent()
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:com.android.contacts", intent.data.toString())
    }

    @Test
    fun openLicenses_startsTheLicenseScreen() {
        effectHandler.handle(Effect.OpenLicenses)

        assertEquals(
            LicenseActivity::class.java.name,
            startedIntent().component?.className,
        )
    }

    private fun startedIntent(): Intent {
        val intentSlot = slot<Intent>()
        verify { activity.startActivity(capture(intentSlot)) }

        return intentSlot.captured
    }

    private companion object {
        const val NEW_LOCAL_PROFILE_EXTRA = "newLocalProfile"
        const val BUILD_VERSION = "1.7.40"
    }
}

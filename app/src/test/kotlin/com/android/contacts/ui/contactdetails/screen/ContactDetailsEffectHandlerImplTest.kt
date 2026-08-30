package com.android.contacts.ui.contactdetails.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.CallLog.Calls
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Intents
import android.telecom.TelecomManager
import androidx.activity.result.ActivityResultLauncher
import com.android.contacts.activities.ContactEditorSpringBoardActivity
import com.android.contacts.activities.ContactSelectionActivity
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.list.UiIntentActions
import com.android.contacts.ui.contactdetails.ContactDetailsLaunchers
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
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
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsEffectHandlerImplTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val clipboardManager = mockk<ClipboardManager>(relaxed = true)
    private val contactEntryIntentFactory = mockk<ContactEntryIntentFactory>()
    private val joinTargetLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val ringtoneLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val editorLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val directoryCopyLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)

    private val handler = ContactDetailsEffectHandlerImpl(
        activity = activity,
        clipboardManager = clipboardManager,
        contactEntryIntentFactory = contactEntryIntentFactory,
        launchers = ContactDetailsLaunchers(
            editor = editorLauncher,
            directoryCopy = directoryCopyLauncher,
            joinTarget = joinTargetLauncher,
            ringtone = ringtoneLauncher,
        ),
    )

    @Before
    fun setUp() {
        every { activity.packageName } returns "com.android.contacts"
        every { activity.getString(any()) } answers {
            RuntimeEnvironment.getApplication().getString(firstArg())
        }
        mockkStatic(ImplicitIntentsUtil::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun editContact_launchesTheEditor() {
        handler.handle(Effect.EditContact(lookupUri = LOOKUP_URI, photoId = 11L))

        val intent = launchedIntent(editorLauncher)

        assertEquals(Intent.ACTION_EDIT, intent.action)
        assertEquals(LOOKUP_URI, intent.data)
    }

    @Test
    fun addDirectoryContact_launchesAPrefilledInsertOrEdit() {
        val prefill = DirectoryContactPrefill(
            name = "Alex Doe",
            values = emptyList(),
            account = null,
            dataSet = null,
        )

        handler.handle(Effect.AddDirectoryContact(prefill))

        val intent = launchedIntent(directoryCopyLauncher)

        assertEquals(Intent.ACTION_INSERT_OR_EDIT, intent.action)
        assertEquals(Contacts.CONTENT_ITEM_TYPE, intent.type)
        assertEquals("com.android.contacts", intent.`package`)
        assertEquals("Alex Doe", intent.getStringExtra(Intents.Insert.NAME))
    }

    @Test
    fun pickJoinTarget_launchesTheContactSelectionActivity() {
        handler.handle(Effect.PickJoinTarget(contactId = 7L))

        val intent = launchedIntent(joinTargetLauncher)

        assertEquals(UiIntentActions.PICK_JOIN_CONTACT_ACTION, intent.action)
        assertEquals(
            ContactSelectionActivity::class.java.name,
            intent.component?.className,
        )
        assertEquals(7L, intent.getLongExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, -1L))
    }

    @Test
    fun pickRingtone_launchesThePickerWithTheCurrentRingtone() {
        handler.handle(Effect.PickRingtone(currentRingtone = "content://tone/1"))

        val intent = launchedIntent(ringtoneLauncher)

        assertEquals(RingtoneManager.ACTION_RINGTONE_PICKER, intent.action)
        assertTrue(intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false))
        assertTrue(intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false))
        assertEquals(
            RingtoneManager.TYPE_RINGTONE,
            intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1),
        )
    }

    @Test
    fun shareContact_launchesAChooserForTheVCard() {
        handler.handle(Effect.ShareContact(lookupKey = "lookup-key"))

        val intent = startedOutsideAppIntent()

        assertEquals(Intent.ACTION_CHOOSER, intent.action)
    }

    @Test
    fun copyToClipboard_putsTheTextOnTheClipboard() {
        val clip = slot<ClipData>()
        every { clipboardManager.setPrimaryClip(capture(clip)) } returns Unit

        handler.handle(Effect.CopyToClipboard(label = "Phone", text = "555 0001"))

        assertEquals("555 0001", clip.captured.getItemAt(0).text)
        assertEquals("Phone", clip.captured.description.label)
    }

    @Test
    fun performEntryAction_startsTheIntentTheFactoryBuilt() {
        val action = ContactEntryAction.Call(number = "555 0001")
        val built = Intent(Intent.ACTION_VIEW)
        every { contactEntryIntentFactory.create(action) } returns built

        handler.handle(Effect.PerformEntryAction(action))

        assertEquals(built, startedInAppIntent())
    }

    @Test
    fun performEntryAction_withoutAnIntent_startsNothing() {
        val action = ContactEntryAction.Call(number = "555 0001")
        every { contactEntryIntentFactory.create(action) } returns null

        handler.handle(Effect.PerformEntryAction(action))

        verify(exactly = 0) { ImplicitIntentsUtil.startActivityInAppIfPossible(any(), any()) }
    }

    @Test
    fun viewGroupMembers_opensTheGroupInTheContactsList() {
        handler.handle(Effect.ViewGroupMembers(groupId = 11L))

        val intent = startedIntent()

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("content://com.android.contacts/groups/11", intent.data.toString())
        assertEquals(PeopleActivity::class.java.name, intent.component?.className)
    }

    @Test
    fun viewLinkedContacts_launchesTheEditorInReadOnlyMode() {
        handler.handle(Effect.ViewLinkedContacts(lookupUri = LOOKUP_URI))

        val intent = launchedIntent(editorLauncher)

        assertEquals(LOOKUP_URI, intent.data)
        assertTrue(
            intent.getBooleanExtra(
                ContactEditorSpringBoardActivity.EXTRA_SHOW_READ_ONLY,
                false,
            ),
        )
    }

    @Test
    fun viewCallLog_opensTheCallLogInTheDefaultDialer() {
        givenDefaultDialer("com.example.dialer")

        handler.handle(Effect.ViewCallLog)

        val intent = startedIntent()

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Calls.CONTENT_TYPE, intent.type)
        assertEquals("com.example.dialer", intent.`package`)
    }

    @Test
    fun viewCallLog_whenNoDialerHandlesIt_reportsNoError() {
        givenDefaultDialer("com.example.dialer")
        every { activity.startActivity(any()) } throws ActivityNotFoundException()

        handler.handle(Effect.ViewCallLog)

        verify { activity.startActivity(any()) }
    }

    @Test
    fun shareContact_whenNoAppHandlesTheChooser_reportsNoError() {
        every {
            ImplicitIntentsUtil.startActivityOutsideApp(any(), any())
        } throws ActivityNotFoundException()

        handler.handle(Effect.ShareContact(lookupKey = "lookup-key"))

        verify { ImplicitIntentsUtil.startActivityOutsideApp(activity, any()) }
    }

    @Test
    fun pickRingtone_whenNoPickerIsInstalled_reportsNoError() {
        every { ringtoneLauncher.launch(any()) } throws ActivityNotFoundException()

        handler.handle(Effect.PickRingtone(currentRingtone = null))

        verify { ringtoneLauncher.launch(any()) }
    }

    @Test
    fun performEntryAction_whenNoAppHandlesTheIntent_reportsNoError() {
        val action = ContactEntryAction.Call(number = "555 0001")
        every { contactEntryIntentFactory.create(action) } returns Intent(Intent.ACTION_VIEW)
        every {
            ImplicitIntentsUtil.startActivityInAppIfPossible(any(), any())
        } throws ActivityNotFoundException()

        handler.handle(Effect.PerformEntryAction(action))

        verify { ImplicitIntentsUtil.startActivityInAppIfPossible(activity, any()) }
    }

    @Test
    fun performEntryAction_whenStartingIsNotPermitted_reportsNoError() {
        val action = ContactEntryAction.Call(number = "555 0001")
        every { contactEntryIntentFactory.create(action) } returns Intent(Intent.ACTION_VIEW)
        every {
            ImplicitIntentsUtil.startActivityInAppIfPossible(any(), any())
        } throws SecurityException("not permitted")

        handler.handle(Effect.PerformEntryAction(action))

        verify { ImplicitIntentsUtil.startActivityInAppIfPossible(activity, any()) }
    }

    private fun givenDefaultDialer(packageName: String) {
        val telecomManager = mockk<TelecomManager>()
        every { telecomManager.defaultDialerPackage } returns packageName
        every { activity.getSystemService(TelecomManager::class.java) } returns telecomManager
    }

    private fun launchedIntent(launcher: ActivityResultLauncher<Intent>): Intent {
        val intent = slot<Intent>()
        verify { launcher.launch(capture(intent)) }

        return intent.captured
    }

    private fun startedIntent(): Intent {
        val intent = slot<Intent>()
        verify { activity.startActivity(capture(intent)) }

        return intent.captured
    }

    private fun startedInAppIntent(): Intent {
        val intent = slot<Intent>()
        verify { ImplicitIntentsUtil.startActivityInAppIfPossible(activity, capture(intent)) }

        return intent.captured
    }

    private fun startedOutsideAppIntent(): Intent {
        val intent = slot<Intent>()
        verify { ImplicitIntentsUtil.startActivityOutsideApp(activity, capture(intent)) }

        return intent.captured
    }

    private companion object {
        val LOOKUP_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")
    }
}

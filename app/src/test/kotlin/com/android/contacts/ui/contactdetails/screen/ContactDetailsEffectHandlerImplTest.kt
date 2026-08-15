package com.android.contacts.ui.contactdetails.screen

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Intents
import androidx.activity.result.ActivityResultLauncher
import com.android.contacts.activities.ContactSelectionActivity
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.list.UiIntentActions
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
        joinTargetLauncher = joinTargetLauncher,
        ringtoneLauncher = ringtoneLauncher,
        editorLauncher = editorLauncher,
        directoryCopyLauncher = directoryCopyLauncher,
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

        handler.handle(entryEffect(action))

        assertEquals(built, startedInAppIntent())
    }

    @Test
    fun performEntryAction_withoutAnIntent_startsNothing() {
        val action = ContactEntryAction.Call(number = "555 0001")
        every { contactEntryIntentFactory.create(action) } returns null

        handler.handle(entryEffect(action))

        verify(exactly = 0) { ImplicitIntentsUtil.startActivityInAppIfPossible(any(), any()) }
    }

    private fun entryEffect(action: ContactEntryAction): Effect {
        return Effect.PerformEntryAction(action)
    }

    private fun launchedIntent(launcher: ActivityResultLauncher<Intent>): Intent {
        val intent = slot<Intent>()
        verify { launcher.launch(capture(intent)) }

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

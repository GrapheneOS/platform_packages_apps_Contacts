package com.android.contacts.ui.selection.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import androidx.core.net.toUri
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.list.UiIntentActions
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.editor.springboard.ContactEditorSpringBoardActivity
import com.android.contacts.ui.interactions.shortcut.BuildShortcutIntent
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactSelectionEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val buildShortcutIntent = mockk<BuildShortcutIntent>()

    @Before
    fun setUp() {
        every { activity.packageName } returns PACKAGE_NAME
        every { activity.isFinishing } returns false
        every { activity.isDestroyed } returns false
        every { activity.resources } returns targetContext.resources
        every { activity.applicationContext } returns targetContext
    }

    // Close

    @Test
    fun closePicked_returnsUriWithReadPermission() {
        handler().handle(Effect.Close(Result.Picked(CONTACT_URI)))

        val intent = resultIntent()
        assertEquals(CONTACT_URI, intent.data)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        verify { activity.finish() }
    }

    @Test
    fun closePickedMultiple_returnsClipDataWithAllUris() {
        val uris = listOf("content://contacts/1".toUri(), "content://contacts/2".toUri())

        handler().handle(Effect.Close(Result.PickedMultiple(uris, Contacts.CONTENT_ITEM_TYPE)))

        val clipData = resultIntent().clipData!!
        assertEquals(Contacts.CONTENT_ITEM_TYPE, clipData.description.getMimeType(0))
        assertEquals(uris, (0 until clipData.itemCount).map { clipData.getItemAt(it).uri })
        verify { activity.finish() }
    }

    @Test
    fun closePickedMultiple_withoutUris_cancels() {
        val result = Result.PickedMultiple(emptyList(), Contacts.CONTENT_ITEM_TYPE)

        handler().handle(Effect.Close(result))

        verifyCanceled()
    }

    @Test
    fun closeGroupMember_returnsContactId() {
        handler().handle(Effect.Close(Result.GroupMember(42L)))

        val intent = resultIntent()
        assertEquals(42L, intent.getLongExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, -1L))
    }

    @Test
    fun closeGroupMembers_returnsContactIds() {
        handler().handle(Effect.Close(Result.GroupMembers(setOf(1L, 2L))))

        val intent = resultIntent()
        assertArrayEquals(
            longArrayOf(1L, 2L),
            intent.getLongArrayExtra(UiIntentActions.TARGET_CONTACT_IDS_EXTRA_KEY),
        )
    }

    @Test
    fun closeCanceled_cancels() {
        handler().handle(Effect.Close(Result.Canceled))

        verifyCanceled()
    }

    // Shortcuts

    @Test
    fun closeShortcut_returnsShortcutIntentForTarget() {
        val shortcuts = mapOf(
            ShortcutTarget.CONTACT to Intent("contact-shortcut"),
            ShortcutTarget.CALL to Intent("call-shortcut"),
            ShortcutTarget.MESSAGE to Intent("message-shortcut"),
        )
        coEvery { buildShortcutIntent.forContact(CONTACT_URI) } returns
            shortcuts.getValue(ShortcutTarget.CONTACT)
        coEvery { buildShortcutIntent.forCall(CONTACT_URI) } returns
            shortcuts.getValue(ShortcutTarget.CALL)
        coEvery { buildShortcutIntent.forMessage(CONTACT_URI) } returns
            shortcuts.getValue(ShortcutTarget.MESSAGE)

        shortcuts.forEach { (target, expected) ->
            handler().handle(Effect.Close(Result.Shortcut(target, CONTACT_URI)))

            verify { activity.setResult(Activity.RESULT_OK, expected) }
        }
        verify(exactly = shortcuts.size) { activity.finish() }
    }

    @Test
    fun closeShortcut_whenShortcutCannotBeBuilt_cancels() {
        coEvery { buildShortcutIntent.forContact(any()) } returns null

        handler().handle(Effect.Close(Result.Shortcut(ShortcutTarget.CONTACT, CONTACT_URI)))

        verifyCanceled()
    }

    @Test
    fun closeShortcut_whenActivityIsFinishing_doesNotSetResult() {
        coEvery { buildShortcutIntent.forContact(any()) } returns Intent("contact-shortcut")
        every { activity.isFinishing } returns true

        handler().handle(Effect.Close(Result.Shortcut(ShortcutTarget.CONTACT, CONTACT_URI)))

        coVerify { buildShortcutIntent.forContact(CONTACT_URI) }
        verify(exactly = 0) { activity.setResult(any()) }
        verify(exactly = 0) { activity.setResult(any(), any()) }
        verify(exactly = 0) { activity.finish() }
    }

    // Editor

    @Test
    fun createContact_forwardsInsertToEditor() {
        handler().handle(Effect.ForwardToEditor.CreateContact)

        val intent = startedIntent()
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(Contacts.CONTENT_URI, intent.data)
        assertTrue(
            intent.getBooleanExtra(
                ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED,
                false,
            ),
        )
        assertForwardedInApp(intent)
        verify { activity.finish() }
    }

    @Test
    fun editContact_forwardsEditToEditor() {
        handler().handle(Effect.ForwardToEditor.EditContact(CONTACT_URI))

        val intent = startedIntent()
        assertEquals(Intent.ACTION_EDIT, intent.action)
        assertEquals(CONTACT_URI, intent.data)
        assertEquals(ContactEditorSpringBoardActivity::class.java.name, intent.component?.className)
        assertForwardedInApp(intent)
        verify { activity.finish() }
    }

    @Test
    fun forwardToEditor_includesCopyOfOriginalExtras() {
        val originalExtras = Bundle().apply { putString(EXTRA_KEY, "original") }
        val handler = handler(originalExtras = originalExtras)
        originalExtras.putString(EXTRA_KEY, "changed")

        handler.handle(Effect.ForwardToEditor.CreateContact)

        assertEquals("original", startedIntent().getStringExtra(EXTRA_KEY))
    }

    @Test
    fun forwardToEditor_whenEditorIsMissing_showsToastAndFinishes() {
        every { activity.startActivity(any()) } throws ActivityNotFoundException()

        handler().handle(Effect.ForwardToEditor.CreateContact)

        assertEquals(MISSING_APP_MESSAGE, ShadowToast.getTextOfLatestToast())
        verify { activity.finish() }
    }

    // Send to

    @Test
    fun sendTo_startsChooserForAddresses() {
        val addresses = listOf("+1 555 0100", "+1 555 0101")

        handler().handle(Effect.SendTo(SentToGroupTarget.SMS, addresses, chooserTitle = "Send"))

        val chooser = startedIntent()
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals("Send", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
        val sendIntent = chooser.sendIntent()
        assertEquals(Intent.ACTION_SENDTO, sendIntent.action)
        assertEquals("smsto", sendIntent.data?.scheme)
        assertEquals("+1 555 0100,+1 555 0101", sendIntent.data?.schemeSpecificPart)
        verify { activity.finish() }
    }

    @Test
    fun sendTo_usesTargetScheme() {
        handler().handle(Effect.SendTo(SentToGroupTarget.EMAIL, listOf("john@example.com"), null))

        assertEquals(
            Uri.fromParts("mailto", "john@example.com", null),
            startedIntent().sendIntent().data,
        )
    }

    @Test
    fun sendTo_whenNoAppIsAvailable_showsToastAndFinishes() {
        every { activity.startActivity(any()) } throws ActivityNotFoundException()

        handler().handle(Effect.SendTo(SentToGroupTarget.SMS, listOf("+1 555 0100"), null))

        assertEquals(MISSING_APP_MESSAGE, ShadowToast.getTextOfLatestToast())
        verify { activity.finish() }
    }

    private fun handler(originalExtras: Bundle = Bundle()): ContactSelectionEffectHandler {
        return ContactSelectionEffectHandlerImpl(
            activity = activity,
            originalExtras = originalExtras,
            buildShortcutIntent = buildShortcutIntent,
            mainDispatcher = UnconfinedTestDispatcher(),
        )
    }

    private fun resultIntent(): Intent {
        val intent = slot<Intent>()
        verify { activity.setResult(Activity.RESULT_OK, capture(intent)) }
        verify { activity.finish() }

        return intent.captured
    }

    private fun startedIntent(): Intent {
        val intent = slot<Intent>()
        verify { activity.startActivity(capture(intent)) }

        return intent.captured
    }

    private fun verifyCanceled() {
        verify { activity.setResult(Activity.RESULT_CANCELED) }
        verify(exactly = 0) { activity.setResult(any(), any()) }
        verify { activity.finish() }
    }

    private fun assertForwardedInApp(intent: Intent) {
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_FORWARD_RESULT != 0)
        assertEquals(PACKAGE_NAME, intent.`package`)
    }

    @Suppress("DEPRECATION")
    private fun Intent.sendIntent(): Intent = getParcelableExtra(Intent.EXTRA_INTENT)!!

    private companion object {
        const val PACKAGE_NAME = "com.android.contacts"
        const val EXTRA_KEY = "extra_key"
        const val MISSING_APP_MESSAGE = "No app was found to handle this action."
        val CONTACT_URI = "content://com.android.contacts/contacts/lookup/key/1".toUri()
    }
}

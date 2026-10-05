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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ContactEditorSpringBoardEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val contactSelectLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val originalExtras = Bundle().also { it.putString("extra", "value") }

    private val effectHandler = ContactEditorSpringBoardEffectHandlerImpl(
        activity = activity,
        contactSelectLauncher = contactSelectLauncher,
        originalExtras = originalExtras,
    )

    @Before
    fun setUp() {
        mockkStatic(ImplicitIntentsUtil::class)
        mockkStatic(ContactSaveService::class)
        mockkStatic(EditorIntents::class)
        mockkStatic(Toast::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun close_finishes() {
        effectHandler.handle(Effect.Close)

        finished()
    }

    @Test
    fun showErrorAndClose_showsToastAndFinishes() {
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<Int>(), any()) } returns toast

        effectHandler.handle(Effect.ShowErrorAndClose)

        verify { Toast.makeText(activity, R.string.editor_failed_to_load, any()) }
        verify { toast.show() }
        finished()
    }

    @Test
    fun editContact_startsEditContactForRawContact() {
        val intent = mockIntent()
        every {
            EditorIntents.createEditContactIntentForRawContact(any(), any(), any(), any())
        } returns intent

        val uri = mockk<Uri>()
        val rawContactId = 123L
        effectHandler.handle(Effect.EditContact(uri, rawContactId))

        verify {
            EditorIntents.createEditContactIntentForRawContact(activity, uri, rawContactId, null)
        }
        verify { intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT) }
        verify { intent.putExtras(originalExtras) }
        assertEquals(intent, startedIntentInApp())
        finished()
    }

    @Test
    fun createContact_startsEditContact() {
        val intent = mockIntent()
        every {
            EditorIntents.createEditContactIntent(any(), any(), any(), any())
        } returns intent

        val uri = mockk<Uri>()
        effectHandler.handle(Effect.CreateContact(uri))

        verify {
            EditorIntents.createEditContactIntent(activity, uri, null, -1)
        }
        verify { intent.setClass(activity, ContactEditorActivity::class.java) }
        verify { intent.putExtras(originalExtras) }
        assertEquals(intent, startedIntentInApp())
        finished()
    }

    @Test
    fun selectContactToJoin_startsContactSelectionActivity() {
        val contactId = 123L
        effectHandler.handle(Effect.SelectContactToJoin(contactId))

        val intentSlot = slot<Intent>()
        verify { contactSelectLauncher.launch(capture(intentSlot)) }
        val intent = intentSlot.captured

        assertEquals(ContactSelectionActivity::class.java.name, intent.component?.className)
        assertEquals(UiIntentActions.PICK_JOIN_CONTACT_ACTION, intent.action)
        assertEquals(contactId, intent.extras?.getLong(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY))
    }

    @Test
    fun unlinkRawContacts_startsContactSaveServiceToSplit() {
        val intent = mockIntent()
        every { ContactSaveService.createHardSplitContactIntent(any(), any()) } returns intent

        val contactIds = listOf(1L, 2L, 3L)
        effectHandler.handle(Effect.UnlinkRawContacts(contactIds))

        verify {
            ContactSaveService.createHardSplitContactIntent(
                activity,
                arrayOf(
                    arrayOf(1L).toLongArray(),
                    arrayOf(2L).toLongArray(),
                    arrayOf(3L).toLongArray(),
                ),
            )
        }
        assertEquals(intent, startedServiceIntent())
        finished()
    }

    @Test
    fun joinContacts_startsContactSaveServiceToJoin() {
        val intent = mockIntent()
        every {
            ContactSaveService.createJoinContactsIntent(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns intent

        val contactId1 = 1L
        val contactId2 = 2L
        effectHandler.handle(Effect.JoinContacts(contactId1, contactId2))

        verify {
            ContactSaveService.createJoinContactsIntent(
                activity,
                contactId1,
                contactId2,
                QuickContactActivity::class.java,
                Intent.ACTION_VIEW,
            )
        }
        assertEquals(intent, startedServiceIntent())
        finished()
    }

    private fun startedIntentInApp(): Intent {
        val intentSlot = slot<Intent>()
        verify { ImplicitIntentsUtil.startActivityInApp(activity, capture(intentSlot)) }
        return intentSlot.captured
    }

    private fun startedServiceIntent(): Intent {
        val intentSlot = slot<Intent>()
        verify { activity.startService(capture(intentSlot)) }
        return intentSlot.captured
    }

    private fun finished() {
        verify { activity.finish() }
    }

    private fun mockIntent(): Intent {
        return mockk<Intent>(relaxed = true) {
            every { setAction(any()) } returns this
            every { setClass(any(), any()) } returns this
            every { setFlags(any()) } returns this
            every { putExtras(any<Bundle>()) } returns this
            every { putExtra(any(), any<Long>()) } returns this
        }
    }
}

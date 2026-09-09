package com.android.contacts.ui.interactions.showorcreate.screen

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateEffect as Effect
import com.android.contacts.util.ImplicitIntentsUtil
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
class ShowOrCreateEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val intentSlot = slot<Intent>()

    private val effectHandler: ShowOrCreateEffectHandler = ShowOrCreateEffectHandlerImpl(
        activity = activity,
    )

    @Before
    fun setUp() {
        mockkStatic(ImplicitIntentsUtil::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun close_finishesActivity() {
        effectHandler.handle(Effect.Close)

        verify { activity.finish() }
    }

    @Test
    fun showContact_startsViewIntentAndFinishesActivity() {
        val uri = "content://1".toUri()
        effectHandler.handle(Effect.ShowContact(uri))

        verify { ImplicitIntentsUtil.startActivityInApp(activity, capture(intentSlot)) }
        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(uri, intent.data)
        verify { activity.finish() }
    }

    @Test
    fun showContactList_startsSearchIntentAndFinishesActivity() {
        val extras = Bundle().apply { putString("key", "value") }
        effectHandler.handle(Effect.ShowContactList(extras))

        verify { activity.startActivity(capture(intentSlot)) }
        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_SEARCH, intent.action)
        assertEquals(ComponentName(activity, PeopleActivity::class.java), intent.component)
        assertEquals(1, intent.extras!!.size())
        assertEquals("value", intent.extras!!.getString("key"))
        verify { activity.finish() }
    }

    @Test
    fun createContact_startsInsertIntentAndFinishesActivity() {
        val extras = Bundle().apply { putString("key", "value") }
        effectHandler.handle(Effect.CreateContact(extras))

        verify { ImplicitIntentsUtil.startActivityInApp(activity, capture(intentSlot)) }
        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(ContactsContract.RawContacts.CONTENT_URI, intent.data)
        assertEquals(ContactsContract.RawContacts.CONTENT_TYPE, intent.type)
        assertEquals(1, intent.extras!!.size())
        assertEquals("value", intent.extras!!.getString("key"))
        verify { activity.finish() }
    }

    @Test
    fun createOrEditContact_startsInsertOrEditIntentAndFinishesActivity() {
        val extras = Bundle().apply { putString("key", "value") }
        effectHandler.handle(Effect.CreateOrEditContact(extras))

        verify { ImplicitIntentsUtil.startActivityInApp(activity, capture(intentSlot)) }
        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_INSERT_OR_EDIT, intent.action)
        assertEquals(ContactsContract.RawContacts.CONTENT_ITEM_TYPE, intent.type)
        assertEquals(1, intent.extras!!.size())
        assertEquals("value", intent.extras!!.getString("key"))
        verify { activity.finish() }
    }
}

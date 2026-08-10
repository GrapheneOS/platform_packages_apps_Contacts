package com.android.contacts.data.contactdetails.repository

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.cash.turbine.test
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactActionsRepositoryImplTest {

    private val context = spyk<Context>(RuntimeEnvironment.getApplication())
    private val localBroadcastManager = mockk<LocalBroadcastManager>(relaxed = true)
    private val intentSlot = slot<Intent>()
    private val receiverSlot = slot<BroadcastReceiver>()
    private val filterSlot = slot<IntentFilter>()

    private val repository = ContactActionsRepositoryImpl(
        context = context,
        localBroadcastManager = localBroadcastManager,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        every { context.startService(capture(intentSlot)) } returns null
        every {
            localBroadcastManager.registerReceiver(capture(receiverSlot), capture(filterSlot))
        } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun setStarred_startsTheSaveServiceWithTheStarredFlag() = runTest {
        repository.setStarred(LOOKUP_URI, isStarred = true)

        val intent = intentSlot.captured
        assertEquals(ContactSaveService.ACTION_SET_STARRED, intent.action)
        assertEquals(LOOKUP_URI, intent.getParcelableExtra(ContactSaveService.EXTRA_CONTACT_URI))
        assertTrue(intent.getBooleanExtra(ContactSaveService.EXTRA_STARRED_FLAG, false))
    }

    @Test
    fun setStarred_whenUnstarring_startsTheSaveServiceWithTheClearedFlag() = runTest {
        repository.setStarred(LOOKUP_URI, isStarred = false)

        assertFalse(
            intentSlot.captured.getBooleanExtra(ContactSaveService.EXTRA_STARRED_FLAG, true),
        )
    }

    @Test
    fun setRingtone_startsTheSaveServiceWithTheRingtone() = runTest {
        repository.setRingtone(LOOKUP_URI, ringtone = "content://media/ringtone/7")

        val intent = intentSlot.captured
        assertEquals(ContactSaveService.ACTION_SET_RINGTONE, intent.action)
        assertEquals(LOOKUP_URI, intent.getParcelableExtra(ContactSaveService.EXTRA_CONTACT_URI))
        assertEquals(
            "content://media/ringtone/7",
            intent.getStringExtra(ContactSaveService.EXTRA_CUSTOM_RINGTONE),
        )
    }

    @Test
    fun setRingtone_withoutARingtone_startsTheSaveServiceWithoutOne() = runTest {
        repository.setRingtone(LOOKUP_URI, ringtone = null)

        assertNull(intentSlot.captured.getStringExtra(ContactSaveService.EXTRA_CUSTOM_RINGTONE))
    }

    @Test
    fun setSuperPrimary_startsTheSaveServiceForTheDataRow() = runTest {
        repository.setSuperPrimary(DATA_ID)

        val intent = intentSlot.captured
        assertEquals(ContactSaveService.ACTION_SET_SUPER_PRIMARY, intent.action)
        assertEquals(DATA_ID, intent.getLongExtra(ContactSaveService.EXTRA_DATA_ID, 0L))
    }

    @Test
    fun clearPrimary_startsTheSaveServiceForTheDataRow() = runTest {
        repository.clearPrimary(DATA_ID)

        val intent = intentSlot.captured
        assertEquals(ContactSaveService.ACTION_CLEAR_PRIMARY, intent.action)
        assertEquals(DATA_ID, intent.getLongExtra(ContactSaveService.EXTRA_DATA_ID, 0L))
    }

    @Test
    fun joinContacts_startsTheSaveServiceForBothContacts() = runTest {
        repository.joinContacts(
            contactId = CONTACT_ID,
            otherContactId = OTHER_CONTACT_ID,
            callbackActivity = Activity::class.java,
        )

        val intent = intentSlot.captured
        assertEquals(ContactSaveService.ACTION_JOIN_CONTACTS, intent.action)
        assertEquals(CONTACT_ID, intent.getLongExtra(ContactSaveService.EXTRA_CONTACT_ID1, 0L))
        assertEquals(
            OTHER_CONTACT_ID,
            intent.getLongExtra(ContactSaveService.EXTRA_CONTACT_ID2, 0L),
        )
    }

    @Test
    fun observeLinkOperations_whenLinkingCompletes_emitsLink() = runTest {
        repository.observeLinkOperations().test {
            sendBroadcast(ContactSaveService.BROADCAST_LINK_COMPLETE)

            assertEquals(ContactLinkOperation.LINK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeLinkOperations_whenUnlinkingCompletes_emitsUnlink() = runTest {
        repository.observeLinkOperations().test {
            sendBroadcast(ContactSaveService.BROADCAST_UNLINK_COMPLETE)

            assertEquals(ContactLinkOperation.UNLINK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeLinkOperations_forAnotherBroadcast_emitsNothing() = runTest {
        repository.observeLinkOperations().test {
            sendBroadcast(ContactSaveService.BROADCAST_SERVICE_STATE_CHANGED)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeLinkOperations_registersForBothCompletionBroadcasts() = runTest {
        repository.observeLinkOperations().test {
            val filter = filterSlot.captured

            assertTrue(filter.hasAction(ContactSaveService.BROADCAST_LINK_COMPLETE))
            assertTrue(filter.hasAction(ContactSaveService.BROADCAST_UNLINK_COMPLETE))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeLinkOperations_whenCollectionStops_unregistersTheReceiver() = runTest {
        repository.observeLinkOperations().test {
            cancelAndIgnoreRemainingEvents()
        }

        verify { localBroadcastManager.unregisterReceiver(receiverSlot.captured) }
    }

    @Test
    fun getPendingLinkOperation_whileSplitIsPending_returnsUnlink() {
        givenPendingActions(ContactSaveService.ACTION_SPLIT_CONTACT)

        assertEquals(ContactLinkOperation.UNLINK, repository.getPendingLinkOperation())
    }

    @Test
    fun getPendingLinkOperation_whileJoinIsPending_returnsLink() {
        givenPendingActions(ContactSaveService.ACTION_JOIN_CONTACTS)

        assertEquals(ContactLinkOperation.LINK, repository.getPendingLinkOperation())
    }

    @Test
    fun getPendingLinkOperation_withoutPendingActions_returnsNull() {
        givenPendingActions()

        assertNull(repository.getPendingLinkOperation())
    }

    private fun givenPendingActions(vararg actions: String) {
        val state = mockk<ContactSaveService.State>()
        every { state.isActionPending(any()) } answers { firstArg<String>() in actions }
        mockkStatic(ContactSaveService::class)
        every { ContactSaveService.getState() } returns state
    }

    private fun sendBroadcast(action: String) {
        receiverSlot.captured.onReceive(context, Intent(action))
    }

    private companion object {
        val LOOKUP_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")
        const val CONTACT_ID = 7L
        const val OTHER_CONTACT_ID = 11L
        const val DATA_ID = 42L
    }
}

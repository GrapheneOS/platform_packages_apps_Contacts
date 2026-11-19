package com.android.contacts.ui.group.list.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.android.contacts.group.GroupUtil
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.group.list.GroupsActivity
import com.android.contacts.ui.group.list.screen.model.GroupsEffect as Effect
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GroupsEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)

    private val effectHandler: GroupsEffectHandler = GroupsEffectHandlerImpl(
        activity = activity,
    )

    @Before
    fun setUp() {
        mockkObject(GroupNameEditActivity.Companion)
        mockkObject(GroupActivity.Companion)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun whenClose_finishesActivity() {
        effectHandler.handle(Effect.Close)

        verify { activity.finish() }
    }

    @Test
    fun whenCreateNewGroup_startsGroupNameActivity() {
        val intent = Intent()
        every { GroupNameEditActivity.buildCreateIntent(any(), any(), any(), any()) } returns intent
        val account = AccountModelFactory.build()

        effectHandler.handle(Effect.CreateNewGroup(account))

        verify {
            GroupNameEditActivity.buildCreateIntent(
                activity,
                account,
                GroupsActivity::class.java,
                GroupUtil.ACTION_CREATE_GROUP,
            )
        }
        assertEquals(
            intent,
            startedIntent(),
        )
    }

    @Test
    fun whenOpenGroup_opensTheGroupScreen() {
        val uri = mockk<Uri>()
        val intent = Intent()
        every { GroupActivity.buildIntent(any(), any<Uri>()) } returns intent

        effectHandler.handle(Effect.OpenGroup(uri))

        verify { GroupActivity.buildIntent(activity, uri) }
        assertEquals(
            intent,
            startedIntent(),
        )
    }

    @Test
    fun whenOpenGroup_closesTheGroupsScreen() {
        every { GroupActivity.buildIntent(any(), any<Uri>()) } returns Intent()

        effectHandler.handle(Effect.OpenGroup(mockk()))

        verify { activity.finish() }
    }

    private fun startedIntent(): Intent {
        val intentSlot = slot<Intent>()
        verify { activity.startActivity(capture(intentSlot)) }

        return intentSlot.captured
    }
}

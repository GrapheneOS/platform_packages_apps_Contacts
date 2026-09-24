package com.android.contacts.ui.group.list.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.group.GroupUtil
import com.android.contacts.tests.AccountModelFactory
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

    private val effectHandler = GroupsEffectHandlerImpl(
        activity = activity,
    )

    @Before
    fun setUp() {
        mockkObject(GroupNameEditActivity.Companion)
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
        verify { activity.startActivityForResult(intent, GroupsActivity.REQUEST_CREATE_GROUP) }
        verify { activity.finish() }
    }

    @Test
    fun whenOpenGroup_opensGroupsScreen() {
        val groupUri = mockk<Uri>()

        effectHandler.handle(Effect.OpenGroup(groupUri))

        val intent = startedIntent()
        assertEquals(PeopleActivity::class.java.name, intent.component?.className)
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(groupUri, intent.data)
        verify { activity.finish() }
    }

    private fun startedIntent(): Intent {
        val intentSlot = slot<Intent>()
        verify { activity.startActivity(capture(intentSlot)) }

        return intentSlot.captured
    }
}

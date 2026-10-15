package com.android.contacts.ui.group.edit.screen

import android.app.Activity
import android.content.Intent
import com.android.contacts.ContactSaveService
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.mapper.AccountModelMapperImpl
import com.android.contacts.group.GroupUtil
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect
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
internal class GroupNameEditEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)

    private val accountModelMapper: AccountModelMapper = AccountModelMapperImpl()
    private val effectHandler = GroupNameEditEffectHandlerImpl(
        accountModelMapper = accountModelMapper,
        activity = activity,
        callbackActivity = CALLBACK_ACTIVITY,
        callbackAction = CALLBACK_ACTION,
    )

    @Before
    fun setUp() {
        mockkStatic(ContactSaveService::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun closeSuccessful() {
        effectHandler.handle(Effect.Close(isSuccessful = true))

        verify { activity.setResult(Activity.RESULT_OK) }
        verify { activity.finish() }
    }

    @Test
    fun closeUnsuccessful() {
        effectHandler.handle(Effect.Close(isSuccessful = false))

        verify { activity.setResult(Activity.RESULT_CANCELED) }
        verify { activity.finish() }
    }

    @Test
    fun createGroup() {
        val account = AccountModelFactory.build()
        val intent = Intent()
        every {
            ContactSaveService.createNewGroupIntent(any(), any(), any(), any(), any(), any())
        } returns intent

        effectHandler.handle(Effect.CreateGroup(account = account, groupName = "Name"))

        verify {
            ContactSaveService.createNewGroupIntent(
                activity,
                accountModelMapper.map(account),
                "Name",
                null,
                CALLBACK_ACTIVITY,
                CALLBACK_ACTION,
            )
        }
        assertEquals(intent, startedService())
    }

    @Test
    fun editGroup() {
        val intent = Intent()
        every {
            ContactSaveService.createGroupRenameIntent(any(), any(), any(), any(), any())
        } returns intent

        effectHandler.handle(Effect.EditGroup(groupId = 123L, groupName = "New Name"))

        verify {
            ContactSaveService.createGroupRenameIntent(
                activity,
                123L,
                "New Name",
                CALLBACK_ACTIVITY,
                CALLBACK_ACTION,
            )
        }
        assertEquals(intent, startedService())
    }

    private fun startedService(): Intent {
        val intentSlot = slot<Intent>()
        verify { ContactSaveService.startService(activity, capture(intentSlot)) }
        return intentSlot.captured
    }

    private companion object {
        private val CALLBACK_ACTIVITY = PeopleActivity::class.java
        private const val CALLBACK_ACTION = GroupUtil.ACTION_UPDATE_GROUP
    }
}

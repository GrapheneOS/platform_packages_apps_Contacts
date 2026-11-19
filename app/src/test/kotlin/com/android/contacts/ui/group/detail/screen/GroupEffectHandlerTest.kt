package com.android.contacts.ui.group.detail.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Groups
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import com.android.contacts.R
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.group.GroupUtil
import com.android.contacts.list.UiIntentActions
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupFactory
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.detail.screen.model.GroupEffect as Effect
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.selection.ContactSelectionActivity
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget
import com.android.contacts.util.ImplicitIntentsUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val pickMembersLauncher = mockk<ActivityResultLauncher<Intent>>(relaxed = true)
    private val snackbarHostState = SnackbarHostState()

    private val effectHandler = GroupEffectHandlerImpl(
        activity = activity,
        pickMembersLauncher = pickMembersLauncher,
        snackbarHostState = snackbarHostState,
        coroutineScope = TestScope(UnconfinedTestDispatcher()),
    )

    @Before
    fun setUp() {
        every { activity.packageName } returns "com.android.contacts"
        every { activity.getString(any()) } answers {
            targetContext.getString(firstArg())
        }
        mockkStatic(ImplicitIntentsUtil::class)
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
    fun whenOpenContact_startsQuickContact() {
        every { ImplicitIntentsUtil.startQuickContact(any(), any()) } returns Unit

        effectHandler.handle(Effect.OpenContact(CONTACT_URI))

        verify { ImplicitIntentsUtil.startQuickContact(activity, CONTACT_URI) }
    }

    @Test
    fun whenCreateContact_startsInsertWithTheGroupAccount() {
        val account = AccountModelFactory.build(name = "name", type = "type", dataSet = "set")

        effectHandler.handle(Effect.CreateContact(GroupFactory.build(account = account)))

        val intent = startedInAppIntent()
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(Contacts.CONTENT_URI, intent.data)
        assertEquals(
            AccountWithDataSet("name", "type", "set"),
            intent.getParcelableExtra(
                ContactEditorActivity.EXTRA_ACCOUNT_WITH_DATA_SET,
                AccountWithDataSet::class.java,
            ),
        )
    }

    @Test
    fun whenPickMembers_launchesTheContactSelection() {
        val account = AccountModelFactory.build(name = "name", type = "type", dataSet = "set")

        effectHandler.handle(
            Effect.PickMembers(account = account, existingMemberIds = setOf(1L, 2L)),
        )

        val intentSlot = slot<Intent>()
        verify { pickMembersLauncher.launch(capture(intentSlot)) }
        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_PICK, intent.action)
        assertEquals(Groups.CONTENT_TYPE, intent.type)
        assertEquals(ContactSelectionActivity::class.java.name, intent.component?.className)
        assertEquals("name", intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_NAME))
        assertEquals("type", intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_TYPE))
        assertEquals("set", intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_DATA_SET))
        assertEquals(
            listOf("1", "2"),
            intent.getStringArrayListExtra(UiIntentActions.GROUP_CONTACT_IDS),
        )
    }

    @Test
    fun whenPickMembers_andNoActivityIsFound_showsMissingAppMessage() {
        every { pickMembersLauncher.launch(any()) } throws ActivityNotFoundException()

        effectHandler.handle(
            Effect.PickMembers(
                account = AccountModelFactory.build(),
                existingMemberIds = emptySet(),
            ),
        )

        assertEquals(
            targetContext.getString(R.string.missing_app),
            snackbarHostState.currentSnackbarData?.visuals?.message,
        )
    }

    @Test
    fun whenEditName_startsGroupNameEditActivity() {
        val intent = Intent()
        every {
            GroupNameEditActivity.buildEditIntent(any(), any(), any(), any(), any(), any())
        } returns intent
        val group = GroupFactory.build(id = 7L, name = "Family")

        effectHandler.handle(Effect.EditName(group))

        verify {
            GroupNameEditActivity.buildEditIntent(
                activity,
                7L,
                "Family",
                group.account,
                GroupActivity::class.java,
                GroupUtil.ACTION_UPDATE_GROUP,
            )
        }
        assertEquals(intent, startedInAppIntent())
    }

    @Test
    fun whenSendToEmail_startsAChooserWithTheJoinedAddresses() {
        effectHandler.handle(
            Effect.SendTo(
                target = SendToGroupTarget.EMAIL,
                values = listOf("a@example.com", "b@example.com"),
            ),
        )

        val intent = startedInAppIntent()
        assertEquals(Intent.ACTION_CHOOSER, intent.action)
        assertEquals(
            targetContext.getString(R.string.menu_sendEmailOption),
            intent.getCharSequenceExtra(Intent.EXTRA_TITLE),
        )
        val sendIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(Intent.ACTION_SENDTO, sendIntent?.action)
        assertEquals(
            Uri.fromParts("mailto", "a@example.com,b@example.com", null),
            sendIntent?.data,
        )
    }

    @Test
    fun whenSendToSms_startsAChooserWithTheJoinedNumbers() {
        effectHandler.handle(
            Effect.SendTo(target = SendToGroupTarget.SMS, values = listOf("123", "456")),
        )

        val intent = startedInAppIntent()
        assertEquals(
            targetContext.getString(R.string.menu_sendMessageOption),
            intent.getCharSequenceExtra(Intent.EXTRA_TITLE),
        )
        val sendIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(Uri.fromParts("smsto", "123,456", null), sendIntent?.data)
    }

    @Test
    fun whenSendTo_andNoActivityIsFound_showsMissingAppMessage() {
        every {
            ImplicitIntentsUtil.startActivityInAppIfPossible(any(), any())
        } throws ActivityNotFoundException()

        effectHandler.handle(
            Effect.SendTo(target = SendToGroupTarget.SMS, values = listOf("123")),
        )

        assertEquals(
            targetContext.getString(R.string.missing_app),
            snackbarHostState.currentSnackbarData?.visuals?.message,
        )
    }

    @Test
    fun whenPickSendTargetsForEmail_startsTheContactSelection() {
        effectHandler.handle(
            Effect.PickSendTargets(
                target = SendToGroupTarget.EMAIL,
                contactIds = setOf(1L, 2L, 3L),
                defaultSelection = setOf(2L),
            ),
        )

        val intent = startedInAppIntent()
        assertEquals(UiIntentActions.ACTION_SELECT_ITEMS, intent.action)
        assertEquals(Email.CONTENT_TYPE, intent.type)
        assertEquals(ContactSelectionActivity::class.java.name, intent.component?.className)
        assertArrayEquals(
            longArrayOf(1L, 2L, 3L),
            intent.getLongArrayExtra(UiIntentActions.SELECTION_ITEM_LIST),
        )
        assertArrayEquals(
            longArrayOf(2L),
            intent.getLongArrayExtra(UiIntentActions.SELECTION_DEFAULT_SELECTION),
        )
        assertEquals("mailto", intent.getStringExtra(UiIntentActions.SELECTION_SEND_SCHEME))
        assertEquals(
            targetContext.getString(R.string.menu_sendEmailOption),
            intent.getStringExtra(UiIntentActions.SELECTION_SEND_TITLE),
        )
    }

    @Test
    fun whenPickSendTargetsForSms_startsTheContactSelectionForPhones() {
        effectHandler.handle(
            Effect.PickSendTargets(
                target = SendToGroupTarget.SMS,
                contactIds = setOf(1L),
                defaultSelection = emptySet(),
            ),
        )

        val intent = startedInAppIntent()
        assertEquals(Phone.CONTENT_TYPE, intent.type)
        assertEquals("smsto", intent.getStringExtra(UiIntentActions.SELECTION_SEND_SCHEME))
        assertEquals(
            targetContext.getString(R.string.menu_sendMessageOption),
            intent.getStringExtra(UiIntentActions.SELECTION_SEND_TITLE),
        )
    }

    @Test
    fun whenShowMessage_showsASnackbar() {
        effectHandler.handle(Effect.ShowMessage(R.string.groupDeletedToast))

        assertEquals(
            targetContext.getString(R.string.groupDeletedToast),
            snackbarHostState.currentSnackbarData?.visuals?.message,
        )
    }

    @Test
    fun whenShowMessageShouldSurviveClose_showsAToast() {
        every { activity.applicationContext } returns targetContext

        effectHandler.handle(
            Effect.ShowMessage(R.string.groupLoadErrorToast, shouldSurviveClose = true),
        )

        assertEquals(
            targetContext.getString(R.string.groupLoadErrorToast),
            ShadowToast.getTextOfLatestToast(),
        )
        assertNull(snackbarHostState.currentSnackbarData)
    }

    private fun startedInAppIntent(): Intent {
        val intentSlot = slot<Intent>()
        verify { ImplicitIntentsUtil.startActivityInAppIfPossible(activity, capture(intentSlot)) }

        return intentSlot.captured
    }

    private companion object {
        val CONTACT_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")
    }
}

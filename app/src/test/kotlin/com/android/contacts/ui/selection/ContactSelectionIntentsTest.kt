package com.android.contacts.ui.selection

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Groups
import com.android.contacts.list.UiIntentActions
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class ContactSelectionIntentsTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun addGroupMembers_picksContactsForTheGroupAccount() {
        val account = AccountModelFactory.build(
            name = "user@example.org",
            type = "com.example",
            dataSet = "plus",
        )

        val intent = ContactSelectionIntents.addGroupMembers(context, account, listOf(1L, 2L))

        assertEquals(ContactSelectionActivity::class.java.name, intent.component?.className)
        assertEquals(Intent.ACTION_PICK, intent.action)
        assertEquals(Groups.CONTENT_TYPE, intent.type)
        assertEquals("user@example.org", intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_NAME))
        assertEquals("com.example", intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_TYPE))
        assertEquals("plus", intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_DATA_SET))
        assertEquals(
            arrayListOf("1", "2"),
            intent.getStringArrayListExtra(UiIntentActions.GROUP_CONTACT_IDS),
        )
    }

    @Test
    fun addGroupMembers_withoutMembers_passesEmptyList() {
        val intent = ContactSelectionIntents.addGroupMembers(
            context,
            AccountModelFactory.build(),
            emptyList(),
        )

        assertEquals(
            arrayListOf<String>(),
            intent.getStringArrayListExtra(UiIntentActions.GROUP_CONTACT_IDS),
        )
    }

    @Test
    fun getPickedGroupMemberIds_withNullResult_returnsEmptySet() {
        assertEquals(emptySet<Long>(), ContactSelectionIntents.getPickedGroupMemberIds(null))
    }

    @Test
    fun getPickedGroupMemberIds_withoutExtras_returnsEmptySet() {
        assertEquals(emptySet<Long>(), ContactSelectionIntents.getPickedGroupMemberIds(Intent()))
    }

    @Test
    fun getPickedGroupMemberIds_withSeveralContacts_returnsThem() {
        val result = Intent()
            .putExtra(UiIntentActions.TARGET_CONTACT_IDS_EXTRA_KEY, longArrayOf(1L, 2L, 2L))

        assertEquals(setOf(1L, 2L), ContactSelectionIntents.getPickedGroupMemberIds(result))
    }

    @Test
    fun getPickedGroupMemberIds_withSingleContact_returnsIt() {
        val result = Intent().putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, 3L)

        assertEquals(setOf(3L), ContactSelectionIntents.getPickedGroupMemberIds(result))
    }

    @Test
    fun getPickedGroupMemberIds_withBothExtras_prefersTheContactIds() {
        val result = Intent()
            .putExtra(UiIntentActions.TARGET_CONTACT_IDS_EXTRA_KEY, longArrayOf(1L))
            .putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, 3L)

        assertEquals(setOf(1L), ContactSelectionIntents.getPickedGroupMemberIds(result))
    }

    @Test
    fun sendToGroup_withSms_selectsPhones() {
        val intent = ContactSelectionIntents.sendToGroup(
            context = context,
            target = SendToGroupTarget.SMS,
            contactIds = listOf(1L, 2L),
            defaultSelection = listOf(10L),
            chooserTitle = "Send",
        )

        assertEquals(ContactSelectionActivity::class.java.name, intent.component?.className)
        assertEquals(UiIntentActions.ACTION_SELECT_ITEMS, intent.action)
        assertEquals(Phone.CONTENT_TYPE, intent.type)
        assertArrayEquals(
            longArrayOf(1L, 2L),
            intent.getLongArrayExtra(UiIntentActions.SELECTION_ITEM_LIST),
        )
        assertArrayEquals(
            longArrayOf(10L),
            intent.getLongArrayExtra(UiIntentActions.SELECTION_DEFAULT_SELECTION),
        )
        assertEquals("smsto", intent.getStringExtra(UiIntentActions.SELECTION_SEND_SCHEME))
        assertEquals("Send", intent.getStringExtra(UiIntentActions.SELECTION_SEND_TITLE))
    }

    @Test
    fun sendToGroup_withEmail_selectsEmails() {
        val intent = ContactSelectionIntents.sendToGroup(
            context = context,
            target = SendToGroupTarget.EMAIL,
            contactIds = listOf(1L),
            defaultSelection = emptyList(),
            chooserTitle = null,
        )

        assertEquals(Email.CONTENT_TYPE, intent.type)
        assertEquals("mailto", intent.getStringExtra(UiIntentActions.SELECTION_SEND_SCHEME))
        assertNull(intent.getStringExtra(UiIntentActions.SELECTION_SEND_TITLE))
    }
}

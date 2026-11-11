package com.android.contacts.ui.selection.screen.mapper

import android.app.SearchManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.provider.Contacts.ContactMethods as LegacyContactMethods
import android.provider.Contacts.People as LegacyPeople
import android.provider.Contacts.Phones as LegacyPhones
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Groups
import android.provider.ContactsContract.Intents.Insert
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.list.UiIntentActions
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
internal class ContactSelectionModeMapperTest {

    private val contentResolver = mockk<ContentResolver>()

    private val subject = ContactSelectionModeMapperImpl(contentResolver = contentResolver)

    @Test
    fun unknownAction_returnsNull() {
        assertNull(subject.map(Intent("com.example.UNKNOWN")))
        assertNull(subject.map(Intent()))
    }

    // ACTION_PICK

    @Test
    fun pick_withContactsType_returnsPickWithFavorites() {
        assertEquals(
            Mode.Pick(includeFavorites = true),
            subject.map(pick(Contacts.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withoutTypeOrData_returnsPickWithFavorites() {
        assertEquals(
            Mode.Pick(includeFavorites = true),
            subject.map(Intent(Intent.ACTION_PICK)),
        )
    }

    @Test
    fun pick_withAllowMultiple_returnsMultiplePick() {
        val intent = pick(Contacts.CONTENT_TYPE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        assertEquals(
            Mode.Pick(multiple = true, includeFavorites = true),
            subject.map(intent),
        )
    }

    @Test
    fun pick_withoutType_resolvesTypeFromData() {
        every { contentResolver.getType(Phone.CONTENT_URI) } returns Phone.CONTENT_TYPE
        val intent = Intent(Intent.ACTION_PICK, Phone.CONTENT_URI)

        assertEquals(Mode.PickPhoneNumber(), subject.map(intent))
    }

    @Test
    fun pick_withLegacyPeopleType_returnsLegacyPick() {
        assertEquals(
            Mode.Pick(legacy = true),
            subject.map(pick(LegacyPeople.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withPhoneType_returnsPickPhoneNumber() {
        assertEquals(
            Mode.PickPhoneNumber(),
            subject.map(pick(Phone.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withPhoneTypeAndAllowMultiple_returnsMultiplePickPhoneNumber() {
        val intent = pick(Phone.CONTENT_TYPE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        assertEquals(Mode.PickPhoneNumber(multiple = true), subject.map(intent))
    }

    @Test
    fun pick_withLegacyPhonesType_returnsLegacyPickPhoneNumber() {
        assertEquals(
            Mode.PickPhoneNumber(legacy = true),
            subject.map(pick(LegacyPhones.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withEmailType_returnsPickEmail() {
        assertEquals(
            Mode.PickEmail(),
            subject.map(pick(Email.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withEmailTypeAndAllowMultiple_returnsMultiplePickEmail() {
        val intent = pick(Email.CONTENT_TYPE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        assertEquals(Mode.PickEmail(multiple = true), subject.map(intent))
    }

    @Test
    fun pick_withPostalType_returnsPickPostal() {
        assertEquals(
            Mode.PickPostal(),
            subject.map(pick(StructuredPostal.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withLegacyPostalType_returnsLegacyPickPostal() {
        assertEquals(
            Mode.PickPostal(legacy = true),
            subject.map(pick(LegacyContactMethods.CONTENT_POSTAL_TYPE)),
        )
    }

    @Test
    fun pick_withGroupsType_returnsAddGroupMembers() {
        val intent = pick(Groups.CONTENT_TYPE)
            .putExtra(UiIntentActions.GROUP_ACCOUNT_NAME, "john@example.com")
            .putExtra(UiIntentActions.GROUP_ACCOUNT_TYPE, "com.example")
            .putExtra(UiIntentActions.GROUP_ACCOUNT_DATA_SET, "plus")
            .putStringArrayListExtra(
                UiIntentActions.GROUP_CONTACT_IDS,
                arrayListOf("1", "invalid", "3"),
            )

        assertEquals(
            Mode.AddGroupMembers(
                account = AccountModel(
                    name = "john@example.com",
                    type = "com.example",
                    dataSet = "plus",
                ),
                existingMemberIds = setOf(1L, 3L),
            ),
            subject.map(intent),
        )
    }

    @Test
    fun pick_withGroupsTypeAndNoExtras_returnsAddGroupMembersWithNullAccount() {
        assertEquals(
            Mode.AddGroupMembers(account = AccountModel.NULL, existingMemberIds = emptySet()),
            subject.map(pick(Groups.CONTENT_TYPE)),
        )
    }

    @Test
    fun pick_withUnknownType_returnsNull() {
        assertNull(subject.map(pick("vnd.example/unknown")))
    }

    // ACTION_GET_CONTENT

    @Test
    fun getContent_mapsItemTypes() {
        val expected = mapOf(
            Contacts.CONTENT_ITEM_TYPE to Mode.Pick(includeFavorites = true),
            LegacyPeople.CONTENT_ITEM_TYPE to Mode.Pick(legacy = true),
            Phone.CONTENT_ITEM_TYPE to Mode.PickPhoneNumber(),
            LegacyPhones.CONTENT_ITEM_TYPE to Mode.PickPhoneNumber(legacy = true),
            StructuredPostal.CONTENT_ITEM_TYPE to Mode.PickPostal(),
            LegacyContactMethods.CONTENT_POSTAL_ITEM_TYPE to Mode.PickPostal(legacy = true),
        )

        expected.forEach { (type, mode) ->
            assertEquals(
                type,
                mode,
                subject.map(Intent(Intent.ACTION_GET_CONTENT).setType(type)),
            )
        }
    }

    @Test
    fun getContent_withUnknownType_returnsNull() {
        assertNull(subject.map(Intent(Intent.ACTION_GET_CONTENT).setType(Email.CONTENT_ITEM_TYPE)))
        assertNull(subject.map(Intent(Intent.ACTION_GET_CONTENT)))
    }

    // Other actions

    @Test
    fun insertOrEdit_returnsInsertOrEdit() {
        assertEquals(Mode.InsertOrEdit, subject.map(Intent(Intent.ACTION_INSERT_OR_EDIT)))
    }

    @Test
    fun createShortcut_mapsComponentToTarget() {
        assertEquals(
            Mode.CreateShortcut(ShortcutTarget.CALL),
            subject.map(createShortcut("alias.DialShortcut")),
        )
        assertEquals(
            Mode.CreateShortcut(ShortcutTarget.MESSAGE),
            subject.map(createShortcut("alias.MessageShortcut")),
        )
        assertEquals(
            Mode.CreateShortcut(ShortcutTarget.CONTACT),
            subject.map(createShortcut("alias.ContactShortcut")),
        )
        assertEquals(
            Mode.CreateShortcut(ShortcutTarget.CONTACT),
            subject.map(Intent(Intent.ACTION_CREATE_SHORTCUT)),
        )
    }

    @Test
    fun selectItems_withPhoneType_returnsSendToGroupSms() {
        val intent = selectItems(Phone.CONTENT_TYPE)
            .putExtra(UiIntentActions.SELECTION_ITEM_LIST, longArrayOf(1L, 2L, 3L))
            .putExtra(UiIntentActions.SELECTION_DEFAULT_SELECTION, longArrayOf(2L))
            .putExtra(UiIntentActions.SELECTION_SEND_TITLE, "Send message")

        assertEquals(
            Mode.SendToGroup(
                target = SentToGroupTarget.SMS,
                itemIds = listOf(1L, 2L, 3L),
                defaultSelection = listOf(2L),
                chooserTitle = "Send message",
            ),
            subject.map(intent),
        )
    }

    @Test
    fun selectItems_withEmailTypeAndNoExtras_returnsEmptySendToGroupEmail() {
        assertEquals(
            Mode.SendToGroup(
                target = SentToGroupTarget.EMAIL,
                itemIds = emptyList(),
                defaultSelection = emptyList(),
                chooserTitle = null,
            ),
            subject.map(selectItems(Email.CONTENT_TYPE)),
        )
    }

    @Test
    fun selectItems_withUnknownType_returnsNull() {
        assertNull(subject.map(selectItems(Contacts.CONTENT_TYPE)))
    }

    @Test
    fun pickJoinContact_withTargetId_returnsJoin() {
        val intent = Intent(UiIntentActions.PICK_JOIN_CONTACT_ACTION)
            .putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, 42L)

        assertEquals(Mode.Join(targetContactId = 42L), subject.map(intent))
    }

    @Test
    fun pickJoinContact_withoutTargetId_returnsNull() {
        assertNull(subject.map(Intent(UiIntentActions.PICK_JOIN_CONTACT_ACTION)))
    }

    @Test
    fun listDefault_returnsPickWithFavorites() {
        assertEquals(
            Mode.Pick(includeFavorites = true),
            subject.map(Intent(UiIntentActions.LIST_DEFAULT)),
        )
    }

    // ACTION_SEARCH

    @Test
    fun search_usesSearchQuery() {
        val intent = Intent(Intent.ACTION_SEARCH)
            .putExtra(SearchManager.QUERY, "john")
            .putExtra(Insert.PHONE, "+1 555 0100")

        assertEquals(
            Mode.Pick(includeFavorites = true, searchQuery = "john"),
            subject.map(intent),
        )
    }

    @Test
    fun search_withEmptyQuery_fallsBackToPhone() {
        val intent = Intent(Intent.ACTION_SEARCH)
            .putExtra(SearchManager.QUERY, "")
            .putExtra(Insert.PHONE, "+1 555 0100")
            .putExtra(Insert.EMAIL, "john@example.com")

        assertEquals(
            "+1 555 0100",
            (subject.map(intent) as Mode.Pick).searchQuery,
        )
    }

    @Test
    fun search_withoutQueryOrPhone_fallsBackToEmail() {
        val intent = Intent(Intent.ACTION_SEARCH).putExtra(Insert.EMAIL, "john@example.com")

        assertEquals(
            "john@example.com",
            (subject.map(intent) as Mode.Pick).searchQuery,
        )
    }

    @Test
    fun search_withoutExtras_returnsEmptyQuery() {
        assertEquals(
            Mode.Pick(includeFavorites = true, searchQuery = ""),
            subject.map(Intent(Intent.ACTION_SEARCH)),
        )
    }

    private fun pick(type: String): Intent {
        return Intent(Intent.ACTION_PICK).setType(type)
    }

    private fun selectItems(type: String): Intent {
        return Intent(UiIntentActions.ACTION_SELECT_ITEMS).setType(type)
    }

    private fun createShortcut(className: String): Intent {
        return Intent(Intent.ACTION_CREATE_SHORTCUT)
            .setComponent(ComponentName("com.android.contacts", className))
    }
}

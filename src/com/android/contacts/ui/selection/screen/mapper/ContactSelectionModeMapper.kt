package com.android.contacts.ui.selection.screen.mapper

import android.app.SearchManager
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
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import javax.inject.Inject

internal fun interface ContactSelectionModeMapper {
    fun map(intent: Intent): ContactSelectionMode?
}

@Suppress("DEPRECATION")
internal class ContactSelectionModeMapperImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : ContactSelectionModeMapper {

    override fun map(intent: Intent): ContactSelectionMode? {
        return when (intent.action) {
            Intent.ACTION_PICK ->
                mapPick(intent)
            Intent.ACTION_GET_CONTENT ->
                mapGetContent(intent)
            Intent.ACTION_INSERT_OR_EDIT ->
                ContactSelectionMode.InsertOrEdit
            Intent.ACTION_CREATE_SHORTCUT ->
                mapCreateShortcut(intent)
            UiIntentActions.ACTION_SELECT_ITEMS ->
                mapSendToGroup(intent)
            UiIntentActions.PICK_JOIN_CONTACT_ACTION ->
                mapJoin(intent)
            UiIntentActions.LIST_DEFAULT ->
                ContactSelectionMode.Pick(includeFavorites = true)
            Intent.ACTION_SEARCH ->
                ContactSelectionMode.Pick(
                    includeFavorites = true,
                    searchQuery = mapSearchQuery(intent),
                )
            else -> null
        }
    }

    private fun mapPick(
        intent: Intent,
    ): ContactSelectionMode? {
        return when (resolveType(intent)) {
            Contacts.CONTENT_TYPE,
            null,
            ->
                ContactSelectionMode.Pick(
                    multiple = isMultiple(intent),
                    includeFavorites = true,
                )
            LegacyPeople.CONTENT_TYPE ->
                ContactSelectionMode.Pick(legacy = true)
            Phone.CONTENT_TYPE ->
                ContactSelectionMode.PickPhoneNumber(multiple = isMultiple(intent))
            LegacyPhones.CONTENT_TYPE ->
                ContactSelectionMode.PickPhoneNumber(legacy = true)
            Email.CONTENT_TYPE ->
                ContactSelectionMode.PickEmail(multiple = isMultiple(intent))
            StructuredPostal.CONTENT_TYPE ->
                ContactSelectionMode.PickPostal()
            LegacyContactMethods.CONTENT_POSTAL_TYPE ->
                ContactSelectionMode.PickPostal(legacy = true)
            Groups.CONTENT_TYPE ->
                ContactSelectionMode.AddGroupMembers(
                    account = mapAccount(intent),
                    existingMemberIds = intent
                        .getStringArrayListExtra(UiIntentActions.GROUP_CONTACT_IDS)
                        .orEmpty()
                        .mapNotNull { it.toLongOrNull() }
                        .toSet(),
                )
            else -> null
        }
    }

    private fun mapGetContent(intent: Intent): ContactSelectionMode? {
        return when (intent.type) {
            Contacts.CONTENT_ITEM_TYPE ->
                ContactSelectionMode.Pick(includeFavorites = true)
            LegacyPeople.CONTENT_ITEM_TYPE ->
                ContactSelectionMode.Pick(legacy = true)
            Phone.CONTENT_ITEM_TYPE ->
                ContactSelectionMode.PickPhoneNumber()
            LegacyPhones.CONTENT_ITEM_TYPE ->
                ContactSelectionMode.PickPhoneNumber(legacy = true)
            StructuredPostal.CONTENT_ITEM_TYPE ->
                ContactSelectionMode.PickPostal()
            LegacyContactMethods.CONTENT_POSTAL_ITEM_TYPE ->
                ContactSelectionMode.PickPostal(legacy = true)
            else ->
                null
        }
    }

    private fun mapCreateShortcut(intent: Intent): ContactSelectionMode {
        val target = when (intent.component?.className) {
            DIAL_SHORTCUT_ALIAS -> ShortcutTarget.CALL
            MESSAGE_SHORTCUT_ALIAS -> ShortcutTarget.MESSAGE
            else -> ShortcutTarget.CONTACT
        }
        return ContactSelectionMode.CreateShortcut(target)
    }

    private fun mapSendToGroup(intent: Intent): ContactSelectionMode? {
        val target = when (resolveType(intent)) {
            Phone.CONTENT_TYPE -> SentToGroupTarget.SMS
            Email.CONTENT_TYPE -> SentToGroupTarget.EMAIL
            else -> return null
        }
        return ContactSelectionMode.SendToGroup(
            target = target,
            itemIds = intent.getLongArrayExtra(UiIntentActions.SELECTION_ITEM_LIST)
                ?.toList().orEmpty(),
            defaultSelection = intent
                .getLongArrayExtra(UiIntentActions.SELECTION_DEFAULT_SELECTION)
                ?.toList().orEmpty(),
            chooserTitle = intent.getStringExtra(UiIntentActions.SELECTION_SEND_TITLE),
        )
    }

    private fun mapJoin(intent: Intent): ContactSelectionMode? {
        val targetContactId =
            intent.getLongExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, NO_CONTACT_ID)
        return when {
            targetContactId == NO_CONTACT_ID -> null
            else -> ContactSelectionMode.Join(targetContactId)
        }
    }

    private fun mapSearchQuery(intent: Intent): String {
        return listOf(
            SearchManager.QUERY,
            Insert.PHONE,
            Insert.EMAIL,
        ).firstNotNullOfOrNull { key ->
            intent.getStringExtra(key)?.takeIf { it.isNotEmpty() }
        }.orEmpty()
    }

    private fun resolveType(intent: Intent): String? {
        return intent.type
            ?: intent.data?.let { contentResolver.getType(it) }
    }

    private fun isMultiple(intent: Intent): Boolean {
        return intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    }

    private fun mapAccount(intent: Intent): AccountModel {
        return AccountModel(
            name = intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_NAME),
            type = intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_TYPE),
            dataSet = intent.getStringExtra(UiIntentActions.GROUP_ACCOUNT_DATA_SET),
        )
    }

    private companion object {
        const val DIAL_SHORTCUT_ALIAS = "alias.DialShortcut"
        const val MESSAGE_SHORTCUT_ALIAS = "alias.MessageShortcut"
        const val NO_CONTACT_ID = -1L
    }
}

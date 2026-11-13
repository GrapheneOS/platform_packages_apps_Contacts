package com.android.contacts.ui.selection.screen.mapper

import android.content.ContentUris
import android.net.Uri
import android.provider.Contacts.ContactMethods as LegacyContactMethods
import android.provider.Contacts.People as LegacyPeople
import android.provider.Contacts.Phones as LegacyPhones
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.DIRECTORY_PARAM_KEY
import android.provider.ContactsContract.Data
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.contacts.model.ContactLookupQuery
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal fun interface ContactSelectionEffectMapper {
    suspend fun map(
        mode: Mode,
        keys: Set<ContactItemKey>,
    ): Effect
}

@Suppress("DEPRECATION")
internal class ContactSelectionEffectMapperImpl @Inject constructor(
    private val contactsRepository: ContactsRepository,
) : ContactSelectionEffectMapper {

    override suspend fun map(
        mode: Mode,
        keys: Set<ContactItemKey>,
    ): Effect {
        val directoryKeys = keys.filterIsInstance<ContactItemKey.Directory>()
        val localKeys = keys.filterIsInstance<ContactItemKey.Local>().toSet()
        return when {
            keys.isEmpty() ->
                Effect.Close(Result.Canceled)
            directoryKeys.any() ->
                mapDirectoryContact(mode, directoryKeys.first())
            else ->
                mapLocalContacts(mode, localKeys)
        }
    }

    private fun mapDirectoryContact(
        mode: Mode,
        key: ContactItemKey.Directory,
    ): Effect {
        val uri = Contacts.getLookupUri(key.id, key.lookupKey)
            .buildUpon()
            .appendQueryParameter(DIRECTORY_PARAM_KEY, key.directoryId.toString())
            .build()
        return close(
            when {
                mode is Mode.Pick ->
                    Result.Picked(uri)
                mode is Mode.CreateShortcut && mode.target == ShortcutTarget.CONTACT ->
                    Result.Shortcut(mode.target, uri)
                // Remote directories are only queried for the modes above
                else ->
                    Result.Canceled
            },
        )
    }

    suspend fun mapLocalContacts(
        mode: Mode,
        keys: Set<ContactItemKey.Local>,
    ): Effect {
        val contactIds = keys.map { it.id }.toSet()
        val contactId = contactIds.first()

        return when (mode) {
            is Mode.Pick -> mapPick(mode, contactIds)
            Mode.InsertOrEdit -> mapInsertOrEdit(contactId)
            is Mode.PickPhoneNumber -> mapPickPhoneNumber(mode, contactIds)
            is Mode.PickEmail -> mapPickEmail(mode, contactIds)
            is Mode.PickPostal -> mapPickPostal(mode, contactId)
            is Mode.CreateShortcut -> mapCreateShortcut(mode, contactId)
            is Mode.Join -> mapJoin(contactId)
            is Mode.AddGroupMembers -> mapAddGroupMembers(contactIds)
            is Mode.SendToGroup -> mapSentToGroup(mode, contactIds)
        }
    }

    private suspend fun mapPick(
        mode: Mode.Pick,
        contactIds: Set<Long>,
    ): Effect {
        val contactId = contactIds.first()
        val effect = when {
            mode.legacy ->
                Result.Picked(LegacyPeople.CONTENT_URI.withId(contactId))
            mode.multiple ->
                Result.PickedMultiple(
                    uris = contactIds.map { Contacts.CONTENT_URI.withId(it) },
                    mimeType = Contacts.CONTENT_ITEM_TYPE,
                )
            else ->
                Result.Picked(getContactLookupUri(contactId))
        }
        return close(effect)
    }

    private suspend fun mapInsertOrEdit(contactId: Long): Effect.ForwardToEditor.EditContact {
        return Effect.ForwardToEditor.EditContact(getContactLookupUri(contactId))
    }

    private fun mapPickPhoneNumber(
        mode: Mode.PickPhoneNumber,
        contactIds: Set<Long>,
    ): Effect {
        val effect = when {
            mode.legacy ->
                Result.Picked(LegacyPhones.CONTENT_URI.withId(contactIds.first()))
            mode.multiple ->
                Result.PickedMultiple(
                    uris = contactIds.map { Phone.CONTENT_URI.withId(it) },
                    mimeType = Phone.CONTENT_ITEM_TYPE,
                )
            else ->
                Result.Picked(Data.CONTENT_URI.withId(contactIds.first()))
        }
        return close(effect)
    }

    private fun mapPickEmail(
        mode: Mode.PickEmail,
        contactIds: Set<Long>,
    ): Effect {
        val effect = when {
            mode.multiple ->
                Result.PickedMultiple(
                    uris = contactIds.map { Email.CONTENT_URI.withId(it) },
                    mimeType = Email.CONTENT_ITEM_TYPE,
                )
            else ->
                Result.Picked(Data.CONTENT_URI.withId(contactIds.first()))
        }
        return close(effect)
    }

    private fun mapPickPostal(
        mode: Mode.PickPostal,
        contactId: Long,
    ): Effect {
        val uri = when {
            mode.legacy -> LegacyContactMethods.CONTENT_URI.withId(contactId)
            else -> Data.CONTENT_URI.withId(contactId)
        }
        return close(Result.Picked(uri))
    }

    private suspend fun mapCreateShortcut(
        mode: Mode.CreateShortcut,
        contactId: Long,
    ): Effect {
        val uri = when (mode.target) {
            ShortcutTarget.CONTACT -> getContactLookupUri(contactId)
            ShortcutTarget.CALL,
            ShortcutTarget.MESSAGE,
            -> Data.CONTENT_URI.withId(contactId)
        }
        return close(Result.Shortcut(mode.target, uri))
    }

    private suspend fun mapJoin(contactId: Long): Effect =
        close(Result.Picked(getContactLookupUri(contactId)))

    private fun mapAddGroupMembers(contactIds: Set<Long>): Effect {
        val effect = when {
            contactIds.size == 1 -> Result.GroupMember(contactIds.first())
            else -> Result.GroupMembers(contactIds)
        }
        return close(effect)
    }

    private suspend fun mapSentToGroup(
        mode: Mode.SendToGroup,
        contactIds: Set<Long>,
    ): Effect {
        val addresses = queryAddresses(mode.target, contactIds)
        return when {
            addresses.isEmpty() -> close(Result.Canceled)
            else -> Effect.SendTo(mode.target, addresses, mode.chooserTitle)
        }
    }

    private fun close(result: Result): Effect = Effect.Close(result)

    private fun Uri.withId(id: Long): Uri = ContentUris.withAppendedId(this, id)

    private suspend fun getContactLookupUri(contactId: Long): Uri {
        return contactsRepository.lookup(ContactLookupQuery.Id(contactId))
            .first()
            ?.firstOrNull()
            ?.uri
            ?: ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
    }

    private suspend fun queryAddresses(
        target: SentToGroupTarget,
        dataIds: Set<Long>,
    ): List<String> {
        val kind = when (target) {
            SentToGroupTarget.SMS -> ContactDataKind.Phone()
            SentToGroupTarget.EMAIL -> ContactDataKind.Email
        }
        return contactsRepository.getContactDataValues(kind, dataIds)
    }
}

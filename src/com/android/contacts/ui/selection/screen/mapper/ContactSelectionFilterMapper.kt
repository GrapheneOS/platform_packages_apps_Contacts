package com.android.contacts.ui.selection.screen.mapper

import com.android.contacts.domain.contacts.model.ContactSelectionFilter
import com.android.contacts.domain.contacts.model.ContactSelectionFilter as Filter
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import javax.inject.Inject

internal fun interface ContactSelectionFilterMapper {
    fun map(mode: Mode): ContactSelectionFilter
}

internal class ContactSelectionFilterMapperImpl @Inject constructor() :
    ContactSelectionFilterMapper {

    override fun map(mode: Mode): ContactSelectionFilter {
        return when (mode) {
            is Mode.Pick -> mapPick(mode)
            Mode.InsertOrEdit -> Filter.Contacts()
            is Mode.CreateShortcut -> mapCreateShortcut(mode)
            is Mode.PickPhoneNumber -> mapPickPhoneNumber(mode)
            is Mode.PickEmail -> Filter.Emails()
            is Mode.PickPostal -> mapPickPostal(mode)
            is Mode.Join -> Filter.JoinCandidates(mode.targetContactId)
            is Mode.AddGroupMembers -> Filter.Contacts(
                account = mode.account,
                excludeIds = mode.existingMemberIds,
            )
            is Mode.SendToGroup -> mapSentToGroup(mode)
        }
    }

    private fun mapPick(mode: Mode.Pick): Filter {
        return when {
            mode.legacy -> Filter.Legacy.People
            else -> Filter.Contacts(includesDirectories = !mode.multiple)
        }
    }

    private fun mapCreateShortcut(mode: Mode.CreateShortcut): Filter {
        return when (mode.target) {
            ShortcutTarget.CONTACT -> Filter.Contacts(includesDirectories = true)
            ShortcutTarget.CALL -> Filter.PhoneNumbers(callableOnly = true)
            ShortcutTarget.MESSAGE -> Filter.PhoneNumbers()
        }
    }

    private fun mapPickPhoneNumber(mode: Mode.PickPhoneNumber): Filter {
        return when {
            mode.legacy -> Filter.Legacy.Phones
            else -> Filter.PhoneNumbers()
        }
    }

    private fun mapPickPostal(mode: Mode.PickPostal): Filter {
        return when {
            mode.legacy -> Filter.Legacy.PostalAddresses
            else -> Filter.PostalAddresses
        }
    }

    private fun mapSentToGroup(mode: Mode.SendToGroup): Filter {
        val contactIds = mode.itemIds.toSet()
        return when (mode.target) {
            SentToGroupTarget.SMS -> Filter.PhoneNumbers(restrictToContactIds = contactIds)
            SentToGroupTarget.EMAIL -> Filter.Emails(restrictToContactIds = contactIds)
        }
    }
}

package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactAccount
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactGroup
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.domain.calllog.model.RecentCall
import com.android.contacts.domain.contactdetails.model.ContactConnectedApp
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.usecase.IsEntryActionAvailable
import com.android.contacts.domain.telecom.model.CallingSimOptions
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.contactdetails.screen.model.CallingSimAccountUiModel
import com.android.contacts.ui.contactdetails.screen.model.CallingSimNumberUiModel
import com.android.contacts.ui.contactdetails.screen.model.CallingSimUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactAccountUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactConnectedAppUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal interface ContactDetailsUiStateMapper {
    fun map(
        details: ContactDetails,
        cards: ContactDetailsCards,
        quickActions: List<ContactQuickAction>,
        recentCalls: List<RecentCall>,
        callingSimOptions: CallingSimOptions?,
        menu: ContactDetailsMenu,
        displayOrder: DisplayOrder,
    ): ContactDetailsContent
}

internal class ContactDetailsUiStateMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val isEntryActionAvailable: IsEntryActionAvailable,
    private val contactQuickActionsMapper: ContactQuickActionsMapper,
    private val recentCallsMapper: RecentCallsMapper,
) : ContactDetailsUiStateMapper {

    override fun map(
        details: ContactDetails,
        cards: ContactDetailsCards,
        quickActions: List<ContactQuickAction>,
        recentCalls: List<RecentCall>,
        callingSimOptions: CallingSimOptions?,
        menu: ContactDetailsMenu,
        displayOrder: DisplayOrder,
    ): ContactDetailsContent {
        val callingSim = callingSim(callingSimOptions)
        val isEditable = !details.capabilities.isDirectoryEntry
        val isCallingSimChangeable = callingSim != null
        val contactCard = mapGroups(cards.contactCard, isEditable, isCallingSimChangeable)
        val connectedApps = mapConnectedApps(cards.connectedApps, isEditable)
        val notes = mapGroups(cards.notes, isEditable, isCallingSimChangeable)

        return ContactDetailsContent.Loaded(
            header = mapHeader(details, cards, displayOrder),
            quickActions = contactQuickActionsMapper.map(quickActions),
            recentCalls = recentCallsMapper.map(recentCalls),
            callingSim = callingSim,
            groups = mapContactGroups(cards.groups),
            contactCard = contactCard,
            connectedApps = connectedApps,
            notes = notes,
            settings = settings(details, menu, callingSim),
            accounts = mapAccounts(details.accounts),
            emptyPrompt = emptyPrompt(details, contactCard, notes),
            menu = menu,
            isStarred = details.isStarred,
        )
    }

    private fun mapHeader(
        details: ContactDetails,
        cards: ContactDetailsCards,
        displayOrder: DisplayOrder,
    ): ContactHeaderUiModel {
        val orderedName = when (displayOrder) {
            DisplayOrder.GIVEN_NAME_FIRST -> details.displayName
            DisplayOrder.FAMILY_NAME_FIRST -> details.alternativeDisplayName
        }
        val displayName = orderedName
            ?.takeIf { name -> name.isNotBlank() }
            ?: context.getString(R.string.missing_name)

        return ContactHeaderUiModel(
            displayName = displayName,
            subtitles = subtitles(details, cards, displayName),
            photo = avatarImage(details.photo),
            avatarSeed = details.lookupKey,
            isBusiness = details.displayNameSource == ContactDisplayNameSource.ORGANIZATION,
            isDisplayNameLtr = details.displayNameSource == ContactDisplayNameSource.PHONE,
        )
    }

    private fun subtitles(
        details: ContactDetails,
        cards: ContactDetailsCards,
        displayName: String,
    ): ImmutableList<String> {
        val phoneticName = phoneticName(details, displayName)
        val quotedPhoneticName = phoneticName?.let { name ->
            context.getString(R.string.header_phonetic_name_quoted, name)
        }
        val nicknames = cards.headerNicknames
            .joinToString(NICKNAME_SEPARATOR)
            .takeIf { line -> line.isNotEmpty() }
        val nameLine = listOfNotNull(
            nicknames,
            quotedPhoneticName,
        ).joinToString(SUBTITLE_SEPARATOR)
        val organizationLines = cards.headerOrganizations
            .map { parts -> parts.joinToString(SUBTITLE_SEPARATOR) }

        return (listOf(nameLine) + organizationLines)
            .filter { line -> line.isNotEmpty() }
            .toImmutableList()
    }

    private fun avatarImage(photo: ContactPhoto?): ContactAvatarImage? {
        return when (photo) {
            null -> null
            is ContactPhoto.Bytes -> ContactAvatarImage.Bytes(photo.value)
            is ContactPhoto.Uri -> ContactAvatarImage.Uri(photo.value)
        }
    }

    private fun phoneticName(
        details: ContactDetails,
        displayName: String,
    ): String? {
        return details.phoneticName?.takeIf { name ->
            name.isNotBlank() && name != displayName
        }
    }

    private fun callingSim(options: CallingSimOptions?): CallingSimUiModel? {
        if (options == null) {
            return null
        }

        return CallingSimUiModel(
            accounts = options.sims
                .map { sim ->
                    CallingSimAccountUiModel(
                        accountId = sim.accountId,
                        label = sim.label,
                    )
                }
                .toImmutableList(),
            numbers = options.choices
                .map { choice ->
                    CallingSimNumberUiModel(
                        dataId = choice.dataId,
                        number = choice.number,
                        numberLabel = choice.numberLabel,
                        selectedAccountId = choice.selectedAccountId,
                    )
                }
                .toImmutableList(),
        )
    }

    private fun settings(
        details: ContactDetails,
        menu: ContactDetailsMenu,
        callingSim: CallingSimUiModel?,
    ): ImmutableList<ContactSettingUiModel> {
        return listOfNotNull(
            setting(
                icon = ContactSettingIcon.CALLING_SIM,
                titleResource = R.string.contact_details_set_calling_sim,
                action = ContactDetailsAction.CallingSimClick,
            ).takeIf { callingSim != null },
            setting(
                icon = ContactSettingIcon.RINGTONE,
                titleResource = R.string.menu_set_ring_tone,
                action = ContactDetailsAction.RingtoneClick,
                subtitle = details.customRingtoneTitle,
            ).takeIf { menu.isRingtoneVisible },
            setting(
                icon = ContactSettingIcon.SEND_TO_VOICEMAIL,
                titleResource = R.string.contact_details_send_to_voicemail,
                action = ContactDetailsAction.SendToVoicemailClick,
                isChecked = details.isSendToVoicemail,
            ).takeIf { menu.isSendToVoicemailVisible },
            setting(
                icon = ContactSettingIcon.SHARE,
                titleResource = R.string.menu_share,
                action = ContactDetailsAction.ShareClick,
            ).takeIf { menu.isShareVisible },
            setting(
                icon = ContactSettingIcon.SHORTCUT,
                titleResource = R.string.menu_create_contact_shortcut,
                action = ContactDetailsAction.ShortcutClick,
            ).takeIf { menu.isShortcutVisible },
            setting(
                icon = ContactSettingIcon.LINK,
                titleResource = R.string.menu_joinAggregate,
                action = ContactDetailsAction.JoinClick,
            ).takeIf { menu.isJoinVisible },
            setting(
                icon = ContactSettingIcon.LINKED_CONTACTS,
                titleResource = R.string.menu_linkedContacts,
                action = ContactDetailsAction.LinkedContactsClick,
            ).takeIf { menu.isLinkedContactsVisible },
            setting(
                icon = ContactSettingIcon.DELETE,
                titleResource = R.string.menu_deleteContact,
                action = ContactDetailsAction.DeleteClick,
                isDestructive = true,
            ).takeIf { menu.isDeleteVisible },
        ).toImmutableList()
    }

    private fun setting(
        icon: ContactSettingIcon,
        @StringRes titleResource: Int,
        action: ContactDetailsAction,
        subtitle: String? = null,
        isDestructive: Boolean = false,
        isChecked: Boolean? = null,
    ): ContactSettingUiModel {
        return ContactSettingUiModel(
            icon = icon,
            title = context.getString(titleResource),
            subtitle = subtitle,
            action = action,
            isDestructive = isDestructive,
            isChecked = isChecked,
        )
    }

    private fun mapAccounts(accounts: List<ContactAccount>): ImmutableList<ContactAccountUiModel> {
        return accounts
            .map { account ->
                ContactAccountUiModel(
                    name = account.name,
                    iconUri = account.iconUri,
                )
            }
            .toImmutableList()
    }

    private fun mapContactGroups(
        groups: List<ContactGroup>,
    ): ImmutableList<ContactGroupUiModel> {
        return groups
            .map { group ->
                ContactGroupUiModel(
                    id = group.id,
                    title = group.title,
                )
            }
            .toImmutableList()
    }

    private fun mapConnectedApps(
        connectedApps: List<ContactConnectedApp>,
        isEditable: Boolean,
    ): ImmutableList<ContactConnectedAppUiModel> {
        return connectedApps
            .map { connectedApp -> mapConnectedApp(connectedApp, isEditable) }
            .toImmutableList()
    }

    private fun mapConnectedApp(
        connectedApp: ContactConnectedApp,
        isEditable: Boolean,
    ): ContactConnectedAppUiModel {
        return ContactConnectedAppUiModel(
            packageName = connectedApp.app.packageName,
            label = connectedApp.app.label,
            iconUri = connectedApp.app.iconUri,
            entries = connectedApp.entries
                .map { entry ->
                    mapEntry(
                        mimeType = entry.mimeType,
                        entry = entry,
                        isDefaultChangeable = isEditable && entry.isSuperPrimary,
                        isCallingSimChangeable = false,
                    )
                }
                .toImmutableList(),
        )
    }

    private fun mapGroups(
        groups: List<ContactEntryGroup>,
        isEditable: Boolean,
        isCallingSimChangeable: Boolean,
    ): ImmutableList<ContactEntryGroupUiModel> {
        return groups
            .map { group -> mapGroup(group, isEditable, isCallingSimChangeable) }
            .toImmutableList()
    }

    private fun mapGroup(
        group: ContactEntryGroup,
        isEditable: Boolean,
        isCallingSimChangeable: Boolean,
    ): ContactEntryGroupUiModel {
        val hasSeveralOfMimeType = when (group.mimeType) {
            in DEFAULT_MARKED_MIME_TYPES -> group.entries.size > 1
            else -> false
        }

        return ContactEntryGroupUiModel(
            entries = group.entries
                .map { entry ->
                    val isDefaultChangeable =
                        isEditable && (hasSeveralOfMimeType || entry.isDefault)

                    mapEntry(
                        mimeType = group.mimeType,
                        entry = entry,
                        isDefaultChangeable = isDefaultChangeable,
                        isCallingSimChangeable = isCallingSimChangeable &&
                            entry.kind == ContactEntryKind.PHONE,
                    )
                }
                .toImmutableList(),
        )
    }

    private fun mapEntry(
        mimeType: String?,
        entry: ContactEntry,
        isDefaultChangeable: Boolean,
        isCallingSimChangeable: Boolean,
    ): ContactEntryUiModel {
        return ContactEntryUiModel(
            id = entry.id,
            isSuperPrimary = entry.isSuperPrimary,
            isDefault = entry.isDefault,
            isDefaultChangeable = isDefaultChangeable,
            isCallingSimChangeable = isCallingSimChangeable,
            icon = entryIcon(entry.kind),
            header = text(entry.header),
            isHeaderLtr = isDialable(mimeType),
            subHeader = text(entry.subHeader),
            text = entryText(entry),
            action = registeredAction(entry.actions.primaryAction),
            alternateAction = actionUiModel(registeredAction(entry.actions.alternateAction)),
            enhancedCallAction = actionUiModel(entry.actions.enhancedCallAction),
            editBeforeCallAction = registeredAction(entry.actions.editBeforeCallAction),
            copyText = entry.copyText,
            copyLabel = text(entry.copyLabel),
        )
    }

    private fun entryText(entry: ContactEntry): String? {
        if (!entry.isDefault || !isDefaultMarked(entry.kind)) {
            return entry.text
        }

        val default = context.getString(R.string.contact_entry_default)
        val type = entry.text

        return when {
            type.isNullOrEmpty() -> default
            else -> context.getString(R.string.contact_entry_default_type, type, default)
        }
    }

    private fun isDefaultMarked(kind: ContactEntryKind): Boolean {
        return kind in DEFAULT_MARKED_KINDS
    }

    private fun isDialable(mimeType: String?): Boolean {
        return mimeType == Phone.CONTENT_ITEM_TYPE || mimeType == SipAddress.CONTENT_ITEM_TYPE
    }

    private fun registeredAction(action: ContactEntryAction?): ContactEntryAction? {
        return action?.takeIf { candidate -> isEntryActionAvailable(candidate) }
    }

    private fun entryIcon(kind: ContactEntryKind): ContactEntryIcon? {
        return when (kind) {
            ContactEntryKind.PHONE -> ContactEntryIcon.CALL
            ContactEntryKind.SIP_ADDRESS -> ContactEntryIcon.SIP_CALL
            ContactEntryKind.EMAIL -> ContactEntryIcon.EMAIL
            ContactEntryKind.POSTAL -> ContactEntryIcon.PLACE
            ContactEntryKind.IM -> ContactEntryIcon.CHAT
            ContactEntryKind.ORGANIZATION -> ContactEntryIcon.ORGANIZATION
            ContactEntryKind.NICKNAME -> ContactEntryIcon.NICKNAME
            ContactEntryKind.WEBSITE -> ContactEntryIcon.WEBSITE
            ContactEntryKind.BIRTHDAY -> ContactEntryIcon.BIRTHDAY
            ContactEntryKind.EVENT -> ContactEntryIcon.EVENT
            ContactEntryKind.GROUP -> ContactEntryIcon.GROUP
            ContactEntryKind.IDENTITY -> ContactEntryIcon.IDENTITY
            ContactEntryKind.NOTE -> null
            ContactEntryKind.RELATION -> null
            ContactEntryKind.CUSTOM_FIELD -> null
            ContactEntryKind.OTHER -> null
        }
    }

    private fun actionUiModel(action: ContactEntryAction?): ContactEntryActionUiModel? {
        return when (action) {
            is ContactEntryAction.Sms -> ContactEntryActionUiModel(
                action = action,
                icon = ContactEntryIcon.MESSAGE,
                contentDescription = context.getString(R.string.sms_custom, action.number),
            )

            is ContactEntryAction.ShowDirections -> ContactEntryActionUiModel(
                action = action,
                icon = ContactEntryIcon.DIRECTIONS,
                contentDescription = context.getString(R.string.content_description_directions),
            )

            is ContactEntryAction.CallWithNote -> ContactEntryActionUiModel(
                action = action,
                icon = ContactEntryIcon.CALL_WITH_NOTE,
                contentDescription = context.getString(R.string.call_with_a_note),
            )

            is ContactEntryAction.VideoCall -> ContactEntryActionUiModel(
                action = action,
                icon = ContactEntryIcon.VIDEO_CALL,
                contentDescription = context.getString(R.string.description_video_call),
            )

            is ContactEntryAction.Call,
            is ContactEntryAction.EditNumberBeforeCall,
            is ContactEntryAction.SipCall,
            is ContactEntryAction.SendEmail,
            is ContactEntryAction.ShowOnMap,
            is ContactEntryAction.OpenUrl,
            is ContactEntryAction.OpenChat,
            is ContactEntryAction.ShowEventDate,
            is ContactEntryAction.SearchContacts,
            is ContactEntryAction.ViewDataItem,
            null,
            -> null
        }
    }

    private fun emptyPrompt(
        details: ContactDetails,
        contactCard: ImmutableList<ContactEntryGroupUiModel>,
        notes: ImmutableList<ContactEntryGroupUiModel>,
    ): ContactDetailsEmptyPromptUiModel? {
        if (contactCard.isNotEmpty() || notes.isNotEmpty()) {
            return null
        }

        val entries = when {
            details.capabilities.areAllRawContactsSimAccounts -> {
                persistentListOf(phoneNumberPrompt())
            }

            else -> persistentListOf(phoneNumberPrompt(), emailPrompt())
        }

        return ContactDetailsEmptyPromptUiModel(entries = entries)
    }

    private fun phoneNumberPrompt(): ContactEntryUiModel {
        return promptEntry(
            icon = ContactEntryIcon.CALL,
            headerResource = R.string.quickcontact_add_phone_number,
        )
    }

    private fun emailPrompt(): ContactEntryUiModel {
        return promptEntry(
            icon = ContactEntryIcon.EMAIL,
            headerResource = R.string.quickcontact_add_email,
        )
    }

    private fun promptEntry(
        icon: ContactEntryIcon,
        @StringRes headerResource: Int,
    ): ContactEntryUiModel {
        return ContactEntryUiModel(
            id = NO_DATA_ID,
            isSuperPrimary = false,
            isDefault = false,
            isDefaultChangeable = false,
            isCallingSimChangeable = false,
            icon = icon,
            header = context.getString(headerResource),
            isHeaderLtr = false,
            subHeader = null,
            text = null,
            action = null,
            alternateAction = null,
            enhancedCallAction = null,
            editBeforeCallAction = null,
            copyText = null,
            copyLabel = null,
        )
    }

    private fun text(entryText: ContactEntryText?): String? {
        return when (entryText) {
            null -> null
            is ContactEntryText.Value -> entryText.text
            is ContactEntryText.Label -> context.getString(labelResource(entryText.label))
        }
    }

    @StringRes
    private fun labelResource(label: ContactEntryLabel): Int {
        return when (label) {
            ContactEntryLabel.PHONE -> R.string.phoneLabelsGroup
            ContactEntryLabel.EMAIL -> R.string.emailLabelsGroup
            ContactEntryLabel.POSTAL -> R.string.postalLabelsGroup
            ContactEntryLabel.IM -> R.string.header_im_entry
            ContactEntryLabel.ORGANIZATION -> R.string.header_organization_entry
            ContactEntryLabel.NICKNAME -> R.string.header_nickname_entry
            ContactEntryLabel.NOTE -> R.string.header_note_entry
            ContactEntryLabel.WEBSITE -> R.string.header_website_entry
            ContactEntryLabel.EVENT -> R.string.header_event_entry
            ContactEntryLabel.RELATION -> R.string.header_relation_entry
            ContactEntryLabel.CUSTOM_FIELD -> R.string.label_custom_field
        }
    }

    private companion object {
        val DEFAULT_MARKED_KINDS = setOf(
            ContactEntryKind.PHONE,
            ContactEntryKind.EMAIL,
            ContactEntryKind.POSTAL,
        )

        val DEFAULT_MARKED_MIME_TYPES = setOf(
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE,
        )

        const val NO_DATA_ID = -1L
        const val SUBTITLE_SEPARATOR = " • "
        const val NICKNAME_SEPARATOR = ", "
    }
}

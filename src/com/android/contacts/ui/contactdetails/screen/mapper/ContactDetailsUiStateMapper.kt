package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.usecase.IsEntryActionAvailable
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
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
        menu: ContactDetailsMenu,
        displayOrder: DisplayOrder,
    ): ContactDetailsContent
}

internal class ContactDetailsUiStateMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val isEntryActionAvailable: IsEntryActionAvailable,
    private val contactQuickActionsMapper: ContactQuickActionsMapper,
) : ContactDetailsUiStateMapper {

    override fun map(
        details: ContactDetails,
        cards: ContactDetailsCards,
        quickActions: List<ContactQuickAction>,
        menu: ContactDetailsMenu,
        displayOrder: DisplayOrder,
    ): ContactDetailsContent {
        val isEditable = !details.capabilities.isDirectoryEntry
        val contactCard = mapGroups(cards.contactCard, isEditable)
        val aboutCard = mapGroups(cards.aboutCard, isEditable)

        return ContactDetailsContent.Loaded(
            header = mapHeader(details, cards, displayOrder),
            quickActions = contactQuickActionsMapper.map(quickActions),
            contactCard = contactCard,
            aboutCard = aboutCard,
            aboutCardTitle = aboutCardTitle(cards.aboutCardGivenName),
            emptyPrompt = emptyPrompt(details, contactCard, aboutCard),
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
        return listOfNotNull(
            phoneticName(details, displayName),
            cards.headerNickname,
            cards.headerOrganization,
        ).toImmutableList()
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

    private fun aboutCardTitle(givenName: String?): String {
        val title = context.getString(R.string.about_card_title)

        return when {
            givenName.isNullOrEmpty() -> title
            else -> "$title $givenName"
        }
    }

    private fun mapGroups(
        groups: List<ContactEntryGroup>,
        isEditable: Boolean,
    ): ImmutableList<ContactEntryGroupUiModel> {
        return groups
            .map { group -> mapGroup(group, isEditable) }
            .toImmutableList()
    }

    private fun mapGroup(
        group: ContactEntryGroup,
        isEditable: Boolean,
    ): ContactEntryGroupUiModel {
        val hasSeveralOfMimeType = when (group.mimeType) {
            Phone.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE -> group.entries.size > 1
            else -> false
        }

        return ContactEntryGroupUiModel(
            entries = group.entries
                .map { entry ->
                    val isDefaultChangeable =
                        isEditable && (hasSeveralOfMimeType || entry.isSuperPrimary)

                    mapEntry(
                        mimeType = group.mimeType,
                        entry = entry,
                        isDefaultChangeable = isDefaultChangeable,
                    )
                }
                .toImmutableList(),
        )
    }

    private fun mapEntry(
        mimeType: String?,
        entry: ContactEntry,
        isDefaultChangeable: Boolean,
    ): ContactEntryUiModel {
        return ContactEntryUiModel(
            id = entry.id,
            isSuperPrimary = entry.isSuperPrimary,
            isDefaultChangeable = isDefaultChangeable,
            icon = entryIcon(mimeType),
            header = text(entry.header),
            isHeaderLtr = isDialable(mimeType),
            subHeader = entry.subHeader,
            text = entry.text,
            action = registeredAction(entry.actions.primary),
            alternateAction = actionUiModel(registeredAction(entry.actions.alternate)),
            thirdAction = actionUiModel(entry.actions.third),
            copyText = entry.copyText,
            copyLabel = text(entry.copyLabel),
        )
    }

    private fun isDialable(mimeType: String?): Boolean {
        return mimeType == Phone.CONTENT_ITEM_TYPE || mimeType == SipAddress.CONTENT_ITEM_TYPE
    }

    private fun registeredAction(action: ContactEntryAction?): ContactEntryAction? {
        return action?.takeIf { candidate -> isEntryActionAvailable(candidate) }
    }

    private fun entryIcon(mimeType: String?): ContactEntryIcon? {
        return when (mimeType) {
            Phone.CONTENT_ITEM_TYPE -> ContactEntryIcon.CALL
            Email.CONTENT_ITEM_TYPE -> ContactEntryIcon.EMAIL
            StructuredPostal.CONTENT_ITEM_TYPE -> ContactEntryIcon.PLACE
            SipAddress.CONTENT_ITEM_TYPE -> ContactEntryIcon.SIP_CALL
            else -> null
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

            else -> null
        }
    }

    private fun emptyPrompt(
        details: ContactDetails,
        contactCard: ImmutableList<ContactEntryGroupUiModel>,
        aboutCard: ImmutableList<ContactEntryGroupUiModel>,
    ): ContactDetailsEmptyPromptUiModel? {
        if (contactCard.isNotEmpty() || aboutCard.isNotEmpty()) {
            return null
        }

        val entries = when {
            details.capabilities.areAllRawContactsSimAccounts -> {
                persistentListOf(promptEntry(R.string.quickcontact_add_phone_number))
            }

            else -> persistentListOf(
                promptEntry(R.string.quickcontact_add_phone_number),
                promptEntry(R.string.quickcontact_add_email),
            )
        }

        return ContactDetailsEmptyPromptUiModel(entries = entries)
    }

    private fun promptEntry(
        @StringRes headerResource: Int,
    ): ContactEntryUiModel {
        val icon = when (headerResource) {
            R.string.quickcontact_add_phone_number -> ContactEntryIcon.CALL
            else -> ContactEntryIcon.EMAIL
        }

        return ContactEntryUiModel(
            id = NO_DATA_ID,
            isSuperPrimary = false,
            isDefaultChangeable = false,
            icon = icon,
            header = context.getString(headerResource),
            isHeaderLtr = false,
            subHeader = null,
            text = null,
            action = null,
            alternateAction = null,
            thirdAction = null,
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
            ContactEntryLabel.PHONETIC_NAME -> R.string.name_phonetic
            ContactEntryLabel.NOTE -> R.string.header_note_entry
            ContactEntryLabel.WEBSITE -> R.string.header_website_entry
            ContactEntryLabel.EVENT -> R.string.header_event_entry
            ContactEntryLabel.RELATION -> R.string.header_relation_entry
            ContactEntryLabel.CUSTOM_FIELD -> R.string.label_custom_field
        }
    }

    private companion object {
        const val NO_DATA_ID = -1L
    }
}

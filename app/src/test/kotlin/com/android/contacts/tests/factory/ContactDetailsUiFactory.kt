package com.android.contacts.tests.factory

import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import kotlinx.collections.immutable.toImmutableList

internal fun contactHeaderUiModel(
    displayName: String = "Alex Doe",
    phoneticName: String? = null,
    photo: ContactAvatarImage? = null,
    avatarSeed: String? = "lookup-key",
    isBusiness: Boolean = false,
): ContactHeaderUiModel {
    return ContactHeaderUiModel(
        displayName = displayName,
        phoneticName = phoneticName,
        photo = photo,
        avatarSeed = avatarSeed,
        isBusiness = isBusiness,
    )
}

internal fun contactEntryUiModel(
    id: Long = 1L,
    isSuperPrimary: Boolean = false,
    isDefaultChangeable: Boolean = false,
    icon: ContactEntryIcon? = ContactEntryIcon.CALL,
    header: String? = "555 0001",
    subHeader: String? = null,
    text: String? = "Mobile",
    action: ContactEntryAction? = null,
    alternateAction: ContactEntryActionUiModel? = null,
    thirdAction: ContactEntryActionUiModel? = null,
    copyText: String? = null,
    copyLabel: String? = null,
): ContactEntryUiModel {
    return ContactEntryUiModel(
        id = id,
        isSuperPrimary = isSuperPrimary,
        isDefaultChangeable = isDefaultChangeable,
        icon = icon,
        header = header,
        subHeader = subHeader,
        text = text,
        action = action,
        alternateAction = alternateAction,
        thirdAction = thirdAction,
        copyText = copyText,
        copyLabel = copyLabel,
    )
}

internal fun contactEntryActionUiModel(
    action: ContactEntryAction = ContactEntryAction.Sms(number = "555 0001"),
    icon: ContactEntryIcon = ContactEntryIcon.MESSAGE,
    contentDescription: String = "Text 555 0001",
): ContactEntryActionUiModel {
    return ContactEntryActionUiModel(
        action = action,
        icon = icon,
        contentDescription = contentDescription,
    )
}

internal fun contactEntryGroupUiModel(
    entries: List<ContactEntryUiModel>,
): ContactEntryGroupUiModel {
    return ContactEntryGroupUiModel(entries = entries.toImmutableList())
}

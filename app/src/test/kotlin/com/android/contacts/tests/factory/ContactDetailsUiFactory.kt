package com.android.contacts.tests.factory

import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.common.components.ContactAvatarImage
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
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingUiModel
import com.android.contacts.ui.contactdetails.screen.model.RecentCallUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal fun contactHeaderUiModel(
    displayName: String = "Alex Doe",
    subtitles: ImmutableList<String> = persistentListOf(),
    photo: ContactAvatarImage? = null,
    avatarSeed: String? = "lookup-key",
    isBusiness: Boolean = false,
    isDisplayNameLtr: Boolean = false,
): ContactHeaderUiModel {
    return ContactHeaderUiModel(
        displayName = displayName,
        subtitles = subtitles,
        photo = photo,
        avatarSeed = avatarSeed,
        isBusiness = isBusiness,
        isDisplayNameLtr = isDisplayNameLtr,
    )
}

internal fun contactEntryUiModel(
    id: Long = 1L,
    isSuperPrimary: Boolean = false,
    isDefault: Boolean = isSuperPrimary,
    isDefaultChangeable: Boolean = false,
    isCallingSimChangeable: Boolean = false,
    icon: ContactEntryIcon? = ContactEntryIcon.CALL,
    header: String? = "555 0001",
    subHeader: String? = null,
    text: String? = "Mobile",
    isHeaderLtr: Boolean = false,
    action: ContactEntryAction? = null,
    alternateAction: ContactEntryActionUiModel? = null,
    enhancedCallAction: ContactEntryActionUiModel? = null,
    editBeforeCallAction: ContactEntryAction? = null,
    copyText: String? = null,
    copyLabel: String? = null,
): ContactEntryUiModel {
    return ContactEntryUiModel(
        id = id,
        isSuperPrimary = isSuperPrimary,
        isDefault = isDefault,
        isDefaultChangeable = isDefaultChangeable,
        isCallingSimChangeable = isCallingSimChangeable,
        icon = icon,
        header = header,
        isHeaderLtr = isHeaderLtr,
        subHeader = subHeader,
        text = text,
        action = action,
        alternateAction = alternateAction,
        enhancedCallAction = enhancedCallAction,
        editBeforeCallAction = editBeforeCallAction,
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

internal fun contactQuickActionUiModel(
    icon: ContactEntryIcon = ContactEntryIcon.CALL,
    label: String = "Call",
    action: ContactEntryAction? = ContactEntryAction.Call(number = "555 0001"),
): ContactQuickActionUiModel {
    return ContactQuickActionUiModel(
        icon = icon,
        label = label,
        action = action,
    )
}

internal fun contactSettingUiModel(
    icon: ContactSettingIcon = ContactSettingIcon.SHARE,
    title: String = "Share",
    subtitle: String? = null,
    action: ContactDetailsAction = ContactDetailsAction.ShareClick,
    isDestructive: Boolean = false,
    isChecked: Boolean? = null,
): ContactSettingUiModel {
    return ContactSettingUiModel(
        icon = icon,
        title = title,
        subtitle = subtitle,
        action = action,
        isDestructive = isDestructive,
        isChecked = isChecked,
    )
}

internal fun contactDetailsLoadedContent(
    header: ContactHeaderUiModel = contactHeaderUiModel(),
    quickActions: ImmutableList<ContactQuickActionUiModel> = persistentListOf(),
    groups: ImmutableList<ContactGroupUiModel> = persistentListOf(),
    contactCard: ImmutableList<ContactEntryGroupUiModel> = persistentListOf(),
    connectedApps: ImmutableList<ContactConnectedAppUiModel> = persistentListOf(),
    notes: ImmutableList<ContactEntryGroupUiModel> = persistentListOf(),
    settings: ImmutableList<ContactSettingUiModel> = persistentListOf(),
    accounts: ImmutableList<ContactAccountUiModel> = persistentListOf(),
    recentCalls: ImmutableList<RecentCallUiModel> = persistentListOf(),
    callingSim: CallingSimUiModel? = null,
    emptyPrompt: ContactDetailsEmptyPromptUiModel? = null,
    menu: ContactDetailsMenu = contactDetailsMenu(),
    isStarred: Boolean = false,
): ContactDetailsContent.Loaded {
    return ContactDetailsContent.Loaded(
        header = header,
        quickActions = quickActions,
        groups = groups,
        contactCard = contactCard,
        connectedApps = connectedApps,
        notes = notes,
        settings = settings,
        accounts = accounts,
        recentCalls = recentCalls,
        callingSim = callingSim,
        emptyPrompt = emptyPrompt,
        menu = menu,
        isStarred = isStarred,
    )
}

package com.android.contacts.ui.contactdetails.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.contactdetails.screen.model.ContactAccountUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactConnectedAppUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingUiModel
import com.android.contacts.ui.contactdetails.screen.model.RecentCallDirection
import com.android.contacts.ui.contactdetails.screen.model.RecentCallUiModel
import com.android.contacts.ui.core.ContactsPreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@PreviewLightDark
@Composable
private fun ContactDetailsContentPreview() {
    ContactsPreviewTheme {
        ContactDetailsContent(
            uiState = State(content = previewContent()),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsContentLoadingPreview() {
    ContactsPreviewTheme {
        ContactDetailsContent(
            uiState = State(content = Content.Loading),
            onAction = {},
        )
    }
}

private fun previewContent(): Content.Loaded {
    return Content.Loaded(
        groups = persistentListOf(
            ContactGroupUiModel(id = 1L, title = "Coworkers"),
            ContactGroupUiModel(id = 2L, title = "Family"),
        ),
        header = previewHeader(),
        quickActions = previewQuickActions(),
        contactCard = persistentListOf(
            ContactEntryGroupUiModel(
                entries = persistentListOf(
                    previewEntry(id = 1L, header = "+1 555 0123", text = "Mobile"),
                ),
            ),
        ),
        connectedApps = previewConnectedApps(),
        notes = previewNotes(),
        callingSim = null,
        recentCalls = persistentListOf(
            RecentCallUiModel(
                title = "Call time 01:20",
                numberLabel = "Mobile",
                date = "Jun 3",
                direction = RecentCallDirection.INCOMING,
                contentDescription = "Incoming call, 01:20, Jun 3",
            ),
        ),
        accounts = persistentListOf(
            ContactAccountUiModel(name = "alex@example.org", iconUri = null),
        ),
        settings = previewSettings(),
        emptyPrompt = null,
        menu = previewMenu(),
        isStarred = false,
    )
}

private fun previewHeader(): ContactHeaderUiModel {
    return ContactHeaderUiModel(
        displayName = "Anna Smith",
        subtitles = persistentListOf("Annie", "Acme"),
        photo = null,
        avatarSeed = "anna-smith",
        isBusiness = false,
        isDisplayNameLtr = false,
    )
}

private fun previewQuickActions(): ImmutableList<ContactQuickActionUiModel> {
    return persistentListOf(
        previewQuickAction(ContactEntryIcon.CALL, "Call", ContactEntryAction.Call("+1 555 0123")),
        previewQuickAction(ContactEntryIcon.MESSAGE, "Text", ContactEntryAction.Sms("+1 555 0123")),
        previewQuickAction(ContactEntryIcon.VIDEO_CALL, "Video", null),
        previewQuickAction(ContactEntryIcon.EMAIL, "Email", null),
    )
}

private fun previewConnectedApps(): ImmutableList<ContactConnectedAppUiModel> {
    return persistentListOf(
        ContactConnectedAppUiModel(
            packageName = "com.example.chat",
            label = "Chat",
            iconUri = null,
            entries = persistentListOf(
                previewEntry(id = 3L, header = "Message +1 555 0123", text = null),
                previewEntry(id = 4L, header = "Voice call +1 555 0123", text = null),
            ),
        ),
    )
}

private fun previewNotes(): ImmutableList<ContactEntryGroupUiModel> {
    return persistentListOf(
        ContactEntryGroupUiModel(
            entries = persistentListOf(
                previewEntry(
                    id = 2L,
                    header = "Met at the conference",
                    text = null,
                    icon = null,
                ),
            ),
        ),
    )
}

private fun previewSettings(): ImmutableList<ContactSettingUiModel> {
    return persistentListOf(
        previewSetting(ContactSettingIcon.RINGTONE, "Set ringtone", "Bright Morning"),
        previewSetting(ContactSettingIcon.SHARE, "Share"),
        previewSetting(ContactSettingIcon.DELETE, "Delete", isDestructive = true),
    )
}

private fun previewMenu(): ContactDetailsMenu {
    return ContactDetailsMenu(
        isStarVisible = true,
        editAction = ContactDetailsEditAction.EDIT,
        isJoinVisible = true,
        isLinkedContactsVisible = false,
        isDeleteVisible = true,
        isShareVisible = true,
        isShortcutVisible = true,
        isRingtoneVisible = true,
        isSendToVoicemailVisible = true,
    )
}

private fun previewSetting(
    icon: ContactSettingIcon,
    title: String,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    isChecked: Boolean? = null,
): ContactSettingUiModel {
    return ContactSettingUiModel(
        icon = icon,
        title = title,
        subtitle = subtitle,
        action = Action.RingtoneClick,
        isDestructive = isDestructive,
        isChecked = isChecked,
    )
}

private fun previewQuickAction(
    icon: ContactEntryIcon,
    label: String,
    action: ContactEntryAction?,
): ContactQuickActionUiModel {
    return ContactQuickActionUiModel(
        icon = icon,
        label = label,
        action = action,
    )
}

private fun previewEntry(
    id: Long,
    header: String,
    text: String?,
    icon: ContactEntryIcon? = ContactEntryIcon.CALL,
): ContactEntryUiModel {
    return ContactEntryUiModel(
        id = id,
        icon = icon,
        header = header,
        isHeaderLtr = false,
        subHeader = null,
        text = text,
        isSuperPrimary = false,
        isDefault = false,
        isDefaultChangeable = false,
        isCallingSimChangeable = false,
        action = null,
        alternateAction = null,
        enhancedCallAction = null,
        editBeforeCallAction = null,
        copyText = header,
        copyLabel = text,
    )
}

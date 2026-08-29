package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import kotlinx.collections.immutable.ImmutableList

internal sealed interface ContactDetailsContent {

    data object Loading : ContactDetailsContent
    data object NotFound : ContactDetailsContent
    data object Error : ContactDetailsContent

    @Immutable
    data class Loaded(
        val header: ContactHeaderUiModel,
        val quickActions: ImmutableList<ContactQuickActionUiModel>,
        val groups: ImmutableList<String>,
        val contactCard: ImmutableList<ContactEntryGroupUiModel>,
        val connectedApps: ImmutableList<ContactConnectedAppUiModel>,
        val notes: ImmutableList<ContactEntryGroupUiModel>,
        val settings: ImmutableList<ContactSettingUiModel>,
        val recentCalls: ImmutableList<RecentCallUiModel>,
        val callingSim: CallingSimUiModel?,
        val emptyPrompt: ContactDetailsEmptyPromptUiModel?,
        val menu: ContactDetailsMenu,
        val isStarred: Boolean,
    ) : ContactDetailsContent
}

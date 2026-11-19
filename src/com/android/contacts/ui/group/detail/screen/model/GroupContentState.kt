package com.android.contacts.ui.group.detail.screen.model

import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.groups.model.GroupDetails

internal sealed interface GroupContentState {

    data object Loading : GroupContentState

    data object Deleted : GroupContentState

    data class Loaded(
        val details: GroupDetails,
        val members: List<ContactItem>,
    ) : GroupContentState
}

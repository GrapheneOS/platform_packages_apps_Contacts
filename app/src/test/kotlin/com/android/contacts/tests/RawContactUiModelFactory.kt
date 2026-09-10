package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel

internal object RawContactUiModelFactory {
    fun build(
        id: Long = 1L,
        avatarImage: ContactAvatarImage? = null,
        displayName: String = "Name",
        accountLabel: String? = null,
        accountIconData: AccountIconData? = null,
    ): RawContactUiModel {
        return RawContactUiModel(
            id = id,
            avatarImage = avatarImage,
            displayName = displayName,
            accountLabel = accountLabel,
            accountIconData = accountIconData,
        )
    }
}

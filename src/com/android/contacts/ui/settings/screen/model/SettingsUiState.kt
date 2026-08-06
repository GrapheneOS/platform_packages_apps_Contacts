package com.android.contacts.ui.settings.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class SettingsUiState(
    val groups: ImmutableList<SettingsGroupUiModel>? = null,
    val sortOrder: SortOrder? = null,
    val displayOrder: DisplayOrder? = null,
    val phoneticNameDisplay: PhoneticNameDisplay? = null,
) {
    val isLoading: Boolean get() = groups == null
}

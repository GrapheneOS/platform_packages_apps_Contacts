package com.android.contacts.ui.common.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

internal typealias ContactSectionedList =
    ImmutableMap<ContactListSection, ImmutableList<ContactUiModel>>

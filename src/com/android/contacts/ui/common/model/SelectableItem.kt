package com.android.contacts.ui.common.model

import androidx.compose.runtime.Immutable

@Immutable
data class SelectableItem<I>(val item: I, val isSelected: Boolean = false)

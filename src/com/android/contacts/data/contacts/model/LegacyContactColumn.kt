package com.android.contacts.data.contacts.model

internal data class LegacyContactColumn(
    val id: Long,
    val kind: LegacyContactsKind,
    val displayName: String? = null,
    val value: String? = null,
    val type: Int? = null,
    val label: String? = null,
    val isStarred: Boolean = false,
)

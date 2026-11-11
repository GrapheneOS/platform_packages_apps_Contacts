package com.android.contacts.data.contacts.model

internal data class DirectoryColumn(
    val id: Long,
    val displayName: String? = null,
    val packageName: String? = null,
    val typeResourceId: Int? = null,
)

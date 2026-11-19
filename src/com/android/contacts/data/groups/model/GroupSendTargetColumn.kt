package com.android.contacts.data.groups.model

internal data class GroupSendTargetColumn(
    val contactId: Long,
    val dataId: Long,
    val value: String,
    val isSuperPrimary: Boolean,
)

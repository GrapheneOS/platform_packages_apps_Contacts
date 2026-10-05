package com.android.contacts.data.groups.model

internal data class GroupColumn(
    val id: Long,
    val title: String,
    val summaryCount: Int?,
    val systemId: String?,
    val accountName: String?,
    val accountType: String?,
    val accountDataSet: String?,
    val isReadOnly: Boolean,
    val isDeleted: Boolean,
)

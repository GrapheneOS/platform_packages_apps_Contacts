package com.android.contacts.data.contacts.model

internal data class RawContact(
    val id: Long,
    val photoUri: String?,
    val displayName: String?,
    val displayNameAlt: String?,
    val accountName: String?,
    val accountType: String?,
    val accountDataSet: String?,
)

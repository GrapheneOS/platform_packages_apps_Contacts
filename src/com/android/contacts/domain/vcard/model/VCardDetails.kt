package com.android.contacts.domain.vcard.model

internal data class VCardDetails(
    val estimatedType: Int,
    val estimatedCharset: String?,
    val version: VCardVersion,
    val entryCount: Int,
)

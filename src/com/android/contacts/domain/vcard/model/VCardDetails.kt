package com.android.contacts.domain.vcard.model

data class VCardDetails(
    val estimatedType: Int,
    val estimatedCharset: String?,
    val version: VCardVersion,
    val entryCount: Int,
)

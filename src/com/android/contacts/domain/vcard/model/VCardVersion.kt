package com.android.contacts.domain.vcard.model

@Suppress("detekt:style:MagicNumber")
enum class VCardVersion(
    val value: Int,
) {
    AutoDetect(0),
    V21(1),
    V30(2),
    V40(3),
}

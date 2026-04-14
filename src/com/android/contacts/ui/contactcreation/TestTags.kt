package com.android.contacts.ui.contactcreation

internal object TestTags {
    // Top-level
    const val SAVE_BUTTON = "contact_creation_save_button"
    const val BACK_BUTTON = "contact_creation_back_button"

    // Name section
    const val NAME_PREFIX = "contact_creation_name_prefix"
    const val NAME_FIRST = "contact_creation_name_first"
    const val NAME_MIDDLE = "contact_creation_name_middle"
    const val NAME_LAST = "contact_creation_name_last"
    const val NAME_SUFFIX = "contact_creation_name_suffix"

    // Phone section
    const val PHONE_ADD = "contact_creation_phone_add"
    fun phoneField(index: Int): String = "contact_creation_phone_field_$index"
    fun phoneDelete(index: Int): String = "contact_creation_phone_delete_$index"
    fun phoneType(index: Int): String = "contact_creation_phone_type_$index"

    // Email section
    const val EMAIL_ADD = "contact_creation_email_add"
    fun emailField(index: Int): String = "contact_creation_email_field_$index"
    fun emailDelete(index: Int): String = "contact_creation_email_delete_$index"
    fun emailType(index: Int): String = "contact_creation_email_type_$index"

    // Account
    const val ACCOUNT_CHIP = "contact_creation_account_chip"

    // Photo
    const val PHOTO_AVATAR = "contact_creation_photo_avatar"
}

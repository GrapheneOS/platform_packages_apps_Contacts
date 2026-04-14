package com.android.contacts.ui.contactcreation

@Suppress("TooManyFunctions")
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

    // Address section
    const val ADDRESS_ADD = "contact_creation_address_add"
    fun addressStreet(index: Int): String = "contact_creation_address_street_$index"
    fun addressCity(index: Int): String = "contact_creation_address_city_$index"
    fun addressRegion(index: Int): String = "contact_creation_address_region_$index"
    fun addressPostcode(index: Int): String = "contact_creation_address_postcode_$index"
    fun addressCountry(index: Int): String = "contact_creation_address_country_$index"
    fun addressDelete(index: Int): String = "contact_creation_address_delete_$index"
    fun addressType(index: Int): String = "contact_creation_address_type_$index"

    // Organization section
    const val ORG_COMPANY = "contact_creation_org_company"
    const val ORG_TITLE = "contact_creation_org_title"

    // More fields section
    const val MORE_FIELDS_TOGGLE = "contact_creation_more_fields_toggle"
    const val MORE_FIELDS_CONTENT = "contact_creation_more_fields_content"

    // Event
    const val EVENT_ADD = "contact_creation_event_add"
    fun eventField(index: Int): String = "contact_creation_event_field_$index"
    fun eventDelete(index: Int): String = "contact_creation_event_delete_$index"
    fun eventType(index: Int): String = "contact_creation_event_type_$index"

    // Relation
    const val RELATION_ADD = "contact_creation_relation_add"
    fun relationField(index: Int): String = "contact_creation_relation_field_$index"
    fun relationDelete(index: Int): String = "contact_creation_relation_delete_$index"
    fun relationType(index: Int): String = "contact_creation_relation_type_$index"

    // IM
    const val IM_ADD = "contact_creation_im_add"
    fun imField(index: Int): String = "contact_creation_im_field_$index"
    fun imDelete(index: Int): String = "contact_creation_im_delete_$index"
    fun imProtocol(index: Int): String = "contact_creation_im_protocol_$index"

    // Website
    const val WEBSITE_ADD = "contact_creation_website_add"
    fun websiteField(index: Int): String = "contact_creation_website_field_$index"
    fun websiteDelete(index: Int): String = "contact_creation_website_delete_$index"
    fun websiteType(index: Int): String = "contact_creation_website_type_$index"

    // Note
    const val NOTE_FIELD = "contact_creation_note_field"

    // Nickname
    const val NICKNAME_FIELD = "contact_creation_nickname_field"

    // SIP
    const val SIP_FIELD = "contact_creation_sip_field"

    // Group section
    const val GROUP_SECTION = "contact_creation_group_section"
    fun groupCheckbox(index: Int): String = "contact_creation_group_checkbox_$index"

    // Account
    const val ACCOUNT_CHIP = "contact_creation_account_chip"

    // Discard dialog
    const val DISCARD_DIALOG = "contact_creation_discard_dialog"
    const val DISCARD_DIALOG_CONFIRM = "contact_creation_discard_dialog_confirm"
    const val DISCARD_DIALOG_DISMISS = "contact_creation_discard_dialog_dismiss"

    // Photo
    const val PHOTO_AVATAR = "contact_creation_photo_avatar"
    const val PHOTO_MENU = "contact_creation_photo_menu"
    const val PHOTO_PICK_GALLERY = "contact_creation_photo_pick_gallery"
    const val PHOTO_TAKE_CAMERA = "contact_creation_photo_take_camera"
    const val PHOTO_REMOVE = "contact_creation_photo_remove"
    const val PHOTO_PLACEHOLDER_ICON = "contact_creation_photo_placeholder_icon"
}

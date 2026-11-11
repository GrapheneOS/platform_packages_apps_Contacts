package com.android.contacts.ui.selection.screen.model

import com.android.contacts.domain.contacts.model.ContactItemKey

internal const val CONTACT_SELECTION_CLOSE_BUTTON_TEST_TAG = "contact_selection_close"
internal const val CONTACT_SELECTION_TITLE_TEST_TAG = "contact_selection_title"
internal const val CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG = "contact_selection_search"
internal const val CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG = "contact_selection_submit"
internal const val CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG = "contact_selection_search_field"
internal const val CONTACT_SELECTION_SEARCH_BACK_BUTTON_TEST_TAG = "contact_selection_search_back"
internal const val CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG = "contact_selection_search_clear"
internal const val CONTACT_SELECTION_DESCRIPTION_TEST_TAG = "contact_selection_description"
internal const val CONTACT_SELECTION_LOADING_TEST_TAG = "contact_selection_loading"
internal const val CONTACT_SELECTION_EMPTY_TEST_TAG = "contact_selection_empty"
internal const val CONTACT_SELECTION_LIST_TEST_TAG = "contact_selection_list"
internal const val CONTACT_SELECTION_LIST_HEADER_TEST_TAG = "contact_selection_list_header"
internal const val CONTACT_SELECTION_CREATE_BUTTON_TEST_TAG = "contact_selection_create"
internal const val CONTACT_SELECTION_SECTION_TEST_TAG_PREFIX = "contact_selection_section_"
internal const val CONTACT_SELECTION_CONTACT_TEST_TAG_PREFIX = "contact_selection_contact_"

internal fun contactSelectionSectionTestTag(section: ContactListSection): String {
    return CONTACT_SELECTION_SECTION_TEST_TAG_PREFIX + section.testTagSuffix()
}

internal fun contactSelectionContactTestTag(
    section: ContactListSection,
    key: ContactItemKey,
): String {
    return CONTACT_SELECTION_CONTACT_TEST_TAG_PREFIX +
        section.testTagSuffix() + "_" + key.testTagSuffix()
}

private fun ContactListSection.testTagSuffix(): String {
    return when (this) {
        ContactListSection.Undefined -> "undefined"
        ContactListSection.Suggestions -> "suggestions"
        ContactListSection.Favorites -> "favorites"
        is ContactListSection.Alphabetical -> "letter_$letter"
        is ContactListSection.Directory -> "directory_$directoryId"
    }
}

private fun ContactItemKey.testTagSuffix(): String {
    return when (this) {
        is ContactItemKey.Local -> "local_$id"
        is ContactItemKey.Directory -> "directory_${directoryId}_$id"
    }
}

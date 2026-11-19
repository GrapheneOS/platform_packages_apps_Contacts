package com.android.contacts.ui.group.detail.screen.model

import com.android.contacts.domain.contacts.model.ContactItemKey

internal const val GROUP_BACK_BUTTON_TEST_TAG = "group_back"
internal const val GROUP_CLOSE_SELECTION_BUTTON_TEST_TAG = "group_close_selection"
internal const val GROUP_TITLE_TEST_TAG = "group_title"
internal const val GROUP_SUBTITLE_TEST_TAG = "group_subtitle"
internal const val GROUP_ADD_MEMBERS_BUTTON_TEST_TAG = "group_add_members"
internal const val GROUP_REMOVE_MEMBERS_BUTTON_TEST_TAG = "group_remove_members"
internal const val GROUP_OVERFLOW_BUTTON_TEST_TAG = "group_overflow"
internal const val GROUP_OVERFLOW_SELECT_ALL_TEST_TAG = "group_overflow_select_all"
internal const val GROUP_OVERFLOW_SEND_EMAIL_TEST_TAG = "group_overflow_send_email"
internal const val GROUP_OVERFLOW_SEND_MESSAGE_TEST_TAG = "group_overflow_send_message"
internal const val GROUP_OVERFLOW_RENAME_TEST_TAG = "group_overflow_rename"
internal const val GROUP_OVERFLOW_DELETE_TEST_TAG = "group_overflow_delete"
internal const val GROUP_CREATE_CONTACT_BUTTON_TEST_TAG = "group_create_contact"
internal const val GROUP_LOADING_TEST_TAG = "group_loading"
internal const val GROUP_EMPTY_TEST_TAG = "group_empty"
internal const val GROUP_EMPTY_ADD_MEMBERS_BUTTON_TEST_TAG = "group_empty_add_members"
internal const val GROUP_LIST_TEST_TAG = "group_list"
internal const val GROUP_DELETE_DIALOG_MESSAGE_TEST_TAG = "group_delete_dialog_message"
internal const val GROUP_DELETE_DIALOG_CONFIRM_BUTTON_TEST_TAG = "group_delete_dialog_confirm"
internal const val GROUP_DELETE_DIALOG_DISMISS_BUTTON_TEST_TAG = "group_delete_dialog_dismiss"
internal const val GROUP_CONTACT_TEST_TAG_PREFIX = "group_contact_"

internal fun groupContactTestTag(key: ContactItemKey): String {
    return GROUP_CONTACT_TEST_TAG_PREFIX + when (key) {
        is ContactItemKey.Local -> "local_${key.id}"
        is ContactItemKey.Directory -> "directory_${key.directoryId}_${key.id}"
    }
}

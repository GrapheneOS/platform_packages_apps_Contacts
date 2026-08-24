package com.android.contacts.ui.contactdetails.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Voicemail
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingIcon

internal fun ContactSettingIcon.imageVector(): ImageVector {
    return when (this) {
        ContactSettingIcon.RINGTONE -> Icons.Rounded.Notifications
        ContactSettingIcon.SEND_TO_VOICEMAIL -> Icons.Rounded.Voicemail
        ContactSettingIcon.SHARE -> Icons.Rounded.Share
        ContactSettingIcon.SHORTCUT -> Icons.Rounded.AddToHomeScreen
        ContactSettingIcon.LINK -> Icons.Rounded.Link
        ContactSettingIcon.LINKED_CONTACTS -> Icons.Rounded.People
        ContactSettingIcon.DELETE -> Icons.Rounded.Delete
    }
}

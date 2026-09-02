package com.android.contacts.ui.contactdetails.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.SpeakerNotes
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DialerSip
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon

@Suppress("CyclomaticComplexMethod")
internal fun ContactEntryIcon.imageVector(): ImageVector {
    return when (this) {
        ContactEntryIcon.CALL -> Icons.Rounded.Call
        ContactEntryIcon.MESSAGE -> Icons.AutoMirrored.Rounded.Message
        ContactEntryIcon.VIDEO_CALL -> Icons.Rounded.Videocam
        ContactEntryIcon.CALL_WITH_NOTE -> Icons.AutoMirrored.Rounded.SpeakerNotes
        ContactEntryIcon.EMAIL -> Icons.Rounded.Email
        ContactEntryIcon.PLACE -> Icons.Rounded.Place
        ContactEntryIcon.DIRECTIONS -> Icons.Rounded.Directions
        ContactEntryIcon.SIP_CALL -> Icons.Rounded.DialerSip
        ContactEntryIcon.CHAT -> Icons.AutoMirrored.Rounded.Chat
        ContactEntryIcon.ORGANIZATION -> Icons.Rounded.Business
        ContactEntryIcon.NICKNAME -> Icons.Rounded.Badge
        ContactEntryIcon.WEBSITE -> Icons.Rounded.Link
        ContactEntryIcon.BIRTHDAY -> Icons.Rounded.Cake
        ContactEntryIcon.EVENT -> Icons.Rounded.Event
        ContactEntryIcon.GROUP -> Icons.AutoMirrored.Rounded.Label
        ContactEntryIcon.IDENTITY -> Icons.Rounded.Fingerprint
    }
}

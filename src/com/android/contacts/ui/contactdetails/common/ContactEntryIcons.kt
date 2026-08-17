package com.android.contacts.ui.contactdetails.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.SpeakerNotes
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DialerSip
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon

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
    }
}

package com.android.contacts.ui.interactions.callsubject.screen.model

import android.os.Parcelable
import android.telecom.PhoneAccountHandle
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CallSubjectArguments(
    val thumbnailPhotoUri: String? = null,
    val isBusiness: Boolean = false,
    val nameOrNumber: String,
    val number: String,
    val displayNumber: String? = null,
    val numberLabel: String? = null,
    val phoneAccountHandle: PhoneAccountHandle? = null,
) : Parcelable

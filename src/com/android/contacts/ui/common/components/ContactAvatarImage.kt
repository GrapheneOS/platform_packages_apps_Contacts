package com.android.contacts.ui.common.components

import java.nio.ByteBuffer

internal sealed interface ContactAvatarImage {

    data class Uri(
        val value: String,
    ) : ContactAvatarImage

    data class Bytes(
        val value: ByteBuffer,
    ) : ContactAvatarImage
}

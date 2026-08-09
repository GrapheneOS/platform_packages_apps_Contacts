package com.android.contacts.data.contactdetails.model

import java.nio.ByteBuffer

internal sealed interface ContactPhoto {

    data class Uri(
        val value: String,
    ) : ContactPhoto

    data class Bytes(
        val value: ByteBuffer,
    ) : ContactPhoto
}

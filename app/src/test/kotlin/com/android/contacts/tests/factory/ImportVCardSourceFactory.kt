package com.android.contacts.tests.factory

import android.net.Uri
import com.android.contacts.domain.vcard.model.ImportVCardSource

object ImportVCardSourceFactory {
    fun build(
        uri: Uri = Uri.parse("file://storage/test.vcf"),
        name: String = uri.toString(),
    ) = ImportVCardSource(
        uri = uri,
        name = name,
    )
}

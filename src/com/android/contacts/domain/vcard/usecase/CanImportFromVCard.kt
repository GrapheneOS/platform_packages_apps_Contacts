package com.android.contacts.domain.vcard.usecase

import android.content.Context
import com.android.contacts.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface CanImportFromVCard {
    operator fun invoke(): Boolean
}

internal class CanImportFromVCardImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CanImportFromVCard {
    override fun invoke(): Boolean {
        return context.resources.getBoolean(R.bool.config_allow_import_from_vcf_file)
    }
}

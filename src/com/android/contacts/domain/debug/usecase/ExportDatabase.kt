package com.android.contacts.domain.debug.usecase

import android.content.Context
import android.content.Intent
import com.android.contacts.util.ImplicitIntentsUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface ExportDatabase {
    operator fun invoke()
}

internal class ExportDatabaseImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ExportDatabase {
    override fun invoke() {
        val intent = Intent("com.android.providers.contacts.DUMP_DATABASE")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_NEW_TASK)
        ImplicitIntentsUtil.startActivityOutsideApp(context, intent)
    }
}

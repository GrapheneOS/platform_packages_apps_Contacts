package com.android.contacts.domain.util

import android.content.Context
import com.android.contacts.CallUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface IsCallWithNoteSupported {
    operator fun invoke(): Boolean
}

internal class IsCallWithNoteSupportedImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : IsCallWithNoteSupported {

    override operator fun invoke(): Boolean {
        return CallUtil.isCallWithSubjectSupported(context)
    }
}

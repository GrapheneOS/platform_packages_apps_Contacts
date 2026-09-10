package com.android.contacts.util.core

import android.content.ContentResolver
import android.net.Uri
import com.android.contacts.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface GetUriType {
    suspend operator fun invoke(uri: Uri): String?
}

internal class GetUriTypeImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : GetUriType {
    override suspend operator fun invoke(uri: Uri): String? {
        return withContext(coroutineDispatcher) {
            contentResolver.getType(uri)
        }
    }
}

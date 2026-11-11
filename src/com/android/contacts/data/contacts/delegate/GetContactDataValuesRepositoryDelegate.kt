package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Data
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface GetContactDataValuesRepositoryDelegate {
    suspend fun getContactDataValues(
        kind: ContactDataKind,
        dataIds: Set<Long>,
    ): List<String>
}

internal class GetContactDataValuesRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetContactDataValuesRepositoryDelegate {

    override suspend fun getContactDataValues(
        kind: ContactDataKind,
        dataIds: Set<Long>,
    ): List<String> {
        if (dataIds.isEmpty()) return emptyList()

        val selection = "${Data.MIMETYPE} = ? AND ${Data._ID} IN (${dataIds.joinToString(",")})"
        val selectionArgs = arrayOf(kind.mimeType())

        return withContext(ioDispatcher) {
            contentResolver.query(
                Data.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                null,
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        cursor.getString(VALUE)
                            ?.takeIf { it.isNotBlank() }
                            ?.let(::add)
                    }
                }
            }.orEmpty()
        }
    }

    private fun ContactDataKind.mimeType(): String {
        return when (this) {
            is ContactDataKind.Phone -> Phone.CONTENT_ITEM_TYPE
            ContactDataKind.Email -> Email.CONTENT_ITEM_TYPE
            ContactDataKind.PostalAddress -> StructuredPostal.CONTENT_ITEM_TYPE
        }
    }

    companion object {
        // Phone.NUMBER, Email.ADDRESS and StructuredPostal.FORMATTED_ADDRESS are all DATA1
        @VisibleForTesting
        val PROJECTION = arrayOf(Data.DATA1)
        private const val VALUE = 0
    }
}

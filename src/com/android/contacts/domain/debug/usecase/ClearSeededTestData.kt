package com.android.contacts.domain.debug.usecase

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.debug.model.TestContact
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface ClearSeededTestData {
    suspend operator fun invoke()
}

internal class ClearSeededTestDataImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : ClearSeededTestData {
    override suspend fun invoke() {
        withContext(coroutineDispatcher) {
            val rawContactIds = mutableListOf<Int>()

            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data.RAW_CONTACT_ID),
                "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
                arrayOf("${TestContact.PHONE_PREFIX}%"),
                null,
            )?.use {
                while (it.moveToNext()) {
                    val index = it.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)
                    if (index != -1) {
                        rawContactIds.add(it.getInt(index))
                    }
                }
            }

            val jointIds = rawContactIds.joinToString(",")
            val result = contentResolver.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                "${ContactsContract.RawContacts._ID} IN ($jointIds)",
                null,
            )

            Log.i(TAG, "Cleared $result seeded test contacts")
        }
    }

    private companion object {
        const val TAG = "ClearSeededTestData"
    }
}

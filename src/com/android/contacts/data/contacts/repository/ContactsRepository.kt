package com.android.contacts.data.contacts.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.OperationCanceledException
import android.provider.ContactsContract
import android.util.Log
import com.android.contacts.data.contacts.model.ContactLookupQuery
import com.android.contacts.data.contacts.model.ContactLookupResult
import com.android.contacts.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface ContactsRepository {
    fun lookup(query: ContactLookupQuery): Flow<List<ContactLookupResult>>
}

internal class ContactsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContactsRepository {
    override fun lookup(query: ContactLookupQuery): Flow<List<ContactLookupResult>> {
        return observeUri(getQueryUri(query))
            .map { queryContacts(query) }
            .flowOn(ioDispatcher)
    }

    private fun observeUri(uri: Uri): Flow<Unit> {
        return callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            contentResolver.registerContentObserver(uri, true, observer)
            trySend(Unit)

            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    private fun getQueryUri(query: ContactLookupQuery): Uri {
        return when (query) {
            is ContactLookupQuery.Email ->
                Uri.withAppendedPath(
                    ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
                    Uri.encode(query.value),
                )
            is ContactLookupQuery.Phone -> {
                Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(query.value),
                )
            }
        }
    }

    private fun getQueryProjection(query: ContactLookupQuery): Array<String> {
        return when (query) {
            is ContactLookupQuery.Email -> LOOKUP_WITH_EMAIL_PROJECTION
            is ContactLookupQuery.Phone -> LOOKUP_WITH_PHONE_PROJECTION
        }
    }

    private fun queryContacts(query: ContactLookupQuery): List<ContactLookupResult> {
        return try {
            contentResolver.query(
                getQueryUri(query),
                getQueryProjection(query),
                null,
                null,
                null,
            )
                ?.use(::toLookupResults)
                ?: emptyList()
        } catch (e: SecurityException) {
            failedLookup(e)
        } catch (e: SQLiteException) {
            failedLookup(e)
        } catch (e: OperationCanceledException) {
            failedLookup(e)
        }
    }

    private fun toLookupResults(cursor: Cursor): List<ContactLookupResult> {
        val results = mutableListOf<ContactLookupResult>()
        while (cursor.moveToNext()) {
            buildContactLookupResult(cursor)
                ?.let(results::add)
        }
        return results
    }

    private fun buildContactLookupResult(cursor: Cursor): ContactLookupResult? {
        val id = cursor.getLong(LOOKUP_ID_INDEX)
        val key = cursor.getString(LOOKUP_KEY_INDEX) ?: return null
        return ContactsContract.Contacts.getLookupUri(id, key)
            ?.let { uri ->
                ContactLookupResult(
                    id = id,
                    key = key,
                    uri = uri,
                )
            }
    }

    private fun failedLookup(cause: Exception): List<ContactLookupResult> {
        Log.w(TAG, "Could not load the user profile", cause)
        return emptyList()
    }

    private companion object {
        const val TAG = "ContactsRepository"

        val LOOKUP_WITH_EMAIL_PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Email.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.LOOKUP_KEY,
        )

        val LOOKUP_WITH_PHONE_PROJECTION = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.LOOKUP_KEY,
        )

        const val LOOKUP_ID_INDEX = 0
        const val LOOKUP_KEY_INDEX = 1
    }
}

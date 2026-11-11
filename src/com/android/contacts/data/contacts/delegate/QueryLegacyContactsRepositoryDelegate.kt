@file:Suppress("DEPRECATION")

package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.provider.Contacts
import android.provider.Contacts.ContactMethods
import android.provider.Contacts.People
import android.provider.Contacts.Phones
import android.provider.ContactsContract
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.delegate.util.getIntOrNull
import com.android.contacts.data.contacts.model.LegacyContactColumn
import com.android.contacts.data.contacts.model.LegacyContactsKind
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface QueryLegacyContactsRepositoryDelegate {
    fun queryLegacyContacts(
        kind: LegacyContactsKind,
        searchQuery: String?,
    ): Flow<List<LegacyContactColumn>>
}

internal class QueryLegacyContactsRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : QueryLegacyContactsRepositoryDelegate {

    override fun queryLegacyContacts(
        kind: LegacyContactsKind,
        searchQuery: String?,
    ): Flow<List<LegacyContactColumn>> {
        return observeContentUri(contentResolver, ContactsContract.AUTHORITY_URI)
            .map {
                query(kind)
                    .filter { contact -> contact.matches(searchQuery) }
            }
            .flowOn(ioDispatcher)
    }

    private fun query(kind: LegacyContactsKind): List<LegacyContactColumn> {
        return when (kind) {
            LegacyContactsKind.PEOPLE -> {
                contentResolver.query(
                    People.CONTENT_URI,
                    PEOPLE_PROJECTION,
                    null,
                    null,
                    People.DISPLAY_NAME,
                )?.use { it.toPeopleColumns() }
            }

            LegacyContactsKind.PHONES -> {
                contentResolver.query(
                    Phones.CONTENT_URI,
                    PHONES_PROJECTION,
                    null,
                    null,
                    People.DISPLAY_NAME,
                )?.use { it.toDataColumns(kind) }
            }

            LegacyContactsKind.POSTAL_ADDRESSES -> {
                contentResolver.query(
                    ContactMethods.CONTENT_URI,
                    POSTAL_PROJECTION,
                    POSTAL_SELECTION,
                    null,
                    People.DISPLAY_NAME,
                )?.use { it.toDataColumns(kind) }
            }
        }.orEmpty()
    }

    private fun LegacyContactColumn.matches(searchQuery: String?): Boolean {
        return when {
            searchQuery == null -> true
            else -> displayName.orEmpty().contains(searchQuery, ignoreCase = true)
        }
    }

    private fun Cursor.toPeopleColumns(): List<LegacyContactColumn> {
        return buildList {
            while (moveToNext()) {
                add(
                    LegacyContactColumn(
                        id = getLong(PEOPLE_ID),
                        kind = LegacyContactsKind.PEOPLE,
                        displayName = getString(PEOPLE_DISPLAY_NAME),
                        isStarred = getInt(PEOPLE_STARRED) != 0,
                    ),
                )
            }
        }
    }

    private fun Cursor.toDataColumns(kind: LegacyContactsKind): List<LegacyContactColumn> {
        return buildList {
            while (moveToNext()) {
                add(
                    LegacyContactColumn(
                        id = getLong(DATA_ID),
                        kind = kind,
                        displayName = getString(DATA_DISPLAY_NAME),
                        value = getString(DATA_VALUE),
                        type = getIntOrNull(DATA_TYPE),
                        label = getString(DATA_LABEL),
                    ),
                )
            }
        }
    }

    companion object {
        @VisibleForTesting
        val PEOPLE_PROJECTION = arrayOf(
            People._ID,
            People.DISPLAY_NAME,
            People.STARRED,
        )
        private const val PEOPLE_ID = 0
        private const val PEOPLE_DISPLAY_NAME = 1
        private const val PEOPLE_STARRED = 2

        @VisibleForTesting
        val PHONES_PROJECTION = arrayOf(
            Phones._ID,
            Phones.TYPE,
            Phones.LABEL,
            Phones.NUMBER,
            People.DISPLAY_NAME,
        )

        @VisibleForTesting
        val POSTAL_PROJECTION = arrayOf(
            ContactMethods._ID,
            ContactMethods.TYPE,
            ContactMethods.LABEL,
            ContactMethods.DATA,
            People.DISPLAY_NAME,
        )
        private const val DATA_ID = 0
        private const val DATA_TYPE = 1
        private const val DATA_LABEL = 2
        private const val DATA_VALUE = 3
        private const val DATA_DISPLAY_NAME = 4

        @VisibleForTesting
        const val POSTAL_SELECTION = ContactMethods.KIND + "=" + Contacts.KIND_POSTAL
    }
}

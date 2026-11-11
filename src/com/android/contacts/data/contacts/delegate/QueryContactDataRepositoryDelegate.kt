package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Callable
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Data
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.delegate.util.addressBookSectionLabels
import com.android.contacts.data.contacts.delegate.util.appendAddressBookIndex
import com.android.contacts.data.contacts.delegate.util.appendDefaultDirectory
import com.android.contacts.data.contacts.delegate.util.getContactPhoto
import com.android.contacts.data.contacts.delegate.util.getIntOrNull
import com.android.contacts.data.contacts.delegate.util.toSortKeyColumn
import com.android.contacts.data.contacts.model.ContactDataColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface QueryContactDataRepositoryDelegate {
    fun queryContactData(
        kind: ContactDataKind,
        searchQuery: String?,
        sortOrder: SortOrder,
        restrictToContactIds: Set<Long>? = null,
    ): Flow<List<ContactDataColumn>>
}

internal class QueryContactDataRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : QueryContactDataRepositoryDelegate {

    override fun queryContactData(
        kind: ContactDataKind,
        searchQuery: String?,
        sortOrder: SortOrder,
        restrictToContactIds: Set<Long>?,
    ): Flow<List<ContactDataColumn>> {
        return observeContentUri(contentResolver, Data.CONTENT_URI)
            .map {
                val uri = buildUri(kind, searchQuery)
                val selection = buildSelection(kind, restrictToContactIds)
                contentResolver.query(
                    uri,
                    PROJECTION,
                    selection,
                    null,
                    sortOrder.toSortKeyColumn(),
                )?.use { cursor -> cursor.toContactDataColumns(kind) }
                    .orEmpty()
            }
            .flowOn(ioDispatcher)
    }

    private fun buildUri(kind: ContactDataKind, searchQuery: String?): Uri {
        val filterUri = when (kind) {
            is ContactDataKind.Phone -> when {
                kind.callableOnly -> Callable.ENTERPRISE_CONTENT_FILTER_URI
                else -> Phone.ENTERPRISE_CONTENT_FILTER_URI
            }
            ContactDataKind.Email -> Email.CONTENT_FILTER_URI
            ContactDataKind.PostalAddress -> null
        }
        val builder = when {
            searchQuery != null && filterUri != null ->
                filterUri.buildUpon().appendPath(searchQuery)
            else ->
                contentUri(kind).buildUpon().appendAddressBookIndex()
        }
        return builder
            .appendDefaultDirectory()
            .appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
            .build()
    }

    private fun contentUri(kind: ContactDataKind): Uri {
        return when (kind) {
            is ContactDataKind.Phone -> when {
                kind.callableOnly -> Callable.CONTENT_URI
                else -> Phone.CONTENT_URI
            }
            ContactDataKind.Email -> Email.CONTENT_URI
            ContactDataKind.PostalAddress -> StructuredPostal.CONTENT_URI
        }
    }

    private fun buildSelection(
        kind: ContactDataKind,
        restrictToContactIds: Set<Long>?,
    ): String? {
        val clauses = buildList {
            if (kind is ContactDataKind.Phone) {
                // Ignore invalid phone numbers that are too long. These can potentially cause
                // freezes in the UI and there is no reason to display them.
                add(IGNORE_NUMBER_TOO_LONG_CLAUSE)
            }
            if (restrictToContactIds != null) {
                add("${Data.CONTACT_ID} IN (${restrictToContactIds.joinToString(",")})")
            }
        }
        return clauses
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" AND ")
    }

    private fun Cursor.toContactDataColumns(kind: ContactDataKind): List<ContactDataColumn> {
        val sectionLabels = addressBookSectionLabels()
        return buildList {
            while (moveToNext()) {
                add(
                    ContactDataColumn(
                        id = getLong(ID),
                        kind = kind,
                        displayNamePrimary = getString(DISPLAY_NAME_PRIMARY),
                        displayNameAlternative = getString(DISPLAY_NAME_ALTERNATIVE),
                        photo = getContactPhoto(PHOTO_THUMBNAIL_URI),
                        value = getString(VALUE),
                        type = getIntOrNull(TYPE),
                        label = getString(LABEL),
                        sectionLabel = sectionLabels.getOrNull(position),
                        isStarred = getInt(STARRED) != 0,
                    ),
                )
            }
        }
    }

    companion object {
        @VisibleForTesting
        val PROJECTION = arrayOf(
            Data._ID,
            Data.DISPLAY_NAME_PRIMARY,
            Data.DISPLAY_NAME_ALTERNATIVE,
            Data.PHOTO_THUMBNAIL_URI,
            Data.STARRED,
            Data.DATA1,
            Data.DATA2,
            Data.DATA3,
        )
        private const val ID = 0
        private const val DISPLAY_NAME_PRIMARY = 1
        private const val DISPLAY_NAME_ALTERNATIVE = 2
        private const val PHOTO_THUMBNAIL_URI = 3
        private const val STARRED = 4

        // Phone.NUMBER, Email.ADDRESS and StructuredPostal.FORMATTED_ADDRESS
        private const val VALUE = 5
        private const val TYPE = 6
        private const val LABEL = 7

        @VisibleForTesting
        const val IGNORE_NUMBER_TOO_LONG_CLAUSE = "length(" + Phone.NUMBER + ") < 1000"
    }
}

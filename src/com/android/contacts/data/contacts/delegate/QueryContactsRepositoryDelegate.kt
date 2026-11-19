package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.delegate.util.addressBookSectionLabels
import com.android.contacts.data.contacts.delegate.util.appendAddressBookIndex
import com.android.contacts.data.contacts.delegate.util.appendDefaultDirectory
import com.android.contacts.data.contacts.delegate.util.getContactPhoto
import com.android.contacts.data.contacts.delegate.util.toSortKeyColumn
import com.android.contacts.data.contacts.model.AccountQuerySelection
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface QueryContactsRepositoryDelegate {
    fun queryContacts(
        searchQuery: String?,
        sortOrder: SortOrder,
        account: AccountModel? = null,
        excludedContactId: Long? = null,
    ): Flow<List<ContactColumn>>
}

internal class QueryContactsRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : QueryContactsRepositoryDelegate {

    override fun queryContacts(
        searchQuery: String?,
        sortOrder: SortOrder,
        account: AccountModel?,
        excludedContactId: Long?,
    ): Flow<List<ContactColumn>> {
        return observeContentUri(contentResolver, Contacts.CONTENT_URI)
            .map {
                val accountSelection = AccountQuerySelection.forAccount(account)
                query(
                    uri = buildUri(searchQuery),
                    selection = selection(accountSelection, excludedContactId),
                    selectionArgs = selectionArgs(accountSelection, excludedContactId),
                    sortOrder = sortOrder,
                )
            }
            .flowOn(ioDispatcher)
    }

    private fun buildUri(searchQuery: String?): Uri {
        val builder = when (searchQuery) {
            null -> Contacts.CONTENT_URI.buildUpon().appendAddressBookIndex()
            else -> Contacts.ENTERPRISE_CONTENT_FILTER_URI.buildUpon().appendPath(searchQuery)
        }
        return builder
            .appendDefaultDirectory()
            .build()
    }

    private fun selection(
        accountQuerySelection: AccountQuerySelection,
        excludedContactId: Long?,
    ): String? {
        return listOfNotNull(
            accountSelection(accountQuerySelection),
            EXCLUDE_CONTACT_SELECTION.takeIf { excludedContactId != null },
        )
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" AND ") { "($it)" }
    }

    private fun accountSelection(account: AccountQuerySelection): String? {
        return account.rawContactSelection?.let { "$RAW_CONTACTS_SELECTION_PREFIX$it)" }
    }

    private fun selectionArgs(
        account: AccountQuerySelection,
        excludedContactId: Long?,
    ): Array<String>? {
        val accountArgs = account.rawContactSelectionArgs
        return (accountArgs + excludedContactId?.toString())
            .filterNotNull()
            .takeIf { it.isNotEmpty() }
            ?.toTypedArray()
    }

    private fun query(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: SortOrder,
    ): List<ContactColumn> {
        return contentResolver.query(
            uri,
            PROJECTION,
            selection,
            selectionArgs,
            sortOrder.toSortKeyColumn(),
        )?.use { it.toContactColumns() }
            .orEmpty()
    }

    companion object {
        internal val PROJECTION = arrayOf(
            Contacts._ID,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.DISPLAY_NAME_ALTERNATIVE,
            Contacts.PHOTO_THUMBNAIL_URI,
            Contacts.STARRED,
        )
        private const val ID = 0
        private const val DISPLAY_NAME_PRIMARY = 1
        private const val DISPLAY_NAME_ALTERNATIVE = 2
        private const val PHOTO_THUMBNAIL_URI = 3
        private const val STARRED = 4

        @VisibleForTesting
        const val EXCLUDE_CONTACT_SELECTION = Contacts._ID + "!=?"

        private const val RAW_CONTACTS_SELECTION_PREFIX =
            Contacts._ID + " IN (" +
                "SELECT ${ContactsContract.RawContacts.CONTACT_ID} FROM view_raw_contacts WHERE "

        internal fun Cursor.toContactColumns(): List<ContactColumn> {
            val sectionLabels = addressBookSectionLabels()
            return buildList {
                while (moveToNext()) {
                    add(
                        ContactColumn(
                            id = getLong(ID),
                            displayNamePrimary = getString(DISPLAY_NAME_PRIMARY),
                            displayNameAlternative = getString(DISPLAY_NAME_ALTERNATIVE),
                            photo = getContactPhoto(PHOTO_THUMBNAIL_URI),
                            sectionLabel = sectionLabels.getOrNull(position),
                            isStarred = getInt(STARRED) != 0,
                        ),
                    )
                }
            }
        }
    }
}

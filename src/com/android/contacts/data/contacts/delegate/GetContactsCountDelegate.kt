package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.model.ContactsCount
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface GetContactsCountDelegate {
    fun getContactsCount(): Flow<ContactsCount<AccountModel>?>
}

internal class GetContactsCountDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : GetContactsCountDelegate {
    override fun getContactsCount(): Flow<ContactsCount<AccountModel>?> {
        return observeContentUri(contentResolver, ContactsContract.RawContacts.CONTENT_URI)
            .map { get() }
            .flowOn(coroutineDispatcher)
    }

    private fun get(): ContactsCount<AccountModel>? {
        return contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            val allContacts = buildList {
                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(CONTACT_ID_INDEX)
                    val account = AccountModel(
                        name = cursor.getString(ACCOUNT_NAME_INDEX),
                        type = cursor.getString(ACCOUNT_TYPE_INDEX),
                        dataSet = cursor.getString(DATA_SET_INDEX),
                    )
                    add(account to contactId)
                }
            }

            return ContactsCount(
                all = allContacts.distinctBy { it.second }.size,
                byAccount = allContacts
                    .groupBy { (account, _) -> account }
                    .mapValues { (_, contacts) -> contacts.distinct().size }
            )
        }
    }

    companion object {
        @VisibleForTesting
        val PROJECTION = arrayOf(
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.DATA_SET,
        )
        const val CONTACT_ID_INDEX = 0
        const val ACCOUNT_NAME_INDEX = 1
        const val ACCOUNT_TYPE_INDEX = 2
        const val DATA_SET_INDEX = 3
    }
}

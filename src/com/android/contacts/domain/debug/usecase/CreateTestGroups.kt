package com.android.contacts.domain.debug.usecase

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.ContactsContract
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.debug.model.DebugDataConstants
import javax.inject.Inject

internal fun interface CreateTestGroups {
    operator fun invoke(account: AccountModel, count: Int): List<Long>
}

internal class CreateTestGroupsImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : CreateTestGroups {
    override operator fun invoke(account: AccountModel, count: Int): List<Long> {
        if (count <= 0) {
            return emptyList()
        }

        return (1..count).mapNotNull { number ->
            val title = "${DebugDataConstants.GROUP_PREFIX} Group $number"
            createGroup(account, title)
                ?.let(ContentUris::parseId)
        }
    }

    private fun createGroup(account: AccountModel, title: String): Uri? {
        val values = ContentValues()
        values.put(ContactsContract.Groups.TITLE, title)
        values.put(ContactsContract.Groups.ACCOUNT_NAME, account.name)
        values.put(ContactsContract.Groups.ACCOUNT_TYPE, account.type)
        values.put(ContactsContract.Groups.DATA_SET, account.dataSet)
        return contentResolver.insert(ContactsContract.Groups.CONTENT_URI, values)
    }

    companion object {
        private const val TAG = "CreateTestGroup"
    }
}

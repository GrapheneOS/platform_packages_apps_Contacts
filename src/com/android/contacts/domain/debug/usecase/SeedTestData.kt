package com.android.contacts.domain.debug.usecase

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.OperationApplicationException
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.di.debug.SeedTestContactsCount
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.debug.model.TestContact
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal fun interface SeedTestData {
    suspend operator fun invoke()
}

internal class SeedTestDataImpl @Inject constructor(
    private val loadAccounts: LoadAccounts,
    private val getDefaultAccount: GetDefaultAccount,
    private val clearSeededTestData: ClearSeededTestData,
    private val generateTestContact: GenerateTestContact,
    private val contentResolver: ContentResolver,
    @param:SeedTestContactsCount val testContactsCount: Int,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : SeedTestData {
    override suspend fun invoke() {
        withContext(coroutineDispatcher) {
            val account = getDeviceAccount() ?: return@withContext
            clearSeededTestData()
            val contacts = (1..testContactsCount).map { generateTestContact() }
            saveContacts(account, contacts)
        }
    }

    /*
     * Choses the account based on the following order:
     *  - Device account if available
     *  - Default account if any is configured
     *  - First account from the accounts list
     */
    private suspend fun getDeviceAccount(): AccountModel? {
        val accounts = loadAccounts(AccountFilter.CONTACTS_INSERTABLE).first()
        return accounts.firstOrNull { it.isDeviceAccount }?.account
            ?: getDefaultAccount()
            ?: accounts.firstOrNull()?.account
            ?: run {
                Log.w(TAG, "No account available to save test data")
                null
            }
    }

    private fun saveContacts(account: AccountModel, contacts: List<TestContact>) {
        contacts.forEach { contact ->
            val operations = ArrayList<ContentProviderOperation>()
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                    .withValue(ContactsContract.RawContacts.DATA_SET, account.dataSet)
                    .build(),
            )

            operations.addAll(
                contact.toContentValuesList().map { contentValues ->
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValues(contentValues)
                        .build()
                },
            )

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Failed to save test data", e)
            } catch (e: OperationApplicationException) {
                Log.w(TAG, "Failed to save test data", e)
            } catch (e: RemoteException) {
                Log.w(TAG, "Failed to save test data", e)
            }
        }
    }

    private fun TestContact.toContentValuesList(): List<ContentValues> {
        return listOfNotNull(
            phonesContentValues(),
            nameContentValues(),
            nicknameContentValues(),
            emailsContentValues(),
            postalContentValues(),
            organizationContentValues(),
            relationContentValues(),
            websiteContentValues(),
            photoContentValues(),
        ).flatten()
    }

    private fun TestContact.phonesContentValues(): List<ContentValues> {
        return phones.map {
            ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                )
                put(ContactsContract.CommonDataKinds.Phone.NUMBER, it.value)
                put(ContactsContract.CommonDataKinds.Phone.TYPE, it.type)
            }
        }
    }

    private fun TestContact.nameContentValues(): List<ContentValues> {
        return listOf(
            ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                )
                put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
                put(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, middleName)
                put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
            },
        )
    }

    private fun TestContact.nicknameContentValues(): List<ContentValues>? {
        return nickname?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it.value)
                    put(ContactsContract.CommonDataKinds.Nickname.TYPE, it.type)
                },
            )
        }
    }

    private fun TestContact.emailsContentValues(): List<ContentValues> {
        return emails.map {
            ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                )
                put(ContactsContract.CommonDataKinds.Email.ADDRESS, it.value)
                put(ContactsContract.CommonDataKinds.Email.TYPE, it.type)
            }
        }
    }

    private fun TestContact.postalContentValues(): List<ContentValues>? {
        if (city == null && country == null) return null
        return listOf(
            ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                )
                put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, city)
                put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, country)
            },
        )
    }

    private fun TestContact.organizationContentValues(): List<ContentValues>? {
        return organization?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Organization.COMPANY, it)
                },
            )
        }
    }

    private fun TestContact.relationContentValues(): List<ContentValues>? {
        return relation?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Relation.NAME, it.value)
                    put(ContactsContract.CommonDataKinds.Relation.TYPE, it.type)
                },
            )
        }
    }

    private fun TestContact.websiteContentValues(): List<ContentValues>? {
        return website?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Website.URL, it.value)
                    put(ContactsContract.CommonDataKinds.Website.TYPE, it.type)
                },
            )
        }
    }

    private fun TestContact.photoContentValues(): List<ContentValues>? {
        return photo?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo.bytes)
                    put(ContactsContract.CommonDataKinds.Photo.IS_PRIMARY, 1)
                },
            )
        }
    }

    companion object {
        private const val TAG = "SeedTestData"
    }
}

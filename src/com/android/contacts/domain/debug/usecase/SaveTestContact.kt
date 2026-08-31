package com.android.contacts.domain.debug.usecase

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.OperationApplicationException
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.debug.model.TestContact
import javax.inject.Inject

internal fun interface SaveTestContact {
    operator fun invoke(
        account: AccountModel,
        contact: TestContact,
        groupIds: List<Long>,
    )
}

internal class SaveTestContactImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : SaveTestContact {
    override operator fun invoke(
        account: AccountModel,
        contact: TestContact,
        groupIds: List<Long>,
    ) {
        val contactId = saveContact(account, contact) ?: return
        addGroups(contactId, groupIds)
    }

    private fun saveContact(
        account: AccountModel,
        contact: TestContact,
    ): Long? {
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

        val results = applyOperations(operations)
        return results?.firstOrNull()?.uri?.let(ContentUris::parseId)
    }

    private fun addGroups(contactId: Long, groupIds: List<Long>) {
        val operations = groupIds.map { groupId ->
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                )
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                .build()
        }
        applyOperations(ArrayList(operations))
    }

    private fun applyOperations(
        operations: ArrayList<ContentProviderOperation>,
    ): Array<ContentProviderResult?>? {
        return try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Failed to save test data", e)
            null
        } catch (e: OperationApplicationException) {
            Log.w(TAG, "Failed to save test data", e)
            null
        } catch (e: RemoteException) {
            Log.w(TAG, "Failed to save test data", e)
            null
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
            eventContentValues(),
            imContentValues(),
            sipAddressContentValues(),
            identityContentValues(),
            noteContentValues(),
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
                put(ContactsContract.CommonDataKinds.Phone.LABEL, it.label)
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
                put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name.given)
                put(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, name.middle)
                put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, name.family)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.display)
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
                    put(ContactsContract.CommonDataKinds.Nickname.LABEL, it.label)
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
                put(ContactsContract.CommonDataKinds.Email.LABEL, it.label)
            }
        }
    }

    private fun TestContact.postalContentValues(): List<ContentValues>? {
        return postal?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, it.value.city)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, it.value.country)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, it.type)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, it.label)
                },
            )
        }
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
                    put(ContactsContract.CommonDataKinds.Relation.LABEL, it.label)
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
                    put(ContactsContract.CommonDataKinds.Website.LABEL, it.label)
                },
            )
        }
    }

    private fun TestContact.eventContentValues(): List<ContentValues>? {
        return event?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Event.START_DATE, it.value)
                    put(ContactsContract.CommonDataKinds.Event.TYPE, it.type)
                    put(ContactsContract.CommonDataKinds.Event.LABEL, it.label)
                },
            )
        }
    }

    private fun TestContact.imContentValues(): List<ContentValues>? {
        return im?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Im.DATA, it.value.data)
                    put(
                        ContactsContract.CommonDataKinds.Im.PROTOCOL,
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM,
                    )
                    put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, it.value.protocol)
                    put(ContactsContract.CommonDataKinds.Im.TYPE, it.type)
                    put(ContactsContract.CommonDataKinds.Im.LABEL, it.label)
                },
            )
        }
    }

    private fun TestContact.sipAddressContentValues(): List<ContentValues>? {
        return sipAddress?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS, it.value)
                    put(ContactsContract.CommonDataKinds.SipAddress.TYPE, it.type)
                    put(ContactsContract.CommonDataKinds.SipAddress.LABEL, it.label)
                },
            )
        }
    }

    private fun TestContact.identityContentValues(): List<ContentValues>? {
        if (identityValue == null || identityNamespace == null) return null
        return listOf(
            ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE,
                )
                put(ContactsContract.CommonDataKinds.Identity.IDENTITY, identityValue)
                put(ContactsContract.CommonDataKinds.Identity.NAMESPACE, identityNamespace)
            },
        )
    }

    private fun TestContact.noteContentValues(): List<ContentValues>? {
        return note?.let {
            listOf(
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                    )
                    put(ContactsContract.CommonDataKinds.Note.NOTE, it)
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
        private const val TAG = "SaveTestContact"
    }
}

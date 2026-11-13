package com.android.contacts.domain.photo.usecase

import android.net.Uri
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.accounts.usecase.ShouldPromptForDefaultAccount
import com.android.contacts.domain.contacts.usecase.LoadRawContacts
import com.android.contacts.domain.photo.model.AttachPhotoTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal fun interface ResolveAttachPhotoTarget {
    suspend operator fun invoke(contactUri: Uri): AttachPhotoTarget
}

internal class ResolveAttachPhotoTargetImpl @Inject constructor(
    private val loadRawContacts: LoadRawContacts,
    private val loadAccounts: LoadAccounts,
    private val shouldPromptForDefaultAccount: ShouldPromptForDefaultAccount,
    private val getDefaultAccount: GetDefaultAccount,
) : ResolveAttachPhotoTarget {

    override suspend operator fun invoke(contactUri: Uri): AttachPhotoTarget {
        val result = loadRawContacts(contactUri, onlyWritable = true).first()
        val writableRawContact = result?.rawContacts?.firstOrNull()
        return when {
            result == null -> {
                AttachPhotoTarget.NotFound
            }
            writableRawContact != null -> {
                AttachPhotoTarget.Ready.ExistingRawContact(rawContactId = writableRawContact.id)
            }
            else -> {
                resolveNewRawContactTarget()
            }
        }
    }

    private suspend fun resolveNewRawContactTarget(): AttachPhotoTarget {
        val accounts = loadAccounts(AccountFilter.CONTACTS_INSERTABLE)
            .first()
            .map { it.account }

        return when {
            shouldPromptForDefaultAccount(accounts) -> {
                AttachPhotoTarget.NeedsAccountSelection
            }
            else -> {
                AttachPhotoTarget.Ready.NewRawContact(
                    account = accounts.singleOrNull() ?: getDefaultAccount() ?: AccountModel.NULL,
                )
            }
        }
    }
}

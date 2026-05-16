package com.android.contacts.editornew.contact

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import com.android.contacts.editor.ContactEditorUtils
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.RawContact
import com.android.contacts.model.RawContactDelta
import com.android.contacts.model.RawContactModifier
import com.android.contacts.model.ValuesDelta
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountType
import com.android.contacts.model.account.AccountWithDataSet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import javax.inject.Inject

internal interface ContactDelegate {
    val state: StateFlow<ContactState>
    suspend fun init()
}

internal class ContactDelegateImpl
@Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val editorUtils: ContactEditorUtils,
    private val accountTypeManager: AccountTypeManager,
) : ContactDelegate {

    private val _state = MutableStateFlow<ContactState>(ContactState.Loading)
    override val state: StateFlow<ContactState> = _state.asStateFlow()

    override suspend fun init() {
        val accountFilter = AccountTypeManager.insertableFilter(context)

        val accounts = accountTypeManager
            .filterAccountsAsync(accountFilter)
            .await()

        val accountsWithDataSet = AccountInfo.extractAccounts(accounts)
        // TODO: Correctly handle account selection; Prompt for account creation if none available
        val defaultAccount = accountsWithDataSet
            .let(editorUtils::getOnlyOrDefaultAccount)
            ?: accountsWithDataSet.first()

        val defaultAccountType = accountTypeManager.getAccountTypeForAccount(defaultAccount)

        _state.value = ContactState.Data(
            accounts = accounts,
            rawContactDelta = createNewRawContactDelta(defaultAccount, defaultAccountType),
        )
    }

    private fun createNewRawContactDelta(
        account: AccountWithDataSet,
        accountType: AccountType,
    ): RawContactDelta {
        val rawContact = RawContact()
            .apply { setAccount(account) }

        return RawContactDelta(ValuesDelta.fromAfter(rawContact.values))
            .apply { ensureDefaultMimeTypes(accountType) }
    }

    private fun RawContactDelta.ensureDefaultMimeTypes(accountType: AccountType) {
        listOf(
            Photo.CONTENT_ITEM_TYPE,
            StructuredName.CONTENT_ITEM_TYPE,
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            Organization.CONTENT_ITEM_TYPE,
            Event.CONTENT_ITEM_TYPE,
        ).forEach { mimeType ->
            RawContactModifier.ensureKindExists(this, accountType, mimeType)
        }
    }
}

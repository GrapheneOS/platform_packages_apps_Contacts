package com.android.contacts.domain.contacts.mapper

import com.android.contacts.data.contacts.model.RawContact
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.contacts.model.RawContactWithAccount
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import javax.inject.Inject

internal interface RawContactWithAccountMapper {
    fun map(rawContact: RawContact): RawContactWithAccount?
}

internal class RawContactWithAccountMapperImpl @Inject constructor(
    private val accountTypeManager: AccountTypeManager,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
) : RawContactWithAccountMapper {
    override fun map(rawContact: RawContact): RawContactWithAccount? {
        val accountInfo = getAccountInfo(rawContact) ?: return null
        val accountDisplayModel = accountDisplayModelMapper.map(accountInfo)
        return RawContactWithAccount(
            id = rawContact.id,
            photoUri = rawContact.photoUri,
            displayName = rawContact.displayName,
            displayNameAlt = rawContact.displayNameAlt,
            account = accountDisplayModel,
        )
    }

    private fun getAccountInfo(rawContact: RawContact): AccountInfo? {
        val account = accountWithDataSet(rawContact)
        return accountTypeManager.getAccountInfoForAccount(account)
    }

    private fun accountWithDataSet(rawContact: RawContact): AccountWithDataSet {
        return AccountWithDataSet(
            rawContact.accountName,
            rawContact.accountType,
            rawContact.accountDataSet,
        )
    }
}

package com.android.contacts.domain.accounts.mapper

import android.content.Context
import android.graphics.drawable.Drawable
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.list.ContactListFilter
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.util.DeviceLocalAccountTypeFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface ContactsAccountFilterMapper {
    fun map(filter: ContactsAccountFilter): ContactListFilter
    fun map(filter: ContactListFilter?): ContactsAccountFilter
}

internal class ContactsAccountFilterMapperImpl @Inject constructor(
    private val accountModelMapper: AccountModelMapper,
    private val accountTypeManager: AccountTypeManager,
    @param:ApplicationContext private val context: Context,
    private val deviceLocalFactory: DeviceLocalAccountTypeFactory,
) : ContactsAccountFilterMapper {
    override fun map(filter: ContactsAccountFilter): ContactListFilter {
        return when (filter) {
            ContactsAccountFilter.All -> {
                ContactListFilter.createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS)
            }
            is ContactsAccountFilter.One -> {
                mapAccountFilter(filter.account)
            }
        }
    }

    private fun mapAccountFilter(accountModel: AccountModel): ContactListFilter {
        val account = accountModelMapper.map(accountModel)
        val accountInfo = accountTypeManager.getAccountInfoForAccount(account)
        val accountType = accountInfo.type

        val icon: Drawable? = accountType?.getDisplayIcon(context)
        val localAccountType = deviceLocalFactory.classifyAccount(accountModel.type)

        return when (localAccountType) {
            DeviceLocalAccountTypeFactory.TYPE_DEVICE ->
                ContactListFilter.createDeviceContactsFilter(icon, account)
            DeviceLocalAccountTypeFactory.TYPE_SIM ->
                ContactListFilter.createSimContactsFilter(icon, account)
            else ->
                ContactListFilter.createAccountFilter(
                    account.type,
                    account.name,
                    account.dataSet,
                    icon,
                )
        }
    }

    override fun map(filter: ContactListFilter?): ContactsAccountFilter {
        return when (filter?.filterType) {
            ContactListFilter.FILTER_TYPE_ACCOUNT,
            ContactListFilter.FILTER_TYPE_DEVICE_CONTACTS,
            ContactListFilter.FILTER_TYPE_SIM_CONTACTS,
            ->
                ContactsAccountFilter.One(
                    AccountModel(
                        filter.accountName,
                        filter.accountType,
                        filter.dataSet,
                    ),
                )
            else -> ContactsAccountFilter.All
        }
    }
}

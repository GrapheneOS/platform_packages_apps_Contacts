package com.android.contacts.ui.interactions.account.filter.screen.mapper

import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterItem
import org.junit.Assert.assertEquals
import org.junit.Test

internal class AccountFilterItemMapperTest {

    private val subject: AccountFilterItemMapper = AccountFilterItemMapperImpl()

    @Test
    fun withoutAccounts_returnsOnlyAll() {
        assertEquals(
            listOf(AccountFilterItem.All(0)),
            subject.map(emptyMap()),
        )
    }

    @Test
    fun withAccounts_returnsAllWithSummedCount() {
        val account1 = AccountDisplayModelFactory.build(name = "Account 1")
        val account2 = AccountDisplayModelFactory.build(name = "Account 2")

        val result = subject.map(
            mapOf(
                account1 to 5,
                account2 to 10,
            ),
        )

        assertEquals(
            AccountFilterItem.All(15),
            result.first(),
        )
    }

    @Test
    fun withAccounts_returnsAccountItems() {
        val account1 = AccountDisplayModelFactory.build(name = "Account 1", type = "Device")
        val account2IconData = AccountIconData(
            iconRes = com.android.contacts.R.drawable.quantum_ic_smartphone_vd_theme_24,
        )
        val account2 = AccountDisplayModelFactory.build(
            name = null,
            type = "Device",
            iconData = account2IconData,
        )

        val result = subject.map(
            mapOf(
                account1 to 5,
                account2 to 10,
            ),
        )

        assertEquals(
            3,
            result.size,
        )
        assertEquals(
            AccountFilterItem.One(
                name = account1.name!!,
                description = account1.type,
                iconData = null,
                contactsCount = 5,
                filter = ContactsAccountFilter.One(account1.account),
            ),
            result[1],
        )
        assertEquals(
            AccountFilterItem.One(
                name = account2.type!!,
                description = null,
                iconData = account2IconData,
                contactsCount = 10,
                filter = ContactsAccountFilter.One(account2.account),
            ),
            result[2],
        )
    }
}

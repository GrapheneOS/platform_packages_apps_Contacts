package com.android.contacts.domain.accounts.usecase

import com.android.contacts.domain.accounts.mapper.AccountModelMapperImpl
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.preference.ContactsPreferences
import com.android.contacts.tests.factory.AccountModelFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ShouldPromptForDefaultAccountTest {

    private val contactsPreferences = mockk<ContactsPreferences>()
    private val accountModelMapper = AccountModelMapperImpl()
    private val subject: ShouldPromptForDefaultAccount = ShouldPromptForDefaultAccountImpl(
        contactsPreferences = contactsPreferences,
        accountModelMapper = accountModelMapper,
    )

    @Test
    fun passesMappedAccountsToPreferences_andReturnsTrue() {
        val account = AccountModelFactory.build(
            name = "user@example.org",
            type = "com.example",
            dataSet = "plus",
        )
        every {
            contactsPreferences.shouldShowAccountChangedNotification(
                listOf(accountModelMapper.map(account)),
            )
        } returns true

        assertTrue(subject(listOf(account)))
    }

    @Test
    fun whenPreferencesSaysNoPrompt_returnsFalse() {
        every { contactsPreferences.shouldShowAccountChangedNotification(any()) } returns false

        assertFalse(subject(emptyList()))
    }
}

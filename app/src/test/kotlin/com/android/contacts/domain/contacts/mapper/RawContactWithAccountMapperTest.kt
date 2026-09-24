package com.android.contacts.domain.contacts.mapper

import com.android.contacts.data.contacts.model.RawContact
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.tests.AccountDisplayModelFactory
import com.android.contacts.tests.RawContactFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class RawContactWithAccountMapperTest {

    private val accountTypeManager = mockk<AccountTypeManager>(relaxed = true)
    private val accountDisplayModelMapper = mockk<AccountDisplayModelMapper>(relaxed = true)
    private val subject = RawContactWithAccountMapperImpl(
        accountTypeManager = accountTypeManager,
        accountDisplayModelMapper = accountDisplayModelMapper,
    )

    @Test
    fun whenAccountInfoIsAvailable_mapsFields() {
        val rawContact = RawContact(
            id = 123L,
            photoUri = "content://photo/1",
            displayName = "Name",
            displayNameAlt = "Name Alt",
            accountName = "Account",
            accountType = "AccountType",
            accountDataSet = null,
        )
        val accountSlot = slot<AccountWithDataSet>()
        val accountInfo = mockk<AccountInfo>(relaxed = true)
        every {
            accountTypeManager.getAccountInfoForAccount(capture(accountSlot))
        } returns accountInfo
        val accountDisplayModel = AccountDisplayModelFactory.build()
        every { accountDisplayModelMapper.map(any()) } returns accountDisplayModel

        val result = subject.map(rawContact)!!

        assertEquals(rawContact.id, result.id)
        assertEquals(rawContact.photoUri, result.photoUri)
        assertEquals(rawContact.displayName, result.displayName)
        assertEquals(rawContact.displayNameAlt, result.displayNameAlt)
        assertEquals(accountDisplayModel, result.account)

        verify { accountTypeManager.getAccountInfoForAccount(any()) }
        assertEquals(rawContact.accountName, accountSlot.captured.name)
        assertEquals(rawContact.accountType, accountSlot.captured.type)
        assertEquals(rawContact.accountDataSet, accountSlot.captured.dataSet)

        verify { accountDisplayModelMapper.map(accountInfo) }
    }

    @Test
    fun whenAccountInfoIsNull_returnsNull() {
        val rawContact = RawContactFactory.build()
        every { accountTypeManager.getAccountInfoForAccount(any()) } returns null

        assertNull(subject.map(rawContact))
    }
}

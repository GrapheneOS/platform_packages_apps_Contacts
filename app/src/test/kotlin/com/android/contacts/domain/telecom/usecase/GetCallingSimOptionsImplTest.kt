package com.android.contacts.domain.telecom.usecase

import com.android.contacts.data.telecom.model.CallingSim
import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.data.telecom.repository.PhoneAccountsRepository
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsOf
import com.android.contacts.tests.factory.phone
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

internal class GetCallingSimOptionsImplTest {

    private val phoneAccountsRepository = mockk<PhoneAccountsRepository>()

    private val getCallingSimOptions = GetCallingSimOptionsImpl(
        phoneAccountsRepository = phoneAccountsRepository,
    )

    @Before
    fun setUp() {
        coEvery { phoneAccountsRepository.getCallCapableSims() } returns
            listOf(callingSim("1"), callingSim("2"))
    }

    @Test
    fun invoke_withoutPhoneNumbers_returnsNothing() = runTest {
        assertNull(getCallingSimOptions(contactDetails()))
        coVerify(exactly = 0) { phoneAccountsRepository.getCallCapableSims() }
    }

    @Test
    fun invoke_withASingleSim_returnsNothing() = runTest {
        coEvery { phoneAccountsRepository.getCallCapableSims() } returns listOf(callingSim("1"))

        val details = contactDetailsOf(phone(number = "4155551212"))

        assertNull(getCallingSimOptions(details))
    }

    @Test
    fun invoke_withTwoSims_offersEveryPhoneNumber() = runTest {
        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551212", typeLabel = "Mobile"),
            phone(id = 2L, number = "4155553434", typeLabel = "Work"),
        )

        val options = getCallingSimOptions(details)

        assertEquals(listOf(1L, 2L), options?.choices?.map { choice -> choice.dataId })
        assertEquals(
            listOf("Mobile", "Work"),
            options?.choices?.map { choice -> choice.numberLabel },
        )
    }

    @Test
    fun invoke_reportsTheSelectedAccount() = runTest {
        val account = PhoneAccountId(componentName = "phone/Sim", id = "2")
        val details = contactDetailsOf(
            phone(number = "4155551212", preferredPhoneAccount = account),
        )

        assertEquals(account, getCallingSimOptions(details)?.choices?.single()?.selectedAccountId)
    }

    private fun callingSim(id: String): CallingSim {
        return CallingSim(
            accountId = PhoneAccountId(componentName = "phone/Sim", id = id),
            label = "SIM $id",
        )
    }
}

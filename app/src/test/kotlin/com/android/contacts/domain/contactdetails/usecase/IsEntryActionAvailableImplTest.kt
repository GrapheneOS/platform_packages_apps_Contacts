package com.android.contacts.domain.contactdetails.usecase

import android.content.Intent
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.data.contactdetails.intent.IsIntentRegistered
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class IsEntryActionAvailableImplTest {

    private val contactEntryIntentFactory = mockk<ContactEntryIntentFactory>()
    private val isIntentRegistered = mockk<IsIntentRegistered>()
    private val intent = Intent(Intent.ACTION_VIEW)

    private val isEntryActionAvailable = IsEntryActionAvailableImpl(
        contactEntryIntentFactory = contactEntryIntentFactory,
        isIntentRegistered = isIntentRegistered,
    )

    @Test
    fun invoke_whenTheIntentResolves_isAvailable() {
        every { contactEntryIntentFactory.create(ACTION) } returns intent
        every { isIntentRegistered(intent) } returns true

        assertTrue(isEntryActionAvailable(ACTION))
    }

    @Test
    fun invoke_whenNothingResolvesTheIntent_isNotAvailable() {
        every { contactEntryIntentFactory.create(ACTION) } returns intent
        every { isIntentRegistered(intent) } returns false

        assertFalse(isEntryActionAvailable(ACTION))
    }

    @Test
    fun invoke_whenTheActionHasNoIntent_isNotAvailable() {
        every { contactEntryIntentFactory.create(ACTION) } returns null

        assertFalse(isEntryActionAvailable(ACTION))
        verify(exactly = 0) { isIntentRegistered(any()) }
    }

    private companion object {
        val ACTION = ContactEntryAction.Call(number = "555 0001")
    }
}

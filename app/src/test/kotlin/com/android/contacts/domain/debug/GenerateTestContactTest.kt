package com.android.contacts.domain.debug

import com.android.contacts.domain.debug.model.TestContact
import com.android.contacts.domain.debug.usecase.GenerateTestContact
import com.android.contacts.domain.debug.usecase.GenerateTestContactImpl
import junit.framework.TestCase.assertTrue
import kotlin.random.Random
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GenerateTestContactTest {

    private val subject: GenerateTestContact = GenerateTestContactImpl(Random)

    @Test
    fun alwaysHaveAtLeastOnePhone() {
        val contacts = (1..10).map { subject() }
        assertTrue(
            "There was a contact without any phone",
            contacts.all { it.phones.isNotEmpty() },
        )
    }

    @Test
    fun allPhonesHaveTheSamePrefix() {
        val contact = subject()
        assertTrue(
            "There was a contact with a phone not matching the prefix",
            contact.phones.all { it.value.startsWith(TestContact.PHONE_PREFIX) },
        )
    }

    @Test
    fun alwaysHaveGivenName() {
        val contacts = (1..10).map { subject() }
        assertTrue(
            "There was a contact with a blank given name",
            contacts.all { it.givenName.isNotBlank() },
        )
    }
}

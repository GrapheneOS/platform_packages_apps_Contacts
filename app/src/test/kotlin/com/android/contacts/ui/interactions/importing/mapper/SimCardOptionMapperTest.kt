package com.android.contacts.ui.interactions.importing.mapper

import androidx.compose.ui.text.VerbatimTtsAnnotation
import androidx.core.text.BidiFormatter
import com.android.contacts.tests.factory.SimCardFactory
import com.android.contacts.tests.factory.SimContactFactory
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapperImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SimCardOptionMapperTest {

    private val bidiFormatter = BidiFormatter.getInstance()
    private val mapper: SimCardOptionMapper = SimCardOptionMapperImpl(bidiFormatter)

    @Test
    fun map_setsBasicFields() {
        val simCard = SimCardFactory.build(
            subscriptionId = 123,
            displayName = "John Smith",
        ).withContacts(SimContactFactory.build())

        val option = mapper.map(simCard)

        assertEquals(123, option.subscriptionId)
        assertEquals("John Smith", option.name)
        assertEquals(1, option.contactsCount)
    }

    @Test
    fun map_withNullPhone_isSetNull() {
        val simCard = SimCardFactory.build(phoneNumber = null)

        val option = mapper.map(simCard)

        assertNull(option.phone)
    }

    @Test
    fun map_withBlankPhone_isSetNull() {
        val simCard = SimCardFactory.build(phoneNumber = "")

        val option = mapper.map(simCard)

        assertNull(option.phone)
    }

    @Test
    fun map_withInvalidPhone_isSetExactlyTheSame() {
        val simCard = SimCardFactory.build(phoneNumber = "invalid")

        val option = mapper.map(simCard)

        checkNotNull(option.phone)
        assertEquals("invalid", option.phone.text)
    }

    @Test
    fun map_withPhone_hasVerbatimTtsAnnotation() {
        val simCard = SimCardFactory.build(phoneNumber = "123456789")

        val option = mapper.map(simCard)

        checkNotNull(option.phone)
        val annotations = option.phone.getTtsAnnotations(0, option.phone.length)
        assertEquals(1, annotations.size)
        assertEquals(VerbatimTtsAnnotation("123456789"), annotations.first().item)
    }

    @Test
    fun map_withSimplePhone_isNotFormatted() {
        val simCard = SimCardFactory.build(phoneNumber = "123456789")

        val option = mapper.map(simCard)

        checkNotNull(option.phone)
        assertEquals("123456789", option.phone.text)
    }

    @Test
    fun map_withPhoneWithInternationalCode_isFormatted() {
        val simCard = SimCardFactory.build(phoneNumber = "+15551234567")

        val option = mapper.map(simCard)

        checkNotNull(option.phone)
        assertEquals("+1 555-123-4567", option.phone.text)
    }
}

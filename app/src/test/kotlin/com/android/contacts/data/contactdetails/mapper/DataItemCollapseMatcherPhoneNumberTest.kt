package com.android.contacts.data.contactdetails.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DataItemCollapseMatcherPhoneNumberTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val matcher = DataItemCollapseMatcherImpl(context)

    @Test
    fun shouldCollapse_withEqualMimeTypesAndData_returnsTrue() {
        assertBothOrders(true, null, null, null, null)
        assertBothOrders(true, "a", "b", "a", "b")
        assertBothOrders(true, Phone.CONTENT_ITEM_TYPE, null, Phone.CONTENT_ITEM_TYPE, null)
        assertBothOrders(true, Phone.CONTENT_ITEM_TYPE, "1", Phone.CONTENT_ITEM_TYPE, "1")
    }

    @Test
    fun shouldCollapse_withDifferentMimeTypes_returnsFalse() {
        assertBothOrders(false, "a", null, null, null)
        assertBothOrders(false, "a", "b", null, null)
        assertBothOrders(false, "a", "b", null, "b")
        assertBothOrders(false, "a", "b", "x", "b")
    }

    @Test
    fun shouldCollapse_withEqualMimeTypesAndDifferentData_returnsFalse() {
        assertBothOrders(false, null, "a", null, null)
        assertBothOrders(false, "a", "b", "a", null)
        assertBothOrders(false, "a", "b", "a", "x")
    }

    @Test
    fun shouldCollapse_withPhoneNumberMissingOnOneSide_returnsFalse() {
        assertPhonesBothOrders(false, "1234567", null)
    }

    @Test
    fun shouldCollapse_withPlainPhoneNumbers_matchesOnlyEqualNumbers() {
        assertPhonesBothOrders(true, "1234567", "1234567")
        assertPhonesBothOrders(false, "1234567", "1234568")
    }

    @Test
    fun shouldCollapse_withWaitSymbolExtensions_comparesEveryPart() {
        assertPhonesBothOrders(false, "1234567;89321", "1234567;89322")
        assertPhonesBothOrders(false, "1234567;0111111111", "1234567;")
        assertPhonesBothOrders(false, "12345675426;91970xxxxx", "12345675426")
        assertPhonesBothOrders(false, "12345675426;23456xxxxx", "12345675426;234567xxxx")
        assertPhonesBothOrders(false, "1234567;1234567;1234567", "1234567;1234567")
    }

    @Test
    fun shouldCollapse_withInternationalNumbers_ignoresSeparatorsButNotDigits() {
        assertPhonesBothOrders(true, "+49 (89) 12345678", "+49 (89)12345678")
        assertPhonesBothOrders(true, "+49 (8092) 1234", "+49 (8092)1234")
        assertPhonesBothOrders(false, "0049 (8092) 1234", "+49/80921234")
        assertPhonesBothOrders(false, "+49 (89) 12345678", "+49 (89) 12345679")
    }

    @Test
    fun shouldCollapse_withNanpCountryCode_prefersTheNumberWithTheCountryCode() {
        assertCollapsesPhones(true, "+1 (415) 555-1212", "(415) 555-1212")
        assertCollapsesPhones(true, "+14155551212", "4155551212")
        assertCollapsesPhones(false, "(415) 555-1212", "+1 (415) 555-1212")
        assertCollapsesPhones(false, "4155551212", "+14155551212")
    }

    @Test
    fun shouldCollapse_withNanpTrunkPrefix_requiresAnExplicitCountryCode() {
        assertCollapsesPhones(false, "1-415-555-1212", "415-555-1212")
        assertCollapsesPhones(false, "14155551212", "4155551212")
        assertCollapsesPhones(false, "+1 (415) 555-1212", " 1 (415) 555-1212")
        assertCollapsesPhones(false, "+14155551212", " 14155551212")
        assertCollapsesPhones(false, "1 (415) 555-1212", "+1 (415) 555-1212")
        assertCollapsesPhones(false, "14155551212", "+14155551212")
    }

    @Test
    fun shouldCollapse_withWaitSymbolAndAreaCode_comparesEveryPart() {
        assertPhonesBothOrders(true, "+49 (8092) 1234;89321", "+49/80921234;89321")
        assertPhonesBothOrders(false, "+49 (8092) 1234;89321", "+49/80921235;89321")
        assertPhonesBothOrders(false, "+49 (8092) 1234;89322", "+49/80921234;89321")
        assertPhonesBothOrders(true, "1234567;+49 (8092) 1234", "1234567;+49/80921234")
    }

    @Test
    fun shouldCollapse_withUnparsableNumbers_comparesTheRawValues() {
        assertPhonesBothOrders(true, "", "")
        assertPhonesBothOrders(false, "1", "")
        assertPhonesBothOrders(true, "---", "---")
        assertPhonesBothOrders(false, "1-/().", "--\$%1")
    }

    @Test
    fun shouldCollapse_withKeypadLetters_prefersTheNumberWithLetters() {
        assertCollapsesPhones(true, "abcdefghijklmnopqrstuvwxyz", "22233344455566677778889999")
        assertCollapsesPhones(false, "22233344455566677778889999", "abcdefghijklmnopqrstuvwxyz")
    }

    @Test
    fun shouldCollapse_withPauseOrWaitSeparators_returnsFalse() {
        assertPhonesBothOrders(false, "1;2", "12")
        assertPhonesBothOrders(false, "1,2", "12")
    }

    @Test
    fun shouldCollapse_withPoundOrStarPrefix_returnsFalse() {
        assertPhonesBothOrders(false, "#555", "555")
        assertPhonesBothOrders(false, "*555", "555")
        assertPhonesBothOrders(false, "#555", "*555")
    }

    @Test
    fun shouldCollapse_withSameNumberInDifferentFormats_returnsTrue() {
        assertCollapsesPhones(true, "555-1212", "5551212")
        assertCollapsesPhones(true, "415-555-1212", "(415) 555-1212")
        assertCollapsesPhones(true, "4155551212", "(415) 555-1212")
        assertCollapsesPhones(true, "1-415-555-1212", "1 (415) 555-1212")
        assertCollapsesPhones(true, "14155551212", "1 (415) 555-1212")
    }

    private fun assertBothOrders(
        expected: Boolean,
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ) {
        val message = "$mimeType/$data vs $otherMimeType/$otherData"
        assertEquals(message, expected, shouldCollapse(mimeType, data, otherMimeType, otherData))
        assertEquals(message, expected, shouldCollapse(otherMimeType, otherData, mimeType, data))
    }

    private fun assertPhonesBothOrders(
        expected: Boolean,
        number: String?,
        otherNumber: String?,
    ) {
        assertCollapsesPhones(expected, number, otherNumber)
        assertCollapsesPhones(expected, otherNumber, number)
    }

    private fun assertCollapsesPhones(
        expected: Boolean,
        number: String?,
        otherNumber: String?,
    ) {
        assertEquals(
            "$number vs $otherNumber",
            expected,
            shouldCollapse(
                Phone.CONTENT_ITEM_TYPE,
                number,
                Phone.CONTENT_ITEM_TYPE,
                otherNumber,
            ),
        )
    }

    private fun shouldCollapse(
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ): Boolean {
        return matcher.shouldCollapse(
            collapsibleDataItem(mimeType = mimeType, data = data),
            collapsibleDataItem(mimeType = otherMimeType, data = otherData),
        )
    }
}

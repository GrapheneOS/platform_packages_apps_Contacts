package com.android.contacts.data.contactdetails.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.contacts.tests.factory.collapsibleDataItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class DataItemCollapseMatcherPhoneNumberTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val matcher = DataItemCollapseMatcherImpl(context)

    @Test
    fun shouldCollapse_withPhoneNumberMissingOnOneSide_returnsFalse() {
        assertNumbersDoNotCollapseInAnyOrder("1234567", null)
    }

    @Test
    fun shouldCollapse_withPlainPhoneNumbers_matchesOnlyEqualNumbers() {
        assertNumbersCollapseInAnyOrder("1234567", "1234567")
        assertNumbersDoNotCollapseInAnyOrder("1234567", "1234568")
    }

    @Test
    fun shouldCollapse_withWaitSymbolExtensions_comparesEveryPart() {
        assertNumbersDoNotCollapseInAnyOrder("1234567;89321", "1234567;89322")
        assertNumbersDoNotCollapseInAnyOrder("1234567;0111111111", "1234567;")
        assertNumbersDoNotCollapseInAnyOrder("12345675426;91970xxxxx", "12345675426")
        assertNumbersDoNotCollapseInAnyOrder("12345675426;23456xxxxx", "12345675426;234567xxxx")
        assertNumbersDoNotCollapseInAnyOrder("1234567;1234567;1234567", "1234567;1234567")
    }

    @Test
    fun shouldCollapse_withInternationalNumbers_ignoresSeparatorsButNotDigits() {
        assertNumbersCollapseInAnyOrder("+49 (89) 12345678", "+49 (89)12345678")
        assertNumbersCollapseInAnyOrder("+49 (8092) 1234", "+49 (8092)1234")
        assertNumbersDoNotCollapseInAnyOrder("0049 (8092) 1234", "+49/80921234")
        assertNumbersDoNotCollapseInAnyOrder("+49 (89) 12345678", "+49 (89) 12345679")
    }

    @Test
    fun shouldCollapse_withNanpCountryCode_prefersTheNumberWithTheCountryCode() {
        assertNumbersCollapse("+1 (415) 555-1212", "(415) 555-1212")
        assertNumbersCollapse("+14155551212", "4155551212")
        assertNumbersDoNotCollapse("(415) 555-1212", "+1 (415) 555-1212")
        assertNumbersDoNotCollapse("4155551212", "+14155551212")
    }

    @Test
    fun shouldCollapse_withNanpTrunkPrefix_requiresAnExplicitCountryCode() {
        assertNumbersDoNotCollapse("1-415-555-1212", "415-555-1212")
        assertNumbersDoNotCollapse("14155551212", "4155551212")
        assertNumbersDoNotCollapse("+1 (415) 555-1212", " 1 (415) 555-1212")
        assertNumbersDoNotCollapse("+14155551212", " 14155551212")
        assertNumbersDoNotCollapse("1 (415) 555-1212", "+1 (415) 555-1212")
        assertNumbersDoNotCollapse("14155551212", "+14155551212")
    }

    @Test
    fun shouldCollapse_withWaitSymbolAndAreaCode_comparesEveryPart() {
        assertNumbersCollapseInAnyOrder("+49 (8092) 1234;89321", "+49/80921234;89321")
        assertNumbersDoNotCollapseInAnyOrder("+49 (8092) 1234;89321", "+49/80921235;89321")
        assertNumbersDoNotCollapseInAnyOrder("+49 (8092) 1234;89322", "+49/80921234;89321")
        assertNumbersCollapseInAnyOrder("1234567;+49 (8092) 1234", "1234567;+49/80921234")
    }

    @Test
    fun shouldCollapse_withUnparsableNumbers_comparesTheRawValues() {
        assertNumbersCollapseInAnyOrder("", "")
        assertNumbersDoNotCollapseInAnyOrder("1", "")
        assertNumbersCollapseInAnyOrder("---", "---")
        assertNumbersDoNotCollapseInAnyOrder("1-/().", "--\$%1")
    }

    @Test
    fun shouldCollapse_withKeypadLetters_prefersTheNumberWithLetters() {
        assertNumbersCollapse("abcdefghijklmnopqrstuvwxyz", "22233344455566677778889999")
        assertNumbersDoNotCollapse("22233344455566677778889999", "abcdefghijklmnopqrstuvwxyz")
    }

    @Test
    fun shouldCollapse_withPauseOrWaitSeparators_returnsFalse() {
        assertNumbersDoNotCollapseInAnyOrder("1;2", "12")
        assertNumbersDoNotCollapseInAnyOrder("1,2", "12")
    }

    @Test
    fun shouldCollapse_withPoundOrStarPrefix_returnsFalse() {
        assertNumbersDoNotCollapseInAnyOrder("#555", "555")
        assertNumbersDoNotCollapseInAnyOrder("*555", "555")
        assertNumbersDoNotCollapseInAnyOrder("#555", "*555")
    }

    @Test
    fun shouldCollapse_withSameNumberInDifferentFormats_returnsTrue() {
        assertNumbersCollapse("555-1212", "5551212")
        assertNumbersCollapse("415-555-1212", "(415) 555-1212")
        assertNumbersCollapse("4155551212", "(415) 555-1212")
        assertNumbersCollapse("1-415-555-1212", "1 (415) 555-1212")
        assertNumbersCollapse("14155551212", "1 (415) 555-1212")
    }

    private fun assertNumbersCollapseInAnyOrder(
        number: String?,
        otherNumber: String?,
    ) {
        assertNumbersCollapse(number, otherNumber)
        assertNumbersCollapse(otherNumber, number)
    }

    private fun assertNumbersDoNotCollapseInAnyOrder(
        number: String?,
        otherNumber: String?,
    ) {
        assertNumbersDoNotCollapse(number, otherNumber)
        assertNumbersDoNotCollapse(otherNumber, number)
    }

    private fun assertNumbersCollapse(
        number: String?,
        otherNumber: String?,
    ) {
        assertNumbersCollapse(true, number, otherNumber)
    }

    private fun assertNumbersDoNotCollapse(
        number: String?,
        otherNumber: String?,
    ) {
        assertNumbersCollapse(false, number, otherNumber)
    }

    private fun assertNumbersCollapse(
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

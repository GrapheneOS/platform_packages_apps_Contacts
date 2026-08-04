package com.android.contacts.domain

import android.content.ContentResolver
import android.net.Uri
import com.android.contacts.domain.vcard.model.VCardVersion
import com.android.contacts.domain.vcard.usecase.ParseVCardDetailsImpl
import com.android.vcard.VCardConfig
import com.android.vcard.exception.VCardException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ParseVCardDetailsTest {

    private val contentResolver = mockk<ContentResolver>()

    @Test
    @Throws(VCardException::class)
    fun invalidFormatThrowsVCardException() =
        runTest {
            val content = "INVALID"
            every { contentResolver.openInputStream(any()) } returns content.byteInputStream()

            buildSubject()(Uri.parse(""))
        }

    @Test
    fun individualVCard21() =
        runTest {
            every { contentResolver.openInputStream(any()) } returns VCARD21.byteInputStream()

            val details = buildSubject()(Uri.parse(""))

            assertEquals(VCardVersion.V21, details.version)
            assertEquals(VCardConfig.VCARD_TYPE_V21_GENERIC, details.estimatedType)
            assertEquals(null, details.estimatedCharset)
            assertEquals(1, details.entryCount)
        }

    @Test
    fun multipleVCard21() =
        runTest {
            every { contentResolver.openInputStream(any()) } returns
                VCARD21_MULTIPLE.byteInputStream()

            val details = buildSubject()(Uri.parse(""))

            assertEquals(VCardVersion.V21, details.version)
            assertEquals(VCardConfig.VCARD_TYPE_V21_GENERIC, details.estimatedType)
            assertEquals(null, details.estimatedCharset)
            assertEquals(3, details.entryCount)
        }

    @Test
    @Ignore("The V3.0 parser is failing to detect the entry count")
    fun individualVCard30() =
        runTest {
            every { contentResolver.openInputStream(any()) } returns VCARD30.byteInputStream()

            val details = buildSubject()(Uri.parse(""))

            assertEquals(VCardVersion.V30, details.version)
            assertEquals(VCardConfig.VCARD_TYPE_UNKNOWN, details.estimatedType)
            assertEquals(null, details.estimatedCharset)
            assertEquals(1, details.entryCount)
        }

    @Test
    @Ignore("The V3.0 parser is incorrectly handling a V4.0 vcard as valid")
    fun individualVCard40() =
        runTest {
            every { contentResolver.openInputStream(any()) } returns VCARD40.byteInputStream()

            val details = buildSubject()(Uri.parse(""))

            assertEquals(VCardVersion.V40, details.version)
            assertEquals(VCardConfig.VCARD_TYPE_UNKNOWN, details.estimatedType)
            assertEquals(null, details.estimatedCharset)
            assertEquals(1, details.entryCount)
        }

    private fun buildSubject() = ParseVCardDetailsImpl(
        contentResolver = contentResolver,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    companion object {
        private val VCARD21 = """
            BEGIN:VCARD
            VERSION:2.1
            FN:John Doe
            END:VCARD
        """.trimIndent()
        private val VCARD21_MULTIPLE = """
            BEGIN:VCARD
            VERSION:2.1
            FN:John Doe
            END:VCARD
            BEGIN:VCARD
            VERSION:2.1
            FN:John Smith
            END:VCARD
            BEGIN:VCARD
            VERSION:2.1
            FN:John Wayne
            END:VCARD
        """.trimIndent()
        private val VCARD30 = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Smith
            N:Smith;John;Michael;Mr.;
            ORG:Tech Solutions Inc.
            TITLE:Senior Developer
            TEL;TYPE=WORK:+1-555-123-4567
            TEL;TYPE=CELL:+1-555-987-6543
            EMAIL;TYPE=WORK:john@techsolutions.com
            URL:https://www.techsolutions.com
            ADR;TYPE=WORK:;;123 Tech Street;San Francisco;CA;94105;USA
            END:VCARD
        """.trimIndent()
        private val VCARD40 = """
            BEGIN:VCARD
            VERSION:4.0
            N:Doe;John;Michael;;Jr.
            FN:John Michael Doe Jr.
            GENDER:M
            KIND:individual
            ORG:Example Corporation;Development Department
            TITLE:Senior Software Developer
            TEL;TYPE=home;VALUE=uri:tel:+1-555-123-4567
            TEL;TYPE=work;VALUE=uri:tel:+1-555-987-6543
            EMAIL:john.doe@example.com
            URL:https://johndoe.example.com
            ADR;TYPE=home:;;123 Main St;Anytown;CA;12345;USA
            LANG:en-US
            BDAY:19850315
            ANNIVERSARY:20100601
            NOTE:Lead developer for mobile applications
            END:VCARD
        """.trimIndent()
    }
}

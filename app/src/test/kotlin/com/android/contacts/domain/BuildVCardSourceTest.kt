package com.android.contacts.domain

import android.content.Context
import androidx.core.net.toUri
import com.android.contacts.domain.util.SaveUriToFile
import com.android.contacts.domain.vcard.usecase.BuildVCardSourceImpl
import com.android.contacts.domain.vcard.usecase.ResolveFileDisplayName
import com.android.contacts.tests.factory.ImportVCardSourceFactory
import com.android.contacts.vcard.VCardService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BuildVCardSourceTest {

    private val context = mockk<Context>()
    private val resolveFileDisplayName = mockk<ResolveFileDisplayName>()
    private val saveUriToFile = mockk<SaveUriToFile>()

    @Test
    fun whenNoFileExists_picksFirstLocalFilename() =
        runTest {
            val source = ImportVCardSourceFactory.build()
            val file = File("non_existing")
            every { context.getFileStreamPath(any()) } returns file
            coEvery { saveUriToFile.invoke(any(), any()) } returns false

            buildSubject()(source.uri)
            coVerify { saveUriToFile(source.uri, VCardService.CACHE_FILE_PREFIX + "0.vcf") }
        }

    @Test
    fun whenFirstFileExists_picksSecondLocalFilename() =
        runTest {
            val source = ImportVCardSourceFactory.build()
            every {
                context.getFileStreamPath(VCardService.CACHE_FILE_PREFIX + "0.vcf")
            } returns createFile()
            every {
                context.getFileStreamPath(VCardService.CACHE_FILE_PREFIX + "1.vcf")
            } returns File("non_existing")
            coEvery { saveUriToFile.invoke(any(), any()) } returns false

            buildSubject()(source.uri)
            coVerify { saveUriToFile(source.uri, VCardService.CACHE_FILE_PREFIX + "1.vcf") }
        }

    @Test
    fun whenSaveFileFailed_returnsNullAndDoesNotResolveDisplayName() =
        runTest {
            val source = ImportVCardSourceFactory.build()
            every { context.getFileStreamPath(any()) } returns File("non_existing")
            coEvery { saveUriToFile.invoke(any(), any()) } returns false

            assertNull(buildSubject()(source.uri))
            verify(exactly = 0) { resolveFileDisplayName.invoke(any()) }
        }

    @Test
    fun whenSaveFileIsSuccessful_resolvesDisplayNameAndReturnsSource() =
        runTest {
            val uri = ImportVCardSourceFactory.build().uri
            val displayName = "Contacts Export"
            val file = File("non_existing")
            every { context.getFileStreamPath(any()) } returns file
            coEvery { saveUriToFile.invoke(any(), any()) } returns true
            every { resolveFileDisplayName.invoke(any()) } returns displayName

            val source = buildSubject()(uri)!!
            assertEquals(file.toURI().toString().toUri(), source.uri)
            assertEquals(displayName, source.name)
            verify { resolveFileDisplayName.invoke(uri) }
        }

    private fun buildSubject() = BuildVCardSourceImpl(
        context = context,
        resolveFileDisplayName = resolveFileDisplayName,
        saveUriToFile = saveUriToFile,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    private fun createFile(): File {
        val file = File.createTempFile("test", ".vcf")
        file.deleteOnExit()
        return file
    }
}

package com.android.contacts.domain.photo.usecase

import com.android.contacts.data.photo.repository.ContactPhotoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GetPhotoCropSizeTest {

    private val contactPhotoRepository = mockk<ContactPhotoRepository>()

    private val subject: GetPhotoCropSize = GetPhotoCropSizeImpl(
        contactPhotoRepository = contactPhotoRepository,
    )

    @Test
    fun whenProviderReturnsMaxDimension_returnsIt() = runTest {
        coEvery { contactPhotoRepository.getMaxPhotoDimension() } returns 1080

        assertEquals(1080, subject())
    }

    @Test
    fun whenProviderReturnsNull_returnsDefault() = runTest {
        coEvery { contactPhotoRepository.getMaxPhotoDimension() } returns null

        assertEquals(720, subject())
    }
}

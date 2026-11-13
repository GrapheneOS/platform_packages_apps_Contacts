package com.android.contacts.domain.photo.usecase

import androidx.core.net.toUri
import com.android.contacts.data.photo.repository.ContactPhotoRepository
import com.android.contacts.domain.photo.model.AttachPhotoResult
import com.android.contacts.domain.photo.model.AttachPhotoTarget
import com.android.contacts.tests.factory.AccountModelFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AttachPhotoToContactTest {

    private val contactPhotoRepository = mockk<ContactPhotoRepository>()

    private val subject: AttachPhotoToContact = AttachPhotoToContactImpl(
        contactPhotoRepository = contactPhotoRepository,
    )

    @Test
    fun whenThumbnailCannotBeBuilt_returnsInvalidPhotoAndDoesNotSave() = runTest {
        coEvery { contactPhotoRepository.buildThumbnail(PHOTO_URI) } returns null

        val result = subject(CONTACT_URI, EXISTING_TARGET, PHOTO_URI)

        assertEquals(AttachPhotoResult.INVALID_PHOTO, result)
        coVerify(exactly = 0) {
            contactPhotoRepository.savePhotoToRawContact(any(), any(), any(), any())
            contactPhotoRepository.savePhotoToNewRawContact(any(), any(), any(), any())
        }
    }

    @Test
    fun whenTargetIsExistingRawContact_savesToThatRawContact() = runTest {
        coEvery { contactPhotoRepository.buildThumbnail(PHOTO_URI) } returns THUMBNAIL
        coEvery {
            contactPhotoRepository.savePhotoToRawContact(any(), any(), any(), any())
        } returns true

        val result = subject(CONTACT_URI, EXISTING_TARGET, PHOTO_URI)

        assertEquals(AttachPhotoResult.SUCCESS, result)
        coVerify {
            contactPhotoRepository.savePhotoToRawContact(
                contactUri = CONTACT_URI,
                rawContactId = RAW_CONTACT_ID,
                thumbnail = THUMBNAIL,
                photoUri = PHOTO_URI,
            )
        }
    }

    @Test
    fun whenTargetIsNewRawContact_savesToNewRawContactInAccount() = runTest {
        coEvery { contactPhotoRepository.buildThumbnail(PHOTO_URI) } returns THUMBNAIL
        coEvery {
            contactPhotoRepository.savePhotoToNewRawContact(any(), any(), any(), any())
        } returns true

        val result = subject(CONTACT_URI, AttachPhotoTarget.Ready.NewRawContact(ACCOUNT), PHOTO_URI)

        assertEquals(AttachPhotoResult.SUCCESS, result)
        coVerify {
            contactPhotoRepository.savePhotoToNewRawContact(
                contactUri = CONTACT_URI,
                account = ACCOUNT,
                thumbnail = THUMBNAIL,
                photoUri = PHOTO_URI,
            )
        }
    }

    @Test
    fun whenSaveFails_returnsSaveFailed() = runTest {
        coEvery { contactPhotoRepository.buildThumbnail(PHOTO_URI) } returns THUMBNAIL
        coEvery {
            contactPhotoRepository.savePhotoToRawContact(any(), any(), any(), any())
        } returns false

        val result = subject(CONTACT_URI, EXISTING_TARGET, PHOTO_URI)

        assertEquals(AttachPhotoResult.SAVE_FAILED, result)
    }

    private companion object {
        const val RAW_CONTACT_ID = 42L
        val CONTACT_URI = "content://com.android.contacts/contacts/lookup/abc/1".toUri()
        val PHOTO_URI = "content://com.android.contacts.files/photo.jpg".toUri()
        val THUMBNAIL = byteArrayOf(1, 2, 3)
        val ACCOUNT = AccountModelFactory.build(name = "user@example.org", type = "com.example")
        val EXISTING_TARGET = AttachPhotoTarget.Ready.ExistingRawContact(RAW_CONTACT_ID)
    }
}

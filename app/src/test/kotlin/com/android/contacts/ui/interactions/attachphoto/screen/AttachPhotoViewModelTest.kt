package com.android.contacts.ui.interactions.attachphoto.screen

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.data.photo.model.TempPhotoUris
import com.android.contacts.data.photo.repository.ContactPhotoRepository
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.photo.model.AttachPhotoResult
import com.android.contacts.domain.photo.model.AttachPhotoTarget
import com.android.contacts.domain.photo.usecase.AttachPhotoToContact
import com.android.contacts.domain.photo.usecase.GetPhotoCropSize
import com.android.contacts.domain.photo.usecase.ResolveAttachPhotoTarget
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.interactions.attachphoto.AttachPhotoActivity.Companion.EXTRA_MIME_TYPE
import com.android.contacts.ui.interactions.attachphoto.AttachPhotoActivity.Companion.EXTRA_SOURCE_URI
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoAction as Action
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoEffect as Effect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AttachPhotoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val contactPhotoRepository = mockk<ContactPhotoRepository>(relaxed = true)
    private val getPhotoCropSize = mockk<GetPhotoCropSize>()
    private val resolveAttachPhotoTarget = mockk<ResolveAttachPhotoTarget>()
    private val attachPhotoToContact = mockk<AttachPhotoToContact>()
    private val getDefaultAccount = mockk<GetDefaultAccount>()

    @Before
    fun setUp() {
        coEvery { contactPhotoRepository.generateTempPhotoUris() } returns TEMP_URIS
        coEvery { contactPhotoRepository.copyPhoto(any(), any()) } returns true
        coEvery { contactPhotoRepository.cropToSquare(any(), any(), any()) } returns true
        coEvery { getPhotoCropSize() } returns CROP_SIZE
        coEvery { resolveAttachPhotoTarget(any()) } returns EXISTING_TARGET
        coEvery { attachPhotoToContact(any(), any(), any()) } returns AttachPhotoResult.SUCCESS
    }

    @Test
    fun whenSourceUriIsNull_closesWithoutPickingContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(sourceUri = null)

            viewModel.effects.test {
                assertEquals(Effect.Close(isSuccess = false), awaitItem())
                expectNoEvents()
            }
            coVerify(exactly = 0) { contactPhotoRepository.generateTempPhotoUris() }
        }

    @Test
    fun onStart_generatesTempUrisAndPicksContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                assertEquals(Effect.PickContact, awaitItem())
            }
            coVerify(exactly = 1) { contactPhotoRepository.generateTempPhotoUris() }
        }

    @Test
    fun whenRestoredWithTempUris_doesNotPickContactAgain() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(restoredState = mapOf(KEY_TEMP_PHOTO_URIS to TEMP_URIS))

            viewModel.effects.test {
                expectNoEvents()
            }
            coVerify(exactly = 0) { contactPhotoRepository.generateTempPhotoUris() }
        }

    @Test
    fun onContactPicked_whenContactIsNull_closes() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createStartedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.ContactPicked(contactUri = null))

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
        }

    @Test
    fun onContactPicked_whenCopyFails_closes() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { contactPhotoRepository.copyPhoto(any(), any()) } returns false
            val viewModel = createStartedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.ContactPicked(CONTACT_URI))

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
        }

    @Test
    fun onContactPicked_copiesSourceToTempAndCrops() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createStartedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.ContactPicked(CONTACT_URI))

                assertEquals(
                    Effect.CropPhoto(
                        inputUri = TEMP_URIS.temp,
                        outputUri = TEMP_URIS.cropped,
                        mimeType = MIME_TYPE,
                        size = CROP_SIZE,
                    ),
                    awaitItem(),
                )
            }
            coVerify { contactPhotoRepository.copyPhoto(SOURCE_URI, TEMP_URIS.temp) }
        }

    @Test
    fun onCropFinished_whenCropFailed_deletesTempAndCroppedAndCloses() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropFinished(isSuccess = false))

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.temp) }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
            coVerify(exactly = 0) { attachPhotoToContact(any(), any(), any()) }
        }

    @Test
    fun onCropFinished_whenCropSucceeded_deletesTempAttachesCroppedPhotoAndCloses() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropFinished(isSuccess = true))

                assertEquals(Effect.Close(isSuccess = true), awaitItem())
            }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.temp) }
            coVerify { resolveAttachPhotoTarget(CONTACT_URI) }
            coVerify { attachPhotoToContact(CONTACT_URI, EXISTING_TARGET, TEMP_URIS.cropped) }
            coVerify(exactly = 0) { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
        }

    @Test
    fun onCropFinished_whenAttachFails_deletesCroppedAndClosesWithFailure() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { attachPhotoToContact(any(), any(), any()) } returns
                AttachPhotoResult.SAVE_FAILED
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropFinished(isSuccess = true))

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
        }

    @Test
    fun onCropFinished_whenContactNotFound_deletesCroppedAndClosesWithoutAttaching() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { resolveAttachPhotoTarget(any()) } returns AttachPhotoTarget.NotFound
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropFinished(isSuccess = true))

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
            coVerify(exactly = 0) { attachPhotoToContact(any(), any(), any()) }
        }

    @Test
    fun onCropUnavailable_cropsTempToSquareAttachesCroppedPhotoAndDeletesTemp() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropUnavailable)

                assertEquals(Effect.Close(isSuccess = true), awaitItem())
            }
            coVerifyOrder {
                contactPhotoRepository.cropToSquare(TEMP_URIS.temp, TEMP_URIS.cropped, CROP_SIZE)
                attachPhotoToContact(CONTACT_URI, EXISTING_TARGET, TEMP_URIS.cropped)
                contactPhotoRepository.deletePhoto(TEMP_URIS.temp)
            }
        }

    @Test
    fun onCropUnavailable_whenCropFails_deletesTempAndCroppedAndClosesWithoutAttaching() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { contactPhotoRepository.cropToSquare(any(), any(), any()) } returns false
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropUnavailable)

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.temp) }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
            coVerify(exactly = 0) { attachPhotoToContact(any(), any(), any()) }
        }

    @Test
    fun whenAccountSelectionIsNeeded_picksDefaultAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { resolveAttachPhotoTarget(any()) } returns
                AttachPhotoTarget.NeedsAccountSelection
            val viewModel = createPickedViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CropFinished(isSuccess = true))

                assertEquals(Effect.PickDefaultAccount, awaitItem())
                expectNoEvents()
            }
            coVerify(exactly = 0) { attachPhotoToContact(any(), any(), any()) }
        }

    @Test
    fun onDefaultAccountPicked_attachesToNewRawContactInDefaultAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { getDefaultAccount() } returns ACCOUNT
            val viewModel = createViewModel(restoredState = AWAITING_ACCOUNT_STATE)

            viewModel.effects.test {
                viewModel.onAction(Action.DefaultAccountPicked)

                assertEquals(Effect.Close(isSuccess = true), awaitItem())
            }
            coVerify {
                attachPhotoToContact(
                    CONTACT_URI,
                    AttachPhotoTarget.Ready.NewRawContact(ACCOUNT),
                    TEMP_URIS.cropped,
                )
            }
            coVerify(exactly = 0) { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
        }

    @Test
    fun onDefaultAccountPicked_whenNoDefaultAccount_deletesCroppedAndClosesWithoutAttaching() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery { getDefaultAccount() } returns null
            val viewModel = createViewModel(restoredState = AWAITING_ACCOUNT_STATE)

            viewModel.effects.test {
                viewModel.onAction(Action.DefaultAccountPicked)

                assertEquals(Effect.Close(isSuccess = false), awaitItem())
            }
            coVerify { contactPhotoRepository.deletePhoto(TEMP_URIS.cropped) }
            coVerify(exactly = 0) { attachPhotoToContact(any(), any(), any()) }
        }

    /** View model that already emitted [Effect.PickContact]. */
    private suspend fun createStartedViewModel(): AttachPhotoViewModel {
        val viewModel = createViewModel()
        viewModel.effects.test {
            assertEquals(Effect.PickContact, awaitItem())
        }
        return viewModel
    }

    /** View model that already picked [CONTACT_URI] and emitted [Effect.CropPhoto]. */
    private suspend fun createPickedViewModel(): AttachPhotoViewModel {
        val viewModel = createStartedViewModel()
        viewModel.effects.test {
            viewModel.onAction(Action.ContactPicked(CONTACT_URI))
            assertEquals(Effect.CropPhoto::class, awaitItem()::class)
        }
        return viewModel
    }

    private fun createViewModel(
        sourceUri: Uri? = SOURCE_URI,
        restoredState: Map<String, Any?> = emptyMap(),
    ): AttachPhotoViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                EXTRA_SOURCE_URI to sourceUri,
                EXTRA_MIME_TYPE to MIME_TYPE,
            ) + restoredState,
        )
        return AttachPhotoViewModel(
            savedStateHandle = savedStateHandle,
            contactPhotoRepository = contactPhotoRepository,
            getPhotoCropSize = getPhotoCropSize,
            resolveAttachPhotoTarget = resolveAttachPhotoTarget,
            attachPhotoToContact = attachPhotoToContact,
            getDefaultAccount = getDefaultAccount,
        )
    }

    private companion object {
        const val KEY_TEMP_PHOTO_URIS = "temp_photo_uris"
        const val KEY_CONTACT_URI = "contact_uri"
        const val KEY_PHOTO_URI = "photo_uri"

        const val MIME_TYPE = "image/jpeg"
        const val CROP_SIZE = 1080

        val SOURCE_URI = "content://media/external/images/media/794".toUri()
        val CONTACT_URI = "content://com.android.contacts/contacts/lookup/abc/1".toUri()
        val TEMP_URIS = TempPhotoUris(
            temp = "content://com.android.contacts.files/temp.jpg".toUri(),
            cropped = "content://com.android.contacts.files/cropped.jpg".toUri(),
        )
        val ACCOUNT = AccountModelFactory.build(name = "user@example.org", type = "com.example")
        val EXISTING_TARGET = AttachPhotoTarget.Ready.ExistingRawContact(rawContactId = 42L)

        val AWAITING_ACCOUNT_STATE = mapOf(
            KEY_TEMP_PHOTO_URIS to TEMP_URIS,
            KEY_CONTACT_URI to CONTACT_URI,
            KEY_PHOTO_URI to TEMP_URIS.cropped,
        )
    }
}

package com.android.contacts.editornew.viewmodel

import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.android.contacts.editornew.ContactEditorEvent
import com.android.contacts.editornew.ContactEditorUiState
import com.android.contacts.editornew.ContactEditorUiState.PhotoUiState
import com.android.contacts.editornew.ContactEditorViewModel
import com.android.contacts.editornew.contact.ContactDelegate
import com.android.contacts.editornew.photo.PhotoType
import com.android.contacts.editornew.photo.picker.PhotoDelegate
import com.android.contacts.editornew.photo.picker.PhotoDelegateImpl
import com.android.contacts.util.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ContactEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `On add photo click, show photo source choose dialog`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(ContactEditorEvent.Photo.AddOrChangeClick)
            assertPhotoSourceDialogPhotoType(expectedType = PhotoType.New)
        }
    }

    @Test
    fun `If photo exists, add photo click shows photo source dialog with type replace`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            setCropResultAndAssert(viewModel)
            viewModel.onEvent(ContactEditorEvent.Photo.AddOrChangeClick)
            assertPhotoSourceDialogPhotoType(expectedType = PhotoType.Replace)
        }
    }

    @Test
    fun `On cropped photo result, will display photo`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            setCropResultAndAssert(viewModel)
        }
    }

    @Test
    fun `On remove photo, will display photo placeholder`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            setCropResultAndAssert(viewModel)
            viewModel.onEvent(ContactEditorEvent.Photo.RemoveClick)
            awaitItem().apply {
                assertEquals(photoUiState, PhotoUiState.Placeholder)
            }
        }
    }

    private suspend fun TurbineTestContext<ContactEditorUiState>.setCropResultAndAssert(
        viewModel: ContactEditorViewModel,
    ) {
        viewModel.onEvent(ContactEditorEvent.Photo.CropResult("test".toUri()))
        awaitItem().apply {
            assertEquals(photoUiState, PhotoUiState.Photo("test".toUri()))
        }
    }

    private suspend fun TurbineTestContext<ContactEditorUiState>.assertPhotoSourceDialogPhotoType(
        expectedType: PhotoType,
    ) {
        awaitItem().apply {
            assertNotNull(photoSourceDialogUiState)
            assertEquals(photoSourceDialogUiState!!.type, expectedType)
        }
    }

    private fun viewModel(
        photoDelegate: PhotoDelegate = PhotoDelegateImpl(
            helper = mockk(relaxed = true),
        ),
        contactDelegate: ContactDelegate = mockk(relaxed = true),
    ): ContactEditorViewModel {
        return ContactEditorViewModel(
            context = ApplicationProvider.getApplicationContext(),
            photoDelegate = photoDelegate,
            contactDelegate = contactDelegate,
        )
    }
}

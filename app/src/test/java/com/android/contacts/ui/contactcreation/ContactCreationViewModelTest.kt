package com.android.contacts.ui.contactcreation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.model.RawContactDelta
import com.android.contacts.test.MainDispatcherRule
import com.android.contacts.ui.contactcreation.delegate.ContactFieldsDelegate
import com.android.contacts.ui.contactcreation.mapper.RawContactDeltaMapper
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationEffect
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactCreationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_isDefault() {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertEquals(NameState(), state.nameState)
        assertEquals(1, state.phoneNumbers.size)
        assertEquals(1, state.emails.size)
        assertFalse(state.isSaving)
    }

    @Test
    fun updateFirstName_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateFirstName("John"))
        assertEquals("John", vm.uiState.value.nameState.first)
    }

    @Test
    fun updateLastName_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateLastName("Doe"))
        assertEquals("Doe", vm.uiState.value.nameState.last)
    }

    @Test
    fun addPhone_addsRow() {
        val vm = createViewModel()
        val initialCount = vm.uiState.value.phoneNumbers.size
        vm.onAction(ContactCreationAction.AddPhone)
        assertEquals(initialCount + 1, vm.uiState.value.phoneNumbers.size)
    }

    @Test
    fun removePhone_removesRow() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.AddPhone)
        val id = vm.uiState.value.phoneNumbers[0].id
        vm.onAction(ContactCreationAction.RemovePhone(id))
        assertEquals(1, vm.uiState.value.phoneNumbers.size)
        assertTrue(vm.uiState.value.phoneNumbers.none { it.id == id })
    }

    @Test
    fun updatePhone_updatesValue() {
        val vm = createViewModel()
        val id = vm.uiState.value.phoneNumbers[0].id
        vm.onAction(ContactCreationAction.UpdatePhone(id, "555-1234"))
        assertEquals("555-1234", vm.uiState.value.phoneNumbers[0].number)
    }

    @Test
    fun addEmail_addsRow() {
        val vm = createViewModel()
        val initialCount = vm.uiState.value.emails.size
        vm.onAction(ContactCreationAction.AddEmail)
        assertEquals(initialCount + 1, vm.uiState.value.emails.size)
    }

    @Test
    fun saveAction_withPendingChanges_emitsSaveEffect() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("John"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                assertIs<ContactCreationEffect.Save>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun saveAction_withNoChanges_doesNotEmitSaveEffect() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                expectNoEvents()
            }
        }

    @Test
    fun navigateBack_withNoChanges_emitsNavigateBack() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.effects.test {
                vm.onAction(ContactCreationAction.NavigateBack)
                assertIs<ContactCreationEffect.NavigateBack>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun navigateBack_withChanges_setsShowDiscardDialog() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateFirstName("John"))
        vm.onAction(ContactCreationAction.NavigateBack)
        assertTrue(vm.uiState.value.showDiscardDialog)
    }

    @Test
    fun navigateBack_withChanges_doesNotEmitNavigateBack() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("John"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.NavigateBack)
                expectNoEvents()
            }
        }

    @Test
    fun dismissDiscardDialog_clearsShowDiscardDialog() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateFirstName("John"))
        vm.onAction(ContactCreationAction.NavigateBack)
        assertTrue(vm.uiState.value.showDiscardDialog)

        vm.onAction(ContactCreationAction.DismissDiscardDialog)
        assertFalse(vm.uiState.value.showDiscardDialog)
    }

    @Test
    fun confirmDiscard_emitsNavigateBack() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("John"))
            vm.onAction(ContactCreationAction.NavigateBack)

            vm.effects.test {
                vm.onAction(ContactCreationAction.ConfirmDiscard)
                assertIs<ContactCreationEffect.NavigateBack>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun confirmDiscard_clearsShowDiscardDialog() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateFirstName("John"))
        vm.onAction(ContactCreationAction.NavigateBack)
        assertTrue(vm.uiState.value.showDiscardDialog)

        vm.onAction(ContactCreationAction.ConfirmDiscard)
        assertFalse(vm.uiState.value.showDiscardDialog)
    }

    // --- Zero-account / local-only ---

    @Test
    fun save_withNoAccount_usesLocalAccount() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("Local"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)
                val delta = effect.result.state[0]
                assertIs<RawContactDelta>(delta)
                // When no account selected, mapper calls setAccountToLocal()
                assertNull(vm.uiState.value.selectedAccount)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onSaveResult_success_emitsSaveSuccess() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            val uri = Uri.parse("content://contacts/1")

            vm.effects.test {
                vm.onSaveResult(true, uri)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.SaveSuccess>(effect)
                assertEquals(uri, effect.contactUri)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onSaveResult_failure_emitsShowError() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()

            vm.effects.test {
                vm.onSaveResult(false, null)
                assertIs<ContactCreationEffect.ShowError>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun processDeathRestore_preservesState() {
        val savedState = ContactCreationUiState(
            nameState = NameState(first = "Saved"),
            phoneNumbers = listOf(PhoneFieldState(number = "555")),
        )
        val vm = createViewModel(initialState = savedState)
        assertEquals("Saved", vm.uiState.value.nameState.first)
        assertEquals("555", vm.uiState.value.phoneNumbers[0].number)
    }

    // --- Photo ---

    @Test
    fun setPhoto_updatesPhotoUri() {
        val vm = createViewModel()
        val uri = Uri.parse("content://media/external/images/1234")
        vm.onAction(ContactCreationAction.SetPhoto(uri))
        assertEquals(uri, vm.uiState.value.photoUri)
    }

    @Test
    fun removePhoto_clearsPhotoUri() {
        val vm = createViewModel()
        val uri = Uri.parse("content://media/external/images/1234")
        vm.onAction(ContactCreationAction.SetPhoto(uri))
        vm.onAction(ContactCreationAction.RemovePhoto)
        assertNull(vm.uiState.value.photoUri)
    }

    @Test
    fun setPhoto_countsAsPendingChange() {
        val vm = createViewModel()
        val uri = Uri.parse("content://media/external/images/1234")
        vm.onAction(ContactCreationAction.SetPhoto(uri))
        assertTrue(vm.uiState.value.hasPendingChanges())
    }

    @Test
    fun saveAction_setsIsSaving() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("John"))
            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                awaitItem() // Save effect
                assertTrue(vm.uiState.value.isSaving)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createViewModel(
        initialState: ContactCreationUiState = ContactCreationUiState(),
    ): ContactCreationViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(ContactCreationViewModel.STATE_KEY to initialState),
        )
        return ContactCreationViewModel(
            savedStateHandle = savedStateHandle,
            fieldsDelegate = ContactFieldsDelegate(),
            deltaMapper = RawContactDeltaMapper(),
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            appContext = RuntimeEnvironment.getApplication(),
        )
    }
}

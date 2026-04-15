package com.android.contacts.ui.contactcreation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.model.RawContactDelta
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.test.MainDispatcherRule
import com.android.contacts.ui.contactcreation.mapper.RawContactDeltaMapper
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationEffect
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
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

    // --- Process death round-trip ---

    @Test
    fun processDeathRestore_preservesAllFieldTypes() {
        val savedState = ContactCreationUiState(
            nameState = NameState(
                prefix = "Dr",
                first = "John",
                middle = "M",
                last = "Doe",
                suffix = "Jr",
            ),
            phoneNumbers = listOf(PhoneFieldState(number = "555")),
            emails = listOf(
                EmailFieldState(address = "a@b.com"),
            ),
            addresses = listOf(
                AddressFieldState(
                    street = "123 Main",
                ),
            ),
            organization = OrganizationFieldState(
                company = "Acme",
                title = "Eng",
            ),
            events = listOf(
                EventFieldState(
                    startDate = "1990-01-01",
                ),
            ),
            relations = listOf(
                RelationFieldState(name = "Jane"),
            ),
            imAccounts = listOf(
                ImFieldState(data = "user@jabber"),
            ),
            websites = listOf(
                WebsiteFieldState(
                    url = "https://site.com",
                ),
            ),
            note = "Important",
            nickname = "Johnny",
            sipAddress = "sip:user@voip.example.com",
            photoUri = Uri.parse("content://media/external/images/99"),
            isMoreFieldsExpanded = true,
        )
        val vm = createViewModel(initialState = savedState)
        val restored = vm.uiState.value

        assertEquals("Dr", restored.nameState.prefix)
        assertEquals("John", restored.nameState.first)
        assertEquals("M", restored.nameState.middle)
        assertEquals("Doe", restored.nameState.last)
        assertEquals("Jr", restored.nameState.suffix)
        assertEquals("555", restored.phoneNumbers[0].number)
        assertEquals("a@b.com", restored.emails[0].address)
        assertEquals("123 Main", restored.addresses[0].street)
        assertEquals("Acme", restored.organization.company)
        assertEquals("Eng", restored.organization.title)
        assertEquals("1990-01-01", restored.events[0].startDate)
        assertEquals("Jane", restored.relations[0].name)
        assertEquals("user@jabber", restored.imAccounts[0].data)
        assertEquals("https://site.com", restored.websites[0].url)
        assertEquals("Important", restored.note)
        assertEquals("Johnny", restored.nickname)
        assertEquals("sip:user@voip.example.com", restored.sipAddress)
        assertEquals(Uri.parse("content://media/external/images/99"), restored.photoUri)
        assertTrue(restored.isMoreFieldsExpanded)
    }

    // --- ToggleMoreFields ---

    @Test
    fun toggleMoreFields_togglesIsMoreFieldsExpanded() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.isMoreFieldsExpanded)
        vm.onAction(ContactCreationAction.ToggleMoreFields)
        assertTrue(vm.uiState.value.isMoreFieldsExpanded)
        vm.onAction(ContactCreationAction.ToggleMoreFields)
        assertFalse(vm.uiState.value.isMoreFieldsExpanded)
    }

    // --- Extended field actions ---

    @Test
    fun addAddress_addsRow() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.addresses.isEmpty())
        vm.onAction(ContactCreationAction.AddAddress)
        assertEquals(1, vm.uiState.value.addresses.size)
    }

    @Test
    fun addEvent_addsRow() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.events.isEmpty())
        vm.onAction(ContactCreationAction.AddEvent)
        assertEquals(1, vm.uiState.value.events.size)
    }

    @Test
    fun updateNote_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateNote("A note"))
        assertEquals("A note", vm.uiState.value.note)
    }

    @Test
    fun updateNickname_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateNickname("Johnny"))
        assertEquals("Johnny", vm.uiState.value.nickname)
    }

    @Test
    fun updateSipAddress_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateSipAddress("sip:user@voip"))
        assertEquals("sip:user@voip", vm.uiState.value.sipAddress)
    }

    @Test
    fun updateCompany_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateCompany("Acme"))
        assertEquals("Acme", vm.uiState.value.organization.company)
    }

    @Test
    fun updateJobTitle_updatesState() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateJobTitle("Engineer"))
        assertEquals("Engineer", vm.uiState.value.organization.title)
    }

    @Test
    fun selectAccount_clearsGroups() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.ToggleGroup(1L, "Friends"))
        assertEquals(1, vm.uiState.value.groups.size)

        val account = AccountWithDataSet(
            "test",
            "com.test",
            null,
        )
        vm.onAction(ContactCreationAction.SelectAccount(account))
        assertTrue(vm.uiState.value.groups.isEmpty())
        assertEquals(account, vm.uiState.value.selectedAccount)
    }

    @Test
    fun hasPendingChanges_trueForNote() {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.UpdateNote("text"))
        assertTrue(vm.uiState.value.hasPendingChanges())
    }

    @Test
    fun hasPendingChanges_falseForDefaultState() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.hasPendingChanges())
    }

    private fun createViewModel(
        initialState: ContactCreationUiState = ContactCreationUiState(),
    ): ContactCreationViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(ContactCreationViewModel.STATE_KEY to initialState),
        )
        return ContactCreationViewModel(
            savedStateHandle = savedStateHandle,
            deltaMapper = RawContactDeltaMapper(),
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            appContext = RuntimeEnvironment.getApplication(),
        )
    }
}

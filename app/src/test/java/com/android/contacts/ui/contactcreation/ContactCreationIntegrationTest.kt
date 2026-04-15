package com.android.contacts.ui.contactcreation

import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.test.MainDispatcherRule
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.mapper.RawContactDeltaMapper
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationEffect
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Integration tests using real [ContactCreationViewModel] + real [RawContactDeltaMapper].
 * No mocks except appContext via Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
class ContactCreationIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // --- 1. Basic contact produces correct delta ---

    @Test
    fun createBasicContact_producesCorrectDelta() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("John"))
            val phoneId = vm.uiState.value.phoneNumbers.first().id
            vm.onAction(ContactCreationAction.UpdatePhone(phoneId, "555-0100"))
            val emailId = vm.uiState.value.emails.first().id
            vm.onAction(ContactCreationAction.UpdateEmail(emailId, "john@test.com"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                assertNotNull(delta.getMimeEntries(StructuredName.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Phone.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Email.CONTENT_ITEM_TYPE))
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 2. All fields produce all MIME types ---

    @Test
    fun createAllFields_producesAllMimeTypes() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel(initialState = TestFactory.fullState())

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                assertNotNull(delta.getMimeEntries(StructuredName.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Phone.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Email.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Organization.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Event.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Relation.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Im.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Website.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Note.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(Nickname.CONTENT_ITEM_TYPE))
                assertNotNull(delta.getMimeEntries(SipAddress.CONTENT_ITEM_TYPE))
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 3. Empty form save produces no effect ---

    @Test
    fun emptyForm_save_noEffect() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                expectNoEvents()
            }
        }

    // --- 4. Custom phone type produces TYPE_CUSTOM and LABEL ---

    @Test
    fun customPhoneType_deltaHasTypeCustomAndLabel() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("Test"))
            val phoneId = vm.uiState.value.phoneNumbers.first().id
            vm.onAction(ContactCreationAction.UpdatePhone(phoneId, "555-0001"))
            vm.onAction(
                ContactCreationAction.UpdatePhoneType(
                    phoneId,
                    PhoneType.Custom("Work cell"),
                ),
            )

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                val phoneEntries = delta.getMimeEntries(Phone.CONTENT_ITEM_TYPE)!!
                assertEquals(Phone.TYPE_CUSTOM, phoneEntries[0].getAsInteger(Phone.TYPE))
                assertEquals("Work cell", phoneEntries[0].getAsString(Phone.LABEL))
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 5. Process death round-trip delta matches ---

    @Test
    fun processDeathRoundTrip_deltaMatchesOriginal() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Build a ViewModel, fill it, capture state
            val vm1 = createViewModel()
            vm1.onAction(ContactCreationAction.UpdateFirstName("Saved"))
            val phoneId = vm1.uiState.value.phoneNumbers.first().id
            vm1.onAction(ContactCreationAction.UpdatePhone(phoneId, "555-9999"))
            val stateAfterFill = vm1.uiState.value

            // Simulate process death: create new VM with the saved state
            val vm2 = createViewModel(initialState = stateAfterFill)

            vm2.effects.test {
                vm2.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                val nameEntries = delta.getMimeEntries(StructuredName.CONTENT_ITEM_TYPE)!!
                assertEquals("Saved", nameEntries[0].getAsString(StructuredName.GIVEN_NAME))
                val phoneEntries = delta.getMimeEntries(Phone.CONTENT_ITEM_TYPE)!!
                assertEquals("555-9999", phoneEntries[0].getAsString(Phone.NUMBER))
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 6. Photo URI in updatedPhotos bundle ---

    @Test
    fun photoUri_inUpdatedPhotosBundle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("Photo"))
            val photoUri = Uri.parse("content://media/external/images/42")
            vm.onAction(ContactCreationAction.SetPhoto(photoUri))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val photos = effect.result.updatedPhotos
                assertTrue(photos.size() > 0)
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 7. Multiple phones produce multiple entries ---

    @Test
    fun multiplePhones_produceMultipleEntries() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("Multi"))
            val phoneId1 = vm.uiState.value.phoneNumbers.first().id
            vm.onAction(ContactCreationAction.UpdatePhone(phoneId1, "111"))
            vm.onAction(ContactCreationAction.AddPhone)
            val phoneId2 = vm.uiState.value.phoneNumbers[1].id
            vm.onAction(ContactCreationAction.UpdatePhone(phoneId2, "222"))
            vm.onAction(ContactCreationAction.AddPhone)
            val phoneId3 = vm.uiState.value.phoneNumbers[2].id
            vm.onAction(ContactCreationAction.UpdatePhone(phoneId3, "333"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                val phoneEntries = delta.getMimeEntries(Phone.CONTENT_ITEM_TYPE)!!
                assertEquals(3, phoneEntries.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 8. IM protocol uses PROTOCOL column not TYPE ---

    @Test
    fun imProtocol_usesProtocolNotType() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("IM"))
            vm.onAction(ContactCreationAction.AddIm)
            val imId = vm.uiState.value.imAccounts.first().id
            vm.onAction(ContactCreationAction.UpdateIm(imId, "user@xmpp"))
            vm.onAction(
                ContactCreationAction.UpdateImProtocol(imId, ImProtocol.Jabber),
            )

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                val imEntries = delta.getMimeEntries(Im.CONTENT_ITEM_TYPE)!!
                assertEquals(Im.PROTOCOL_JABBER, imEntries[0].getAsInteger(Im.PROTOCOL))
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 9. Address with partial fill still included ---

    @Test
    fun addressPartialFill_included() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("Addr"))
            vm.onAction(ContactCreationAction.AddAddress)
            val addrId = vm.uiState.value.addresses.first().id
            vm.onAction(ContactCreationAction.UpdateAddressCity(addrId, "Portland"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                val effect = awaitItem()
                assertIs<ContactCreationEffect.Save>(effect)

                val delta = effect.result.state[0]
                val addrEntries = delta.getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE)!!
                assertEquals(1, addrEntries.size)
                assertEquals(
                    "Portland",
                    addrEntries[0].getAsString(StructuredPostal.CITY),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- 10. Save sets isSaving flag ---

    @Test
    fun save_setsIsSavingFlag() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = createViewModel()
            vm.onAction(ContactCreationAction.UpdateFirstName("Flag"))

            vm.effects.test {
                vm.onAction(ContactCreationAction.Save)
                awaitItem() // Save effect
                assertTrue(vm.uiState.value.isSaving)
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- Helper ---

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

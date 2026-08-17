package com.android.contacts.domain.contactdetails.usecase

import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType.CALL
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType.EMAIL
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType.MESSAGE
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType.VIDEO_CALL
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.email
import com.android.contacts.tests.factory.phone
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetContactQuickActionsImplTest {

    private val isEntryActionAvailable = mockk<IsEntryActionAvailable>()

    private val getContactQuickActions = GetContactQuickActionsImpl(
        isEntryActionAvailable = isEntryActionAvailable,
    )

    @Before
    fun setUp() {
        every { isEntryActionAvailable(any()) } returns true
    }

    @Test
    fun invoke_withoutAnyDataItems_stillBuildsAllFourActions() {
        val quickActions = getContactQuickActions(contactDetails())

        assertEquals(
            listOf(
                CALL,
                MESSAGE,
                VIDEO_CALL,
                EMAIL,
            ),
            quickActions.map(ContactQuickAction::type),
        )
    }

    @Test
    fun invoke_withoutAnyDataItems_leavesEveryActionUnavailable() {
        val quickActions = getContactQuickActions(contactDetails())

        assertEquals(
            emptyList<ContactEntryAction>(),
            quickActions.mapNotNull(ContactQuickAction::action),
        )
    }

    @Test
    fun invoke_withoutAPhoneNumber_disablesTheNumberActions() {
        val quickActions = getContactQuickActions(detailsOf(email(address = "alex@example.org")))

        assertNull(quickActions.actionOf(CALL))
        assertNull(quickActions.actionOf(MESSAGE))
        assertNull(quickActions.actionOf(VIDEO_CALL))
        assertEquals(
            ContactEntryAction.SendEmail("alex@example.org"),
            quickActions.actionOf(EMAIL),
        )
    }

    @Test
    fun invoke_withoutAnEmail_disablesTheEmailAction() {
        val quickActions = getContactQuickActions(detailsOf(phone(number = "4155551212")))

        assertNull(quickActions.actionOf(EMAIL))
        assertEquals(ContactEntryAction.Call("4155551212"), quickActions.actionOf(CALL))
        assertEquals(ContactEntryAction.Sms("4155551212"), quickActions.actionOf(MESSAGE))
        assertEquals(
            ContactEntryAction.VideoCall("4155551212"),
            quickActions.actionOf(VIDEO_CALL),
        )
    }

    @Test
    fun invoke_withSeveralPhoneNumbers_usesTheSuperPrimaryOne() {
        val details = detailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isSuperPrimary = true),
        )

        assertEquals(
            ContactEntryAction.Call("4155552222"),
            getContactQuickActions(details).actionOf(CALL),
        )
    }

    @Test
    fun invoke_withSeveralEmails_usesThePrimaryOne() {
        val details = detailsOf(
            email(id = 1L, address = "alex@example.org"),
            email(id = 2L, address = "alex@work.example.org", isPrimary = true),
        )

        assertEquals(
            ContactEntryAction.SendEmail("alex@work.example.org"),
            getContactQuickActions(details).actionOf(EMAIL),
        )
    }

    @Test
    fun invoke_withABlankPhoneNumber_disablesTheNumberActions() {
        val quickActions = getContactQuickActions(detailsOf(phone(number = " ")))

        assertNull(quickActions.actionOf(CALL))
        assertNull(quickActions.actionOf(MESSAGE))
    }

    @Test
    fun invoke_whenNothingResolvesAnAction_disablesIt() {
        every { isEntryActionAvailable(ContactEntryAction.VideoCall("4155551212")) } returns false

        val quickActions = getContactQuickActions(detailsOf(phone(number = "4155551212")))

        assertNull(quickActions.actionOf(VIDEO_CALL))
        assertEquals(ContactEntryAction.Call("4155551212"), quickActions.actionOf(CALL))
    }

    private fun detailsOf(vararg dataItems: ContactDataItem): ContactDetails {
        return contactDetails(dataItems = dataItems.toList())
    }

    private fun List<ContactQuickAction>.actionOf(
        type: ContactQuickActionType,
    ): ContactEntryAction? {
        return first { quickAction -> quickAction.type == type }.action
    }
}

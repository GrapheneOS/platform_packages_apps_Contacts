package com.android.contacts.ui.contactdetails.screen.mapper.contactdetailsuistatemapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactGroup
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.telecom.model.CallingSim
import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.domain.contactdetails.model.ContactConnectedApp
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.contactdetails.usecase.IsEntryActionAvailable
import com.android.contacts.domain.telecom.model.CallingSimChoice
import com.android.contacts.domain.telecom.model.CallingSimOptions
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactQuickActionUiModel
import com.android.contacts.ui.contactdetails.screen.mapper.ContactDetailsUiStateMapperImpl
import com.android.contacts.ui.contactdetails.screen.mapper.ContactQuickActionsMapper
import com.android.contacts.ui.contactdetails.screen.mapper.RecentCallsMapper
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingUiModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal abstract class BaseContactDetailsUiStateMapperTest {

    protected val context: Context = RuntimeEnvironment.getApplication()

    protected val isEntryActionAvailable = mockk<IsEntryActionAvailable>()
    protected val contactQuickActionsMapper = mockk<ContactQuickActionsMapper>()
    private val recentCallsMapper = mockk<RecentCallsMapper>()

    private val mapper = ContactDetailsUiStateMapperImpl(
        context = context,
        isEntryActionAvailable = isEntryActionAvailable,
        contactQuickActionsMapper = contactQuickActionsMapper,
        recentCallsMapper = recentCallsMapper,
    )

    @Before
    fun setUpBase() {
        every { isEntryActionAvailable(any()) } returns true
        every { contactQuickActionsMapper.map(any()) } returns QUICK_ACTIONS
        every { recentCallsMapper.map(any()) } returns persistentListOf()
    }

    protected fun mapState(
        details: ContactDetails = contactDetails(),
        cards: ContactDetailsCards = cardsOf(),
        menu: ContactDetailsMenu = contactDetailsMenu(),
        displayOrder: DisplayOrder = DisplayOrder.GIVEN_NAME_FIRST,
        callingSimOptions: CallingSimOptions? = null,
    ): ContactDetailsContent.Loaded {
        return mapper.map(
            details = details,
            cards = cards,
            quickActions = emptyList(),
            recentCalls = emptyList(),
            callingSimOptions = callingSimOptions,
            menu = menu,
            displayOrder = displayOrder,
        ) as ContactDetailsContent.Loaded
    }

    protected fun cardsOf(
        contactCard: List<ContactEntryGroup> = emptyList(),
        notes: List<ContactEntryGroup> = emptyList(),
        headerNicknames: List<String> = emptyList(),
        headerOrganizations: List<List<String>> = emptyList(),
        groups: List<ContactGroup> = emptyList(),
        connectedApps: List<ContactConnectedApp> = emptyList(),
    ): ContactDetailsCards {
        return ContactDetailsCards(
            contactCard = contactCard,
            connectedApps = connectedApps,
            notes = notes,
            headerNicknames = headerNicknames,
            headerOrganizations = headerOrganizations,
            groups = groups,
        )
    }

    protected fun groupOf(
        entry: ContactEntry,
        mimeType: String? = Email.CONTENT_ITEM_TYPE,
    ): List<ContactEntryGroup> {
        return listOf(ContactEntryGroup(mimeType = mimeType, entries = listOf(entry)))
    }

    protected fun entry(
        header: ContactEntryText? = ContactEntryText.Value("value"),
        copyLabel: ContactEntryText? = null,
        actions: ContactEntryActions = ContactEntryActions(),
        isSuperPrimary: Boolean = false,
        isDefault: Boolean = isSuperPrimary,
        text: String? = null,
        kind: ContactEntryKind = ContactEntryKind.OTHER,
    ): ContactEntry {
        return ContactEntry(
            id = 1L,
            mimeType = null,
            kind = kind,
            isSuperPrimary = isSuperPrimary,
            isDefault = isDefault,
            header = header,
            subHeader = null,
            text = text,
            copyText = null,
            copyLabel = copyLabel,
            actions = actions,
        )
    }

    protected fun sendToVoicemailSetting(
        state: ContactDetailsContent.Loaded,
    ): ContactSettingUiModel? {
        return state.settings.firstOrNull { setting ->
            setting.action == ContactDetailsAction.SendToVoicemailClick
        }
    }

    protected fun firstContactEntry(state: ContactDetailsContent.Loaded): ContactEntryUiModel {
        return state.contactCard.first().entries.first()
    }

    protected fun firstNoteEntry(state: ContactDetailsContent.Loaded): ContactEntryUiModel {
        return state.notes.first().entries.first()
    }

    protected companion object {
        val QUICK_ACTIONS = persistentListOf(contactQuickActionUiModel())

        val CALLING_SIM_OPTIONS = CallingSimOptions(
            sims = listOf(
                CallingSim(accountId = phoneAccountId("sim-1"), label = "SIM 1"),
                CallingSim(accountId = phoneAccountId("sim-2"), label = "SIM 2"),
            ),
            choices = listOf(
                CallingSimChoice(
                    dataId = 1L,
                    number = "555 0001",
                    numberLabel = "Mobile",
                    selectedAccountId = null,
                ),
            ),
        )

        private fun phoneAccountId(id: String): PhoneAccountId {
            return PhoneAccountId(componentName = "com.example/Sims", id = id)
        }
    }
}

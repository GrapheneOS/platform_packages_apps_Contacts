package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class ContactQuickActionsMapperImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val mapper = ContactQuickActionsMapperImpl(context = context)

    @Test
    fun map_picksAnIconPerType() {
        val quickActions = mapper.map(allTypes())

        assertEquals(
            listOf(
                ContactEntryIcon.CALL,
                ContactEntryIcon.MESSAGE,
                ContactEntryIcon.VIDEO_CALL,
                ContactEntryIcon.EMAIL,
            ),
            quickActions.map(ContactQuickActionUiModel::icon),
        )
    }

    @Test
    fun map_labelsEveryType() {
        val quickActions = mapper.map(allTypes())

        assertEquals(
            listOf(
                context.getString(R.string.call_other),
                context.getString(R.string.quickcontact_action_message),
                context.getString(R.string.quickcontact_action_video),
                context.getString(R.string.email),
            ),
            quickActions.map(ContactQuickActionUiModel::label),
        )
    }

    @Test
    fun map_passesTheActionThrough() {
        val action = ContactEntryAction.Call("4155551212")
        val quickAction = ContactQuickAction(type = ContactQuickActionType.CALL, action = action)

        assertEquals(action, mapper.map(listOf(quickAction)).first().action)
    }

    @Test
    fun map_withoutAnAction_keepsTheEntryDisabled() {
        val quickAction = ContactQuickAction(type = ContactQuickActionType.CALL, action = null)

        assertNull(mapper.map(listOf(quickAction)).first().action)
    }

    private fun allTypes(): List<ContactQuickAction> {
        return ContactQuickActionType.entries.map { type ->
            ContactQuickAction(type = type, action = null)
        }
    }
}

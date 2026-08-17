package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface ContactQuickActionsMapper {
    fun map(quickActions: List<ContactQuickAction>): ImmutableList<ContactQuickActionUiModel>
}

internal class ContactQuickActionsMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ContactQuickActionsMapper {

    override fun map(
        quickActions: List<ContactQuickAction>,
    ): ImmutableList<ContactQuickActionUiModel> {
        return quickActions
            .map { quickAction -> mapQuickAction(quickAction) }
            .toImmutableList()
    }

    private fun mapQuickAction(quickAction: ContactQuickAction): ContactQuickActionUiModel {
        return ContactQuickActionUiModel(
            icon = icon(quickAction.type),
            label = context.getString(labelResource(quickAction.type)),
            action = quickAction.action,
        )
    }

    private fun icon(type: ContactQuickActionType): ContactEntryIcon {
        return when (type) {
            ContactQuickActionType.CALL -> ContactEntryIcon.CALL
            ContactQuickActionType.MESSAGE -> ContactEntryIcon.MESSAGE
            ContactQuickActionType.VIDEO_CALL -> ContactEntryIcon.VIDEO_CALL
            ContactQuickActionType.EMAIL -> ContactEntryIcon.EMAIL
        }
    }

    @StringRes
    private fun labelResource(type: ContactQuickActionType): Int {
        return when (type) {
            ContactQuickActionType.CALL -> R.string.call_other
            ContactQuickActionType.MESSAGE -> R.string.quickcontact_action_message
            ContactQuickActionType.VIDEO_CALL -> R.string.quickcontact_action_video
            ContactQuickActionType.EMAIL -> R.string.email
        }
    }
}

package com.android.contacts.ui.contactcreation.component

import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState

/**
 * Groups the parameters needed by [MoreFieldsSectionContent] to keep the call-site clean
 * and avoid triggering detekt's LongParameterList rule.
 */
internal data class MoreFieldsState(
    val isExpanded: Boolean,
    val organization: OrganizationFieldState = OrganizationFieldState(),
    val events: List<EventFieldState>,
    val relations: List<RelationFieldState>,
    val imAccounts: List<ImFieldState>,
    val websites: List<WebsiteFieldState>,
    val note: String,
    val nickname: String,
    val sipAddress: String,
    val showSipField: Boolean,
)

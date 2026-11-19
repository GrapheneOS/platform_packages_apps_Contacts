package com.android.contacts.tests.factory

import com.android.contacts.domain.groups.model.Group
import com.android.contacts.domain.groups.model.GroupDetails

internal object GroupDetailsFactory {
    fun build(
        group: Group = GroupFactory.build(),
        isMembershipEditable: Boolean = true,
    ) = GroupDetails(
        group = group,
        isMembershipEditable = isMembershipEditable,
    )
}

package com.android.contacts.tests.factory

import com.android.contacts.ui.interactions.importing.screen.model.SimCardOption

internal object SimCardOptionFactory {
    fun build(
        subscriptionId: Int = 1,
        name: String? = null,
        contactsCount: Int? = null,
        phone: String? = null,
    ) = SimCardOption(
        subscriptionId = subscriptionId,
        name = name,
        contactsCount = contactsCount,
        phone = phone,
    )
}

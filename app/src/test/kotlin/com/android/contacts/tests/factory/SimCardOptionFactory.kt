package com.android.contacts.tests

import com.android.contacts.ui.interactions.importing.screen.model.SimCardOption
import kotlin.random.Random

object SimCardOptionFactory {
    fun build(
        subscriptionId: Int = Random.nextInt(),
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

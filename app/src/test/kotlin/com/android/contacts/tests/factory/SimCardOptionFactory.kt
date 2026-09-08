package com.android.contacts.tests.factory

import androidx.compose.ui.text.AnnotatedString
import com.android.contacts.ui.interactions.importing.screen.model.SimCardOption

internal object SimCardOptionFactory {
    fun build(
        subscriptionId: Int = 1,
        name: String? = null,
        contactsCount: Int? = null,
        phone: AnnotatedString? = null,
    ) = SimCardOption(
        subscriptionId = subscriptionId,
        name = name,
        contactsCount = contactsCount,
        phone = phone,
    )
}

package com.android.contacts.tests

import com.android.contacts.model.SimContact

internal object SimContactFactory {
    fun build(
        recordNumber: Int = 1,
        name: String = "Contact Name",
        phone: String? = null,
    ) = SimContact(
        recordNumber,
        name,
        phone,
    )
}

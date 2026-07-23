package com.android.contacts.tests.factory

import com.android.contacts.model.SimContact
import kotlin.random.Random

object SimContactFactory {
    fun build(
        recordNumber: Int = Random.nextInt(),
        name: String = "Contact Name",
        phone: String? = null,

    ) = SimContact(
        recordNumber,
        name,
        phone,
    )
}

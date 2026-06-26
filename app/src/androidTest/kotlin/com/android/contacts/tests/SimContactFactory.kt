package com.android.contacts.tests

import com.android.contacts.model.SimContact
import kotlin.random.Random
import kotlin.random.nextUInt

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

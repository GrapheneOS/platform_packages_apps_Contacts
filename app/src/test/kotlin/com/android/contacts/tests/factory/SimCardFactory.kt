package com.android.contacts.tests.factory

import com.android.contacts.model.SimCard

object SimCardFactory {
    fun build(
        subscriptionId: Int = 1,
        simId: String = subscriptionId.toString(),
        carrierName: String? = null,
        displayName: String? = null,
        phoneNumber: String? = null,
        countryCode: String? = null,
    ) = SimCard(
        simId,
        subscriptionId,
        carrierName,
        displayName,
        phoneNumber,
        countryCode,
    )
}

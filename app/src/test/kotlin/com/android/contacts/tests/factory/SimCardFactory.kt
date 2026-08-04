package com.android.contacts.tests.factory

import com.android.contacts.model.SimCard
import kotlin.random.Random

object SimCardFactory {
    fun build(
        subscriptionId: Int = Random.nextInt(),
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

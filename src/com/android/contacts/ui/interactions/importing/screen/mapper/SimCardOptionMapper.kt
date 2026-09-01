package com.android.contacts.ui.interactions.importing.screen.mapper

import androidx.core.text.BidiFormatter
import androidx.core.text.TextDirectionHeuristicsCompat
import com.android.contacts.compat.PhoneNumberUtilsCompat
import com.android.contacts.model.SimCard
import com.android.contacts.ui.interactions.importing.screen.model.SimCardOption
import javax.inject.Inject

internal fun interface SimCardOptionMapper {
    fun map(simCard: SimCard): SimCardOption
}

internal class SimCardOptionMapperImpl @Inject constructor(
    private val bidiFormatter: BidiFormatter,
) : SimCardOptionMapper {
    override fun map(simCard: SimCard): SimCardOption {
        return SimCardOption(
            subscriptionId = simCard.subscriptionId,
            name = simCard.displayName?.toString(),
            contactsCount = simCard.contacts?.size,
            phone = getFormattedPhone(simCard),
        )
    }

    private fun getFormattedPhone(simCard: SimCard): String? {
        return (simCard.getFormattedPhone() ?: simCard.phone)
            ?.ifEmpty { null }
            ?.let { phone ->
                bidiFormatter.unicodeWrap(
                    PhoneNumberUtilsCompat.createTtsSpannable(phone),
                    TextDirectionHeuristicsCompat.LTR,
                )?.toString()
            }
    }
}

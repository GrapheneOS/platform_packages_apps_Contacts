package com.android.contacts.ui.interactions.importing.screen.mapper

import android.telephony.PhoneNumberUtils
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.VerbatimTtsAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.core.text.BidiFormatter
import androidx.core.text.TextDirectionHeuristicsCompat
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
            phone = annotatedPhone(simCard),
        )
    }

    private fun annotatedPhone(simCard: SimCard): AnnotatedString? {
        val phone = (simCard.getFormattedPhone() ?: simCard.phone)
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val wrappedPhone = bidiFormatter.unicodeWrap(
            PhoneNumberUtils.createTtsSpannable(phone),
            TextDirectionHeuristicsCompat.LTR,
        )
            .takeIf { it.isNotBlank() }
            ?: phone

        return buildAnnotatedString {
            withAnnotation(VerbatimTtsAnnotation(phone.toString())) {
                append(wrappedPhone)
            }
        }
    }
}

package com.android.contacts.data.contactdetails.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.telephony.PhoneNumberUtils
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.model.dataitem.EventDataItem
import com.android.contacts.model.dataitem.ImDataItem
import com.android.contacts.model.dataitem.RelationDataItem
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.MatchType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface DataItemCollapseMatcher {
    fun shouldCollapse(current: DataItem, other: DataItem): Boolean
}

internal class DataItemCollapseMatcherImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DataItemCollapseMatcher {

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    override fun shouldCollapse(
        current: DataItem,
        other: DataItem,
    ): Boolean {
        if (current.dataKind == null || other.dataKind == null) {
            return false
        }

        return when (current) {
            is ImDataItem -> shouldCollapseIm(current, other)
            is EventDataItem -> shouldCollapseEvent(current, other)
            is RelationDataItem -> shouldCollapseRelation(current, other)
            else -> shouldCollapseDataStrings(current, other)
        }
    }

    private fun shouldCollapseIm(
        current: ImDataItem,
        other: DataItem,
    ): Boolean {
        if (other !is ImDataItem) {
            return false
        }

        val hasProtocol = current.isProtocolValid
        val otherHasProtocol = other.isProtocolValid
        val isCustomProtocol = current.protocol == Im.PROTOCOL_CUSTOM

        return when {
            current.data != other.data -> false
            !hasProtocol || !otherHasProtocol -> matchesAsCustomProtocol(current, other)
            current.protocol != other.protocol -> false
            isCustomProtocol -> current.customProtocol == other.customProtocol
            else -> true
        }
    }

    private fun matchesAsCustomProtocol(
        current: ImDataItem,
        other: ImDataItem,
    ): Boolean {
        return when {
            current.isProtocolValid -> current.protocol == Im.PROTOCOL_CUSTOM
            other.isProtocolValid -> other.protocol == Im.PROTOCOL_CUSTOM
            else -> true
        }
    }

    private fun shouldCollapseEvent(
        current: EventDataItem,
        other: DataItem,
    ): Boolean {
        if (other !is EventDataItem) {
            return false
        }

        val hasType = current.hasKindTypeColumn(current.dataKind)
        val otherHasType = other.hasKindTypeColumn(other.dataKind)

        return when {
            current.startDate != other.startDate -> false
            !hasType || !otherHasType -> hasType == otherHasType
            else -> matchesEventType(current, other)
        }
    }

    private fun matchesEventType(
        current: EventDataItem,
        other: EventDataItem,
    ): Boolean {
        val type = current.getKindTypeColumn(current.dataKind)
        val otherType = other.getKindTypeColumn(other.dataKind)

        return when {
            type != otherType -> false
            type == Event.TYPE_CUSTOM -> current.label == other.label
            else -> true
        }
    }

    private fun shouldCollapseRelation(
        current: RelationDataItem,
        other: DataItem,
    ): Boolean {
        if (other !is RelationDataItem) {
            return false
        }

        val hasType = current.hasKindTypeColumn(current.dataKind)
        val otherHasType = other.hasKindTypeColumn(other.dataKind)

        return when {
            current.name != other.name -> false
            !hasType || !otherHasType -> hasType == otherHasType
            else -> matchesRelationType(current, other)
        }
    }

    private fun matchesRelationType(
        current: RelationDataItem,
        other: RelationDataItem,
    ): Boolean {
        val type = current.getKindTypeColumn(current.dataKind)
        val otherType = other.getKindTypeColumn(other.dataKind)

        return when {
            type != otherType -> false
            type == Relation.TYPE_CUSTOM -> current.label == other.label
            else -> true
        }
    }

    private fun shouldCollapseDataStrings(
        current: DataItem,
        other: DataItem,
    ): Boolean {
        return shouldCollapseValues(
            mimeType = current.mimeType,
            data = current.buildDataString(context, current.dataKind),
            otherMimeType = other.mimeType,
            otherData = other.buildDataString(context, other.dataKind),
        )
    }

    private fun shouldCollapseValues(
        mimeType: String?,
        data: String?,
        otherMimeType: String?,
        otherData: String?,
    ): Boolean {
        return when {
            mimeType != otherMimeType -> false
            data == otherData -> true
            data == null || otherData == null -> false
            mimeType != Phone.CONTENT_ITEM_TYPE -> false
            else -> shouldCollapsePhoneNumbers(data, otherData)
        }
    }

    private fun shouldCollapsePhoneNumbers(
        number: String,
        otherNumber: String,
    ): Boolean {
        if (number.contains(POUND) != otherNumber.contains(POUND) ||
            number.contains(STAR) != otherNumber.contains(STAR)
        ) {
            return false
        }

        val parts = splitOnWaitSymbol(number)
        val otherParts = splitOnWaitSymbol(otherNumber)

        if (parts.size != otherParts.size) {
            return false
        }

        return parts.indices.all { index ->
            partsMatch(parts[index], otherParts[index])
        }
    }

    private fun splitOnWaitSymbol(number: String): List<String> {
        if (!number.contains(PhoneNumberUtils.WAIT)) {
            return listOf(number)
        }

        return number.split(PhoneNumberUtils.WAIT)
            .dropLastWhile { part -> part.isEmpty() }
    }

    private fun partsMatch(
        part: String,
        otherPart: String,
    ): Boolean {
        val convertedPart = PhoneNumberUtils.convertKeypadLettersToDigits(part)
        if (convertedPart == otherPart) {
            return true
        }

        return when (phoneNumberUtil.isNumberMatch(convertedPart, otherPart)) {
            MatchType.EXACT_MATCH -> true
            MatchType.NSN_MATCH -> nsnMatchCollapses(convertedPart, otherPart)
            MatchType.NOT_A_NUMBER -> false
            MatchType.NO_MATCH -> false
            MatchType.SHORT_NSN_MATCH -> false
        }
    }

    private fun nsnMatchCollapses(
        part: String,
        otherPart: String,
    ): Boolean {
        val countryCode = countryCode(part) ?: return countryCode(otherPart) == null

        if (countryCode != NANP_COUNTRY_CODE) {
            return false
        }

        return otherPart.trim().firstOrNull() != TRUNK_PREFIX
    }

    private fun countryCode(number: String): Int? {
        return try {
            phoneNumberUtil.parse(number, null).countryCode
        } catch (_: NumberParseException) {
            null
        }
    }

    private companion object {
        const val POUND = '#'
        const val STAR = '*'
        const val TRUNK_PREFIX = '1'
        const val NANP_COUNTRY_CODE = 1
    }
}

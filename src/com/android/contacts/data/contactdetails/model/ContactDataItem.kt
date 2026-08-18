package com.android.contacts.data.contactdetails.model

internal sealed interface ContactDataItem {
    val id: Long
    val rawContactId: Long
    val mimeType: String
    val isPrimary: Boolean
    val isSuperPrimary: Boolean
    val displayString: String?

    data class Phone(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val number: String?,
        val formattedNumber: String?,
        val typeLabel: String?,
        val isCarrierVideoCallCapable: Boolean,
    ) : ContactDataItem

    data class SipAddress(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val address: String?,
        val typeLabel: String?,
    ) : ContactDataItem

    data class Email(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val address: String?,
        val data: String?,
        val typeLabel: String?,
    ) : ContactDataItem

    data class Postal(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val formattedAddress: String?,
        val typeLabel: String?,
    ) : ContactDataItem

    data class Im(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val data: String?,
        val protocol: Int,
        val customProtocol: String?,
        val protocolLabel: String?,
        val isCustomProtocol: Boolean,
        val chatCapability: Int,
    ) : ContactDataItem

    data class Organization(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val formattedCompany: String?,
        val company: String?,
        val department: String?,
        val title: String?,
    ) : ContactDataItem

    data class Nickname(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val name: String?,
    ) : ContactDataItem

    data class Note(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val note: String?,
    ) : ContactDataItem

    data class Website(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val url: String?,
    ) : ContactDataItem

    data class Event(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val formattedDate: String?,
        val typeLabel: String?,
        val isRecurringAnnually: Boolean,
        val isBirthday: Boolean,
    ) : ContactDataItem

    data class Relation(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val name: String?,
        val typeLabel: String?,
    ) : ContactDataItem

    data class Custom(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val summary: String?,
        val content: String?,
    ) : ContactDataItem

    data class StructuredName(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val givenName: String?,
    ) : ContactDataItem

    data class Generic(
        override val id: Long,
        override val rawContactId: Long,
        override val mimeType: String,
        override val isPrimary: Boolean,
        override val isSuperPrimary: Boolean,
        override val displayString: String?,
        val typeColumn: String?,
    ) : ContactDataItem
}

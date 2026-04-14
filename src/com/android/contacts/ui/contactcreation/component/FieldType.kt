package com.android.contacts.ui.contactcreation.component

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class PhoneType : Parcelable {
    data object Mobile : PhoneType()
    data object Home : PhoneType()
    data object Work : PhoneType()
    data object WorkMobile : PhoneType()
    data object Main : PhoneType()
    data object FaxWork : PhoneType()
    data object FaxHome : PhoneType()
    data object Pager : PhoneType()
    data object Other : PhoneType()
    data class Custom(val label: String) : PhoneType()

    val rawValue: Int
        get() = when (this) {
            is Mobile -> Phone.TYPE_MOBILE
            is Home -> Phone.TYPE_HOME
            is Work -> Phone.TYPE_WORK
            is WorkMobile -> Phone.TYPE_WORK_MOBILE
            is Main -> Phone.TYPE_MAIN
            is FaxWork -> Phone.TYPE_FAX_WORK
            is FaxHome -> Phone.TYPE_FAX_HOME
            is Pager -> Phone.TYPE_PAGER
            is Other -> Phone.TYPE_OTHER
            is Custom -> Phone.TYPE_CUSTOM
        }
}

@Parcelize
internal sealed class EmailType : Parcelable {
    data object Home : EmailType()
    data object Work : EmailType()
    data object Other : EmailType()
    data object Mobile : EmailType()
    data class Custom(val label: String) : EmailType()

    val rawValue: Int
        get() = when (this) {
            is Home -> Email.TYPE_HOME
            is Work -> Email.TYPE_WORK
            is Other -> Email.TYPE_OTHER
            is Mobile -> Email.TYPE_MOBILE
            is Custom -> Email.TYPE_CUSTOM
        }
}

@Parcelize
internal sealed class AddressType : Parcelable {
    data object Home : AddressType()
    data object Work : AddressType()
    data object Other : AddressType()
    data class Custom(val label: String) : AddressType()

    val rawValue: Int
        get() = when (this) {
            is Home -> StructuredPostal.TYPE_HOME
            is Work -> StructuredPostal.TYPE_WORK
            is Other -> StructuredPostal.TYPE_OTHER
            is Custom -> StructuredPostal.TYPE_CUSTOM
        }
}

@Parcelize
internal sealed class EventType : Parcelable {
    data object Birthday : EventType()
    data object Anniversary : EventType()
    data object Other : EventType()
    data class Custom(val label: String) : EventType()

    val rawValue: Int
        get() = when (this) {
            is Birthday -> Event.TYPE_BIRTHDAY
            is Anniversary -> Event.TYPE_ANNIVERSARY
            is Other -> Event.TYPE_OTHER
            is Custom -> Event.TYPE_CUSTOM
        }
}

@Parcelize
internal sealed class RelationType : Parcelable {
    data object Assistant : RelationType()
    data object Brother : RelationType()
    data object Child : RelationType()
    data object DomesticPartner : RelationType()
    data object Father : RelationType()
    data object Friend : RelationType()
    data object Manager : RelationType()
    data object Mother : RelationType()
    data object Parent : RelationType()
    data object Partner : RelationType()
    data object Sister : RelationType()
    data object Spouse : RelationType()
    data object Relative : RelationType()
    data object ReferredBy : RelationType()
    data class Custom(val label: String) : RelationType()

    val rawValue: Int
        get() = when (this) {
            is Assistant -> Relation.TYPE_ASSISTANT
            is Brother -> Relation.TYPE_BROTHER
            is Child -> Relation.TYPE_CHILD
            is DomesticPartner -> Relation.TYPE_DOMESTIC_PARTNER
            is Father -> Relation.TYPE_FATHER
            is Friend -> Relation.TYPE_FRIEND
            is Manager -> Relation.TYPE_MANAGER
            is Mother -> Relation.TYPE_MOTHER
            is Parent -> Relation.TYPE_PARENT
            is Partner -> Relation.TYPE_PARTNER
            is Sister -> Relation.TYPE_SISTER
            is Spouse -> Relation.TYPE_SPOUSE
            is Relative -> Relation.TYPE_RELATIVE
            is ReferredBy -> Relation.TYPE_REFERRED_BY
            is Custom -> Relation.TYPE_CUSTOM
        }
}

@Parcelize
internal sealed class ImProtocol : Parcelable {
    data object Aim : ImProtocol()
    data object Msn : ImProtocol()
    data object Yahoo : ImProtocol()
    data object Skype : ImProtocol()
    data object Qq : ImProtocol()
    data object GoogleTalk : ImProtocol()
    data object Icq : ImProtocol()
    data object Jabber : ImProtocol()
    data class Custom(val label: String) : ImProtocol()

    val rawValue: Int
        get() = when (this) {
            is Aim -> Im.PROTOCOL_AIM
            is Msn -> Im.PROTOCOL_MSN
            is Yahoo -> Im.PROTOCOL_YAHOO
            is Skype -> Im.PROTOCOL_SKYPE
            is Qq -> Im.PROTOCOL_QQ
            is GoogleTalk -> Im.PROTOCOL_GOOGLE_TALK
            is Icq -> Im.PROTOCOL_ICQ
            is Jabber -> Im.PROTOCOL_JABBER
            is Custom -> Im.PROTOCOL_CUSTOM
        }
}

@Parcelize
internal sealed class WebsiteType : Parcelable {
    data object Homepage : WebsiteType()
    data object Blog : WebsiteType()
    data object Profile : WebsiteType()
    data object Home : WebsiteType()
    data object Work : WebsiteType()
    data object Ftp : WebsiteType()
    data object Other : WebsiteType()
    data class Custom(val label: String) : WebsiteType()

    val rawValue: Int
        get() = when (this) {
            is Homepage -> Website.TYPE_HOMEPAGE
            is Blog -> Website.TYPE_BLOG
            is Profile -> Website.TYPE_PROFILE
            is Home -> Website.TYPE_HOME
            is Work -> Website.TYPE_WORK
            is Ftp -> Website.TYPE_FTP
            is Other -> Website.TYPE_OTHER
            is Custom -> Website.TYPE_CUSTOM
        }
}

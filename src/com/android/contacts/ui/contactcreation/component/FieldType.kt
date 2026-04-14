package com.android.contacts.ui.contactcreation.component

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
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

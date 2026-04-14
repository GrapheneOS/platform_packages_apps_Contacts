package com.android.contacts.ui.contactcreation.delegate

import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Suppress("TooManyFunctions")
internal class ContactFieldsDelegate @Inject constructor() {

    private var phones: PersistentList<PhoneFieldState> = persistentListOf(PhoneFieldState())
    private var emails: PersistentList<EmailFieldState> = persistentListOf(EmailFieldState())

    fun getPhones(): List<PhoneFieldState> = phones

    fun getEmails(): List<EmailFieldState> = emails

    fun restorePhones(list: List<PhoneFieldState>) {
        phones = list.toPersistentList()
    }

    fun restoreEmails(list: List<EmailFieldState>) {
        emails = list.toPersistentList()
    }

    fun addPhone(): List<PhoneFieldState> {
        phones = phones.add(PhoneFieldState())
        return phones
    }

    fun removePhone(id: String): List<PhoneFieldState> {
        phones = phones.removeAll { it.id == id }
        return phones
    }

    fun updatePhone(id: String, value: String): List<PhoneFieldState> {
        phones = phones.map { if (it.id == id) it.copy(number = value) else it }.toPersistentList()
        return phones
    }

    fun updatePhoneType(id: String, type: PhoneType): List<PhoneFieldState> {
        phones = phones.map { if (it.id == id) it.copy(type = type) else it }.toPersistentList()
        return phones
    }

    fun addEmail(): List<EmailFieldState> {
        emails = emails.add(EmailFieldState())
        return emails
    }

    fun removeEmail(id: String): List<EmailFieldState> {
        emails = emails.removeAll { it.id == id }
        return emails
    }

    fun updateEmail(id: String, value: String): List<EmailFieldState> {
        emails = emails.map { if (it.id == id) it.copy(address = value) else it }.toPersistentList()
        return emails
    }

    fun updateEmailType(id: String, type: EmailType): List<EmailFieldState> {
        emails = emails.map { if (it.id == id) it.copy(type = type) else it }.toPersistentList()
        return emails
    }
}

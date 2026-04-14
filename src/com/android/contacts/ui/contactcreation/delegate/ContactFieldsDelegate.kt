package com.android.contacts.ui.contactcreation.delegate

import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Suppress("TooManyFunctions")
internal class ContactFieldsDelegate @Inject constructor() {

    private var phones: PersistentList<PhoneFieldState> = persistentListOf(PhoneFieldState())
    private var emails: PersistentList<EmailFieldState> = persistentListOf(EmailFieldState())
    private var addresses: PersistentList<AddressFieldState> = persistentListOf()
    private var events: PersistentList<EventFieldState> = persistentListOf()
    private var relations: PersistentList<RelationFieldState> = persistentListOf()
    private var imAccounts: PersistentList<ImFieldState> = persistentListOf()
    private var websites: PersistentList<WebsiteFieldState> = persistentListOf()
    private var groups: PersistentList<GroupFieldState> = persistentListOf()

    // --- Getters ---

    fun getPhones(): List<PhoneFieldState> = phones
    fun getEmails(): List<EmailFieldState> = emails
    fun getAddresses(): List<AddressFieldState> = addresses
    fun getEvents(): List<EventFieldState> = events
    fun getRelations(): List<RelationFieldState> = relations
    fun getImAccounts(): List<ImFieldState> = imAccounts
    fun getWebsites(): List<WebsiteFieldState> = websites
    fun getGroups(): List<GroupFieldState> = groups

    // --- Restore ---

    fun restorePhones(list: List<PhoneFieldState>) {
        phones = list.toPersistentList()
    }

    fun restoreEmails(list: List<EmailFieldState>) {
        emails = list.toPersistentList()
    }

    fun restoreAddresses(list: List<AddressFieldState>) {
        addresses = list.toPersistentList()
    }

    fun restoreEvents(list: List<EventFieldState>) {
        events = list.toPersistentList()
    }

    fun restoreRelations(list: List<RelationFieldState>) {
        relations = list.toPersistentList()
    }

    fun restoreImAccounts(list: List<ImFieldState>) {
        imAccounts = list.toPersistentList()
    }

    fun restoreWebsites(list: List<WebsiteFieldState>) {
        websites = list.toPersistentList()
    }

    fun restoreGroups(list: List<GroupFieldState>) {
        groups = list.toPersistentList()
    }

    // --- Phone ---

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

    // --- Email ---

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

    // --- Address ---

    fun addAddress(): List<AddressFieldState> {
        addresses = addresses.add(AddressFieldState())
        return addresses
    }

    fun removeAddress(id: String): List<AddressFieldState> {
        addresses = addresses.removeAll { it.id == id }
        return addresses
    }

    fun updateAddressStreet(id: String, value: String): List<AddressFieldState> {
        addresses = addresses.map {
            if (it.id == id) it.copy(street = value) else it
        }.toPersistentList()
        return addresses
    }

    fun updateAddressCity(id: String, value: String): List<AddressFieldState> {
        addresses = addresses.map {
            if (it.id == id) it.copy(city = value) else it
        }.toPersistentList()
        return addresses
    }

    fun updateAddressRegion(id: String, value: String): List<AddressFieldState> {
        addresses = addresses.map {
            if (it.id == id) it.copy(region = value) else it
        }.toPersistentList()
        return addresses
    }

    fun updateAddressPostcode(id: String, value: String): List<AddressFieldState> {
        addresses = addresses.map {
            if (it.id == id) it.copy(postcode = value) else it
        }.toPersistentList()
        return addresses
    }

    fun updateAddressCountry(id: String, value: String): List<AddressFieldState> {
        addresses = addresses.map {
            if (it.id == id) it.copy(country = value) else it
        }.toPersistentList()
        return addresses
    }

    fun updateAddressType(id: String, type: AddressType): List<AddressFieldState> {
        addresses = addresses.map {
            if (it.id == id) it.copy(type = type) else it
        }.toPersistentList()
        return addresses
    }

    // --- Event ---

    fun addEvent(): List<EventFieldState> {
        events = events.add(EventFieldState())
        return events
    }

    fun removeEvent(id: String): List<EventFieldState> {
        events = events.removeAll { it.id == id }
        return events
    }

    fun updateEvent(id: String, value: String): List<EventFieldState> {
        events = events.map {
            if (it.id == id) it.copy(startDate = value) else it
        }.toPersistentList()
        return events
    }

    fun updateEventType(id: String, type: EventType): List<EventFieldState> {
        events = events.map {
            if (it.id == id) it.copy(type = type) else it
        }.toPersistentList()
        return events
    }

    // --- Relation ---

    fun addRelation(): List<RelationFieldState> {
        relations = relations.add(RelationFieldState())
        return relations
    }

    fun removeRelation(id: String): List<RelationFieldState> {
        relations = relations.removeAll { it.id == id }
        return relations
    }

    fun updateRelation(id: String, value: String): List<RelationFieldState> {
        relations = relations.map {
            if (it.id == id) it.copy(name = value) else it
        }.toPersistentList()
        return relations
    }

    fun updateRelationType(id: String, type: RelationType): List<RelationFieldState> {
        relations = relations.map {
            if (it.id == id) it.copy(type = type) else it
        }.toPersistentList()
        return relations
    }

    // --- IM ---

    fun addIm(): List<ImFieldState> {
        imAccounts = imAccounts.add(ImFieldState())
        return imAccounts
    }

    fun removeIm(id: String): List<ImFieldState> {
        imAccounts = imAccounts.removeAll { it.id == id }
        return imAccounts
    }

    fun updateIm(id: String, value: String): List<ImFieldState> {
        imAccounts = imAccounts.map {
            if (it.id == id) it.copy(data = value) else it
        }.toPersistentList()
        return imAccounts
    }

    fun updateImProtocol(id: String, protocol: ImProtocol): List<ImFieldState> {
        imAccounts = imAccounts.map {
            if (it.id == id) it.copy(protocol = protocol) else it
        }.toPersistentList()
        return imAccounts
    }

    // --- Website ---

    fun addWebsite(): List<WebsiteFieldState> {
        websites = websites.add(WebsiteFieldState())
        return websites
    }

    fun removeWebsite(id: String): List<WebsiteFieldState> {
        websites = websites.removeAll { it.id == id }
        return websites
    }

    fun updateWebsite(id: String, value: String): List<WebsiteFieldState> {
        websites = websites.map {
            if (it.id == id) it.copy(url = value) else it
        }.toPersistentList()
        return websites
    }

    fun updateWebsiteType(id: String, type: WebsiteType): List<WebsiteFieldState> {
        websites = websites.map {
            if (it.id == id) it.copy(type = type) else it
        }.toPersistentList()
        return websites
    }

    // --- Group ---

    fun toggleGroup(groupId: Long, title: String): List<GroupFieldState> {
        val existing = groups.find { it.groupId == groupId }
        groups = if (existing != null) {
            groups.removeAll { it.groupId == groupId }
        } else {
            groups.add(GroupFieldState(groupId = groupId, title = title))
        }
        return groups
    }

    fun clearGroups(): List<GroupFieldState> {
        groups = persistentListOf()
        return groups
    }
}

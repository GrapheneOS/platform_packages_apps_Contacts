package com.android.contacts.ui.contactcreation

import android.net.Uri
import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState

internal object TestFactory {

    fun phone(
        id: String = "phone-1",
        number: String = "555-1234",
        type: PhoneType = PhoneType.Mobile,
    ) = PhoneFieldState(id = id, number = number, type = type)

    fun email(
        id: String = "email-1",
        address: String = "test@example.com",
        type: EmailType = EmailType.Home,
    ) = EmailFieldState(id = id, address = address, type = type)

    fun address(
        id: String = "addr-1",
        street: String = "123 Main St",
        city: String = "Springfield",
        region: String = "",
        postcode: String = "",
        country: String = "",
        type: AddressType = AddressType.Home,
    ) = AddressFieldState(
        id = id,
        street = street,
        city = city,
        region = region,
        postcode = postcode,
        country = country,
        type = type,
    )

    fun organization(
        company: String = "Acme Corp",
        title: String = "Engineer",
    ) = OrganizationFieldState(company = company, title = title)

    fun event(
        id: String = "event-1",
        startDate: String = "1990-01-15",
        type: EventType = EventType.Birthday,
    ) = EventFieldState(id = id, startDate = startDate, type = type)

    fun relation(
        id: String = "rel-1",
        name: String = "Jane Doe",
        type: RelationType = RelationType.Spouse,
    ) = RelationFieldState(id = id, name = name, type = type)

    fun im(
        id: String = "im-1",
        data: String = "user@jabber",
        protocol: ImProtocol = ImProtocol.Jabber,
    ) = ImFieldState(id = id, data = data, protocol = protocol)

    fun website(
        id: String = "web-1",
        url: String = "https://example.com",
        type: WebsiteType = WebsiteType.Homepage,
    ) = WebsiteFieldState(id = id, url = url, type = type)

    fun nameState(
        prefix: String = "",
        first: String = "Jane",
        middle: String = "",
        last: String = "Doe",
        suffix: String = "",
    ) = NameState(prefix = prefix, first = first, middle = middle, last = last, suffix = suffix)

    fun group(groupId: Long = 1L, title: String = "Friends") =
        GroupFieldState(groupId = groupId, title = title)

    fun fullState() = ContactCreationUiState(
        nameState = nameState(),
        phoneNumbers = listOf(phone()),
        emails = listOf(email()),
        addresses = listOf(address()),
        organization = organization(),
        events = listOf(event()),
        relations = listOf(relation()),
        imAccounts = listOf(im()),
        websites = listOf(website()),
        note = "Important note",
        nickname = "JD",
        sipAddress = "sip:jane@voip.example.com",
        groups = listOf(group()),
        photoUri = Uri.parse("content://media/external/images/99"),
        isMoreFieldsExpanded = true,
    )

    fun basicState() = ContactCreationUiState(
        nameState = nameState(),
        phoneNumbers = listOf(phone()),
        emails = listOf(email()),
    )
}

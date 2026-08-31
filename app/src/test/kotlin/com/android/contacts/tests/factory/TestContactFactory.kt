package com.android.contacts.tests.factory

import com.android.contacts.domain.debug.model.DebugDataConstants
import com.android.contacts.domain.debug.model.TestContact
import com.android.contacts.domain.debug.model.TestContact.Im
import com.android.contacts.domain.debug.model.TestContact.Name
import com.android.contacts.domain.debug.model.TestContact.Postal
import com.android.contacts.domain.debug.model.TestContact.ValueWithType

internal object TestContactFactory {
    fun build(
        phones: List<ValueWithType<String>> = listOf(
            ValueWithType(value = DebugDataConstants.PHONE_PREFIX + "123456"),
        ),
        name: Name = Name(given = "Name"),
        nickname: ValueWithType<String>? = null,
        emails: List<ValueWithType<String>> = emptyList(),
        postal: ValueWithType<Postal>? = null,
        organization: String? = null,
        relation: ValueWithType<String>? = null,
        website: ValueWithType<String>? = null,
        event: ValueWithType<String>? = null,
        im: ValueWithType<Im>? = null,
        sipAddress: ValueWithType<String>? = null,
        identityValue: String? = null,
        identityNamespace: String? = null,
        note: String? = null,
        photo: TestContact.Photo? = null,
    ) = TestContact(
        phones = phones,
        name = name,
        nickname = nickname,
        emails = emails,
        postal = postal,
        organization = organization,
        relation = relation,
        website = website,
        event = event,
        im = im,
        sipAddress = sipAddress,
        identityValue = identityValue,
        identityNamespace = identityNamespace,
        note = note,
        photo = photo,
    )
}

package com.android.contacts.tests.factory

import com.android.contacts.domain.debug.model.TestContact

internal object TestContactFactory {
    fun build(
        phones: List<TestContact.ValueWithType> = listOf(
            TestContact.ValueWithType(value = TestContact.PHONE_PREFIX + "123456"),
        ),
        givenName: String = "Name",
        familyName: String? = null,
        middleName: String? = null,
        displayName: String? = null,
        nickname: TestContact.ValueWithType? = null,
        emails: List<TestContact.ValueWithType> = emptyList(),
        city: String? = null,
        country: String? = null,
        organization: String? = null,
        relation: TestContact.ValueWithType? = null,
        website: TestContact.ValueWithType? = null,
        photo: TestContact.Photo? = null,
    ) = TestContact(
        phones = phones,
        givenName = givenName,
        familyName = familyName,
        middleName = middleName,
        displayName = displayName,
        nickname = nickname,
        emails = emails,
        city = city,
        country = country,
        organization = organization,
        relation = relation,
        website = website,
        photo = photo,
    )
}

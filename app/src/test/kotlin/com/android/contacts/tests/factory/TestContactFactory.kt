package com.android.contacts.tests.factory

import com.android.contacts.domain.debug.model.TestContact
import kotlin.random.Random

internal object TestContactFactory {
    fun build(
        phones: List<TestContact.ValueWithType> = listOf(
            TestContact.ValueWithType(
                value = TestContact.PHONE_PREFIX +
                    Random.nextInt(999_999).toString().padStart(6, '0'),
                type = null,
            ),
        ),
        givenName: String = "Name${Random.nextInt(99_999)}",
        familyName: String? = null,
        middleName: String? = null,
        displayName: String? = null,
        nickname: TestContact.ValueWithType? = null,
        emails: List<TestContact.ValueWithType> = emptyList(),
        city: String? = null,
        country: String? = null,
        organization: String? = null,
        relation: Int? = null,
        website: TestContact.ValueWithType? = null,
        photo: ByteArray? = null,
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

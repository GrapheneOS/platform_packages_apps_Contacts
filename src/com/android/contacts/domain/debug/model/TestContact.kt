package com.android.contacts.domain.debug.model

internal data class TestContact(
    val phones: List<ValueWithType>,
    val givenName: String,
    val familyName: String? = null,
    val middleName: String? = null,
    val displayName: String? = null,
    val nickname: ValueWithType? = null,
    val emails: List<ValueWithType> = emptyList(),
    val city: String? = null,
    val country: String? = null,
    val organization: String? = null,
    val relation: Int? = null,
    val website: ValueWithType? = null,
    val photo: ByteArray? = null,
) {
    internal data class ValueWithType(
        val value: String,
        val type: Int?,
    )

    // Due to the ByteArray, we need a custom equals/hashCode

    @Suppress("detekt:CyclomaticComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestContact

        if (relation != other.relation) return false
        if (phones != other.phones) return false
        if (givenName != other.givenName) return false
        if (familyName != other.familyName) return false
        if (middleName != other.middleName) return false
        if (displayName != other.displayName) return false
        if (nickname != other.nickname) return false
        if (emails != other.emails) return false
        if (city != other.city) return false
        if (country != other.country) return false
        if (organization != other.organization) return false
        if (website != other.website) return false
        if (!photo.contentEquals(other.photo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = relation ?: 0
        result = 31 * result + phones.hashCode()
        result = 31 * result + givenName.hashCode()
        result = 31 * result + (familyName?.hashCode() ?: 0)
        result = 31 * result + (middleName?.hashCode() ?: 0)
        result = 31 * result + (displayName?.hashCode() ?: 0)
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + emails.hashCode()
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (organization?.hashCode() ?: 0)
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (photo?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        const val PHONE_PREFIX = "+15550"
    }
}

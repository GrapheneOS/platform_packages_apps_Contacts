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
    val relation: ValueWithType? = null,
    val website: ValueWithType? = null,
    val photo: Photo? = null,
) {
    internal data class ValueWithType(
        val value: String,
        val type: Int? = null,
    )

    internal class Photo(
        val bytes: ByteArray,
    ) {
        override fun equals(other: Any?) = other is Photo && bytes.contentEquals(other.bytes)
        override fun hashCode() = bytes.contentHashCode()
    }

    companion object {
        const val PHONE_PREFIX = "+15550"
    }
}

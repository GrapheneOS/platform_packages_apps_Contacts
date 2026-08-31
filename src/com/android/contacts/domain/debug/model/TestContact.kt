package com.android.contacts.domain.debug.model

import android.provider.ContactsContract

internal data class TestContact(
    val phones: List<ValueWithType<String>>,
    val name: Name,
    val nickname: ValueWithType<String>? = null,
    val emails: List<ValueWithType<String>> = emptyList(),
    val postal: ValueWithType<Postal>? = null,
    val organization: String? = null,
    val relation: ValueWithType<String>? = null,
    val website: ValueWithType<String>? = null,
    val event: ValueWithType<String>? = null,
    val im: ValueWithType<Im>? = null,
    val sipAddress: ValueWithType<String>? = null,
    val identityValue: String? = null,
    val identityNamespace: String? = null,
    val note: String? = null,
    val photo: Photo? = null,
) {
    internal data class ValueWithType<V>(
        val value: V,
        val type: Int? = null,
    ) {
        val label: String?
            get() {
                return when (type) {
                    ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM -> "Custom"
                    else -> null
                }
            }
    }

    internal data class Name(
        val given: String,
        val family: String? = null,
        val middle: String? = null,
        val display: String? = null,
    )

    internal data class Postal(
        val city: String? = null,
        val country: String? = null,
    )

    internal data class Im(
        val data: String,
        val protocol: String,
    )

    internal class Photo(
        val bytes: ByteArray,
    ) {
        override fun equals(other: Any?) = other is Photo && bytes.contentEquals(other.bytes)
        override fun hashCode() = bytes.contentHashCode()
    }
}

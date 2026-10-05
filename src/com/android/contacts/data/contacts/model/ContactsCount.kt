package com.android.contacts.data.contacts.model

internal data class ContactsCount<A : Any>(
    val all: Int,
    val byAccount: Map<A, Int>,
)

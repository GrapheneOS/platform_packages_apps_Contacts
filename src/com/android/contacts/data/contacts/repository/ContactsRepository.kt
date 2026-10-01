package com.android.contacts.data.contacts.repository

import com.android.contacts.data.contacts.delegate.GetContactsCountDelegate
import com.android.contacts.data.contacts.delegate.GetContactsCountDelegateImpl
import javax.inject.Inject

internal interface ContactsRepository : GetContactsCountDelegate

internal class ContactsRepositoryImpl @Inject constructor(
    private val getContactsCountDelegateImpl: GetContactsCountDelegateImpl,
) : ContactsRepository,
    GetContactsCountDelegate by getContactsCountDelegateImpl

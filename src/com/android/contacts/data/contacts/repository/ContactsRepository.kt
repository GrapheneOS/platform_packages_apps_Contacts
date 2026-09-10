package com.android.contacts.data.contacts.repository

import com.android.contacts.data.contacts.delegate.LoadRawContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.LoadRawContactsRepositoryDelegateImpl
import javax.inject.Inject

internal interface ContactsRepository : LoadRawContactsRepositoryDelegate

internal class ContactsRepositoryImpl @Inject constructor(
    private val loadRawContactsRepositoryDelegateImpl: LoadRawContactsRepositoryDelegateImpl,
) : ContactsRepository,
    LoadRawContactsRepositoryDelegate by loadRawContactsRepositoryDelegateImpl

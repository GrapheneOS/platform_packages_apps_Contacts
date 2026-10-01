package com.android.contacts.data.contacts.repository

import com.android.contacts.data.contacts.delegate.GetContactsCountDelegate
import com.android.contacts.data.contacts.delegate.GetContactsCountDelegateImpl
import com.android.contacts.data.contacts.delegate.LoadRawContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.LoadRawContactsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.LookupContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.LookupContactsRepositoryDelegateImpl
import javax.inject.Inject

internal interface ContactsRepository :
    GetContactsCountDelegate,
    LoadRawContactsRepositoryDelegate,
    LookupContactsRepositoryDelegate

internal class ContactsRepositoryImpl @Inject constructor(
    private val getContactsCountDelegateImpl: GetContactsCountDelegateImpl,
    private val loadRawContactsRepositoryDelegateImpl: LoadRawContactsRepositoryDelegateImpl,
    private val lookupContactsRepositoryDelegateImpl: LookupContactsRepositoryDelegateImpl,
) : ContactsRepository,
    GetContactsCountDelegate by getContactsCountDelegateImpl,
    LoadRawContactsRepositoryDelegate by loadRawContactsRepositoryDelegateImpl,
    LookupContactsRepositoryDelegate by lookupContactsRepositoryDelegateImpl

package com.android.contacts.data.contacts.repository

import com.android.contacts.data.contacts.delegate.GetContactDataValuesRepositoryDelegate
import com.android.contacts.data.contacts.delegate.GetContactDataValuesRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.GetContactsCountDelegate
import com.android.contacts.data.contacts.delegate.GetContactsCountDelegateImpl
import com.android.contacts.data.contacts.delegate.LoadRawContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.LoadRawContactsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.LookupContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.LookupContactsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.QueryContactDataRepositoryDelegate
import com.android.contacts.data.contacts.delegate.QueryContactDataRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.QueryDirectoryContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.QueryDirectoryContactsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.QueryJoinSuggestionsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.QueryJoinSuggestionsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.QueryLegacyContactsRepositoryDelegate
import com.android.contacts.data.contacts.delegate.QueryLegacyContactsRepositoryDelegateImpl
import javax.inject.Inject

internal interface ContactsRepository :
    GetContactDataValuesRepositoryDelegate,
    GetContactsCountDelegate,
    LoadRawContactsRepositoryDelegate,
    LookupContactsRepositoryDelegate,
    QueryContactDataRepositoryDelegate,
    QueryContactsRepositoryDelegate,
    QueryDirectoryContactsRepositoryDelegate,
    QueryJoinSuggestionsRepositoryDelegate,
    QueryLegacyContactsRepositoryDelegate

internal class ContactsRepositoryImpl @Inject constructor(
    private val getContactDataValuesDelegateImpl: GetContactDataValuesRepositoryDelegateImpl,
    private val getContactsCountDelegateImpl: GetContactsCountDelegateImpl,
    private val loadRawContactsRepositoryDelegateImpl: LoadRawContactsRepositoryDelegateImpl,
    private val lookupContactsRepositoryDelegateImpl: LookupContactsRepositoryDelegateImpl,
    private val queryContactDataRepositoryDelegateImpl: QueryContactDataRepositoryDelegateImpl,
    private val queryContactsRepositoryDelegateImpl: QueryContactsRepositoryDelegateImpl,
    private val queryDirectoryContactsRepositoryDelegateImpl:
    QueryDirectoryContactsRepositoryDelegateImpl,
    private val queryJoinSuggestionsDelegateImpl: QueryJoinSuggestionsRepositoryDelegateImpl,
    private val queryLegacyContactsDelegateImpl: QueryLegacyContactsRepositoryDelegateImpl,
) : ContactsRepository,
    GetContactDataValuesRepositoryDelegate by getContactDataValuesDelegateImpl,
    GetContactsCountDelegate by getContactsCountDelegateImpl,
    LoadRawContactsRepositoryDelegate by loadRawContactsRepositoryDelegateImpl,
    LookupContactsRepositoryDelegate by lookupContactsRepositoryDelegateImpl,
    QueryContactDataRepositoryDelegate by queryContactDataRepositoryDelegateImpl,
    QueryContactsRepositoryDelegate by queryContactsRepositoryDelegateImpl,
    QueryDirectoryContactsRepositoryDelegate by queryDirectoryContactsRepositoryDelegateImpl,
    QueryJoinSuggestionsRepositoryDelegate by queryJoinSuggestionsDelegateImpl,
    QueryLegacyContactsRepositoryDelegate by queryLegacyContactsDelegateImpl
